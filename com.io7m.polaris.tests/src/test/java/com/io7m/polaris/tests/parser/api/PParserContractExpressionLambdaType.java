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
import com.io7m.polaris.ast.PExprLambda;
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

public interface PParserContractExpressionLambdaType
  extends PParserContractBaseType
{
  @Test
  default void testLambda0()
    throws Exception
  {
    final PParserType p = this.parserForString("(lambda (x y) (+ x y))");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isValid());

    final PExprLambda<PParsed> e = (PExprLambda<PParsed>) r.get().get();
    Assertions.assertEquals(
      "x",
      e.arguments().get(0).value());
    Assertions.assertEquals(
      "y",
      e.arguments().get(1).value());

    final PExprApplication<PParsed> app =
      (PExprApplication<PParsed>) e.expression();

    final PExprReference<PParsed> plus_func =
      (PExprReference<PParsed>) app.function();
    final PExprReference<PParsed> x_arg =
      (PExprReference<PParsed>) app.arguments().get(0);
    final PExprReference<PParsed> y_arg =
      (PExprReference<PParsed>) app.arguments().get(1);

    final PTermReferenceVariable<PParsed> plus_func_ref =
      (PTermReferenceVariable<PParsed>) plus_func.reference();
    final PTermReferenceVariable<PParsed> x_ref =
      (PTermReferenceVariable<PParsed>) x_arg.reference();
    final PTermReferenceVariable<PParsed> y_ref =
      (PTermReferenceVariable<PParsed>) y_arg.reference();

    Assertions.assertEquals("+", plus_func_ref.term().value());
    Assertions.assertEquals("x", x_ref.term().value());
    Assertions.assertEquals("y", y_ref.term().value());
  }

  @Test
  default void testLambda1()
    throws Exception
  {
    final PParserType p = this.parserForString("(λ (x y) (+ x y))");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isValid());

    final PExprLambda<PParsed> e = (PExprLambda<PParsed>) r.get().get();
    Assertions.assertEquals(
      "x",
      e.arguments().get(0).value());
    Assertions.assertEquals(
      "y",
      e.arguments().get(1).value());

    final PExprApplication<PParsed> app =
      (PExprApplication<PParsed>) e.expression();

    final PExprReference<PParsed> plus_func =
      (PExprReference<PParsed>) app.function();
    final PExprReference<PParsed> x_arg =
      (PExprReference<PParsed>) app.arguments().get(0);
    final PExprReference<PParsed> y_arg =
      (PExprReference<PParsed>) app.arguments().get(1);

    final PTermReferenceVariable<PParsed> plus_func_ref =
      (PTermReferenceVariable<PParsed>) plus_func.reference();
    final PTermReferenceVariable<PParsed> x_ref =
      (PTermReferenceVariable<PParsed>) x_arg.reference();
    final PTermReferenceVariable<PParsed> y_ref =
      (PTermReferenceVariable<PParsed>) y_arg.reference();

    Assertions.assertEquals("+", plus_func_ref.term().value());
    Assertions.assertEquals("x", x_ref.term().value());
    Assertions.assertEquals("y", y_ref.term().value());
  }

  @Test
  default void testLambdaInvalid_0()
    throws Exception
  {
    final PParserType p = this.parserForString("(λ (x y) ())");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_APPLICATION));
  }

  @Test
  default void testLambdaInvalid_1()
    throws Exception
  {
    final PParserType p = this.parserForString("(λ (.) (+ x y))");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TERM_VARIABLE_NAME));
  }

  @Test
  default void testLambdaInvalid_2()
    throws Exception
  {
    final PParserType p = this.parserForString("(λ ((+ 23)) (+ x y))");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TERM_VARIABLE_NAME));
  }

  @Test
  default void testLambdaInvalid_3()
    throws Exception
  {
    final PParserType p = this.parserForString("(λ (x))");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_LAMBDA));
  }

  @Test
  default void testLambdaInvalid_4()
    throws Exception
  {
    final PParserType p = this.parserForString("(λ)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_LAMBDA));
  }

  @Test
  default void testLambdaInvalid_5()
    throws Exception
  {
    final PParserType p = this.parserForString("(λ (x) (define-value x y))");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.EXPECTED_EXPRESSION_BUT_GOT_DECLARATION));
  }

  @Test
  default void testLambdaInvalid_6()
    throws Exception
  {
    final PParserType p = this.parserForString("(λ (x x) (x))");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_LAMBDA_DUPLICATE_PARAMETER));
  }

  @Test
  default void testLambdaInvalid_7()
    throws Exception
  {
    final PParserType p = this.parserForString("(lambda (x x) (x))");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);
    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_LAMBDA_DUPLICATE_PARAMETER));
  }
}
