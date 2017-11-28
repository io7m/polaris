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

import com.io7m.polaris.model.PExprConstantReal;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.io7m.polaris.tests.PTestUtilities.dump;

public interface PParserContractExpressionRealType
  extends PParserContractBaseType
{
  @Test
  default void testReals()
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
          final PParserType p = this.parserForString(s);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);

          Assertions.assertTrue(r.isValid());

          final PExprConstantReal<PParsed> e = (PExprConstantReal<PParsed>) r.get().get();
          Assertions.assertEquals(new BigDecimal(s), e.value());
        });

    Assertions.assertAll(execs);
  }

  @Test
  default void testRealsInvalid()
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
          final PParserType p = this.parserForString(s);
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
            p.parseExpressionOrDeclaration();

          dump(this.log(), r);
          Assertions.assertTrue(r.isInvalid());
          Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_REAL));
        });

    Assertions.assertAll(execs);
  }
}
