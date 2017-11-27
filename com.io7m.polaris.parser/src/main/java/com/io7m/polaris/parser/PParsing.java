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

package com.io7m.polaris.parser;

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
import com.io7m.polaris.parser.api.PParsed;
import com.io7m.polaris.parser.implementation.PParsingIntegers;
import com.io7m.polaris.parser.implementation.PParsingNames;
import com.io7m.polaris.parser.implementation.PParsingPatterns;
import com.io7m.polaris.parser.implementation.PParsingReals;
import com.io7m.polaris.parser.implementation.PValidation;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.util.Objects;
import java.util.function.Function;

import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorExpectedExpressionGotDeclaration;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorInvalidExpression;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorMessageEmptyExpression;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorMessageExpectedKeyword;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorMessageInvalidLambda;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorMessageInvalidLocal;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorMessageInvalidMatch;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorMessageInvalidMatchCase;

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
   * @param e The input expression
   *
   * @return A pattern
   */

  public static Validation<Seq<PParseError>, PPatternType<PParsed>> parsePattern(
    final SExpressionType e)
  {
    Objects.requireNonNull(e, "Expression");
    return PParsingPatterns.parseAsPattern(e);
  }

  /**
   * Parse the given s-expression as a term-level expression or a declaration.
   *
   * @param ex The input expression
   *
   * @return A term-level expression or declaration
   */

  public static Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> parseExpressionOrDeclaration(
    final SExpressionType ex)
  {
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
          return parseExpressionOrDeclarationList(e);
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
          return parseExpressionOrDeclarationSymbol(e);
        }
      });
  }

  /**
   * Parse the given s-expression as an expression.
   *
   * @param e The input expression
   *
   * @return A term-level expression
   */

  public static Validation<Seq<PParseError>, PExpressionType<PParsed>> parseExpression(
    final SExpressionType e)
  {
    Objects.requireNonNull(e, "Expression");

    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> result =
      parseExpressionOrDeclaration(e);

    return result.flatMap(ex -> {
      switch (ex.expressionOrDeclarationKind()) {
        case DECLARATION: {
          return errorInvalidExpression(
            e, () -> errorExpectedExpressionGotDeclaration(e));
        }
        case EXPRESSION: {
          return Validation.valid((PExpressionType<PParsed>) ex);
        }
      }

      throw new UnreachableCodeException();
    });
  }

  private static Validation<Seq<PParseError>, PExpressionType<PParsed>> onSymbol(
    final SExpressionSymbolType e)
  {
    final String text = e.text();

    if (PParsingIntegers.appearsToBeNumeric(text)) {
      if (PParsingReals.appearsToBeReal(text)) {
        return PParsingReals.parseReal(e)
          .map(value -> PExprConstantReal.of(e.lexical(), parsed(), value));
      }

      return PParsingIntegers.parseInteger(e)
        .map(pair -> PExprConstantInteger.of(
          e.lexical(),
          parsed(),
          pair._2.intValue(),
          pair._1));
    }

    return PParsingNames.parseTermPath(e)
      .map(PParsing::transformPathToReference);
  }

  private static PExpressionType<PParsed> transformPathToReference(
    final PParsingNames.PTermReferencePath path)
  {
    return PExprReference.of(
      parsed(),
      path.unit(),
      path.base(),
      path.path().map(Function.identity()));
  }

  private static Validation<Seq<PParseError>, PExpressionType<PParsed>> onQuotedString(
    final SExpressionQuotedStringType e)
  {
    return Validation.valid(
      PExprConstantString.of(e.lexical(), parsed(), e.text()));
  }

  private static Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>>
  parseExpressionOrDeclarationSymbol(
    final SExpressionSymbolType e)
  {
    return onSymbol(e).map(Function.identity());
  }

  private static Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>>
  parseExpressionOrDeclarationQuotedString(
    final SExpressionQuotedStringType e)
  {
    return onQuotedString(e).map(Function.identity());
  }

  private static Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>>
  parseExpressionOrDeclarationList(
    final SExpressionListType e)
  {
    /*
     * Empty applications are always syntactically invalid.
     */

    if (e.size() == 0) {
      return errorInvalidExpression(e, () -> errorMessageEmptyExpression(e));
    }

    /*
     * Check the first symbol to see if this expression is a special form.
     */

    if (e.get(0) instanceof SExpressionSymbolType) {
      final SExpressionSymbolType sym = (SExpressionSymbolType) e.get(0);
      switch (sym.text()) {
        case "lambda": {
          return parseExpressionLambda(e).map(Function.identity());
        }
        case "λ": {
          return parseExpressionLambda(e).map(Function.identity());
        }
        case "match": {
          return parseExpressionMatch(e).map(Function.identity());
        }
        case "local": {
          return parseExpressionLocal(e).map(Function.identity());
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

    return PValidation.sequence(e, PParsing::parseExpression)
      .flatMap(exprs -> Validation.valid(PExprApplication.of(
        e.lexical(),
        parsed(),
        exprs.head(),
        exprs.tail())));
  }

  private static Validation<Seq<PParseError>, PExpressionType<PParsed>>
  parseExpressionLocal(
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
        PValidation.sequence(e_locals, PParsing::parseExpressionOrDeclaration);
      final Validation<Seq<PParseError>, PExpressionType<PParsed>> r_body =
        parseExpression(e_body);

      final Validation<Seq<Seq<PParseError>>, PExpressionType<PParsed>> r_result =
        Validation.combine(r_locals, r_body)
          .ap((locals, body) -> PExprLocal.of(
            e.lexical(),
            parsed(),
            locals,
            body));

      return PValidation.errorsFlatten(r_result);
    }

    return errorInvalidExpression(e, () -> errorMessageInvalidLocal(e));
  }

  /*
   * Parse all subexpressions, accumulating errors, and raising errors if
   * any of the sub expressions are not expressions.
   */

  private static Validation<Seq<PParseError>, PExpressionType<PParsed>>
  parseExpressionMatch(
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
        parseExpression(e_target);
      final Validation<Seq<PParseError>, Vector<PMatchCaseType<PParsed>>> r_cases =
        PValidation.sequence(e_rest, PParsing::parseMatchCase);
      final Validation<Seq<Seq<PParseError>>, PExpressionType<PParsed>> r_match =
        Validation.combine(r_target, r_cases)
          .ap((expr, cases) ->
                PExprMatch.of(e.lexical(), parsed(), expr, cases));

      return PValidation.errorsFlatten(r_match);
    }

    return errorInvalidExpression(e, () -> errorMessageInvalidMatch(e));
  }

  private static Validation<Seq<PParseError>, PMatchCaseType<PParsed>>
  parseMatchCase(
    final SExpressionType e)
  {
    if (e instanceof SExpressionListType) {
      final SExpressionListType e_list = (SExpressionListType) e;
      if (e_list.size() == 3) {
        final Validation<Seq<PParseError>, String> r_keyword =
          parseKeyword(e_list.get(0), "case");
        final Validation<Seq<PParseError>, PPatternType<PParsed>> r_pattern =
          parsePattern(e_list.get(1));
        final Validation<Seq<PParseError>, PExpressionType<PParsed>> r_express =
          parseExpression(e_list.get(2));

        final Validation<Seq<Seq<PParseError>>, PMatchCaseType<PParsed>> r_result =
          Validation.combine(r_keyword, r_pattern, r_express)
            .ap((keyword, pattern, expression) ->
                  PMatchCase.of(e.lexical(), parsed(), pattern, expression));

        return PValidation.errorsFlatten(r_result);
      }
    }

    return errorInvalidExpression(e, () -> errorMessageInvalidMatchCase(e));
  }

  private static Validation<Seq<PParseError>, String>
  parseKeyword(
    final SExpressionType e,
    final String name)
  {
    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      if (Objects.equals(es.text(), name)) {
        return Validation.valid(name);
      }
    }

    return errorInvalidExpression(
      e, () -> errorMessageExpectedKeyword(e, name));
  }

  private static Validation<Seq<PParseError>, PExpressionType<PParsed>>
  parseExpressionLambda(
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
          PValidation.sequence(
            (SExpressionListType) e_param_list,
            PParsingNames::parseTermNameUnqualified);

        final Validation<Seq<PParseError>, PExpressionType<PParsed>> r_body =
          parseExpression(e_body);

        final Validation<Seq<Seq<PParseError>>, PExpressionType<PParsed>> rr =
          Validation
            .combine(r_params, r_body)
            .ap((names, body) -> parseExpressionLambdaMake(e, names, body));

        return PValidation.errorsFlatten(rr);
      }
    }

    return errorInvalidExpression(e, () -> errorMessageInvalidLambda(e));
  }

  private static PExprLambda<PParsed> parseExpressionLambdaMake(
    final SExpressionListType e,
    final Vector<PTermName<PParsed>> names,
    final PExpressionType<PParsed> body)
  {
    return PExprLambda.of(
      e.lexical(), parsed(), names.map(Function.identity()), body);
  }

}
