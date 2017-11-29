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

import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionMatcherType;
import com.io7m.jsx.SExpressionQuotedStringType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.ast.PPatternConstantInteger;
import com.io7m.polaris.ast.PPatternConstantReal;
import com.io7m.polaris.ast.PPatternConstantString;
import com.io7m.polaris.ast.PPatternConstructor;
import com.io7m.polaris.ast.PPatternType;
import com.io7m.polaris.ast.PPatternWildcard;
import com.io7m.polaris.ast.PTermName;
import com.io7m.polaris.ast.PTermReferenceConstructor;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.util.Objects;
import java.util.Optional;

import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_PATTERN;
import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PValidation.errorsFlatten;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;

/**
 * Functions to parse patterns.
 */

public final class PParsingPatterns
{
  private PParsingPatterns()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as a pattern.
   *
   * @param m A message provider
   * @param e The expression
   *
   * @return A parsed pattern, or a sequence of errors
   */

  public static Validation<Seq<PParseError>, PPatternType<PParsed>> parseAsPattern(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(e, "Expression");

    return e.matchExpression(
      new SExpressionMatcherType<Validation<Seq<PParseError>, PPatternType<PParsed>>, RuntimeException>()
      {
        @Override
        public Validation<Seq<PParseError>, PPatternType<PParsed>> list(
          final SExpressionListType el)
        {
          return parsePatternList(m, el);
        }

        @Override
        public Validation<Seq<PParseError>, PPatternType<PParsed>> quotedString(
          final SExpressionQuotedStringType eq)
        {
          return parsePatternQuotedString(m, eq);
        }

        @Override
        public Validation<Seq<PParseError>, PPatternType<PParsed>> symbol(
          final SExpressionSymbolType es)
        {
          return parsePatternSymbol(m, es);
        }
      });
  }

  private static Validation<Seq<PParseError>, PPatternType<PParsed>> parsePatternList(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    if (e.size() == 2) {
      final SExpressionType e0 = e.get(0);
      final SExpressionType e1 = e.get(1);

      final Validation<Seq<PParseError>, PTermReferenceConstructor<PParsed>> r_constructor =
        PParsingTermReferences.parseConstructorReference(m, e0);
      final Validation<Seq<PParseError>, PTermName<PParsed>> r_arg =
        PParsingNames.parseTermName(m, e1);
      final Validation<Seq<Seq<PParseError>>, PPatternType<PParsed>> r_result =
        Validation.combine(r_constructor, r_arg)
          .ap((t_cons, t_arg) -> PPatternConstructor.of(
            e.lexical(), parsed(), t_cons, Optional.of(t_arg)));

      return errorsFlatten(r_result);
    }

    return invalid(m.errorExpression(INVALID_PATTERN, e));
  }

  private static Validation<Seq<PParseError>, PPatternType<PParsed>> parsePatternSymbol(
    final PParseErrorMessagesType m,
    final SExpressionSymbolType e)
  {
    final String text = e.text();

    if (Objects.equals(text, "_")) {
      return Validation.valid(PPatternWildcard.of(e.lexical(), parsed()));
    }

    if (PParsingIntegers.appearsToBeNumeric(text)) {
      if (PParsingReals.appearsToBeReal(text)) {
        return PParsingReals.parseReal(m, e)
          .map(value -> PPatternConstantReal.of(e.lexical(), parsed(), value));
      }
      return PParsingIntegers.parseInteger(m, e)
        .map(pair -> PPatternConstantInteger.of(
          e.lexical(),
          parsed(),
          pair._2.intValue(),
          pair._1));
    }

    return PParsingTermReferences.parseConstructorReference(m, e)
      .map(name -> PPatternConstructor.of(
        e.lexical(), parsed(), name, Optional.empty()));
  }

  private static Validation<Seq<PParseError>, PPatternType<PParsed>> parsePatternQuotedString(
    final PParseErrorMessagesType m,
    final SExpressionQuotedStringType e)
  {
    return Validation.valid(
      PPatternConstantString.of(e.lexical(), parsed(), e.text()));
  }
}
