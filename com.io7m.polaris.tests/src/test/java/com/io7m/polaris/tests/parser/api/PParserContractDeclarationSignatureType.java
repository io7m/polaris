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

import com.io7m.polaris.model.PDeclarationSignature;
import com.io7m.polaris.model.PExpressionOrDeclarationType;
import com.io7m.polaris.model.PTypeExprReference;
import com.io7m.polaris.model.PTypeReferenceConstructor;
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

public interface PParserContractDeclarationSignatureType
  extends PParserContractBaseType
{
  @Test
  default void testDeclarationSignature0()
    throws Exception
  {
    final PParserType p = this.parserForString("(: pi Real)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isValid());

    final PDeclarationSignature<PParsed> e = (PDeclarationSignature<PParsed>) r.get().get();
    Assertions.assertEquals("pi", e.name().value());
    final PTypeReferenceConstructor<PParsed> t_ref =
      (PTypeReferenceConstructor<PParsed>)
        ((PTypeExprReference<PParsed>) e.type()).reference();
    Assertions.assertEquals("Real", t_ref.constructor().value());
  }

  @Test
  default void testDeclarationSignatureInvalid0()
    throws Exception
  {
    final PParserType p = this.parserForString("(:)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_SIGNATURE));
  }

  @Test
  default void testDeclarationSignatureInvalid1()
    throws Exception
  {
    final PParserType p = this.parserForString("(: x y z)");
    final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> r =
      p.parseExpressionOrDeclaration();

    dump(this.log(), r);

    Assertions.assertTrue(r.isInvalid());
    Assertions.assertTrue(r.getError().exists(e -> e.code() == PParseErrorCode.INVALID_TYPE_SIGNATURE));
  }
}
