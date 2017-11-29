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

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.polaris.core.PImmutableStyleType;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Optional;

/**
 * The type of patterns.
 *
 * @param <T> The type of associated data
 */

public interface PPatternType<T> extends PModelElementType<T>
{
  /**
   * @return The kind of pattern
   */

  Kind kind();

  /**
   * The kind of patterns.
   */

  enum Kind
  {
    /**
     * @see PPatternConstantRealType
     */

    PATTERN_CONSTANT_REAL,

    /**
     * @see PPatternConstantStringType
     */

    PATTERN_CONSTANT_STRING,

    /**
     * @see PPatternConstantIntegerType
     */

    PATTERN_CONSTANT_INTEGER,

    /**
     * @see PPatternConstructorType
     */

    PATTERN_CONSTRUCTOR,

    /**
     * @see PPatternWildcardType
     */

    PATTERN_WILDCARD
  }

  /**
   * An integer constant pattern.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PPatternConstantIntegerType<T> extends PPatternType<T>
  {
    @Override
    default Kind kind()
    {
      return Kind.PATTERN_CONSTANT_INTEGER;
    }

    @Override
    @Value.Parameter
    @Value.Auxiliary
    LexicalPosition<URI> lexical();

    @Override
    @Value.Parameter
    @Value.Auxiliary
    T data();

    /**
     * @return The number base/radix
     */

    @Value.Parameter
    int radix();

    /**
     * @return The constant value
     */

    @Value.Parameter
    BigInteger value();
  }

  /**
   * A real constant pattern.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PPatternConstantRealType<T> extends PPatternType<T>
  {
    @Override
    default Kind kind()
    {
      return Kind.PATTERN_CONSTANT_REAL;
    }

    @Override
    @Value.Parameter
    @Value.Auxiliary
    LexicalPosition<URI> lexical();

    @Override
    @Value.Parameter
    @Value.Auxiliary
    T data();

    /**
     * @return The constant value
     */

    @Value.Parameter
    BigDecimal value();
  }

  /**
   * A string constant pattern.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PPatternConstantStringType<T> extends PPatternType<T>
  {
    @Override
    default Kind kind()
    {
      return Kind.PATTERN_CONSTANT_STRING;
    }

    @Override
    @Value.Parameter
    @Value.Auxiliary
    LexicalPosition<URI> lexical();

    @Override
    @Value.Parameter
    @Value.Auxiliary
    T data();

    /**
     * @return The constant value
     */

    @Value.Parameter
    String value();
  }

  /**
   * A wildcard pattern.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PPatternWildcardType<T> extends PPatternType<T>
  {
    @Override
    default Kind kind()
    {
      return Kind.PATTERN_WILDCARD;
    }

    @Override
    @Value.Parameter
    @Value.Auxiliary
    LexicalPosition<URI> lexical();

    @Override
    @Value.Parameter
    @Value.Auxiliary
    T data();
  }

  /**
   * A constructor pattern.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PPatternConstructorType<T> extends PPatternType<T>
  {
    @Override
    default Kind kind()
    {
      return Kind.PATTERN_CONSTRUCTOR;
    }

    @Override
    @Value.Parameter
    @Value.Auxiliary
    LexicalPosition<URI> lexical();

    @Override
    @Value.Parameter
    @Value.Auxiliary
    T data();

    /**
     * @return The constructor name
     */

    @Value.Parameter
    PTermReferenceType.PTermReferenceConstructorType<T> constructor();

    /**
     * @return The constructor argument
     */

    @Value.Parameter
    Optional<PTermNameType<T>> argument();
  }
}
