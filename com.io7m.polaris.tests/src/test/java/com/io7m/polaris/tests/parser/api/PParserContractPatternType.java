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

import com.io7m.polaris.model.PPatternConstantInteger;
import com.io7m.polaris.model.PPatternConstantReal;
import com.io7m.polaris.model.PPatternConstantString;
import com.io7m.polaris.model.PPatternConstructor;
import com.io7m.polaris.model.PPatternType;
import com.io7m.polaris.model.PPatternWildcard;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorCode;
import com.io7m.polaris.parser.api.PParsed;
import com.io7m.polaris.parser.api.PParserType;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static com.io7m.polaris.tests.PTestUtilities.dump;

public interface PParserContractPatternType
  extends PParserContractBaseType
{
  @Test
  default void testPatternInteger0()
    throws Exception
  {
    final PParserType p = this.parserForString("0x391930");
    final Validation<Seq<PParseError>, Optional<PPatternType<PParsed>>> r =
      p.parsePattern();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstantInteger<PParsed> e =
      (PPatternConstantInteger<PParsed>) r.get().get();

    Assertions.assertEquals(new BigInteger("391930", 16), e.value());
    Assertions.assertEquals(16, e.radix());
  }

  @Test
  default void testPatternReal0()
    throws Exception
  {
    final PParserType p = this.parserForString("0.230");
    final Validation<Seq<PParseError>, Optional<PPatternType<PParsed>>> r =
      p.parsePattern();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstantReal<PParsed> e =
      (PPatternConstantReal<PParsed>) r.get().get();

    Assertions.assertEquals(new BigDecimal("0.230"), e.value());
  }

  @Test
  default void testPatternWildcard0()
    throws Exception
  {
    final PParserType p = this.parserForString("_");
    final Validation<Seq<PParseError>, Optional<PPatternType<PParsed>>> r =
      p.parsePattern();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PPatternWildcard<PParsed> e = (PPatternWildcard<PParsed>) r.get().get();
  }

  @Test
  default void testPatternString0()
    throws Exception
  {
    final PParserType p = this.parserForString("\"abc\"");
    final Validation<Seq<PParseError>, Optional<PPatternType<PParsed>>> r =
      p.parsePattern();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstantString<PParsed> e =
      (PPatternConstantString<PParsed>) r.get().get();

    Assertions.assertEquals("abc", e.value());
  }

  @Test
  default void testPatternConstructor0()
    throws Exception
  {
    final PParserType p = this.parserForString("(Cons xs)");
    final Validation<Seq<PParseError>, Optional<PPatternType<PParsed>>> r =
      p.parsePattern();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstructor<PParsed> e = (PPatternConstructor<PParsed>) r.get().get();
    Assertions.assertEquals(Optional.empty(), e.constructor().unit());
    Assertions.assertEquals("Cons", e.constructor().constructor().value());
    Assertions.assertEquals("xs", e.argument().get().value());
  }

  @Test
  default void testPatternConstructor1()
    throws Exception
  {
    final PParserType p = this.parserForString("Cons");
    final Validation<Seq<PParseError>, Optional<PPatternType<PParsed>>> r =
      p.parsePattern();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstructor<PParsed> e = (PPatternConstructor<PParsed>) r.get().get();
    Assertions.assertEquals(Optional.empty(), e.constructor().unit());
    Assertions.assertEquals("Cons", e.constructor().constructor().value());
    Assertions.assertEquals(Optional.empty(), e.argument());
  }

  @Test
  default void testPatternConstructor2()
    throws Exception
  {
    final PParserType p = this.parserForString("(Q:Cons xs)");
    final Validation<Seq<PParseError>, Optional<PPatternType<PParsed>>> r =
      p.parsePattern();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstructor<PParsed> e = (PPatternConstructor<PParsed>) r.get().get();
    Assertions.assertEquals("Q", e.constructor().unit().get().value());
    Assertions.assertEquals("Cons", e.constructor().constructor().value());
    Assertions.assertEquals("xs", e.argument().get().value());
  }

  @Test
  default void testPatternConstructor3()
    throws Exception
  {
    final PParserType p = this.parserForString("Q:Cons");
    final Validation<Seq<PParseError>, Optional<PPatternType<PParsed>>> r =
      p.parsePattern();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PPatternConstructor<PParsed> e = (PPatternConstructor<PParsed>) r.get().get();
    Assertions.assertEquals("Q", e.constructor().unit().get().value());
    Assertions.assertEquals("Cons", e.constructor().constructor().value());
    Assertions.assertEquals(Optional.empty(), e.argument());
  }

  @Test
  default void testPatternConstructorInvalid0()
    throws Exception
  {
    final PParserType p = this.parserForString("(Cons xs.x)");
    final Validation<Seq<PParseError>, Optional<PPatternType<PParsed>>> r =
      p.parsePattern();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(c -> c.code() == PParseErrorCode.INVALID_TERM_NAME));
  }

  @Test
  default void testPatternConstructorInvalid1()
    throws Exception
  {
    final PParserType p = this.parserForString("(X:Cons.y x)");
    final Validation<Seq<PParseError>, Optional<PPatternType<PParsed>>> r =
      p.parsePattern();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(c -> c.code() == PParseErrorCode.INVALID_TERM_CONSTRUCTOR_NAME));
  }

  @Test
  default void testPatternConstructorInvalid2()
    throws Exception
  {
    final PParserType p = this.parserForString("(Cons.y x)");
    final Validation<Seq<PParseError>, Optional<PPatternType<PParsed>>> r =
      p.parsePattern();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(c -> c.code() == PParseErrorCode.INVALID_TERM_CONSTRUCTOR_NAME));
  }
}
