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
import io.vavr.collection.Map;
import io.vavr.collection.Vector;
import org.immutables.value.Value;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

/**
 * The type of type-level declarations.
 *
 * @param <T> The type of associated data
 */

public interface PTypeDeclarationType<T> extends PDeclarationType<T>
{
  @Override
  default TermTypeDeclarationKind termTypeDeclarationKind()
  {
    return TermTypeDeclarationKind.TYPE_DECLARATION;
  }

  /**
   * @return The kind of declaration
   */

  TypeDeclarationKind typeDeclarationKind();

  /**
   * The kind of declaration
   */

  enum TypeDeclarationKind
  {
    /**
     * @see PDeclarationRecordType
     */

    RECORD_DECLARATION,

    /**
     * @see PDeclarationVariantType
     */

    VARIANT_DECLARATION
  }

  /**
   * A record declaration.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PDeclarationRecordType<T> extends PTypeDeclarationType<T>
  {
    @Override
    default TermTypeDeclarationKind termTypeDeclarationKind()
    {
      return TermTypeDeclarationKind.TERM_DECLARATION;
    }

    @Override
    default TypeDeclarationKind typeDeclarationKind()
    {
      return TypeDeclarationKind.RECORD_DECLARATION;
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
     * @return The name of the record
     */

    @Value.Parameter
    PTypeConstructorNameType<T> name();

    /**
     * @return The type parameters
     */

    @Value.Parameter
    Vector<PTypeVariableNameType<T>> parameters();

    /**
     * @return The fields in declaration order
     */

    @Value.Parameter
    Vector<PRecordFieldType<T>> fields();

    /**
     * @return The cases by name
     */

    @Value.Derived
    default Map<String, PRecordFieldType<T>> fieldsByName()
    {
      return this.fields().toMap(c -> c.name().value(), Function.identity());
    }

    /**
     * Check preconditions for the type.
     */

    @Value.Check
    default void checkPreconditions()
    {
      Preconditions.checkPrecondition(
        this.fields(),
        this.fieldsByName().size() == this.fields().size(),
        d -> "Field names must be unique");
    }
  }

  /**
   * A record field.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PRecordFieldType<T> extends PModelElementType<T>
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
     * @return The field name
     */

    @Value.Parameter
    PTermNameType<T> name();

    /**
     * @return The field type
     */

    @Value.Parameter
    PTypeExpressionType<T> type();
  }

  /**
   * A variant declaration.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PDeclarationVariantType<T> extends PTypeDeclarationType<T>
  {
    @Override
    default TermTypeDeclarationKind termTypeDeclarationKind()
    {
      return TermTypeDeclarationKind.TERM_DECLARATION;
    }

    @Override
    default TypeDeclarationKind typeDeclarationKind()
    {
      return TypeDeclarationKind.VARIANT_DECLARATION;
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
     * @return The name of the variant
     */

    @Value.Parameter
    PTypeConstructorNameType<T> name();

    /**
     * @return The type parameters
     */

    @Value.Parameter
    Vector<PTypeVariableNameType<T>> parameters();

    /**
     * @return The cases in declaration order
     */

    @Value.Parameter
    Vector<PVariantCaseType<T>> cases();

    /**
     * @return The cases by name
     */

    @Value.Derived
    default Map<String, PVariantCaseType<T>> casesByName()
    {
      return this.cases().toMap(c -> c.name().value(), Function.identity());
    }

    /**
     * Check preconditions for the type.
     */

    @Value.Check
    default void checkPreconditions()
    {
      Preconditions.checkPrecondition(
        this.cases(),
        this.casesByName().size() == this.cases().size(),
        d -> "Variant case names must be unique");
    }
  }

  /**
   * A variant case.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PVariantCaseType<T> extends PModelElementType<T>
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
     * @return The case name
     */

    @Value.Parameter
    PTermConstructorNameType<T> name();

    /**
     * @return The case parameter, if any
     */

    @Value.Parameter
    Optional<PTypeExpressionType<T>> parameter();
  }
}
