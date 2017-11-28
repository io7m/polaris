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

package com.io7m.polaris.model;

/**
 * The type of declarations.
 *
 * @param <T> The type of associated data
 */

public interface PDeclarationType<T> extends PExpressionOrDeclarationType<T>
{
  /**
   * @return The kind of declaration
   */

  TermTypeDeclarationKind termTypeDeclarationKind();

  @Override
  default PExpressionOrDeclarationKind expressionOrDeclarationKind()
  {
    return PExpressionOrDeclarationKind.DECLARATION;
  }

  /**
   * The kind of declaration
   */

  enum TermTypeDeclarationKind
  {
    /**
     * @see PTermDeclarationType
     */

    TERM_DECLARATION,

    /**
     * @see PTypeDeclarationType
     */

    TYPE_DECLARATION
  }
}
