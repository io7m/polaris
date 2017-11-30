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

import java.net.URI;

/**
 * The type of term declarations.
 *
 * @param <T> The type of associated data
 */

public interface PTermDeclarationType<T> extends PDeclarationType<T>
{
  @Override
  default AnyDeclarationKind anyDeclarationKind()
  {
    return AnyDeclarationKind.TERM_DECLARATION;
  }

  /**
   * @return The kind of declaration
   */

  TermDeclarationKind termDeclarationKind();

  /**
   * The kind of declaration
   */

  enum TermDeclarationKind
  {
    /**
     * @see PDeclarationFunctionType
     */

    FUNCTION_DECLARATION,

    /**
     * @see PDeclarationSignatureType
     */

    SIGNATURE_DECLARATION,

    /**
     * @see PDeclarationValueType
     */

    VALUE_DECLARATION
  }

  /**
   * A value declaration.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PDeclarationValueType<T> extends PTermDeclarationType<T>
  {
    @Override
    default AnyDeclarationKind anyDeclarationKind()
    {
      return AnyDeclarationKind.TERM_DECLARATION;
    }

    @Override
    default TermDeclarationKind termDeclarationKind()
    {
      return TermDeclarationKind.VALUE_DECLARATION;
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
     * @return The name of the value
     */

    @Value.Parameter
    PTermVariableNameType<T> name();

    /**
     * @return The expression
     */

    @Value.Parameter
    PExpressionType<T> expression();
  }

  /**
   * A function declaration.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PDeclarationFunctionType<T> extends PTermDeclarationType<T>
  {
    @Override
    default AnyDeclarationKind anyDeclarationKind()
    {
      return AnyDeclarationKind.TERM_DECLARATION;
    }

    @Override
    default TermDeclarationKind termDeclarationKind()
    {
      return TermDeclarationKind.FUNCTION_DECLARATION;
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
     * @return The name of the function
     */

    @Value.Parameter
    PTermVariableNameType<T> name();

    /**
     * @return The function parameters
     */

    @Value.Parameter
    Vector<PTermVariableNameType<T>> parameters();

    /**
     * @return The expression
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
        this.parameters(),
        this.parameters().size() == this.parameters().toSet().size(),
        d -> "Function parameter names must be unique");
    }
  }

  /**
   * A type signature declaration.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PDeclarationSignatureType<T> extends PTermDeclarationType<T>
  {
    @Override
    default AnyDeclarationKind anyDeclarationKind()
    {
      return AnyDeclarationKind.TERM_DECLARATION;
    }

    @Override
    default TermDeclarationKind termDeclarationKind()
    {
      return TermDeclarationKind.SIGNATURE_DECLARATION;
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
     * @return The name of the term
     */

    @Value.Parameter
    PTermVariableNameType<T> name();

    /**
     * @return The type
     */

    @Value.Parameter
    PTypeExpressionType<T> type();
  }
}
