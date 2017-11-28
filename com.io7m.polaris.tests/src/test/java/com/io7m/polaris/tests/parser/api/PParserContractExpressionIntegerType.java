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

import com.io7m.polaris.model.PExprConstantInteger;
import com.io7m.polaris.model.PExpressionOrDeclarationType;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorCode;
import com.io7m.polaris.parser.api.PParsed;
import com.io7m.polaris.parser.api.PParserType;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.io7m.polaris.tests.PTestUtilities.dump;

public interface PParserContractExpressionIntegerType
  extends PParserContractBaseType
{
  @Test
  default void testIntegers_Radix10()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        "0",
        "0_1_2",
        "100",
        "1000",
        "4294967295")
        .stream()
        .map(s -> (Executable) () -> {
          final PParserType p = this.parserForString(s);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);

          Assertions.assertTrue(r.isValid());

          final PExprConstantInteger<PParsed> e =
            (PExprConstantInteger<PParsed>) r.get().get();
          Assertions.assertEquals(
            new BigInteger(s.replace("_", ""), 10),
            e.value());
          Assertions.assertEquals(10, e.radix());
        });

    Assertions.assertAll(execs);
  }

  @Test
  default void testIntegersInvalid_Radix10()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        "0e",
        "10e0",
        "10f00",
        "20_03_05_z",
        "4294a967295")
        .stream()
        .map(s -> (Executable) () -> {
          final PParserType p = this.parserForString(s);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);
          Assertions.assertTrue(r.isInvalid());
        });

    Assertions.assertAll(execs);
  }

  @Test
  default void testIntegers_Radix16()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        "0x10_20_30",
        "0x0",
        "0x100",
        "0x1000",
        "0xffffffff",
        "0x4294967295")
        .stream()
        .map(s -> (Executable) () -> {
          final PParserType p = this.parserForString(s);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);

          Assertions.assertTrue(r.isValid());

          final PExprConstantInteger<PParsed> e =
            (PExprConstantInteger<PParsed>) r.get().get();
          Assertions.assertEquals(
            new BigInteger(s.replaceFirst("0x", "").replace("_", ""), 16),
            e.value());
          Assertions.assertEquals(16, e.radix());
        });

    Assertions.assertAll(execs);
  }

  @Test
  default void testIntegersInvalid_Radix16()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        "0x",
        "0x10z0",
        "0x10z00",
        "0x4294z967295")
        .stream()
        .map(s -> (Executable) () -> {
          final PParserType p = this.parserForString(s);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);
          Assertions.assertTrue(r.isInvalid());
          Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_INTEGER));
        });

    Assertions.assertAll(execs);
  }

  @Test
  default void testIntegers_Radix8()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        "0o0",
        "0o100",
        "0o1000",
        "0o10_20_30",
        "0o77777777",
        "0o4246725")
        .stream()
        .map(s -> (Executable) () -> {
          final PParserType p = this.parserForString(s);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);

          Assertions.assertTrue(r.isValid());

          final PExprConstantInteger<PParsed> e =
            (PExprConstantInteger<PParsed>) r.get().get();
          Assertions.assertEquals(
            new BigInteger(s.replaceFirst("0o", "").replace("_", ""), 8),
            e.value());
          Assertions.assertEquals(8, e.radix());
        });

    Assertions.assertAll(execs);
  }

  @Test
  default void testIntegersInvalid_Radix8()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        "0o",
        "0o10z0",
        "0o10z00",
        "0o4294z967295")
        .stream()
        .map(s -> (Executable) () -> {
          final PParserType p = this.parserForString(s);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);
          Assertions.assertTrue(r.isInvalid());
          Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_INTEGER));
        });

    Assertions.assertAll(execs);
  }

  @Test
  default void testIntegers_Radix2()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        "0b0",
        "0b100",
        "0b1000",
        "0b10_10_10",
        "0b11111111",
        "0b10101010")
        .stream()
        .map(s -> (Executable) () -> {
          final PParserType p = this.parserForString(s);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);

          Assertions.assertTrue(r.isValid());

          final PExprConstantInteger<PParsed> e =
            (PExprConstantInteger<PParsed>) r.get().get();
          Assertions.assertEquals(
            new BigInteger(s.replaceFirst("0b", "").replace("_", ""), 2),
            e.value());
          Assertions.assertEquals(2, e.radix());
        });

    Assertions.assertAll(execs);
  }

  @Test
  default void testIntegersInvalid_Radix2()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        "0b",
        "0b10z0",
        "0b10z00",
        "0b4294z967295")
        .stream()
        .map(s -> (Executable) () -> {
          final PParserType p = this.parserForString(s);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);
          Assertions.assertTrue(r.isInvalid());
          Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_INTEGER));
        });

    Assertions.assertAll(execs);
  }
}
