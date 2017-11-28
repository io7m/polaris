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

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.polaris.core.PImmutableStyleType;
import org.immutables.value.Value;

import java.net.URI;
import java.util.Optional;

/**
 * The type of unit names.
 *
 * @param <T> The type of associated data
 */

@PImmutableStyleType
@Value.Immutable(builder = false)
public interface PUnitNameType<T> extends PModelElementType<T>
{
  @Override
  @Value.Parameter
  @Value.Auxiliary
  LexicalPosition<URI> lexical();

  @Override
  @Value.Parameter
  @Value.Auxiliary
  T data();

  /**
   * @return The actual name value
   */

  @Value.Parameter
  String value();

  /**
   * Check preconditions for the type.
   */

  @Value.Check
  default void checkPreconditions()
  {
    Preconditions.checkPrecondition(
      PUnitNames.isValid(this.value()), "Name must be valid");
  }
}
