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

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.polaris.core.PImmutableStyleType;
import io.vavr.collection.Vector;
import org.immutables.value.Value;

import java.net.URI;
import java.util.Optional;

/**
 * The type of term references.
 *
 * @param <T> The type of associated data
 */

public interface PTermReferenceType<T> extends PASTElementType<T>
{
  /**
   * @return The kind of reference
   */

  ReferenceKind referenceKind();

  /**
   * The kind of reference.
   */

  enum ReferenceKind
  {
    /**
     * @see PTermReferenceConstructorType
     */

    REFERENCE_CONSTRUCTOR,

    /**
     * @see PTermReferenceVariableType
     */

    REFERENCE_VARIABLE
  }

  /**
   * A reference to a constructor.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PTermReferenceConstructorType<T> extends PTermReferenceType<T>
  {
    @Override
    default ReferenceKind referenceKind()
    {
      return ReferenceKind.REFERENCE_CONSTRUCTOR;
    }

    @Override
    default LexicalPosition<URI> lexical()
    {
      return this.unit().map(PUnitNameType::lexical)
        .orElse(this.constructor().lexical());
    }

    @Override
    @Value.Parameter
    @Value.Auxiliary
    T data();

    /**
     * @return The qualifying unit name, if any
     */

    @Value.Parameter
    Optional<PUnitNameType<T>> unit();

    /**
     * @return The constructor name
     */

    @Value.Parameter
    PTermConstructorNameType<T> constructor();
  }

  /**
   * A reference to a variable.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PTermReferenceVariableType<T> extends PTermReferenceType<T>
  {
    @Override
    default ReferenceKind referenceKind()
    {
      return ReferenceKind.REFERENCE_VARIABLE;
    }

    @Override
    default LexicalPosition<URI> lexical()
    {
      return this.unit().map(PUnitNameType::lexical)
        .orElse(this.term().lexical());
    }

    @Override
    @Value.Parameter
    @Value.Auxiliary
    T data();

    /**
     * @return The qualifying unit name, if any
     */

    @Value.Parameter
    Optional<PUnitNameType<T>> unit();

    /**
     * @return The term name
     */

    @Value.Parameter
    PTermVariableNameType<T> term();

    /**
     * @return A sequence of record accesses
     */

    @Value.Parameter
    Vector<PTermVariableNameType<T>> recordPath();
  }
}
