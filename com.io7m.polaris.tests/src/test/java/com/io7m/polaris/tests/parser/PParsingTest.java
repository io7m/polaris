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

package com.io7m.polaris.tests.parser;

import com.io7m.jsx.SExpressionType;
import com.io7m.jsx.api.parser.JSXParserException;
import com.io7m.jsx.lexer.JSXLexerSupplier;
import com.io7m.jsx.parser.JSXParserSupplier;
import com.io7m.polaris.model.PExprApplication;
import com.io7m.polaris.model.PExprConstantInteger;
import com.io7m.polaris.model.PExprConstantReal;
import com.io7m.polaris.model.PExprConstantString;
import com.io7m.polaris.model.PExprLambda;
import com.io7m.polaris.model.PExprLocal;
import com.io7m.polaris.model.PExprMatch;
import com.io7m.polaris.model.PExprReference;
import com.io7m.polaris.model.PExpressionOrDeclarationType;
import com.io7m.polaris.model.PExpressionType;
import com.io7m.polaris.model.PPatternConstantInteger;
import com.io7m.polaris.model.PPatternConstantReal;
import com.io7m.polaris.model.PPatternConstantString;
import com.io7m.polaris.model.PPatternConstructor;
import com.io7m.polaris.model.PPatternType;
import com.io7m.polaris.model.PPatternWildcard;
import com.io7m.polaris.parser.PParsers;
import com.io7m.polaris.parser.PParsing;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.Tuple;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class PParsingTest
{
  private static final Logger LOG = LoggerFactory.getLogger(PParsingTest.class);

  private static <T> void dump(
    final Validation<Seq<PParseError>, T> r)
  {
    if (r.isValid()) {
      LOG.debug("valid: {}", r.get());
    } else {
      r.getError().forEach(e -> LOG.error("error: {}", e));
    }
  }

  @Test
  public void testLocal0()
    throws Exception
  {
    final SExpressionType se = this.parse("(local x)");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isValid());

    final PExprLocal<PParsed> e = (PExprLocal<PParsed>) r.get();
    final PExprReference<PParsed> x = (PExprReference<PParsed>) e.body();
    Assertions.assertEquals("x", x.name().value());
    Assertions.assertEquals(0, e.locals().size());
  }

  @Test
  public void testLocal1()
    throws Exception
  {
    final SExpressionType se = this.parse("(local [y] [z] x)");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isValid());

    final PExprLocal<PParsed> e = (PExprLocal<PParsed>) r.get();
    final PExprReference<PParsed> x = (PExprReference<PParsed>) e.body();
    Assertions.assertEquals("x", x.name().value());
    Assertions.assertEquals(2, e.locals().size());

    final PExprApplication<PParsed> y = (PExprApplication<PParsed>) e.locals().get(0);
    Assertions.assertEquals("y", ((PExprReference<PParsed>) y.function()).name().value());
    final PExprApplication<PParsed> z = (PExprApplication<PParsed>) e.locals().get(1);
    Assertions.assertEquals("z", ((PExprReference<PParsed>) z.function()).name().value());
  }

  @Test
  public void testLocalInvalid_0()
    throws Exception
  {
    final SExpressionType se = this.parse("(local)");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testMatch0()
    throws Exception
  {
    final SExpressionType se = this.parse("(match x [case 23 24])");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isValid());

    final PExprMatch<PParsed> e = (PExprMatch<PParsed>) r.get();
    final PExprReference<PParsed> x = (PExprReference<PParsed>) e.target();
    Assertions.assertEquals("x", x.name().value());

    Assertions.assertEquals(1, e.cases().size());

    final PExpressionType.PMatchCaseType<PParsed> c0 = e.cases().get(0);
    final PPatternConstantInteger<PParsed> cp0 =
      (PPatternConstantInteger<PParsed>) c0.pattern();
    final PExprConstantInteger<PParsed> ce0 =
      (PExprConstantInteger<PParsed>) c0.expression();

    Assertions.assertEquals(new BigInteger("23"), cp0.value());
    Assertions.assertEquals(new BigInteger("24"), ce0.value());
  }

  @Test
  public void testMatch1()
    throws Exception
  {
    final SExpressionType se = this.parse("(match A:x [case 23 24])");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isValid());

    final PExprMatch<PParsed> e = (PExprMatch<PParsed>) r.get();
    final PExprReference<PParsed> x = (PExprReference<PParsed>) e.target();
    Assertions.assertEquals("A", x.unit().get().value());
    Assertions.assertEquals("x", x.name().value());

    Assertions.assertEquals(1, e.cases().size());

    final PExpressionType.PMatchCaseType<PParsed> c0 = e.cases().get(0);
    final PPatternConstantInteger<PParsed> cp0 =
      (PPatternConstantInteger<PParsed>) c0.pattern();
    final PExprConstantInteger<PParsed> ce0 =
      (PExprConstantInteger<PParsed>) c0.expression();

    Assertions.assertEquals(new BigInteger("23"), cp0.value());
    Assertions.assertEquals(new BigInteger("24"), ce0.value());
  }

  @Test
  public void testMatchInvalid0()
    throws Exception
  {
    final SExpressionType se = this.parse("(match)");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testMatchInvalid1()
    throws Exception
  {
    final SExpressionType se = this.parse("(match 23 [case x])");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testMatchInvalid2()
    throws Exception
  {
    final SExpressionType se = this.parse("(match 23 [() y x])");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testMatchInvalid3()
    throws Exception
  {
    final SExpressionType se = this.parse("(match 23 [case (Null x y) x])");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testLambda0()
    throws Exception
  {
    final SExpressionType se = this.parse("(lambda (x y) (+ x y))");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isValid());

    final PExprLambda<PParsed> e = (PExprLambda<PParsed>) r.get();
    Assertions.assertEquals(
      "x",
      e.arguments().get(0).value());
    Assertions.assertEquals(
      "y",
      e.arguments().get(1).value());

    final PExprApplication<PParsed> app =
      (PExprApplication<PParsed>) e.expression();

    Assertions.assertEquals(
      "+",
      ((PExprReference<PParsed>) app.function()).name().value());
    Assertions.assertEquals(
      "x",
      ((PExprReference<PParsed>) app.arguments().get(0)).name().value());
    Assertions.assertEquals(
      "y",
      ((PExprReference<PParsed>) app.arguments().get(1)).name().value());
  }

  @Test
  public void testLambda1()
    throws Exception
  {
    final SExpressionType se = this.parse("(λ (x y) (+ x y))");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isValid());

    final PExprLambda<PParsed> e = (PExprLambda<PParsed>) r.get();
    Assertions.assertEquals(
      "x",
      e.arguments().get(0).value());
    Assertions.assertEquals(
      "y",
      e.arguments().get(1).value());

    final PExprApplication<PParsed> app =
      (PExprApplication<PParsed>) e.expression();

    Assertions.assertEquals(
      "+",
      ((PExprReference<PParsed>) app.function()).name().value());
    Assertions.assertEquals(
      "x",
      ((PExprReference<PParsed>) app.arguments().get(0)).name().value());
    Assertions.assertEquals(
      "y",
      ((PExprReference<PParsed>) app.arguments().get(1)).name().value());
  }

  @Test
  public void testLambdaInvalid_0()
    throws Exception
  {
    final SExpressionType se = this.parse("(λ (x y) ())");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testLambdaInvalid_1()
    throws Exception
  {
    final SExpressionType se = this.parse("(λ (.) (+ x y))");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testLambdaInvalid_2()
    throws Exception
  {
    final SExpressionType se = this.parse("(λ ((+ 23)) (+ x y))");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testLambdaInvalid_3()
    throws Exception
  {
    final SExpressionType se = this.parse("(λ (x))");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testLambdaInvalid_4()
    throws Exception
  {
    final SExpressionType se = this.parse("(λ)");
    final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
      PParsing.parseExpressionOrDeclaration(se);

    dump(r);

    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testStrings()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        "\"abc\"")
        .stream()
        .map(s -> (Executable) () -> {
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);

          Assertions.assertTrue(r.isValid());

          final PExprConstantString<PParsed> e =
            (PExprConstantString<PParsed>) r.get();
          Assertions.assertEquals(s.replace("\"", ""), e.value());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testTermReferencesUnitless()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        Tuple.of(
          "a",
          "a"))
        .stream()
        .map(s -> (Executable) () -> {
          final SExpressionType se = this.parse(s._2);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);

          Assertions.assertTrue(r.isValid());

          final PExprReference<PParsed> e = (PExprReference<PParsed>) r.get();
          Assertions.assertEquals(Optional.empty(), e.unit());
          Assertions.assertEquals(s._1, e.name().value());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testTermReferencesUnitlessInvalid()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        ".",
        "/",
        "<",
        ">",
        ".",
        ":")
        .stream()
        .map(s -> (Executable) () -> {
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);

          Assertions.assertTrue(r.isInvalid());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testTermReferences()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        Tuple.of(
          "A",
          "b",
          "A:b"),
        Tuple.of(
          "A2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678",
          "a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678",
          "A2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678:a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678"))
        .stream()
        .map(s -> (Executable) () -> {
          final SExpressionType se = this.parse(s._3);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);

          Assertions.assertTrue(r.isValid());

          final PExprReference<PParsed> e = (PExprReference<PParsed>) r.get();
          Assertions.assertEquals(s._1, e.unit().get().value());
          Assertions.assertEquals(s._2, e.name().value());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testTermReferencesInvalid()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        "_:A",
        "A:a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678z",
        "A2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678z:a",
        "A2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678z:a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678a2345678z")
        .stream()
        .map(s -> (Executable) () -> {
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);
          Assertions.assertTrue(r.isInvalid());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testReals()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        "0.0",
        "100.0",
        "1000.0",
        "4294967295.0")
        .stream()
        .map(s -> (Executable) () -> {
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);

          Assertions.assertTrue(r.isValid());

          final PExprConstantReal<PParsed> e =
            (PExprConstantReal<PParsed>) r.get();
          Assertions.assertEquals(new BigDecimal(s), e.value());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testRealsInvalid()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        "0.k",
        "100.z",
        "10z0.0",
        "42949a7295.0")
        .stream()
        .map(s -> (Executable) () -> {
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);

          Assertions.assertTrue(r.isInvalid());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testIntegers_Radix10()
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
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);

          Assertions.assertTrue(r.isValid());

          final PExprConstantInteger<PParsed> e =
            (PExprConstantInteger<PParsed>) r.get();
          Assertions.assertEquals(
            new BigInteger(s.replace("_", ""), 10),
            e.value());
          Assertions.assertEquals(10, e.radix());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testIntegersInvalid_Radix10()
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
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);
          Assertions.assertTrue(r.isInvalid());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testIntegers_Radix16()
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
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);

          Assertions.assertTrue(r.isValid());

          final PExprConstantInteger<PParsed> e =
            (PExprConstantInteger<PParsed>) r.get();
          Assertions.assertEquals(
            new BigInteger(s.replaceFirst("0x", "").replace("_", ""), 16),
            e.value());
          Assertions.assertEquals(16, e.radix());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testIntegersInvalid_Radix16()
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
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);
          Assertions.assertTrue(r.isInvalid());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testIntegers_Radix8()
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
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);

          Assertions.assertTrue(r.isValid());

          final PExprConstantInteger<PParsed> e =
            (PExprConstantInteger<PParsed>) r.get();
          Assertions.assertEquals(
            new BigInteger(s.replaceFirst("0o", "").replace("_", ""), 8),
            e.value());
          Assertions.assertEquals(8, e.radix());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testIntegersInvalid_Radix8()
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
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);
          Assertions.assertTrue(r.isInvalid());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testIntegers_Radix2()
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
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);

          Assertions.assertTrue(r.isValid());

          final PExprConstantInteger<PParsed> e =
            (PExprConstantInteger<PParsed>) r.get();
          Assertions.assertEquals(
            new BigInteger(s.replaceFirst("0b", "").replace("_", ""), 2),
            e.value());
          Assertions.assertEquals(2, e.radix());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testIntegersInvalid_Radix2()
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
          final SExpressionType se = this.parse(s);
          final Validation<Seq<PParseError>, PExpressionOrDeclarationType<PParsed>> r =
            PParsing.parseExpressionOrDeclaration(se);

          dump(r);
          Assertions.assertTrue(r.isInvalid());
        });

    Assertions.assertAll(execs);
  }

  @Test
  public void testPatternInteger0()
    throws Exception
  {
    final SExpressionType se = this.parse("0x391930");
    final Validation<Seq<PParseError>, PPatternType<PParsed>> r =
      PParsing.parsePattern(se);

    dump(r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstantInteger<PParsed> e =
      (PPatternConstantInteger<PParsed>) r.get();

    Assertions.assertEquals(new BigInteger("391930", 16), e.value());
    Assertions.assertEquals(16, e.radix());
  }

  @Test
  public void testPatternReal0()
    throws Exception
  {
    final SExpressionType se = this.parse("0.230");
    final Validation<Seq<PParseError>, PPatternType<PParsed>> r =
      PParsing.parsePattern(se);

    dump(r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstantReal<PParsed> e =
      (PPatternConstantReal<PParsed>) r.get();

    Assertions.assertEquals(new BigDecimal("0.230"), e.value());
  }

  @Test
  public void testPatternWildcard0()
    throws Exception
  {
    final SExpressionType se = this.parse("_");
    final Validation<Seq<PParseError>, PPatternType<PParsed>> r =
      PParsing.parsePattern(se);

    dump(r);
    Assertions.assertTrue(r.isValid());

    final PPatternWildcard<PParsed> e = (PPatternWildcard<PParsed>) r.get();
  }

  @Test
  public void testPatternString0()
    throws Exception
  {
    final SExpressionType se = this.parse("\"abc\"");
    final Validation<Seq<PParseError>, PPatternType<PParsed>> r =
      PParsing.parsePattern(se);

    dump(r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstantString<PParsed> e =
      (PPatternConstantString<PParsed>) r.get();

    Assertions.assertEquals("abc", e.value());
  }

  @Test
  public void testPatternConstructor0()
    throws Exception
  {
    final SExpressionType se = this.parse("(Cons xs)");
    final Validation<Seq<PParseError>, PPatternType<PParsed>> r =
      PParsing.parsePattern(se);

    dump(r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstructor<PParsed> e = (PPatternConstructor<PParsed>) r.get();
    Assertions.assertEquals("Cons", e.constructor().value());
    Assertions.assertEquals("xs", e.argument().get().value());
  }

  @Test
  public void testPatternConstructor1()
    throws Exception
  {
    final SExpressionType se = this.parse("Cons");
    final Validation<Seq<PParseError>, PPatternType<PParsed>> r =
      PParsing.parsePattern(se);

    dump(r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstructor<PParsed> e = (PPatternConstructor<PParsed>) r.get();
    Assertions.assertEquals("Cons", e.constructor().value());
    Assertions.assertEquals(Optional.empty(), e.argument());
  }

  @Test
  public void testPatternConstructor2()
    throws Exception
  {
    final SExpressionType se = this.parse("(Q:Cons xs)");
    final Validation<Seq<PParseError>, PPatternType<PParsed>> r =
      PParsing.parsePattern(se);

    dump(r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstructor<PParsed> e = (PPatternConstructor<PParsed>) r.get();
    Assertions.assertEquals("Q", e.unit().get().value());
    Assertions.assertEquals("Cons", e.constructor().value());
    Assertions.assertEquals("xs", e.argument().get().value());
  }

  @Test
  public void testPatternConstructor3()
    throws Exception
  {
    final SExpressionType se = this.parse("Q:Cons");
    final Validation<Seq<PParseError>, PPatternType<PParsed>> r =
      PParsing.parsePattern(se);

    dump(r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstructor<PParsed> e = (PPatternConstructor<PParsed>) r.get();
    Assertions.assertEquals("Q", e.unit().get().value());
    Assertions.assertEquals("Cons", e.constructor().value());
    Assertions.assertEquals(Optional.empty(), e.argument());
  }

  @Test
  public void testPatternConstructorInvalid0()
    throws Exception
  {
    final SExpressionType se = this.parse("(Cons xs.x)");
    final Validation<Seq<PParseError>, PPatternType<PParsed>> r =
      PParsing.parsePattern(se);

    dump(r);
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testPatternConstructorInvalid1()
    throws Exception
  {
    final SExpressionType se = this.parse("(X:Cons.y x)");
    final Validation<Seq<PParseError>, PPatternType<PParsed>> r =
      PParsing.parsePattern(se);

    dump(r);
    Assertions.assertTrue(r.isInvalid());
  }

  @Test
  public void testPatternConstructorInvalid2()
    throws Exception
  {
    final SExpressionType se = this.parse("(Cons.y x)");
    final Validation<Seq<PParseError>, PPatternType<PParsed>> r =
      PParsing.parsePattern(se);

    dump(r);
    Assertions.assertTrue(r.isInvalid());
  }

  private SExpressionType parse(
    final String s)
    throws RuntimeException
  {
    try {
      return PParsers.createWith(
        new JSXParserSupplier(), new JSXLexerSupplier())
        .createSExpressionParser(
          URI.create("urn:unknown"),
          new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)))
        .parseExpression();
    } catch (final JSXParserException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
