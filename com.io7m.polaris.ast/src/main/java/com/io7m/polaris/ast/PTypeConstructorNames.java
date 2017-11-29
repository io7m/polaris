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

package com.io7m.polaris.ast;

import com.io7m.junreachable.UnreachableCodeException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * The type of type constructor names.
 */

public final class PTypeConstructorNames
{
  /**
   * A pattern describing valid names.
   */

  public static final Pattern PATTERN =
    Pattern.compile(
      "\\p{Lu}[\\p{Ll}\\p{Lu}\\p{Digit}_]{0,127}",
      Pattern.UNICODE_CHARACTER_CLASS);

  private PTypeConstructorNames()
  {
    throw new UnreachableCodeException();
  }

  /**
   * @param name The {@code name}
   *
   * @return {@code true} iff {@code name} is valid
   */

  public static boolean isValid(
    final String name)
  {
    Objects.requireNonNull(name, "Name");
    return PATTERN.matcher(name).matches();
  }
}
