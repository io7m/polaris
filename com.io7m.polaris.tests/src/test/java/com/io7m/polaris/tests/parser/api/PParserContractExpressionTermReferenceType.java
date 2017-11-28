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

import com.io7m.polaris.model.PExprReference;
import com.io7m.polaris.model.PExpressionOrDeclarationType;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorCode;
import com.io7m.polaris.parser.api.PParsed;
import com.io7m.polaris.parser.api.PParserType;
import io.vavr.Tuple;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.io7m.polaris.tests.PTestUtilities.dump;

public interface PParserContractExpressionTermReferenceType
  extends PParserContractBaseType
{
  @Test
  default void testTermReferencesUnitless()
    throws Exception
  {
    final Stream<Executable> execs =
      List.of(
        Tuple.of(
          "a",
          "a"))
        .stream()
        .map(s -> (Executable) () -> {
          final PParserType p = this.parserForString(s._2);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);
          Assertions.assertTrue(r.isValid());

          final PExprReference<PParsed> e = (PExprReference<PParsed>) r.get().get();
          Assertions.assertEquals(Optional.empty(), e.unit());
          Assertions.assertEquals(s._1, e.name().value());
        });

    Assertions.assertAll(execs);
  }

  @Test
  default void testTermReferencesUnitlessInvalid()
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
          final PParserType p = this.parserForString(s);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);
          Assertions.assertTrue(r.isInvalid());
          Assertions.assertTrue(r.getError().exists(
            e -> e.code() == PParseErrorCode.INVALID_TERM_NAME
              || e.code() == PParseErrorCode.INVALID_UNIT_NAME));
        });

    Assertions.assertAll(execs);
  }

  @Test
  default void testTermReferences()
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
          final PParserType p = this.parserForString(s._3);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);

          Assertions.assertTrue(r.isValid());

          final PExprReference<PParsed> e = (PExprReference<PParsed>) r.get().get();
          Assertions.assertEquals(s._1, e.unit().get().value());
          Assertions.assertEquals(s._2, e.name().value());
        });

    Assertions.assertAll(execs);
  }

  @Test
  default void testTermReferencesInvalid()
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
          final PParserType p = this.parserForString(s);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);
          Assertions.assertTrue(r.isInvalid());
          Assertions.assertTrue(r.getError().exists(
            e -> e.code() == PParseErrorCode.INVALID_TERM_NAME
              || e.code() == PParseErrorCode.INVALID_UNIT_NAME));
        });

    Assertions.assertAll(execs);
  }
}
