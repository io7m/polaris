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

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.polaris.core.PImmutableStyleType;
import io.vavr.collection.Vector;
import org.immutables.value.Value;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;

/**
 * The type of term-level expressions.
 *
 * @param <T> The type of associated data
 */

public interface PExpressionType<T> extends PExpressionOrDeclarationType<T>
{
  /**
   * @return The kind of expression
   */

  PTermExpressionKind termExpressionKind();

  @Override
  LexicalPosition<URI> lexical();

  @Override
  T data();

  @Override
  default PExpressionOrDeclarationKind expressionOrDeclarationKind()
  {
    return PExpressionOrDeclarationKind.EXPRESSION;
  }

  /**
   * The kind of expressions.
   */

  enum PTermExpressionKind
  {
    /**
     * @see PExprConstantRealType
     */

    EXPR_CONSTANT_REAL,

    /**
     * @see PExprConstantStringType
     */

    EXPR_CONSTANT_STRING,

    /**
     * @see PExprConstantIntegerType
     */

    EXPR_CONSTANT_INTEGER,

    /**
     * @see PExprApplicationType
     */

    EXPR_APPLICATION,

    /**
     * @see PExprLambdaType
     */

    EXPR_LAMBDA,

    /**
     * @see PExprReferenceType
     */

    EXPR_REFERENCE,

    /**
     * @see PExprLocalType
     */

    EXPR_LOCAL,

    /**
     * @see PExprMatchType
     */

    EXPR_MATCH
  }

  /**
   * An integer constant.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PExprConstantIntegerType<T> extends PExpressionType<T>
  {
    @Override
    default PTermExpressionKind termExpressionKind()
    {
      return PTermExpressionKind.EXPR_CONSTANT_INTEGER;
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
   * A real constant.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PExprConstantRealType<T> extends PExpressionType<T>
  {
    @Override
    default PTermExpressionKind termExpressionKind()
    {
      return PTermExpressionKind.EXPR_CONSTANT_REAL;
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
   * A string constant.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PExprConstantStringType<T> extends PExpressionType<T>
  {
    @Override
    default PTermExpressionKind termExpressionKind()
    {
      return PTermExpressionKind.EXPR_CONSTANT_STRING;
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
   * A function application.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PExprApplicationType<T> extends PExpressionType<T>
  {
    @Override
    default PTermExpressionKind termExpressionKind()
    {
      return PTermExpressionKind.EXPR_APPLICATION;
    }

    @Override
    default LexicalPosition<URI> lexical()
    {
      return this.function().lexical();
    }

    @Override
    @Value.Parameter
    @Value.Auxiliary
    T data();

    /**
     * @return The function value
     */

    @Value.Parameter
    PExpressionType<T> function();

    /**
     * @return The function arguments
     */

    @Value.Parameter
    Vector<PExpressionType<T>> arguments();
  }

  /**
   * A lambda expression.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PExprLambdaType<T> extends PExpressionType<T>
  {
    @Override
    default PTermExpressionKind termExpressionKind()
    {
      return PTermExpressionKind.EXPR_LAMBDA;
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
     * @return The arguments
     */

    @Value.Parameter
    Vector<PTermNameType<T>> arguments();

    /**
     * @return The body expression
     */

    @Value.Parameter
    PExpressionType<T> expression();

    /**
     * Check preconditions for the function.
     */

    @Value.Check
    default void checkPreconditions()
    {
      Preconditions.checkPrecondition(
        this.arguments(),
        this.arguments().size() == this.arguments().toSet().size(),
        d -> "Lambda parameter names must be unique");
    }
  }

  /**
   * A reference expression.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PExprReferenceType<T> extends PExpressionType<T>
  {
    @Override
    default PTermExpressionKind termExpressionKind()
    {
      return PTermExpressionKind.EXPR_REFERENCE;
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
     * @return The term reference
     */

    @Value.Parameter
    PTermReferenceType<T> reference();
  }

  /**
   * A local expression.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PExprLocalType<T> extends PExpressionType<T>
  {
    @Override
    default PTermExpressionKind termExpressionKind()
    {
      return PTermExpressionKind.EXPR_LOCAL;
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
     * @return The local expressions
     */

    @Value.Parameter
    Vector<PExpressionOrDeclarationType<T>> locals();

    /**
     * @return The body of the local expression
     */

    @Value.Parameter
    PExpressionType<T> body();
  }

  /**
   * A match expression.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PExprMatchType<T> extends PExpressionType<T>
  {
    @Override
    default PTermExpressionKind termExpressionKind()
    {
      return PTermExpressionKind.EXPR_MATCH;
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
     * @return The target of the match expression
     */

    @Value.Parameter
    PExpressionType<T> target();

    /**
     * @return The list of cases
     */

    @Value.Parameter
    Vector<PMatchCaseType<T>> cases();
  }

  /**
   * A match expression case.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PMatchCaseType<T> extends PASTElementType<T>
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
     * @return The case pattern
     */

    @Value.Parameter
    PPatternType<T> pattern();

    /**
     * @return The case expression
     */

    @Value.Parameter
    PExpressionType<T> expression();
  }
}
