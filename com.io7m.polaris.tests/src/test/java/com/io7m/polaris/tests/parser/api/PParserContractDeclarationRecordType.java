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

import com.io7m.polaris.model.PDeclarationRecord;
import com.io7m.polaris.model.PExpressionOrDeclarationType;
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

public interface PParserContractDeclarationRecordType
  extends PParserContractBaseType
{
  @Test
  default void testDeclarationRecord0()
    throws Exception
  {
    final PParserType p = this.parserForString("(record t [field a integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PDeclarationRecord<PParsed> e = (PDeclarationRecord<PParsed>) r.get().get();
    Assertions.assertEquals(0, e.parameters().size());
    Assertions.assertEquals(1, e.fields().size());
    Assertions.assertTrue(e.fieldsByName().containsKey("a"));
    Assertions.assertEquals("t", e.name().value());
  }

  @Test
  default void testDeclarationRecord1()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(record t [for-all a] [field a integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PDeclarationRecord<PParsed> e = (PDeclarationRecord<PParsed>) r.get().get();
    Assertions.assertEquals("t", e.name().value());
    Assertions.assertEquals(1, e.parameters().size());
    Assertions.assertEquals("a", e.parameters().get(0).value());
    Assertions.assertEquals(1, e.fields().size());
    Assertions.assertTrue(e.fieldsByName().containsKey("a"));
    Assertions.assertEquals("t", e.name().value());
  }

  @Test
  default void testDeclarationRecord2()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(record t [∀ a] [field a integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PDeclarationRecord<PParsed> e = (PDeclarationRecord<PParsed>) r.get().get();
    Assertions.assertEquals("t", e.name().value());
    Assertions.assertEquals(1, e.parameters().size());
    Assertions.assertEquals("a", e.parameters().get(0).value());
    Assertions.assertEquals(1, e.fields().size());
    Assertions.assertTrue(e.fieldsByName().containsKey("a"));
    Assertions.assertEquals("t", e.name().value());
  }

  @Test
  default void testDeclarationRecord3()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(record t [field a integer] [field b integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PDeclarationRecord<PParsed> e = (PDeclarationRecord<PParsed>) r.get().get();
    Assertions.assertEquals("t", e.name().value());
    Assertions.assertEquals(0, e.parameters().size());
    Assertions.assertEquals(2, e.fields().size());
    Assertions.assertTrue(e.fieldsByName().containsKey("a"));
    Assertions.assertTrue(e.fieldsByName().containsKey("b"));
    Assertions.assertEquals("t", e.name().value());
  }

  @Test
  default void testDeclarationRecordInvalid0()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(record t [field a integer] [field a integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_RECORD_DUPLICATE_FIELD));
  }

  @Test
  default void testDeclarationRecordInvalid1()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(record t [field a T:x.a])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_NAME));
  }

  @Test
  default void testDeclarationRecordInvalid2()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(record t)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_RECORD));
  }

  @Test
  default void testDeclarationRecordInvalid3()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(record t [for a b] [field a integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.EXPECTED_KEYWORD));
  }

  @Test
  default void testDeclarationRecordInvalid4()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(record [] [field a integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_NAME));
  }

  @Test
  default void testDeclarationRecordInvalid5()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(record t [field a integer] [bad])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_RECORD_FIELD));
  }

  @Test
  default void testDeclarationRecordInvalid6()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(record t [] [field a integer])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_RECORD_FIELD));
  }

  @Test
  default void testDeclarationRecordInvalid7()
    throws Exception
  {
    final PParserType p = this.parserForString(
      "(record t [field a \"a\"])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION));
  }
}
