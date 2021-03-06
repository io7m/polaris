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
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.core.PImmutableStyleType;
import io.vavr.collection.Vector;
import org.immutables.value.Value;

import java.net.URI;
import java.util.Optional;

/**
 * The type of unit-level declarations.
 *
 * @param <T> The type of associated data
 */

public interface PUnitDeclarationType<T> extends PDeclarationType<T>
{
  @Override
  default AnyDeclarationKind anyDeclarationKind()
  {
    return AnyDeclarationKind.UNIT_DECLARATION;
  }

  /**
   * @return The kind of declaration
   */

  UnitDeclarationKind unitDeclarationKind();

  /**
   * The kind of declaration
   */

  enum UnitDeclarationKind
  {
    /**
     * @see PDeclarationUnitType
     */

    UNIT_DECLARATION,

    /**
     * @see PDeclarationImportType
     */

    IMPORT_DECLARATION,

    /**
     * @see PDeclarationExportTermsType
     */

    EXPORT_TERMS_DECLARATION,

    /**
     * @see PDeclarationExportTypesType
     */

    EXPORT_TYPES_DECLARATION,
  }

  /**
   * A unit declaration.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PDeclarationUnitType<T> extends PUnitDeclarationType<T>
  {
    @Override
    default UnitDeclarationKind unitDeclarationKind()
    {
      return UnitDeclarationKind.UNIT_DECLARATION;
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
     * @return The package within which the unit is defined
     */

    @Value.Parameter
    PPackageNameType<T> packageName();

    /**
     * @return The name of the unit
     */

    @Value.Parameter
    PUnitNameType<T> unit();
  }

  /**
   * An import declaration.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PDeclarationImportType<T> extends PUnitDeclarationType<T>
  {
    @Override
    default UnitDeclarationKind unitDeclarationKind()
    {
      return UnitDeclarationKind.IMPORT_DECLARATION;
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
     * @return The package within which the unit is defined
     */

    @Value.Parameter
    PPackageNameType<T> packageName();

    /**
     * @return The name of the unit
     */

    @Value.Parameter
    PUnitNameType<T> unit();

    /**
     * @return The qualifying unit name
     */

    @Value.Parameter
    Optional<PUnitNameType<T>> unitQualifier();
  }

  /**
   * An export-terms declaration.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PDeclarationExportTermsType<T> extends PUnitDeclarationType<T>
  {
    @Override
    default UnitDeclarationKind unitDeclarationKind()
    {
      return UnitDeclarationKind.EXPORT_TERMS_DECLARATION;
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
     * @return The terms to be exported
     */

    @Value.Parameter
    Vector<PTermNameType<T>> terms();

    /**
     * Check preconditions for the type.
     */

    @Value.Check
    default void checkPreconditions()
    {
      Preconditions.checkPrecondition(
        this.terms(),
        this.terms().size() == this.terms().toSet().size(),
        t -> "Exported term names must be unique");
    }
  }

  /**
   * An export-types declaration.
   *
   * @param <T> The type of associated data
   */

  @PImmutableStyleType
  @Value.Immutable
  interface PDeclarationExportTypesType<T> extends PUnitDeclarationType<T>
  {
    @Override
    default UnitDeclarationKind unitDeclarationKind()
    {
      return UnitDeclarationKind.EXPORT_TYPES_DECLARATION;
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
     * @return The type names to be exported
     */

    @Value.Parameter
    Vector<PTypeConstructorNameType<T>> types();

    /**
     * Check preconditions for the type.
     */

    @Value.Check
    default void checkPreconditions()
    {
      Preconditions.checkPrecondition(
        this.types(),
        this.types().size() == this.types().toSet().size(),
        t -> "Exported type names must be unique");
    }
  }
}
