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

import com.io7m.polaris.ast.PExprApplication;
import com.io7m.polaris.ast.PExprConstantInteger;
import com.io7m.polaris.ast.PExprLambda;
import com.io7m.polaris.ast.PExprRecord;
import com.io7m.polaris.ast.PExprRecordUpdate;
import com.io7m.polaris.ast.PExprReference;
import com.io7m.polaris.ast.PExpressionOrDeclarationType;
import com.io7m.polaris.ast.PTermReferenceVariable;
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

public interface PParserContractExpressionRecordType
  extends PParserContractBaseType
{
  @Test
  default void testRecord0()
    throws Exception
  {
    final PParserType p = this.parserForString("(record t [field x 23] [field y 24])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isValid());

    final PExprRecord<PParsed> e = (PExprRecord<PParsed>) r.get().get();
    Assertions.assertEquals(
      "x",
      e.fields().get(0).field().value());
    Assertions.assertEquals(
      "23",
      ((PExprConstantInteger<PParsed>) e.fields().get(0).expression()).value().toString());
    Assertions.assertEquals(
      "y",
      e.fields().get(1).field().value());
    Assertions.assertEquals(
      "24",
      ((PExprConstantInteger<PParsed>) e.fields().get(1).expression()).value().toString());
  }

  @Test
  default void testRecordInvalid_0()
    throws Exception
  {
    final PParserType p = this.parserForString("(record)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_RECORD_EXPRESSION));
  }

  @Test
  default void testRecordInvalid_1()
    throws Exception
  {
    final PParserType p = this.parserForString("(record 23)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_RECORD_EXPRESSION));
  }

  @Test
  default void testRecordInvalid_2()
    throws Exception
  {
    final PParserType p = this.parserForString("(record t [])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_RECORD_EXPRESSION_FIELD));
  }

  @Test
  default void testRecordInvalid_3()
    throws Exception
  {
    final PParserType p = this.parserForString("(record t [field x 23] [field x 23])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_RECORD_EXPRESSION_DUPLICATE_FIELD));
  }

  @Test
  default void testRecordUpdate0()
    throws Exception
  {
    final PParserType p = this.parserForString("(record-update t [field x 23] [field y 24])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isValid());

    final PExprRecordUpdate<PParsed> e = (PExprRecordUpdate<PParsed>) r.get().get();
    Assertions.assertEquals(
      "x",
      e.fields().get(0).field().value());
    Assertions.assertEquals(
      "23",
      ((PExprConstantInteger<PParsed>) e.fields().get(0).expression()).value().toString());
    Assertions.assertEquals(
      "y",
      e.fields().get(1).field().value());
    Assertions.assertEquals(
      "24",
      ((PExprConstantInteger<PParsed>) e.fields().get(1).expression()).value().toString());
  }

  @Test
  default void testRecordUpdateInvalid_0()
    throws Exception
  {
    final PParserType p = this.parserForString("(record-update)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_RECORD_UPDATE_EXPRESSION));
  }

  @Test
  default void testRecordUpdateInvalid_1()
    throws Exception
  {
    final PParserType p = this.parserForString("(record-update 23)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_RECORD_UPDATE_EXPRESSION));
  }

  @Test
  default void testRecordUpdateInvalid_2()
    throws Exception
  {
    final PParserType p = this.parserForString("(record-update t [])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_RECORD_EXPRESSION_FIELD));
  }

  @Test
  default void testRecordUpdateInvalid_3()
    throws Exception
  {
    final PParserType p = this.parserForString("(record-update t [field x 23] [field x 23])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_RECORD_EXPRESSION_DUPLICATE_FIELD));
  }
}
