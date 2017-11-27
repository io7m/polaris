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
import com.io7m.polaris.model.PTermName;
import com.io7m.polaris.model.PTermNames;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;

public final class PTermNamesTest
{
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
        .map(name -> () -> Assertions.assertTrue(PTermNames.isValid(name), name)));
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
        .map(name -> () -> Assertions.assertFalse(PTermNames.isValid(name), name)));
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
      PTermName.of(lex0, TRUE, "a").lexical());

    Assertions.assertNotEquals(
      PTermName.of(lex1, TRUE, "a").withLexical(lex0),
      PTermName.of(lex0, TRUE, "a").lexical());

    Assertions.assertNotEquals(
      PTermName.of(lex0, TRUE, "a").lexical(),
      PTermName.of(lex1, TRUE, "b").lexical());

    Assertions.assertNotEquals(
      PTermName.of(lex0, TRUE, "a").withLexical(lex1),
      PTermName.of(lex0, TRUE, "a").lexical());
  }

  @Test
  public void testEquals()
  {
    Assertions.assertEquals(
      PTermName.of(Optional.empty(), TRUE, "a"),
      PTermName.of(Optional.empty(), TRUE, "a"));

    Assertions.assertEquals(
      PTermName.of(Optional.empty(), TRUE, "a").value(),
      PTermName.of(Optional.empty(), TRUE, "a").value());

    Assertions.assertEquals(
      PTermName.copyOf(PTermName.of(Optional.empty(), TRUE, "a")),
      PTermName.copyOf(PTermName.of(Optional.empty(), TRUE, "a")));

    Assertions.assertEquals(
      PTermName.of(Optional.empty(), TRUE, "a").withValue("c"),
      PTermName.of(Optional.empty(), TRUE, "c"));

    Assertions.assertNotEquals(
      PTermName.of(Optional.empty(), TRUE, "a"),
      PTermName.of(Optional.empty(), TRUE, "b"));

    Assertions.assertNotEquals(
      PTermName.of(Optional.empty(), TRUE, "a").value(),
      PTermName.of(Optional.empty(), TRUE, "b").value());
  }

  @Test
  public void testToString()
  {
    Assertions.assertEquals(
      PTermName.of(Optional.empty(), TRUE, "a").toString(),
      PTermName.of(Optional.empty(), TRUE, "a").toString());

    Assertions.assertNotEquals(
      PTermName.of(Optional.empty(), TRUE, "a").toString(),
      PTermName.of(Optional.empty(), TRUE, "b").toString());
  }

  @Test
  public void testHashCode()
  {
    Assertions.assertEquals(
      PTermName.of(Optional.empty(), TRUE, "a").hashCode(),
      PTermName.of(Optional.empty(), TRUE, "a").hashCode());
  }
}