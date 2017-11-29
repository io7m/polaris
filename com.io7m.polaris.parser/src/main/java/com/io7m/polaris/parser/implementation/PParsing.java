/*
 * Copyright © 2017 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.polaris.parser.implementation;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionMatcherType;
import com.io7m.jsx.SExpressionQuotedStringType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.model.PExprApplication;
import com.io7m.polaris.model.PExprConstantInteger;
import com.io7m.polaris.model.PExprConstantReal;
import com.io7m.polaris.model.PExprConstantString;
import com.io7m.polaris.model.PExprLambda;
import com.io7m.polaris.model.PExprLocal;
import com.io7m.polaris.model.PExprMatch;
import com.io7m.polaris.model.PExprReference;
import com.io7m.polaris.model.PExpressionOrDeclarationType;
import com.io7m.polaris.model.PExpressionType;
import com.io7m.polaris.model.PExpressionType.PMatchCaseType;
import com.io7m.polaris.model.PMatchCase;
import com.io7m.polaris.model.PPatternType;
import com.io7m.polaris.model.PTermName;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.util.Objects;
import java.util.function.Function;

import static com.io7m.polaris.parser.api.PParseErrorCode.EXPECTED_EXPRESSION_BUT_GOT_DECLARATION;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_APPLICATION;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_LAMBDA;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_LAMBDA_DUPLICATE_PARAMETER;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_LOCAL;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_MATCH;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_MATCH_CASE;
import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PValidation.cast;
import static com.io7m.polaris.parser.implementation.PValidation.errorsFlatten;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;
import static com.io7m.polaris.parser.implementation.PValidation.sequence;

/**
 * Functions to transform s-expressions to AST elements.
 */

public final class PParsing
{
  private PParsing()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given s-expression as a pattern.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return A pattern
   */

  public static Validation<Seq<PParseError>, PPatternType<PParsed>> parsePattern(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");
    return PParsingPatterns.parseAsPattern(m, e);
  }

  /**
   * Parse the given s-expression as a term-level expression or a declaration.
   *
   * @param m  An error message provider
   * @param ex The input expression
   *
   * @return A term-level expression or declaration
   */

  public static Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> parseExpressionOrDeclaration(
    final PParseErrorMessagesType m,
    final SExpressionType ex)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(ex, "Expression");

