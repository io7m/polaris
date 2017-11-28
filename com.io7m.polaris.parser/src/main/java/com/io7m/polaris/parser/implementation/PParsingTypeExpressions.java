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
import com.io7m.junreachable.UnimplementedCodeException;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.model.PTypeExprReference;
import com.io7m.polaris.model.PTypeExpressionType;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.util.Objects;

import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TYPE_EXPRESSION;
import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;

/**
 * Functions to parse type expressions.
 */

public final class PParsingTypeExpressions
{
  private PParsingTypeExpressions()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as a type expression.
   *
   * @param m An error message provider
   * @param e The expression
   *
   * @return A parsed type expression, or a sequence of errors
   */

  public static Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> parseTypeExpression(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(e, "Expression");

    return e.matchExpression(
      new SExpressionMatcherType<Validation<Seq<PParseError>, PTypeExpressionType<PParsed>>, RuntimeException>()
      {
        @Override
        public Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> list(
          final SExpressionListType el)
        {
          return parseTypeExpressionList(m, el);
        }

        @Override
        public Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> quotedString(
          final SExpressionQuotedStringType eq)
        {
          return parseTypeExpressionQuotedString(m, eq);
        }

        @Override
        public Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> symbol(
          final SExpressionSymbolType es)
        {
          return parseTypeExpressionSymbol(m, es);
        }
      });
  }

  private static Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> parseTypeExpressionSymbol(
    final PParseErrorMessagesType m,
    final SExpressionSymbolType es)
  {
    return PParsingNames.parseTypeReference(m, es)
      .map(ref -> PTypeExprReference.of(
        es.lexical(),
        parsed(),
        ref.unit(),
        ref.base()));
  }

  private static Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> parseTypeExpressionQuotedString(
    final PParseErrorMessagesType m,
    final SExpressionQuotedStringType eq)
  {
    return invalid(m.errorExpression(INVALID_TYPE_EXPRESSION, eq));
  }

  private static Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> parseTypeExpressionList(
    final PParseErrorMessagesType m,
    final SExpressionListType el)
  {
    throw new UnimplementedCodeException();
  }
}
