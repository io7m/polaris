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

package com.io7m.polaris.parser.implementation;

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionQuotedStringType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.ast.PDeclarationExportTerms;
import com.io7m.polaris.ast.PDeclarationExportTypes;
import com.io7m.polaris.ast.PDeclarationImport;
import com.io7m.polaris.ast.PDeclarationUnit;
import com.io7m.polaris.ast.PPackageName;
import com.io7m.polaris.ast.PTermName;
import com.io7m.polaris.ast.PTypeConstructorName;
import com.io7m.polaris.ast.PUnitName;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_UNIT;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_UNIT_EXPORT_TERMS;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_UNIT_EXPORT_TERMS_DUPLICATE_NAME;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_UNIT_EXPORT_TYPES;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_UNIT_EXPORT_TYPES_DUPLICATE_NAME;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_UNIT_IMPORT;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_UNIT_IMPORT_QUALIFIED;
import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;

/**
 * Functions to parse unit declarations.
 */

public final class PParsingUnits
{
  private PParsingUnits()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as a unit declaration.
   *
   * @param m An error message provider
   * @param e The expression
   *
   * @return A unit declaration, or a sequence of parse errors
   */

  public static Validation<Seq<PParseError>, PDeclarationUnit<PParsed>>
  parseUnit(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    final SExpressionType e_keyword = e.get(0);

    Preconditions.checkPrecondition(
      e_keyword,
      e_keyword instanceof SExpressionSymbolType,
      c -> "Unit declaration must begin with define-unit keyword");

    if (e.size() == 2) {
      final SExpressionType e_name = e.get(1);
      if (e_name instanceof SExpressionSymbolType) {
        final SExpressionSymbolType e_q = (SExpressionSymbolType) e_name;
        return PParsingNames.parseUnitNameFullyQualifiedNameRaw(
          m, e_q.lexical(), e_q.text())
          .map(p -> PDeclarationUnit.of(e_q.lexical(), parsed(), p._1, p._2));
      }
    }

    return invalid(m.errorExpression(INVALID_UNIT, e));
  }

  /**
   * Parse the given expression as a unit import declaration.
   *
   * @param m An error message provider
   * @param e The expression
   *
   * @return A unit import declaration, or a sequence of parse errors
   */

  public static Validation<Seq<PParseError>, PDeclarationImport<PParsed>>
  parseImport(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    final SExpressionType e_keyword = e.get(0);

    Preconditions.checkPrecondition(
      e_keyword,
      e_keyword instanceof SExpressionSymbolType,
      c -> "Unit declaration must begin with import keyword");

    if (e.size() == 2) {
      final SExpressionType e_name = e.get(1);
      if (e_name instanceof SExpressionSymbolType) {
        final SExpressionSymbolType e_q = (SExpressionSymbolType) e_name;
        final Validation<Seq<PParseError>, Tuple2<PPackageName<PParsed>, PUnitName<PParsed>>> r_result =
          PParsingNames.parseUnitNameFullyQualifiedNameRaw(
            m, e_q.lexical(), e_q.text());
        return r_result.map(p -> PDeclarationImport.of(
          e_q.lexical(), parsed(), p._1, p._2, Optional.empty()));
      }
    }

    return invalid(m.errorExpression(INVALID_UNIT_IMPORT, e));
  }

  /**
   * Parse the given expression as a unit import declaration.
   *
   * @param m An error message provider
   * @param e The expression
   *
   * @return A unit import declaration, or a sequence of parse errors
   */

  public static Validation<Seq<PParseError>, PDeclarationImport<PParsed>>
  parseImportQualified(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    final SExpressionType e_keyword = e.get(0);

    Preconditions.checkPrecondition(
      e_keyword,
      e_keyword instanceof SExpressionSymbolType,
      c -> "Unit declaration must begin with import-qualified keyword");

    if (e.size() == 3) {
      final SExpressionType e_name = e.get(1);
      final SExpressionType e_qual = e.get(2);

      if (e_name instanceof SExpressionSymbolType) {
        final SExpressionSymbolType e_q = (SExpressionSymbolType) e_name;
        final Validation<Seq<PParseError>, Tuple2<PPackageName<PParsed>, PUnitName<PParsed>>> r_unit =
          PParsingNames.parseUnitNameFullyQualifiedNameRaw(
            m, e_q.lexical(), e_q.text());
        final Validation<Seq<PParseError>, PUnitName<PParsed>> r_qual =
          PParsingNames.parseUnitName(m, e_qual);
        final Validation<Seq<Seq<PParseError>>, PDeclarationImport<PParsed>> r_result =
          Validation.combine(r_unit, r_qual)
            .ap((t_unit, t_qual) ->
                  PDeclarationImport.of(
                    e.lexical(),
                    parsed(),
                    t_unit._1,
                    t_unit._2,
                    Optional.of(t_qual)));
        return PValidation.errorsFlatten(r_result);
      }
    }

    return invalid(m.errorExpression(INVALID_UNIT_IMPORT_QUALIFIED, e));
  }

