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

import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.parser.api.PParseError;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Functions to parse real values.
 */

public final class PParsingReals
{
  private PParsingReals()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as a real number.
   *
   * @param e The expression
   *
   * @return A real number, or a sequence of parse errors
   */

  public static Validation<Seq<PParseError>, BigDecimal> parseReal(
    final SExpressionSymbolType e)
  {
    Objects.requireNonNull(e, "Expression");

    try {
      return Validation.valid(new BigDecimal(e.text()));
    } catch (final NumberFormatException ex) {
      return PParseErrors.errorInvalid(PParseErrors.parseError(
        e, () -> PParseErrors.errorUnparseableReal(e.text())));
    }
  }

  /**
   * @param text The text
   *
   * @return {@code true} iff the given text appears to be a real number
   */

  public static boolean appearsToBeReal(
    final String text)
  {
    Objects.requireNonNull(text, "Text");
    return PParsingIntegers.appearsToBeNumeric(text) && text.contains(".");
  }
}
