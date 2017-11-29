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
import com.io7m.polaris.model.PExprMatch;
import com.io7m.polaris.model.PExprReference;
import com.io7m.polaris.model.PExpressionOrDeclarationType;
import com.io7m.polaris.model.PExpressionType;
import com.io7m.polaris.model.PPatternConstantInteger;
import com.io7m.polaris.model.PTermReferenceVariable;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorCode;
import com.io7m.polaris.parser.api.PParsed;
import com.io7m.polaris.parser.api.PParserType;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.Optional;

import static com.io7m.polaris.tests.PTestUtilities.dump;

public interface PParserContractExpressionMatchType
  extends PParserContractBaseType
{
  @Test
  default void testMatch0()
    throws Exception
  {
    final PParserType p = this.parserForString("(match x [case 23 24])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isValid());

    final PExprMatch<PParsed> e = (PExprMatch<PParsed>) r.get().get();
    final PExprReference<PParsed> x = (PExprReference<PParsed>) e.target();
    final PTermReferenceVariable<PParsed> ref = (PTermReferenceVariable<PParsed>) x.reference();
    Assertions.assertEquals("x", ref.term().value());

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
  default void testMatch1()
    throws Exception
  {
    final PParserType p = this.parserForString("(match A:x [case 23 24])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isValid());

    final PExprMatch<PParsed> e = (PExprMatch<PParsed>) r.get().get();
    ;
    final PExprReference<PParsed> x = (PExprReference<PParsed>) e.target();
    final PTermReferenceVariable<PParsed> ref = (PTermReferenceVariable<PParsed>) x.reference();
    Assertions.assertEquals("A", ref.unit().get().value());
    Assertions.assertEquals("x", ref.term().value());
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
  default void testMatchInvalid0()
    throws Exception
  {
    final PParserType p = this.parserForString("(match)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_MATCH));
  }

  @Test
  default void testMatchInvalid1()
    throws Exception
  {
    final PParserType p = this.parserForString("(match 23 [case x])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_MATCH_CASE));
  }

  @Test
  default void testMatchInvalid2()
    throws Exception
  {
    final PParserType p = this.parserForString("(match 23 [() y x])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.EXPECTED_KEYWORD));
  }

  @Test
  default void testMatchInvalid3()
    throws Exception
  {
    final PParserType p = this.parserForString("(match 23 [case (Null x y) x])");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_PATTERN));
  }
}
