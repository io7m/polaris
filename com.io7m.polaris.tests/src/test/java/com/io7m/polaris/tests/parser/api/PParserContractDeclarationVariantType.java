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

import com.io7m.polaris.ast.PDeclarationVariant;
import com.io7m.polaris.ast.PExpressionOrDeclarationType;
import com.io7m.polaris.ast.PTypeDeclarationType;
import com.io7m.polaris.ast.PTypeExprReference;
import com.io7m.polaris.ast.PTypeReferenceConstructor;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorCode;
import com.io7m.polaris.parser.api.PParsed;
import com.io7m.polaris.parser.api.PParserType;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static com.io7m.polaris.tests.PTestUtilities.dump;

public interface PParserContractDeclarationVariantType
  extends PParserContractBaseType
{
  @Test
  default void testDeclarationVariant0()
    throws Exception
  {
    final PParserType p = this.parserForString("(variant T [case A Integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PDeclarationVariant<PParsed> e = (PDeclarationVariant<PParsed>) r.get().get();
    Assertions.assertEquals(0, e.parameters().size());
    Assertions.assertEquals(1, e.cases().size());
    Assertions.assertTrue(e.casesByName().containsKey("A"));
    Assertions.assertEquals("T", e.name().value());

    final PTypeDeclarationType.PVariantCaseType<PParsed> case_a = e.casesByName().get(
      "A").get();
    final PTypeExprReference<PParsed> a_type = (PTypeExprReference<PParsed>) case_a.parameter().get();
    final PTypeReferenceConstructor<PParsed> a_ref = (PTypeReferenceConstructor<PParsed>) a_type.reference();
    Assertions.assertEquals("Integer", a_ref.constructor().value());
    Assertions.assertEquals(Optional.empty(), a_ref.unit());
  }

  @Test
  default void testDeclarationVariant1()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(variant T [for-all a] [case A Integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PDeclarationVariant<PParsed> e = (PDeclarationVariant<PParsed>) r.get().get();
    Assertions.assertEquals("T", e.name().value());
    Assertions.assertEquals(1, e.parameters().size());
    Assertions.assertEquals("a", e.parameters().get(0).value());
    Assertions.assertEquals(1, e.cases().size());
    Assertions.assertTrue(e.casesByName().containsKey("A"));
    Assertions.assertEquals("T", e.name().value());

    final PTypeDeclarationType.PVariantCaseType<PParsed> case_a = e.casesByName().get(
      "A").get();
    final PTypeExprReference<PParsed> a_type = (PTypeExprReference<PParsed>) case_a.parameter().get();
    final PTypeReferenceConstructor<PParsed> a_ref = (PTypeReferenceConstructor<PParsed>) a_type.reference();
    Assertions.assertEquals("Integer", a_ref.constructor().value());
    Assertions.assertEquals(Optional.empty(), a_ref.unit());
  }

  @Test
  default void testDeclarationVariant2()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(variant T [∀ a] [case A Integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PDeclarationVariant<PParsed> e = (PDeclarationVariant<PParsed>) r.get().get();
    Assertions.assertEquals("T", e.name().value());
    Assertions.assertEquals(1, e.parameters().size());
    Assertions.assertEquals("a", e.parameters().get(0).value());
    Assertions.assertEquals(1, e.cases().size());
    Assertions.assertTrue(e.casesByName().containsKey("A"));
    Assertions.assertEquals("T", e.name().value());

    final PTypeDeclarationType.PVariantCaseType<PParsed> case_a = e.casesByName().get(
      "A").get();
    final PTypeExprReference<PParsed> a_type = (PTypeExprReference<PParsed>) case_a.parameter().get();
    final PTypeReferenceConstructor<PParsed> a_ref = (PTypeReferenceConstructor<PParsed>) a_type.reference();
    Assertions.assertEquals("Integer", a_ref.constructor().value());
    Assertions.assertEquals(Optional.empty(), a_ref.unit());
  }

  @Test
  default void testDeclarationVariant3()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(variant T [case A Integer] [case B Integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PDeclarationVariant<PParsed> e = (PDeclarationVariant<PParsed>) r.get().get();
    Assertions.assertEquals("T", e.name().value());
    Assertions.assertEquals(0, e.parameters().size());
    Assertions.assertEquals(2, e.cases().size());
    Assertions.assertTrue(e.casesByName().containsKey("A"));
    Assertions.assertTrue(e.casesByName().containsKey("B"));
    Assertions.assertEquals("T", e.name().value());

    final PTypeDeclarationType.PVariantCaseType<PParsed> case_a = e.casesByName().get(
      "A").get();
    final PTypeExprReference<PParsed> a_type = (PTypeExprReference<PParsed>) case_a.parameter().get();
    final PTypeReferenceConstructor<PParsed> a_ref = (PTypeReferenceConstructor<PParsed>) a_type.reference();
    Assertions.assertEquals("Integer", a_ref.constructor().value());
    Assertions.assertEquals(Optional.empty(), a_ref.unit());

    final PTypeDeclarationType.PVariantCaseType<PParsed> case_b = e.casesByName().get(
      "B").get();
    final PTypeExprReference<PParsed> b_type = (PTypeExprReference<PParsed>) case_b.parameter().get();
    final PTypeReferenceConstructor<PParsed> b_ref = (PTypeReferenceConstructor<PParsed>) b_type.reference();
    Assertions.assertEquals("Integer", b_ref.constructor().value());
    Assertions.assertEquals(Optional.empty(), b_ref.unit());
  }

  @Test
  default void testDeclarationVariant4()
    throws Exception
  {
    final PParserType p = this.parserForString("(variant T [case A])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PDeclarationVariant<PParsed> e = (PDeclarationVariant<PParsed>) r.get().get();
    Assertions.assertEquals(0, e.parameters().size());
    Assertions.assertEquals(1, e.cases().size());
    Assertions.assertTrue(e.casesByName().containsKey("A"));
    Assertions.assertEquals("T", e.name().value());

    final PTypeDeclarationType.PVariantCaseType<PParsed> case_a = e.casesByName().get(
      "A").get();
    Assertions.assertEquals(Optional.empty(), case_a.parameter());
  }

  @Test
  default void testDeclarationVariant5()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(variant T [for-all a] [case A])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PDeclarationVariant<PParsed> e = (PDeclarationVariant<PParsed>) r.get().get();
    Assertions.assertEquals("T", e.name().value());
    Assertions.assertEquals(1, e.parameters().size());
    Assertions.assertEquals("a", e.parameters().get(0).value());
    Assertions.assertEquals(1, e.cases().size());
    Assertions.assertTrue(e.casesByName().containsKey("A"));
    Assertions.assertEquals("T", e.name().value());

    final PTypeDeclarationType.PVariantCaseType<PParsed> case_a = e.casesByName().get(
      "A").get();
    Assertions.assertEquals(Optional.empty(), case_a.parameter());
  }

  @Test
  default void testDeclarationVariant6()
    throws Exception
  {
    final PParserType p = this.parserForString("(variant T [∀ a] [case A])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PDeclarationVariant<PParsed> e = (PDeclarationVariant<PParsed>) r.get().get();
    Assertions.assertEquals("T", e.name().value());
    Assertions.assertEquals(1, e.parameters().size());
    Assertions.assertEquals("a", e.parameters().get(0).value());
    Assertions.assertEquals(1, e.cases().size());
    Assertions.assertTrue(e.casesByName().containsKey("A"));
    Assertions.assertEquals("T", e.name().value());

    final PTypeDeclarationType.PVariantCaseType<PParsed> case_a = e.casesByName().get(
      "A").get();
    Assertions.assertEquals(Optional.empty(), case_a.parameter());
  }

  @Test
  default void testDeclarationVariant7()
    throws Exception
  {
    final PParserType p = this.parserForString("(variant T [case A] [case B])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PDeclarationVariant<PParsed> e = (PDeclarationVariant<PParsed>) r.get().get();
    Assertions.assertEquals("T", e.name().value());
    Assertions.assertEquals(0, e.parameters().size());
    Assertions.assertEquals(2, e.cases().size());
    Assertions.assertTrue(e.casesByName().containsKey("A"));
    Assertions.assertTrue(e.casesByName().containsKey("B"));
    Assertions.assertEquals("T", e.name().value());

    final PTypeDeclarationType.PVariantCaseType<PParsed> case_a = e.casesByName().get(
      "A").get();
    Assertions.assertEquals(Optional.empty(), case_a.parameter());
    final PTypeDeclarationType.PVariantCaseType<PParsed> case_b = e.casesByName().get(
      "B").get();
    Assertions.assertEquals(Optional.empty(), case_b.parameter());
  }

  @Test
  default void testDeclarationVariantInvalid0()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(variant T [case A Integer] [case A Integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_VARIANT_DUPLICATE_CASE));
  }

  @Test
  default void testDeclarationVariantInvalid1()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(variant T [case A T:x.a])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_CONSTRUCTOR_NAME));
  }

  @Test
  default void testDeclarationVariantInvalid2()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(variant t)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_VARIANT));
  }

  @Test
  default void testDeclarationVariantInvalid3()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(variant T [for a b] [case A Integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.EXPECTED_KEYWORD));
  }

  @Test
  default void testDeclarationVariantInvalid4()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(variant [] [case A Integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_CONSTRUCTOR_NAME));
  }

  @Test
  default void testDeclarationVariantInvalid5()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(variant T [case A Integer] [bad])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_VARIANT_CASE));
  }

  @Test
  default void testDeclarationVariantInvalid6()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(variant T [] [case A Integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_VARIANT_CASE));
  }

  @Test
  default void testDeclarationVariantInvalid7()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(variant T [case A \"a\"])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION));
  }
}
