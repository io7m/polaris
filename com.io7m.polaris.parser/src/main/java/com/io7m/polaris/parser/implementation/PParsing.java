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
import com.io7m.polaris.ast.PExprApplication;
import com.io7m.polaris.ast.PExprConstantInteger;
import com.io7m.polaris.ast.PExprConstantReal;
import com.io7m.polaris.ast.PExprConstantString;
import com.io7m.polaris.ast.PExprLambda;
import com.io7m.polaris.ast.PExprLocal;
import com.io7m.polaris.ast.PExprMatch;
import com.io7m.polaris.ast.PExprRecord;
import com.io7m.polaris.ast.PExprRecordField;
import com.io7m.polaris.ast.PExprRecordUpdate;
import com.io7m.polaris.ast.PExprReference;
import com.io7m.polaris.ast.PExpressionOrDeclarationType;
import com.io7m.polaris.ast.PExpressionType;
import com.io7m.polaris.ast.PExpressionType.PMatchCaseType;
import com.io7m.polaris.ast.PMatchCase;
import com.io7m.polaris.ast.PPatternType;
import com.io7m.polaris.ast.PTermVariableName;
import com.io7m.polaris.ast.PTypeReferenceType;
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
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_RECORD_EXPRESSION;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_RECORD_EXPRESSION_DUPLICATE_FIELD;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_RECORD_EXPRESSION_FIELD;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_RECORD_UPDATE_EXPRESSION;
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
        case "record": {
          return cast(parseExpressionRecord(m, e));
        }
        case "record-update": {
          return cast(parseExpressionRecordUpdate(m, e));
        }

        case "define-value": {
          return cast(PParsingValues.parseValue(m, e));
        }
        case "define-function": {
          return cast(PParsingFunctions.parseFunction(m, e));
        }
        case "define-record": {
          return cast(PParsingRecords.parseRecord(m, e));
        }
        case "define-variant": {
          return cast(PParsingVariants.parseVariant(m, e));
        }
        case "define-unit": {
          return cast(PParsingUnits.parseUnit(m, e));
        }
        case "import": {
          return cast(PParsingUnits.parseImport(m, e));
        }
        case "import-qualified": {
          return cast(PParsingUnits.parseImportQualified(m, e));
        }
        case "export-terms": {
          return cast(PParsingUnits.parseExportTerms(m, e));
        }
        case "export-types": {
          return cast(PParsingUnits.parseExportTypes(m, e));
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

        final Validation<Seq<PParseError>, Vector<PTermVariableName<PParsed>>> r_params =
          sequence(
            (SExpressionListType) e_param_list,
            ex -> PParsingNames.parseTermVariableName(m, ex))
            .flatMap(params -> requireUniqueNames(m, params));

        final Validation<Seq<PParseError>, PExpressionType<PParsed>> r_body =
          parseExpression(m, e_body);

        final Validation<Seq<Seq<PParseError>>, PExpressionType<PParsed>> rr =
          Validation.combine(r_params, r_body)
            .ap((names, body) ->
                  PExprLambda.of(
                    e.lexical(),
                    parsed(),
                    PVectors.vectorCast(names),
                    body));
        return errorsFlatten(rr);
      }
    }

    return invalid(m.errorExpression(INVALID_LAMBDA, e));
  }

  private static Validation<Seq<PParseError>, Vector<PTermVariableName<PParsed>>>
  requireUniqueNames(
    final PParseErrorMessagesType m,
    final Vector<PTermVariableName<PParsed>> names)
  {
    return PParsingNames.requireUniqueNames(
      names,
      Function.identity(),
      dups -> dups.map(dup -> m.errorLexical(
        INVALID_LAMBDA_DUPLICATE_PARAMETER, dup.lexical(), dup.value())));
  }

  private static Validation<Seq<PParseError>, PExprRecord<PParsed>>
  parseExpressionRecord(
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
      c -> "Record expression must begin with record keyword");

    if (e.size() >= 3) {
      final SExpressionType e_type = e.get(1);
      final Vector<SExpressionType> e_rest = Vector.ofAll(e).tail().tail();

      final Validation<Seq<PParseError>, PTypeReferenceType<PParsed>> r_type =
        PParsingTypeReferences.parseTypeReference(m, e_type);
      final Validation<Seq<PParseError>, Vector<PExprRecordField<PParsed>>> r_fields =
        sequence(e_rest, ee -> parseRecordField(m, ee));
      final Validation<Seq<PParseError>, Vector<PExprRecordField<PParsed>>> r_unique =
        r_fields.flatMap(names -> requireUniqueRecordFieldNames(m, names));
      final Validation<Seq<Seq<PParseError>>, PExprRecord<PParsed>> r_result =
        Validation.combine(r_type, r_unique)
          .ap((t_type, t_fields) -> PExprRecord.of(
            e.lexical(), parsed(), t_type, PVectors.vectorCast(t_fields)));
      return errorsFlatten(r_result);
    }

    return invalid(m.errorExpression(INVALID_RECORD_EXPRESSION, e));
  }

  private static Validation<Seq<PParseError>, PExprRecordUpdate<PParsed>>
  parseExpressionRecordUpdate(
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
      c -> "Record update expression must begin with record-update keyword");

    if (e.size() >= 3) {
      final SExpressionType e_source = e.get(1);
      final Vector<SExpressionType> e_rest = Vector.ofAll(e).tail().tail();
      final Validation<Seq<PParseError>, PExpressionType<PParsed>> r_source =
        parseExpression(m, e_source);
      final Validation<Seq<PParseError>, Vector<PExprRecordField<PParsed>>> r_fields =
        sequence(e_rest, ee -> parseRecordField(m, ee));
      final Validation<Seq<PParseError>, Vector<PExprRecordField<PParsed>>> r_unique =
        r_fields.flatMap(names -> requireUniqueRecordFieldNames(m, names));
      final Validation<Seq<Seq<PParseError>>, PExprRecordUpdate<PParsed>> r_result =
        Validation.combine(r_source, r_unique)
          .ap((t_source, t_fields) -> PExprRecordUpdate.of(
            e.lexical(), parsed(), t_source, PVectors.vectorCast(t_fields)));
      return errorsFlatten(r_result);
    }

    return invalid(m.errorExpression(INVALID_RECORD_UPDATE_EXPRESSION, e));
  }

  private static Validation<Seq<PParseError>, Vector<PExprRecordField<PParsed>>>
  requireUniqueRecordFieldNames(
    final PParseErrorMessagesType m,
    final Vector<PExprRecordField<PParsed>> names)
  {
    return PParsingNames.requireUniqueNames(
      names,
      PExprRecordField::field,
      dups -> dups.map(
        dup -> m.errorLexical(
          INVALID_RECORD_EXPRESSION_DUPLICATE_FIELD,
          dup.lexical(),
          dup.value())));
  }

  private static Validation<Seq<PParseError>, PExprRecordField<PParsed>>
  parseRecordField(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    if (e instanceof SExpressionListType) {
      final SExpressionListType es = (SExpressionListType) e;
      if (es.size() == 3) {
        final Validation<Seq<PParseError>, String> r_key =
          PParsingNames.parseKeyword(m, es.get(0), "field");
        final Validation<Seq<PParseError>, PTermVariableName<PParsed>> r_name =
          PParsingNames.parseTermVariableName(m, es.get(1));
        final Validation<Seq<PParseError>, PExpressionType<PParsed>> r_expr =
          parseExpression(m, es.get(2));
        final Validation<Seq<Seq<PParseError>>, PExprRecordField<PParsed>> r_result =
          Validation.combine(r_key, r_name, r_expr)
            .ap((kw, name, expr) -> PExprRecordField.of(
              e.lexical(), parsed(), name, expr));
        return errorsFlatten(r_result);
      }
    }

    return invalid(m.errorExpression(INVALID_RECORD_EXPRESSION_FIELD, e));
  }
}
