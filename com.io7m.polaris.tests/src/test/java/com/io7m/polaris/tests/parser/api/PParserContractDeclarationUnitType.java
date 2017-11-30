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

package com.io7m.polaris.tests.parser.api;

import com.io7m.polaris.ast.PDeclarationExportTerms;
import com.io7m.polaris.ast.PDeclarationExportTypes;
import com.io7m.polaris.ast.PDeclarationImport;
import com.io7m.polaris.ast.PDeclarationUnit;
import com.io7m.polaris.ast.PExpressionOrDeclarationType;
import com.io7m.polaris.ast.PPackageName;
import com.io7m.polaris.ast.PTermConstructorName;
import com.io7m.polaris.ast.PTermName;
import com.io7m.polaris.ast.PTypeConstructorName;
import com.io7m.polaris.ast.PUnitName;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorCode;
import com.io7m.polaris.parser.api.PParsed;
import com.io7m.polaris.parser.api.PParserType;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.tests.PTestUtilities.dump;

public interface PParserContractDeclarationUnitType
  extends PParserContractBaseType
{
  @Test
  default void testDeclarationUnit0()
    throws Exception
  {
    final PParserType p = this.parserForString("(define-unit a.b.c.D)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isValid());

    final PDeclarationUnit<PParsed> e = (PDeclarationUnit<PParsed>) r.get().get();
    Assertions.assertEquals(
      PPackageName.of(e.lexical(), parsed(), "a.b.c"),
      e.packageName());
    Assertions.assertEquals(
      PUnitName.of(e.lexical(), parsed(), "D"),
      e.unit());
  }

  @Test
  default void testDeclarationUnitInvalid0()
    throws Exception
  {
    final PParserType p = this.parserForString("(define-unit)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_UNIT));
  }

  @Test
  default void testDeclarationUnitInvalid1()
    throws Exception
  {
    final PParserType p = this.parserForString("(define-unit x)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_PACKAGE_NAME));
  }

  @Test
  default void testDeclarationUnitInvalid2()
    throws Exception
  {
    final PParserType p = this.parserForString("(define-unit $.$)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_PACKAGE_NAME));
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_UNIT_NAME));
  }

  @Test
  default void testDeclarationUnitImport0()
    throws Exception
  {
    final PParserType p = this.parserForString("(import a.b.c.D)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isValid());

    final PDeclarationImport<PParsed> e = (PDeclarationImport<PParsed>) r.get().get();
    Assertions.assertEquals(
      PPackageName.of(e.lexical(), parsed(), "a.b.c"),
      e.packageName());
    Assertions.assertEquals(
      PUnitName.of(e.lexical(), parsed(), "D"),
      e.unit());
    Assertions.assertEquals(
      Optional.empty(),
      e.unitQualifier());
  }

  @Test
  default void testDeclarationUnitImportInvalid1()
    throws Exception
  {
    final PParserType p = this.parserForString("(import x)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_PACKAGE_NAME));
  }

  @Test
  default void testDeclarationUnitImportInvalid2()
    throws Exception
  {
    final PParserType p = this.parserForString("(import $.$)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_PACKAGE_NAME));
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_UNIT_NAME));
  }

  @Test
  default void testDeclarationUnitImportInvalid3()
    throws Exception
  {
    final PParserType p = this.parserForString("(import)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_UNIT_IMPORT));
  }

  @Test
  default void testDeclarationUnitImportQualified0()
    throws Exception
  {
    final PParserType p = this.parserForString("(import-qualified a.b.c.D A)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isValid());

    final PDeclarationImport<PParsed> e = (PDeclarationImport<PParsed>) r.get().get();
    Assertions.assertEquals(
      PPackageName.of(e.lexical(), parsed(), "a.b.c"),
      e.packageName());
    Assertions.assertEquals(
      PUnitName.of(e.lexical(), parsed(), "D"),
      e.unit());
    Assertions.assertEquals(
      Optional.of(PUnitName.of(e.lexical(), parsed(), "A")),
      e.unitQualifier());
  }

  @Test
  default void testDeclarationUnitImportQualifiedInvalid1()
    throws Exception
  {
    final PParserType p = this.parserForString("(import-qualified x u)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_PACKAGE_NAME));
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_UNIT_NAME));
  }

  @Test
  default void testDeclarationUnitImportQualifiedInvalid2()
    throws Exception
  {
    final PParserType p = this.parserForString("(import-qualified $.$ A)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_PACKAGE_NAME));
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_UNIT_NAME));
  }

  @Test
  default void testDeclarationUnitImportQualifiedInvalid3()
    throws Exception
  {
    final PParserType p = this.parserForString("(import-qualified)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_UNIT_IMPORT_QUALIFIED));
  }

  @Test
  default void testDeclarationUnitExportTerms0()
    throws Exception
  {
    final PParserType p = this.parserForString("(export-terms a b c)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isValid());

    final PDeclarationExportTerms<PParsed> e = (PDeclarationExportTerms<PParsed>) r.get().get();
    Assertions.assertEquals(
      PTermName.of(e.lexical(), parsed(), "a"),
      e.terms().get(0));
    Assertions.assertEquals(
      PTermName.of(e.lexical(), parsed(), "b"),
      e.terms().get(1));
    Assertions.assertEquals(
      PTermName.of(e.lexical(), parsed(), "c"),
      e.terms().get(2));
  }

  @Test
  default void testDeclarationUnitExportTermsInvalid0()
    throws Exception
  {
    final PParserType p = this.parserForString("(export-terms a a)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_UNIT_EXPORT_TERMS_DUPLICATE_NAME));
  }

  @Test
  default void testDeclarationUnitExportTermsInvalid1()
    throws Exception
  {
    final PParserType p = this.parserForString("(export-terms)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_UNIT_EXPORT_TERMS));
  }

  @Test
  default void testDeclarationUnitExportTypes0()
    throws Exception
  {
    final PParserType p = this.parserForString("(export-types A B C)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isValid());

    final PDeclarationExportTypes<PParsed> e = (PDeclarationExportTypes<PParsed>) r.get().get();
    Assertions.assertEquals(
      PTypeConstructorName.of(e.lexical(), parsed(), "A"),
      e.types().get(0));
    Assertions.assertEquals(
      PTypeConstructorName.of(e.lexical(), parsed(), "B"),
      e.types().get(1));
    Assertions.assertEquals(
      PTypeConstructorName.of(e.lexical(), parsed(), "C"),
      e.types().get(2));
  }

  @Test
  default void testDeclarationUnitExportTypesInvalid0()
    throws Exception
  {
    final PParserType p = this.parserForString("(export-types A A)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_UNIT_EXPORT_TYPES_DUPLICATE_NAME));
  }

  @Test
  default void testDeclarationUnitExportTypesInvalid1()
    throws Exception
  {
    final PParserType p = this.parserForString("(export-types)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_UNIT_EXPORT_TYPES));
  }
}
