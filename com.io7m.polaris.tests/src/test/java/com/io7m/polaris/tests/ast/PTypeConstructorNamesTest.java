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

package com.io7m.polaris.tests.ast;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.polaris.ast.PTypeConstructorName;
import com.io7m.polaris.ast.PTypeConstructorNames;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;

public final class PTypeConstructorNamesTest
{
  private static final LexicalPosition<URI> LEXICAL =
    LexicalPosition.of(1, 0, Optional.empty());

  @Test
  public void testValid()
  {
    Assertions.assertAll(
      List.of(
        "X",
        "Xa",
        "XA",
        "X_3")
        .stream()
        .map(name -> () -> Assertions.assertTrue(
          PTypeConstructorNames.isValid(name),
          name)));
  }

  @Test
  public void testInvalid()
  {
    Assertions.assertAll(
      List.of(
        "a",
        "a-3",
        "-",
        "",
        "0",
        ".",
        ";",
        "[",
        "/",
        "<",
        ">",
        ".",
        ":")
        .stream()
        .map(name -> () -> Assertions.assertFalse(
          PTypeConstructorNames.isValid(name),
          name)));
  }

  @Test
  public void testLexical()
  {
    final LexicalPosition<URI> lex0 =
      LexicalPosition.of(23, 34, Optional.empty());
    final LexicalPosition<URI> lex1 =
      LexicalPosition.of(24, 34, Optional.empty());

    Assertions.assertEquals(
      lex0,
      PTypeConstructorName.of(lex0, TRUE, "A").lexical());

    Assertions.assertNotEquals(
      PTypeConstructorName.of(lex1, TRUE, "A").withLexical(lex0),
      PTypeConstructorName.of(lex0, TRUE, "A").lexical());

    Assertions.assertNotEquals(
      PTypeConstructorName.of(lex0, TRUE, "A").lexical(),
      PTypeConstructorName.of(lex1, TRUE, "B").lexical());

    Assertions.assertNotEquals(
      PTypeConstructorName.of(lex0, TRUE, "A").withLexical(lex1),
      PTypeConstructorName.of(lex0, TRUE, "A").lexical());
  }

  @Test
  public void testEquals()
  {
    Assertions.assertEquals(
      PTypeConstructorName.of(LEXICAL, TRUE, "A"),
      PTypeConstructorName.of(LEXICAL, TRUE, "A"));

    Assertions.assertEquals(
      PTypeConstructorName.of(LEXICAL, TRUE, "A").value(),
      PTypeConstructorName.of(LEXICAL, TRUE, "A").value());

    Assertions.assertEquals(
      PTypeConstructorName.copyOf(PTypeConstructorName.of(LEXICAL, TRUE, "A")),
      PTypeConstructorName.copyOf(PTypeConstructorName.of(LEXICAL, TRUE, "A")));

    Assertions.assertEquals(
      PTypeConstructorName.of(LEXICAL, TRUE, "A").withValue("C"),
      PTypeConstructorName.of(LEXICAL, TRUE, "C"));

    Assertions.assertNotEquals(
      PTypeConstructorName.of(LEXICAL, TRUE, "A"),
      PTypeConstructorName.of(LEXICAL, TRUE, "B"));

    Assertions.assertNotEquals(
      PTypeConstructorName.of(LEXICAL, TRUE, "A").value(),
      PTypeConstructorName.of(LEXICAL, TRUE, "B").value());
  }

  @Test
  public void testToString()
  {
    Assertions.assertEquals(
      PTypeConstructorName.of(LEXICAL, TRUE, "A").toString(),
      PTypeConstructorName.of(LEXICAL, TRUE, "A").toString());

    Assertions.assertNotEquals(
      PTypeConstructorName.of(LEXICAL, TRUE, "A").toString(),
      PTypeConstructorName.of(LEXICAL, TRUE, "B").toString());
  }

  @Test
  public void testHashCode()
  {
    Assertions.assertEquals(
      PTypeConstructorName.of(LEXICAL, TRUE, "A").hashCode(),
      PTypeConstructorName.of(LEXICAL, TRUE, "A").hashCode());
  }
}
