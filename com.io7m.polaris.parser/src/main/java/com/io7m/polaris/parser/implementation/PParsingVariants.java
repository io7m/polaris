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
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.ast.PDeclarationVariant;
import com.io7m.polaris.ast.PTermConstructorName;
import com.io7m.polaris.ast.PTypeConstructorName;
import com.io7m.polaris.ast.PTypeExpressionType;
import com.io7m.polaris.ast.PTypeVariableName;
import com.io7m.polaris.ast.PVariantCase;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.util.Objects;
import java.util.Optional;

import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_VARIANT;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_VARIANT_CASE;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_VARIANT_DUPLICATE_CASE;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_VARIANT_TYPE_PARAMETERS;
import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PValidation.errorsFlatten;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;
import static com.io7m.polaris.parser.implementation.PValidation.sequence;

/**
 * Functions to parse variant declarations.
 */

public final class PParsingVariants
{
  private PParsingVariants()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as a variant declaration.
   *
   * @param m An error message provider
   * @param e The expression
   *
   * @return A variant declaration, or a sequence of parse errors
   */

  public static Validation<Seq<PParseError>, PDeclarationVariant<PParsed>> parseVariant(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    final SExpressionType e_keyword = e.get(0);

    Preconditions.checkPrecondition(
      e_keyword,
      e_keyword instanceof SExpressionSymbolType,
      c -> "Variant declaration must begin with define-variant keyword");

    if (e.size() >= 3) {
      final Validation<Seq<PParseError>, PTypeConstructorName<PParsed>> r_name =
        PParsingNames.parseTypeConstructorName(m, e.get(1));
      final Validation<Seq<PParseError>, VariantParameters> r_rest =
        parseForAllAndCases(m, Vector.ofAll(e).tail().tail());

      final Validation<Seq<Seq<PParseError>>, PDeclarationVariant<PParsed>> r_result =
        Validation.combine(r_name, r_rest)
          .ap((name, params) ->
                PDeclarationVariant.of(
                  e.lexical(),
                  parsed(),
                  name,
                  PVectors.vectorCast(params.parameters),
                  PVectors.vectorCast(params.cases)));

      return errorsFlatten(r_result);
    }

    return invalid(m.errorExpression(INVALID_VARIANT, e));
  }

  private static Validation<Seq<PParseError>, VariantParameters> parseForAllAndCases(
    final PParseErrorMessagesType messages,
    final Vector<SExpressionType> exprs)
  {
    if (hasForAll(exprs)) {
      final Validation<Seq<PParseError>, Vector<PTypeVariableName<PParsed>>> r_forall =
        parseForAll(messages, exprs.get(0));
      final Validation<Seq<PParseError>, Vector<PVariantCase<PParsed>>> r_cases =
        parseCases(messages, exprs.tail());
      final Validation<Seq<Seq<PParseError>>, VariantParameters> r_result =
        Validation.combine(r_forall, r_cases).ap(VariantParameters::new);
      return errorsFlatten(r_result);
    }

    return parseCases(messages, exprs)
      .map(cases -> new VariantParameters(Vector.empty(), cases));
  }

  private static Validation<Seq<PParseError>, Vector<PVariantCase<PParsed>>> parseCases(
    final PParseErrorMessagesType m,
    final Vector<SExpressionType> exprs)
  {
    return sequence(exprs, f -> parseCase(m, f))
      .flatMap(cases -> requireCasesUnique(m, cases));
  }

  private static Validation<Seq<PParseError>, Vector<PVariantCase<PParsed>>> requireCasesUnique(
    final PParseErrorMessagesType messages,
    final Vector<PVariantCase<PParsed>> cases)
  {
    return PParsingNames.requireUniqueNames(
      cases,
      PVariantCase::name,
      dups -> dups.map(dup -> messages.errorLexical(
        INVALID_VARIANT_DUPLICATE_CASE, dup.lexical(), dup.value())));
  }

  private static Validation<Seq<PParseError>, PVariantCase<PParsed>> parseCase(
    final PParseErrorMessagesType m,
    final SExpressionType ex)
  {
    if (ex instanceof SExpressionListType) {
      final SExpressionListType exs = (SExpressionListType) ex;

      if (exs.size() >= 2) {
        final Validation<Seq<PParseError>, String> r_keyword =
          PParsingNames.parseKeyword(m, exs.get(0), "case");
        final Validation<Seq<PParseError>, PTermConstructorName<PParsed>> r_name =
          PParsingNames.parseTermConstructorName(m, exs.get(1));

        final Validation<Seq<Seq<PParseError>>, PVariantCase<PParsed>> r_result;
        if (exs.size() == 2) {
          r_result =
            Validation.combine(r_keyword, r_name)
              .ap((keyword, name) -> PVariantCase.of(
                exs.lexical(), parsed(), name, Optional.empty()));
          return errorsFlatten(r_result);
        }

        if (exs.size() == 3) {
          final Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> r_expr =
            PParsingTypeExpressions.parseTypeExpression(m, exs.get(2));
          r_result = Validation.combine(r_keyword, r_name, r_expr)
            .ap((keyword, name, expr) -> PVariantCase.of(
              exs.lexical(), parsed(), name, Optional.of(expr)));
          return errorsFlatten(r_result);
        }
      }
    }

    return invalid(m.errorExpression(INVALID_VARIANT_CASE, ex));
  }

  private static Validation<Seq<PParseError>, Vector<PTypeVariableName<PParsed>>> parseForAll(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    if (e instanceof SExpressionListType) {
      final SExpressionListType es = (SExpressionListType) e;
      final SExpressionSymbolType keyword = (SExpressionSymbolType) es.get(0);

      Preconditions.checkPrecondition(
        keyword,
        Objects.equals(keyword.text(), "for-all")
          || Objects.equals(keyword.text(), "∀"),
        k -> "Expecting a for-all");

      return sequence(
        Vector.ofAll(es).tail(),
        name -> PParsingNames.parseTypeVariableName(m, name));
    }

    return invalid(m.errorExpression(
      INVALID_VARIANT_TYPE_PARAMETERS,
      e));
  }

  private static boolean hasForAll(
    final Vector<SExpressionType> exprs)
  {
    Preconditions.checkPrecondition(
      exprs,
      exprs.size() > 0,
      e -> "Must be non-empty list of expressions");

    final SExpressionType ex = exprs.get(0);
    if (ex instanceof SExpressionListType) {
      final SExpressionListType exs = (SExpressionListType) ex;
      if (exs.size() > 0) {
        final SExpressionType ex_sym = exs.get(0);
        if (ex_sym instanceof SExpressionSymbolType) {
          final SExpressionSymbolType ex_syms = (SExpressionSymbolType) ex_sym;
          final String t = ex_syms.text();
          return Objects.equals(t, "∀") || Objects.equals(t, "for-all");
        }
      }
    }

    return false;
  }

  private static final class VariantParameters
  {
    private final Vector<PTypeVariableName<PParsed>> parameters;
    private final Vector<PVariantCase<PParsed>> cases;

    VariantParameters(
      final Vector<PTypeVariableName<PParsed>> in_parameters,
      final Vector<PVariantCase<PParsed>> in_cases)
    {
      this.parameters =
        Objects.requireNonNull(in_parameters, "Parameters");
      this.cases =
        Objects.requireNonNull(in_cases, "Cases");
    }
  }
}