  /**
   * Parse the given expression as a unit export terms declaration.
   *
   * @param m An error message provider
   * @param e The expression
   *
   * @return A unit export terms declaration, or a sequence of parse errors
   */

  public static Validation<Seq<PParseError>, PDeclarationExportTerms<PParsed>>
  parseExportTerms(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    final SExpressionType e_keyword = e.get(0);

    Preconditions.checkPrecondition(
      e_keyword,
      e_keyword instanceof SExpressionSymbolType,
      c -> "Unit declaration must begin with export-terms keyword");

    if (e.size() >= 2) {
      final Vector<SExpressionType> e_terms = Vector.ofAll(e).tail();
      final Validation<Seq<PParseError>, Vector<PTermName<PParsed>>> r_seq =
        PValidation.sequence(e_terms, t -> PParsingNames.parseTermName(m, t));
      final Validation<Seq<PParseError>, Vector<PTermName<PParsed>>> r_unique =
        r_seq.flatMap(xs -> requireUniqueTermNames(m, xs));
      return r_unique.map(
        names -> PDeclarationExportTerms.of(
          e.lexical(), parsed(), PVectors.vectorCast(names)));
    }

    return invalid(m.errorExpression(INVALID_UNIT_EXPORT_TERMS, e));
  }

  /**
   * Parse the given expression as a unit export types declaration.
   *
   * @param m An error message provider
   * @param e The expression
   *
   * @return A unit export types declaration, or a sequence of parse errors
   */

  public static Validation<Seq<PParseError>, PDeclarationExportTypes<PParsed>>
  parseExportTypes(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    final SExpressionType e_keyword = e.get(0);

    Preconditions.checkPrecondition(
      e_keyword,
      e_keyword instanceof SExpressionSymbolType,
      c -> "Unit declaration must begin with export-types keyword");

    if (e.size() >= 2) {
      final Vector<SExpressionType> e_types = Vector.ofAll(e).tail();
      final Validation<Seq<PParseError>, Vector<PTypeConstructorName<PParsed>>> r_seq =
        PValidation.sequence(
          e_types, t -> PParsingNames.parseTypeConstructorName(m, t));
      final Validation<Seq<PParseError>, Vector<PTypeConstructorName<PParsed>>> r_unique =
        r_seq.flatMap(xs -> requireUniqueTypeNames(m, xs));
      return r_unique.map(
        names -> PDeclarationExportTypes.of(
          e.lexical(), parsed(), PVectors.vectorCast(names)));
    }

    return invalid(m.errorExpression(INVALID_UNIT_EXPORT_TYPES, e));
  }

  private static Validation<Seq<PParseError>, Vector<PTermName<PParsed>>>
  requireUniqueTermNames(
    final PParseErrorMessagesType messages,
    final Vector<PTermName<PParsed>> terms)
  {
    return PParsingNames.requireUniqueNames(
      terms,
      Function.identity(),
      dups -> dups.map(dup -> messages.errorLexical(
        INVALID_UNIT_EXPORT_TERMS_DUPLICATE_NAME, dup.lexical(), dup.value())));
  }

  private static Validation<Seq<PParseError>, Vector<PTypeConstructorName<PParsed>>>
  requireUniqueTypeNames(
    final PParseErrorMessagesType messages,
    final Vector<PTypeConstructorName<PParsed>> terms)
  {
    return PParsingNames.requireUniqueNames(
      terms,
      Function.identity(),
      dups -> dups.map(dup -> messages.errorLexical(
        INVALID_UNIT_EXPORT_TYPES_DUPLICATE_NAME, dup.lexical(), dup.value())));
  }
}
