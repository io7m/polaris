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

package com.io7m.polaris.tests.model;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.polaris.model.PUnitName;
import com.io7m.polaris.model.PUnitNames;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;

public final class PUnitNamesTest
{
  @Test
  public void testValid()
  {
    Assertions.assertAll(
      List.of(
        "X",
        "Xa",
        "X_3",
        "X3")
        .stream()
        .map(name -> () -> Assertions.assertTrue(PUnitNames.isValid(name), name)));
  }

  @Test
  public void testInvalid()
  {
    Assertions.assertAll(
      List.of(
        "x",
        "3",
        "",
        "-",
        "$",
        ".")
        .stream()
        .map(name -> () -> Assertions.assertFalse(PUnitNames.isValid(name), name)));
  }

  @Test
  public void testLexical()
  {
    final Optional<LexicalPosition<URI>> lex0 =
      Optional.of(LexicalPosition.of(23, 34, Optional.empty()));
    final Optional<LexicalPosition<URI>> lex1 =
      Optional.of(LexicalPosition.of(24, 34, Optional.empty()));

    Assertions.assertEquals(
      lex0,
      PUnitName.of(lex0, TRUE, "A").lexical());

    Assertions.assertNotEquals(
      PUnitName.of(lex1, TRUE, "A").withLexical(lex0),
      PUnitName.of(lex0, TRUE, "A").lexical());

    Assertions.assertNotEquals(
      PUnitName.of(lex0, TRUE, "A").lexical(),
      PUnitName.of(lex1, TRUE, "B").lexical());

    Assertions.assertNotEquals(
      PUnitName.of(lex0, TRUE, "A").withLexical(lex1),
      PUnitName.of(lex0, TRUE, "A").lexical());
  }

  @Test
  public void testEquals()
  {
    Assertions.assertEquals(
      PUnitName.of(Optional.empty(), TRUE, "A"),
      PUnitName.of(Optional.empty(), TRUE, "A"));

    Assertions.assertEquals(
      PUnitName.of(Optional.empty(), TRUE, "A").value(),
      PUnitName.of(Optional.empty(), TRUE, "A").value());

    Assertions.assertEquals(
      PUnitName.copyOf(PUnitName.of(Optional.empty(), TRUE, "A")),
      PUnitName.copyOf(PUnitName.of(Optional.empty(), TRUE, "A")));

    Assertions.assertEquals(
      PUnitName.of(Optional.empty(), TRUE, "A").withValue("C"),
      PUnitName.of(Optional.empty(), TRUE, "C"));

    Assertions.assertNotEquals(
      PUnitName.of(Optional.empty(), TRUE, "A"),
      PUnitName.of(Optional.empty(), TRUE, "B"));

    Assertions.assertNotEquals(
      PUnitName.of(Optional.empty(), TRUE, "A").value(),
      PUnitName.of(Optional.empty(), TRUE, "B").value());
  }

  @Test
  public void testToString()
  {
    Assertions.assertEquals(
      PUnitName.of(Optional.empty(), TRUE, "A").toString(),
      PUnitName.of(Optional.empty(), TRUE, "A").toString());

    Assertions.assertNotEquals(
      PUnitName.of(Optional.empty(), TRUE, "A").toString(),
      PUnitName.of(Optional.empty(), TRUE, "B").toString());
  }

  @Test
  public void testHashCode()
  {
    Assertions.assertEquals(
      PUnitName.of(Optional.empty(), TRUE, "A").hashCode(),
      PUnitName.of(Optional.empty(), TRUE, "A").hashCode());
  }
}
