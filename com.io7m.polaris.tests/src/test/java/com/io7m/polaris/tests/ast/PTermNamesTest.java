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
import com.io7m.polaris.ast.PTermVariableName;
import com.io7m.polaris.ast.PTermVariableNames;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;

public final class PTermNamesTest
{
  private static final LexicalPosition<URI> LEXICAL =
    LexicalPosition.of(1, 0, Optional.empty());

  @Test
  public void testValid()
  {
    Assertions.assertAll(
      List.of(
        "x",
        "X",
        "xa",
        "Xa",
        "XA",
        "X-3",
        "x_3",
        "X_3")
        .stream()
        .map(name -> () -> Assertions.assertTrue(
          PTermVariableNames.isValid(name),
          name)));
  }

  @Test
  public void testInvalid()
  {
    Assertions.assertAll(
      List.of(
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
          PTermVariableNames.isValid(name),
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
      PTermVariableName.of(lex0, TRUE, "a").lexical());

    Assertions.assertNotEquals(
      PTermVariableName.of(lex1, TRUE, "a").withLexical(lex0),
      PTermVariableName.of(lex0, TRUE, "a").lexical());

    Assertions.assertNotEquals(
      PTermVariableName.of(lex0, TRUE, "a").lexical(),
      PTermVariableName.of(lex1, TRUE, "b").lexical());

    Assertions.assertNotEquals(
      PTermVariableName.of(lex0, TRUE, "a").withLexical(lex1),
      PTermVariableName.of(lex0, TRUE, "a").lexical());
  }

  @Test
  public void testEquals()
  {
    Assertions.assertEquals(
      PTermVariableName.of(LEXICAL, TRUE, "a"),
      PTermVariableName.of(LEXICAL, TRUE, "a"));

    Assertions.assertEquals(
      PTermVariableName.of(LEXICAL, TRUE, "a").value(),
      PTermVariableName.of(LEXICAL, TRUE, "a").value());

    Assertions.assertEquals(
      PTermVariableName.copyOf(PTermVariableName.of(LEXICAL, TRUE, "a")),
      PTermVariableName.copyOf(PTermVariableName.of(LEXICAL, TRUE, "a")));

    Assertions.assertEquals(
      PTermVariableName.of(LEXICAL, TRUE, "a").withValue("c"),
      PTermVariableName.of(LEXICAL, TRUE, "c"));

    Assertions.assertNotEquals(
      PTermVariableName.of(LEXICAL, TRUE, "a"),
      PTermVariableName.of(LEXICAL, TRUE, "b"));

    Assertions.assertNotEquals(
      PTermVariableName.of(LEXICAL, TRUE, "a").value(),
      PTermVariableName.of(LEXICAL, TRUE, "b").value());
  }

  @Test
  public void testToString()
  {
    Assertions.assertEquals(
      PTermVariableName.of(LEXICAL, TRUE, "a").toString(),
      PTermVariableName.of(LEXICAL, TRUE, "a").toString());

    Assertions.assertNotEquals(
      PTermVariableName.of(LEXICAL, TRUE, "a").toString(),
      PTermVariableName.of(LEXICAL, TRUE, "b").toString());
  }

  @Test
  public void testHashCode()
  {
    Assertions.assertEquals(
      PTermVariableName.of(LEXICAL, TRUE, "a").hashCode(),
      PTermVariableName.of(LEXICAL, TRUE, "a").hashCode());
  }
}
