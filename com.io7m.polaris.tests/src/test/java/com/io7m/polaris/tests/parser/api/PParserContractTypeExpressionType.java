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

import com.io7m.polaris.ast.PTypeExprApplication;
import com.io7m.polaris.ast.PTypeExprArrow;
import com.io7m.polaris.ast.PTypeExprForAll;
import com.io7m.polaris.ast.PTypeExprReference;
import com.io7m.polaris.ast.PTypeExpressionType;
import com.io7m.polaris.ast.PTypeReferenceVariable;
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

public interface PParserContractTypeExpressionType
  extends PParserContractBaseType
{
  @Test
  default void testTypeExpressionInvalid0()
    throws Exception
  {
    final PParserType p = this.parserForString("()");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION));
  }

  @Test
  default void testTypeExpressionArrow0()
    throws Exception
  {
    final PParserType p = this.parserForString("(→ a)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PTypeExprArrow<PParsed> e = (PTypeExprArrow<PParsed>) r.get().get();
    Assertions.assertEquals(0, e.parameters().size());

    final PTypeExprReference<PParsed> r_ref = (PTypeExprReference<PParsed>) e.returnType();
    final PTypeReferenceVariable<PParsed> rr = (PTypeReferenceVariable<PParsed>) r_ref.reference();
    Assertions.assertEquals("a", rr.variable().value());

    Assertions.assertFalse(e.isVariadic());
  }

  @Test
  default void testTypeExpressionArrow1()
    throws Exception
  {
    final PParserType p = this.parserForString("(→ a b)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PTypeExprArrow<PParsed> e = (PTypeExprArrow<PParsed>) r.get().get();
    Assertions.assertEquals(1, e.parameters().size());

    final PTypeExprReference<PParsed> a_ref = (PTypeExprReference<PParsed>) e.parameters().get(
      0);
    final PTypeReferenceVariable<PParsed> a_rr = (PTypeReferenceVariable<PParsed>) a_ref.reference();
    Assertions.assertEquals("a", a_rr.variable().value());

    final PTypeExprReference<PParsed> r_ref = (PTypeExprReference<PParsed>) e.returnType();
    final PTypeReferenceVariable<PParsed> rr = (PTypeReferenceVariable<PParsed>) r_ref.reference();
    Assertions.assertEquals("b", rr.variable().value());

    Assertions.assertFalse(e.isVariadic());
  }

  @Test
  default void testTypeExpressionArrow2()
    throws Exception
  {
    final PParserType p = this.parserForString("(→ (… a) b)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PTypeExprArrow<PParsed> e = (PTypeExprArrow<PParsed>) r.get().get();
    Assertions.assertEquals(1, e.parameters().size());

    final PTypeExprReference<PParsed> a_ref = (PTypeExprReference<PParsed>) e.parameters().get(
      0);
    final PTypeReferenceVariable<PParsed> a_rr = (PTypeReferenceVariable<PParsed>) a_ref.reference();
    Assertions.assertEquals("a", a_rr.variable().value());

    final PTypeExprReference<PParsed> r_ref = (PTypeExprReference<PParsed>) e.returnType();
    final PTypeReferenceVariable<PParsed> rr = (PTypeReferenceVariable<PParsed>) r_ref.reference();
    Assertions.assertEquals("b", rr.variable().value());

    Assertions.assertTrue(e.isVariadic());
  }

  @Test
  default void testTypeExpressionArrow3()
    throws Exception
  {
    final PParserType p = this.parserForString("(-> a)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PTypeExprArrow<PParsed> e = (PTypeExprArrow<PParsed>) r.get().get();
    Assertions.assertEquals(0, e.parameters().size());

    final PTypeExprReference<PParsed> r_ref = (PTypeExprReference<PParsed>) e.returnType();
    final PTypeReferenceVariable<PParsed> rr = (PTypeReferenceVariable<PParsed>) r_ref.reference();
    Assertions.assertEquals("a", rr.variable().value());

    Assertions.assertFalse(e.isVariadic());
  }

  @Test
  default void testTypeExpressionArrow4()
    throws Exception
  {
    final PParserType p = this.parserForString("(-> a b)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PTypeExprArrow<PParsed> e = (PTypeExprArrow<PParsed>) r.get().get();
    Assertions.assertEquals(1, e.parameters().size());

    final PTypeExprReference<PParsed> a_ref = (PTypeExprReference<PParsed>) e.parameters().get(
      0);
    final PTypeReferenceVariable<PParsed> a_rr = (PTypeReferenceVariable<PParsed>) a_ref.reference();
    Assertions.assertEquals("a", a_rr.variable().value());

    final PTypeExprReference<PParsed> r_ref = (PTypeExprReference<PParsed>) e.returnType();
    final PTypeReferenceVariable<PParsed> rr = (PTypeReferenceVariable<PParsed>) r_ref.reference();
    Assertions.assertEquals("b", rr.variable().value());

    Assertions.assertFalse(e.isVariadic());
  }

  @Test
  default void testTypeExpressionArrow5()
    throws Exception
  {
    final PParserType p = this.parserForString("(-> (… a) b)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PTypeExprArrow<PParsed> e = (PTypeExprArrow<PParsed>) r.get().get();
    Assertions.assertEquals(1, e.parameters().size());

    final PTypeExprReference<PParsed> a_ref = (PTypeExprReference<PParsed>) e.parameters().get(
      0);
    final PTypeReferenceVariable<PParsed> a_rr = (PTypeReferenceVariable<PParsed>) a_ref.reference();
    Assertions.assertEquals("a", a_rr.variable().value());

    final PTypeExprReference<PParsed> r_ref = (PTypeExprReference<PParsed>) e.returnType();
    final PTypeReferenceVariable<PParsed> rr = (PTypeReferenceVariable<PParsed>) r_ref.reference();
    Assertions.assertEquals("b", rr.variable().value());

    Assertions.assertTrue(e.isVariadic());
  }

  @Test
  default void testTypeExpressionArrowInvalid0()
    throws Exception
  {
    final PParserType p = this.parserForString("(→)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_ARROW));
  }

  @Test
  default void testTypeExpressionArrowInvalid1()
    throws Exception
  {
    final PParserType p = this.parserForString("(→ (… a) (… b) c)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_UNEXPECTED_VARIADIC));
  }

  @Test
  default void testTypeExpressionArrowInvalid2()
    throws Exception
  {
    final PParserType p = this.parserForString("(→ (variadic a) (variadic b) c)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_UNEXPECTED_VARIADIC));
  }

  @Test
  default void testTypeExpressionArrowInvalid3()
    throws Exception
  {
    final PParserType p = this.parserForString("(→ (… a b) c)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_VARIADIC));
  }

  @Test
  default void testTypeExpressionArrowInvalid4()
    throws Exception
  {
    final PParserType p = this.parserForString("(→ (variadic a b) c)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_VARIADIC));
  }

  @Test
  default void testTypeExpressionForAll0()
    throws Exception
  {
    final PParserType p = this.parserForString("(for-all a (option a))");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PTypeExprForAll<PParsed> e = (PTypeExprForAll<PParsed>) r.get().get();
    Assertions.assertEquals(1, e.parameters().size());
    Assertions.assertEquals("a", e.parameters().get(0).value());

    final PTypeExprApplication<PParsed> rr = (PTypeExprApplication<PParsed>) e.expression();
  }

  @Test
  default void testTypeExpressionForAll1()
    throws Exception
  {
    final PParserType p = this.parserForString("(∀ a (option a))");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isValid());

    final PTypeExprForAll<PParsed> e = (PTypeExprForAll<PParsed>) r.get().get();
    Assertions.assertEquals(1, e.parameters().size());
    Assertions.assertEquals("a", e.parameters().get(0).value());

    final PTypeExprApplication<PParsed> rr = (PTypeExprApplication<PParsed>) e.expression();
  }

  @Test
  default void testTypeExpressionForAllInvalid0()
    throws Exception
  {
    final PParserType p = this.parserForString("(for-all)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(c -> c.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_FORALL));
  }

  @Test
  default void testTypeExpressionForAllInvalid1()
    throws Exception
  {
    final PParserType p = this.parserForString("(∀)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(c -> c.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_FORALL));
  }

  @Test
  default void testTypeExpressionForAllInvalid2()
    throws Exception
  {
    final PParserType p = this.parserForString("(for-all a)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(c -> c.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_FORALL));
  }

  @Test
  default void testTypeExpressionForAllInvalid3()
    throws Exception
  {
    final PParserType p = this.parserForString("(∀ a)");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(c -> c.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_FORALL));
  }

  @Test
  default void testTypeExpressionForAllInvalid4()
    throws Exception
  {
    final PParserType p = this.parserForString("(for-all a a (option a))");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(c -> c.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_FORALL_DUPLICATE_NAME));
  }

  @Test
  default void testTypeExpressionForAllInvalid5()
    throws Exception
  {
    final PParserType p = this.parserForString("(∀ a a (option a))");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(c -> c.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_FORALL_DUPLICATE_NAME));
  }

  @Test
  default void testTypeExpressionForAllInvalid6()
    throws Exception
  {
    final PParserType p = this.parserForString("(for-all a (variadic a))");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(c -> c.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_UNEXPECTED_VARIADIC));
  }

  @Test
  default void testTypeExpressionForAllInvalid7()
    throws Exception
  {
    final PParserType p = this.parserForString("(∀ a (… a))");
    final Validation<Seq<PParseError>, Optional<PTypeExpressionType<PParsed>>> r =
      p.parseTypeExpression();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(c -> c.code() == PParseErrorCode.INVALID_TYPE_EXPRESSION_UNEXPECTED_VARIADIC));
  }
}
