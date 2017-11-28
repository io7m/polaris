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
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.math.BigInteger;
import java.util.Objects;
import java.util.regex.Pattern;

import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_INTEGER;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;

/**
 * Functions to parse integers.
 */

public final class PParsingIntegers
{
  private static final Pattern PREFIX_HEX = Pattern.compile("0x");
  private static final Pattern PREFIX_OCTAL = Pattern.compile("0o");
  private static final Pattern PREFIX_BINARY = Pattern.compile("0b");

  private PParsingIntegers()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as an integer.
   *
   * @param m A message provider
   * @param e The expression
   *
   * @return A value and radix, or a sequence of parse errors
   */

  public static Validation<Seq<PParseError>, Tuple2<BigInteger, Integer>> parseInteger(
    final PParseErrorMessagesType m,
    final SExpressionSymbolType e)
  {
    Objects.requireNonNull(e, "Expression");

    final String no_underscores = e.text().replace("_", "");
    try {
      final BigInteger value;
      final int radix;
      if (no_underscores.startsWith("0x")) {
        radix = 16;
        final String text =
          PREFIX_HEX.matcher(no_underscores).replaceFirst("");
        value = new BigInteger(text, radix);
      } else if (no_underscores.startsWith("0o")) {
        radix = 8;
        final String text =
          PREFIX_OCTAL.matcher(no_underscores).replaceFirst("");
        value = new BigInteger(text, radix);
      } else if (no_underscores.startsWith("0b")) {
        radix = 2;
        final String text =
          PREFIX_BINARY.matcher(no_underscores).replaceFirst("");
        value = new BigInteger(text, radix);
      } else {
        radix = 10;
        value = new BigInteger(no_underscores, radix);
      }

      return Validation.valid(Tuple.of(value, Integer.valueOf(radix)));
    } catch (final NumberFormatException ex) {
      return invalid(m.errorExpressionException(INVALID_INTEGER, e, ex));
    }
  }

  /**
   * @param text The text
   *
   * @return {@code true} iff the given text appears to be numeric
   */

  public static boolean appearsToBeNumeric(
    final String text)
  {
    Objects.requireNonNull(text, "Text");
    final int code = text.codePointAt(0);
    return code >= '0' && code <= '9';
  }
}
