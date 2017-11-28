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

import com.io7m.polaris.model.PExprApplication;
import com.io7m.polaris.model.PExprLocal;
import com.io7m.polaris.model.PExprReference;
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

public interface PParserContractExpressionLocalType
  extends PParserContractBaseType
{
  @Test
  default void testLocal0()
    throws Exception
  {
    final PParserType p = this.parserForString("(local x)");

    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PExprLocal<PParsed> e = (PExprLocal<PParsed>) r.get().get();
    final PExprReference<PParsed> x = (PExprReference<PParsed>) e.body();
    Assertions.assertEquals("x", x.name().value());
    Assertions.assertEquals(0, e.locals().size());
  }

  @Test
  default void testLocal1()
    throws Exception
  {
    final PParserType p = this.parserForString("(local [y] [z] x)");

    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PExprLocal<PParsed> e = (PExprLocal<PParsed>) r.get().get();
    final PExprReference<PParsed> x = (PExprReference<PParsed>) e.body();
    Assertions.assertEquals("x", x.name().value());
    Assertions.assertEquals(2, e.locals().size());

    final PExprApplication<PParsed> y =
      (PExprApplication<PParsed>) e.locals().get(0);
    Assertions.assertEquals(
      "y",
      ((PExprReference<PParsed>) y.function()).name().value());
    final PExprApplication<PParsed> z =
      (PExprApplication<PParsed>) e.locals().get(1);
    Assertions.assertEquals(
      "z",
      ((PExprReference<PParsed>) z.function()).name().value());
  }

  @Test
  default void testLocalInvalid_0()
    throws Exception
  {
    final PParserType p = this.parserForString("(local)");

    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_LOCAL));
  }
}