    return ex.matchExpression(
      new SExpressionMatcherType<
        Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>>,
        RuntimeException>()
      {
        @Override
        public Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> list(
          final SExpressionListType e)
        {
          return parseExpressionOrDeclarationList(m, e);
        }

        @Override
        public Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> quotedString(
          final SExpressionQuotedStringType e)
        {
          return parseExpressionOrDeclarationQuotedString(e);
        }

        @Override
        public Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> symbol(
          final SExpressionSymbolType e)
        {
          return parseExpressionOrDeclarationSymbol(m, e);
        }
      });
  }

  /**
   * Parse the given s-expression as an expression.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return A term-level expression
   */

  public static Validation<Seq<PParseError>, PExpressionType<PParsed>> parseExpression(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> result =
      parseExpressionOrDeclaration(m, e);

    return result.flatMap(ex -> {
      switch (ex.expressionOrDeclarationKind()) {
        case DECLARATION: {
          return invalid(m.errorExpression(
            EXPECTED_EXPRESSION_BUT_GOT_DECLARATION, e));
        }
        case EXPRESSION: {
          return Validation.valid((PExpressionType<PParsed>) ex);
        }
      }

      throw new UnreachableCodeException();
    });
  }

  private static Validation<Seq<PParseError>, PExpressionType<PParsed>> onSymbol(
    final PParseErrorMessagesType m,
    final SExpressionSymbolType e)
  {
    final String text = e.text();

    if (PParsingIntegers.appearsToBeNumeric(text)) {
      if (PParsingReals.appearsToBeReal(text)) {
        return PParsingReals.parseReal(m, e)
          .map(value -> PExprConstantReal.of(e.lexical(), parsed(), value));
      }

      return PParsingIntegers.parseInteger(m, e)
        .map(pair -> PExprConstantInteger.of(
          e.lexical(),
          parsed(),
          pair._2.intValue(),
          pair._1));
    }

    return PParsingTermReferences.parseTermReference(m, e)
      .map(pt -> PExprReference.of(parsed(), pt));
  }

  private static Validation<Seq<PParseError>, PExpressionType<PParsed>> onQuotedString(
    final SExpressionQuotedStringType e)
  {
    return Validation.valid(
      PExprConstantString.of(e.lexical(), parsed(), e.text()));
  }

  private static Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>>
  parseExpressionOrDeclarationSymbol(
    final PParseErrorMessagesType m,
    final SExpressionSymbolType e)
  {
    return cast(onSymbol(m, e));
  }

  private static Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>>
  parseExpressionOrDeclarationQuotedString(
    final SExpressionQuotedStringType e)
  {
    return cast(onQuotedString(e));
  }

  private static Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>>
  parseExpressionOrDeclarationList(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    /*
     * Empty applications are always syntactically invalid.
     */

    if (e.size() == 0) {
      return invalid(m.errorExpression(INVALID_APPLICATION, e));
    }

    /*
     * Check the first symbol to see if this expression is a special form.
     */

    if (e.get(0) instanceof SExpressionSymbolType) {
      final SExpressionSymbolType sym = (SExpressionSymbolType) e.get(0);
      switch (sym.text()) {

        case "lambda": {
          return cast(parseExpressionLambda(m, e));
        }

        case "λ": {
          return cast(parseExpressionLambda(m, e));
        }

        case "match": {
          return cast(parseExpressionMatch(m, e));
        }

        case "local": {
          return cast(parseExpressionLocal(m, e));
        }

        case "value": {
          return cast(PParsingValues.parseValue(m, e));
        }

        case "function": {
          return cast(PParsingFunctions.parseFunction(m, e));
        }

        case "record": {
          return cast(PParsingRecords.parseRecord(m, e));
        }

        case "variant": {
          return cast(PParsingVariants.parseVariant(m, e));
        }

        case ":": {
          return cast(PParsingSignatures.parseSignature(m, e));
        }

        default: {
          break;
        }
      }
    }

    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    return sequence(e, se -> parseExpression(m, se))
      .map(es -> PExprApplication.of(parsed(), es.head(), es.tail()));
  }

  private static Validation<Seq<PParseError>, PExpressionType<PParsed>>
  parseExpressionLocal(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    final SExpressionType e_keyword = e.get(0);

    Preconditions.checkPrecondition(
      e_keyword,
      e_keyword instanceof SExpressionSymbolType,
      c -> "Local expression must begin with local keyword");

    if (e.size() >= 2) {
      final Vector<SExpressionType> e_subs = Vector.ofAll(e).tail();
      final SExpressionType e_body = e_subs.last();
      final Vector<SExpressionType> e_locals = e_subs.dropRight(1);

      final Validation<Seq<PParseError>, Vector<PExpressionOrDeclarationType<PParsed>>> r_locals =
        sequence(e_locals, ex -> parseExpressionOrDeclaration(m, ex));
      final Validation<Seq<PParseError>, PExpressionType<PParsed>> r_body =
        parseExpression(m, e_body);

      final Validation<Seq<Seq<PParseError>>, PExpressionType<PParsed>> r_result =
        Validation.combine(r_locals, r_body)
          .ap((locals, body) -> PExprLocal.of(
            e.lexical(),
            parsed(),
            locals,
            body));

      return errorsFlatten(r_result);
    }

    return invalid(m.errorExpression(INVALID_LOCAL, e));
  }

  /*
   * Parse all subexpressions, accumulating errors, and raising errors if
   * any of the sub expressions are not expressions.
   */

  private static Validation<Seq<PParseError>, PExpressionType<PParsed>>
  parseExpressionMatch(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    final SExpressionType e_keyword = e.get(0);

    Preconditions.checkPrecondition(
      e_keyword,
      e_keyword instanceof SExpressionSymbolType,
      c -> "Match expression must begin with match keyword");

    if (e.size() >= 3) {
      final SExpressionType e_target = e.get(1);
      final Vector<SExpressionType> e_rest = Vector.ofAll(e).tail().tail();

      final Validation<Seq<PParseError>, PExpressionType<PParsed>> r_target =
        parseExpression(m, e_target);
      final Validation<Seq<PParseError>, Vector<PMatchCaseType<PParsed>>> r_cases =
        sequence(e_rest, ex -> parseMatchCase(m, ex));
      final Validation<Seq<Seq<PParseError>>, PExpressionType<PParsed>> r_match =
        Validation.combine(r_target, r_cases)
          .ap((expr, cases) ->
                PExprMatch.of(e.lexical(), parsed(), expr, cases));

      return errorsFlatten(r_match);
    }

    return invalid(m.errorExpression(INVALID_MATCH, e));
  }

  private static Validation<Seq<PParseError>, PMatchCaseType<PParsed>>
  parseMatchCase(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    if (e instanceof SExpressionListType) {
      final SExpressionListType e_list = (SExpressionListType) e;
      if (e_list.size() == 3) {
        final Validation<Seq<PParseError>, String> r_keyword =
          PParsingNames.parseKeyword(m, e_list.get(0), "case");
        final Validation<Seq<PParseError>, PPatternType<PParsed>> r_pattern =
          parsePattern(m, e_list.get(1));
        final Validation<Seq<PParseError>, PExpressionType<PParsed>> r_express =
          parseExpression(m, e_list.get(2));

        final Validation<Seq<Seq<PParseError>>, PMatchCaseType<PParsed>> r_result =
          Validation.combine(r_keyword, r_pattern, r_express)
            .ap((keyword, pattern, expression) ->
                  PMatchCase.of(e.lexical(), parsed(), pattern, expression));

        return errorsFlatten(r_result);
      }
    }

    return invalid(m.errorExpression(INVALID_MATCH_CASE, e));
  }

  private static Validation<Seq<PParseError>, PExpressionType<PParsed>>
  parseExpressionLambda(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    final SExpressionType e_keyword = e.get(0);

    Preconditions.checkPrecondition(
      e_keyword,
      e_keyword instanceof SExpressionSymbolType,
      c -> "Lambda expression must begin with lambda/λ keyword");

    if (e.size() == 3) {
      final SExpressionType e_param_list = e.get(1);
      final SExpressionType e_body = e.get(2);
      if (e_param_list instanceof SExpressionListType) {

        final Validation<Seq<PParseError>, Vector<PTermName<PParsed>>> r_params =
          sequence(
            (SExpressionListType) e_param_list,
            ex -> PParsingNames.parseTermName(m, ex))
            .flatMap(params -> requireUniqueNames(m, params));

        final Validation<Seq<PParseError>, PExpressionType<PParsed>> r_body =
          parseExpression(m, e_body);

        final Validation<Seq<Seq<PParseError>>, PExpressionType<PParsed>> rr =
          Validation
            .combine(r_params, r_body)
            .ap((names, body) -> parseExpressionLambdaMake(e, names, body));

        return errorsFlatten(rr);
      }
    }

    return invalid(m.errorExpression(INVALID_LAMBDA, e));
  }

  private static Validation<Seq<PParseError>, Vector<PTermName<PParsed>>>
  requireUniqueNames(
    final PParseErrorMessagesType m,
    final Vector<PTermName<PParsed>> names)
  {
    return PParsingNames.requireUniqueNames(
      names,
      Function.identity(),
      dups -> dups.map(dup -> m.errorLexical(
        INVALID_LAMBDA_DUPLICATE_PARAMETER, dup.lexical(), dup.value())));
  }

  private static PExprLambda<PParsed> parseExpressionLambdaMake(
    final SExpressionListType e,
    final Vector<PTermName<PParsed>> names,
    final PExpressionType<PParsed> body)
  {
    return PExprLambda.of(
      e.lexical(), parsed(), PVectors.vectorCast(names), body);
  }
}
