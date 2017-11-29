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
import io.vavr.collection.Vector;
import org.immutables.value.Value;

import java.net.URI;

/**
 * The type of type-level expressions.
 *
 * @param <T> The type of associated data
 */

public interface PTypeExpressionType<T> extends PModelElementType<T>
{
  /**
   * @return The kind of expression
   */

  PTypeExpressionKind typeExpressionKind();

  @Override
  LexicalPosition<URI> lexical();

  @Override
  T data();

  /**
   * The kind of expressions.
   */

  enum PTypeExpressionKind
  {
    /**
     * @see PTypeExprArrowType
     */

    TYPE_EXPR_ARROW,

    /**
     * @see PTypeExprForAllType
     */

    TYPE_EXPR_FOR_ALL,

    /**
     * @see PTypeExprReferenceType
     */

    TYPE_EXPR_REFERENCE,

    /**
     * @see PTypeExprApplicationType
     */

    TYPE_EXPR_APPLICATION,
  }

  /**
   * An arrow type expression.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PTypeExprArrowType<T> extends PTypeExpressionType<T>
  {
    @Override
    default PTypeExpressionKind typeExpressionKind()
    {
      return PTypeExpressionKind.TYPE_EXPR_ARROW;
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
     * @return The function parameters
     */

    @Value.Parameter
    Vector<PTypeExpressionType<T>> parameters();

    /**
     * @return {@code true} iff the last function parameter is variadic
     */

    @Value.Parameter
    boolean isVariadic();

    /**
     * @return The function return type
     */

    @Value.Parameter
    PTypeExpressionType<T> returnType();
  }

  /**
   * A universally quantified type expression.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PTypeExprForAllType<T> extends PTypeExpressionType<T>
  {
    @Override
    default PTypeExpressionKind typeExpressionKind()
    {
      return PTypeExpressionKind.TYPE_EXPR_FOR_ALL;
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
     * @return The quantified parameters
     */

    @Value.Parameter
    Vector<PTypeNameType<T>> parameters();

    /**
     * @return The quantified expression
     */

    @Value.Parameter
    PTypeExpressionType<T> expression();

    /**
     * Check preconditions for the type.
     */

    @Value.Check
    default void checkPreconditions()
    {
      Preconditions.checkPrecondition(
        this.parameters(),
        !this.parameters().isEmpty(),
        p -> "Must specify at least one type parameter");

      Preconditions.checkPrecondition(
        this.parameters(),
        this.parameters().size() == this.parameters().toSet().size(),
        p -> "Type parameters must be uniquely named");
    }
  }

  /**
   * A reference to a type.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PTypeExprReferenceType<T> extends PTypeExpressionType<T>
  {
    @Override
    default PTypeExpressionKind typeExpressionKind()
    {
      return PTypeExpressionKind.TYPE_EXPR_REFERENCE;
    }

    @Override
    default LexicalPosition<URI> lexical()
    {
      return this.reference().lexical();
    }

    @Override
    @Value.Parameter
    @Value.Auxiliary
    T data();

    /**
     * @return The type reference
     */

    @Value.Parameter
    PTypeReferenceType<T> reference();
  }

  /**
   * A type constructor application.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PTypeExprApplicationType<T> extends PTypeExpressionType<T>
  {
    @Override
    default PTypeExpressionKind typeExpressionKind()
    {
      return PTypeExpressionKind.TYPE_EXPR_APPLICATION;
    }

    @Override
    default LexicalPosition<URI> lexical()
    {
      return this.constructor().lexical();
    }

    @Override
    @Value.Parameter
    @Value.Auxiliary
    T data();

    /**
     * @return The constructor
     */

    @Value.Parameter
    PTypeExpressionType<T> constructor();

    /**
     * @return The constructor arguments
     */

    @Value.Parameter
    Vector<PTypeExpressionType<T>> arguments();
  }
}
