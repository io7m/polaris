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
import com.io7m.polaris.ast.PTermConstructorName;
import com.io7m.polaris.ast.PTermConstructorNames;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;

public final class PTermConstructorNamesTest
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
          PTermConstructorNames.isValid(name),
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
          PTermConstructorNames.isValid(name),
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
      PTermConstructorName.of(lex0, TRUE, "A").lexical());

    Assertions.assertNotEquals(
      PTermConstructorName.of(lex1, TRUE, "A").withLexical(lex0),
      PTermConstructorName.of(lex0, TRUE, "A").lexical());

    Assertions.assertNotEquals(
      PTermConstructorName.of(lex0, TRUE, "A").lexical(),
      PTermConstructorName.of(lex1, TRUE, "B").lexical());

    Assertions.assertNotEquals(
      PTermConstructorName.of(lex0, TRUE, "A").withLexical(lex1),
      PTermConstructorName.of(lex0, TRUE, "A").lexical());
  }

  @Test
  public void testEquals()
  {
    Assertions.assertEquals(
      PTermConstructorName.of(LEXICAL, TRUE, "A"),
      PTermConstructorName.of(LEXICAL, TRUE, "A"));

    Assertions.assertEquals(
      PTermConstructorName.of(LEXICAL, TRUE, "A").value(),
      PTermConstructorName.of(LEXICAL, TRUE, "A").value());

    Assertions.assertEquals(
      PTermConstructorName.copyOf(PTermConstructorName.of(LEXICAL, TRUE, "A")),
      PTermConstructorName.copyOf(PTermConstructorName.of(LEXICAL, TRUE, "A")));

    Assertions.assertEquals(
      PTermConstructorName.of(LEXICAL, TRUE, "A").withValue("C"),
      PTermConstructorName.of(LEXICAL, TRUE, "C"));

    Assertions.assertNotEquals(
      PTermConstructorName.of(LEXICAL, TRUE, "A"),
      PTermConstructorName.of(LEXICAL, TRUE, "B"));

    Assertions.assertNotEquals(
      PTermConstructorName.of(LEXICAL, TRUE, "A").value(),
      PTermConstructorName.of(LEXICAL, TRUE, "B").value());
  }

  @Test
  public void testToString()
  {
    Assertions.assertEquals(
      PTermConstructorName.of(LEXICAL, TRUE, "A").toString(),
      PTermConstructorName.of(LEXICAL, TRUE, "A").toString());

    Assertions.assertNotEquals(
      PTermConstructorName.of(LEXICAL, TRUE, "A").toString(),
      PTermConstructorName.of(LEXICAL, TRUE, "B").toString());
  }

  @Test
  public void testHashCode()
  {
    Assertions.assertEquals(
      PTermConstructorName.of(LEXICAL, TRUE, "A").hashCode(),
      PTermConstructorName.of(LEXICAL, TRUE, "A").hashCode());
  }
}
