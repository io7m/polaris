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
import com.io7m.polaris.model.PTypeName;
import com.io7m.polaris.model.PTypeNames;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;

public final class PTypeNamesTest
{
  @Test
  public void testValid()
  {
    Assertions.assertAll(
      List.of(
        "x",
        "xa",
        "x_3",
        "x-3")
        .stream()
        .map(name -> () -> Assertions.assertTrue(
          PTypeNames.isValid(name),
          name)));
  }

  @Test
  public void testInvalid()
  {
    Assertions.assertAll(
      List.of(
        "X",
        "3",
        "",
        "-",
        "$",
        ".")
        .stream()
        .map(name -> () -> Assertions.assertFalse(
          PTypeNames.isValid(name),
          name)));
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
      PTypeName.of(lex0, TRUE, "a").lexical());

    Assertions.assertNotEquals(
      PTypeName.of(lex1, TRUE, "a").withLexical(lex0),
      PTypeName.of(lex0, TRUE, "a").lexical());

    Assertions.assertNotEquals(
      PTypeName.of(lex0, TRUE, "a").lexical(),
      PTypeName.of(lex1, TRUE, "b").lexical());

    Assertions.assertNotEquals(
      PTypeName.of(lex0, TRUE, "a").withLexical(lex1),
      PTypeName.of(lex0, TRUE, "a").lexical());
  }

  @Test
  public void testEquals()
  {
    Assertions.assertEquals(
      PTypeName.of(Optional.empty(), TRUE, "a"),
      PTypeName.of(Optional.empty(), TRUE, "a"));

    Assertions.assertEquals(
      PTypeName.of(Optional.empty(), TRUE, "a").value(),
      PTypeName.of(Optional.empty(), TRUE, "a").value());

    Assertions.assertEquals(
      PTypeName.copyOf(PTypeName.of(Optional.empty(), TRUE, "a")),
      PTypeName.copyOf(PTypeName.of(Optional.empty(), TRUE, "a")));

    Assertions.assertEquals(
      PTypeName.of(Optional.empty(), TRUE, "a").withValue("c"),
      PTypeName.of(Optional.empty(), TRUE, "c"));

    Assertions.assertNotEquals(
      PTypeName.of(Optional.empty(), TRUE, "a"),
      PTypeName.of(Optional.empty(), TRUE, "b"));

    Assertions.assertNotEquals(
      PTypeName.of(Optional.empty(), TRUE, "a").value(),
      PTypeName.of(Optional.empty(), TRUE, "b").value());
  }

  @Test
  public void testToString()
  {
    Assertions.assertEquals(
      PTypeName.of(Optional.empty(), TRUE, "a").toString(),
      PTypeName.of(Optional.empty(), TRUE, "a").toString());

    Assertions.assertNotEquals(
      PTypeName.of(Optional.empty(), TRUE, "a").toString(),
      PTypeName.of(Optional.empty(), TRUE, "b").toString());
  }

  @Test
  public void testHashCode()
  {
    Assertions.assertEquals(
      PTypeName.of(Optional.empty(), TRUE, "a").hashCode(),
      PTypeName.of(Optional.empty(), TRUE, "a").hashCode());
  }
}
