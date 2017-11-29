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
import com.io7m.polaris.ast.PPackageName;
import com.io7m.polaris.ast.PPackageNames;
import io.vavr.collection.Vector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;


public final class PPackageNamesTest
{
  private static final LexicalPosition<URI> LEXICAL =
    LexicalPosition.of(1, 0, Optional.empty());

  @Test
  public void testValid()
  {
    Assertions.assertAll(
      List.of(
        "x.y.z",
        "xa.ya.za",
        "x_3.y_3.z_3",
        "x3.y3.z3")
        .stream()
        .map(name -> () -> Assertions.assertTrue(
          PPackageNames.isValid(name),
          name)));
  }

  @Test
  public void testInvalid()
  {
    Assertions.assertAll(
      List.of(
        "",
        "-",
        "$",
        ".")
        .stream()
        .map(name -> () -> Assertions.assertFalse(
          PPackageNames.isValid(name),
          name)));
  }

  @Test
  public void testComponents()
  {
    Assertions.assertAll(
      () -> {
        Assertions.assertEquals(
          Vector.of("x"),
          PPackageName.of(LEXICAL, TRUE, "x").nameComponents());
      },
      () -> {
        Assertions.assertEquals(
          Vector.of("x", "y"),
          PPackageName.of(LEXICAL, TRUE, "x.y").nameComponents());
      },
      () -> {
        Assertions.assertEquals(
          Vector.of("x", "y", "z"),
          PPackageName.of(LEXICAL, TRUE, "x.y.z").nameComponents());
      });
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
      PPackageName.of(lex0, LEXICAL, "a").lexical());

    Assertions.assertNotEquals(
      PPackageName.of(lex1, LEXICAL, "a").withLexical(lex0),
      PPackageName.of(lex0, LEXICAL, "a").lexical());

    Assertions.assertNotEquals(
      PPackageName.of(lex0, LEXICAL, "a").lexical(),
      PPackageName.of(lex1, LEXICAL, "b").lexical());

    Assertions.assertNotEquals(
      PPackageName.of(lex0, LEXICAL, "a").withLexical(lex1),
      PPackageName.of(lex0, LEXICAL, "a").lexical());
  }

  @Test
  public void testEquals()
  {
    Assertions.assertEquals(
      PPackageName.of(LEXICAL, TRUE, "a"),
      PPackageName.of(LEXICAL, TRUE, "a"));

    Assertions.assertEquals(
      PPackageName.of(LEXICAL, TRUE, "a").value(),
      PPackageName.of(LEXICAL, TRUE, "a").value());

    Assertions.assertEquals(
      PPackageName.copyOf(
        PPackageName.of(LEXICAL, TRUE, "a")),
      PPackageName.copyOf(
        PPackageName.of(LEXICAL, TRUE, "a")));

    Assertions.assertEquals(
      PPackageName.of(LEXICAL, TRUE, "a").withValue("c"),
      PPackageName.of(LEXICAL, TRUE, "c"));

    Assertions.assertNotEquals(
      PPackageName.of(LEXICAL, TRUE, "a"),
      PPackageName.of(LEXICAL, TRUE, "b"));

    Assertions.assertNotEquals(
      PPackageName.of(LEXICAL, TRUE, "a").value(),
      PPackageName.of(LEXICAL, TRUE, "b").value());
  }

  @Test
  public void testToString()
  {
    Assertions.assertEquals(
      PPackageName.of(LEXICAL, TRUE, "a").toString(),
      PPackageName.of(LEXICAL, TRUE, "a").toString());

    Assertions.assertNotEquals(
      PPackageName.of(LEXICAL, TRUE, "a").toString(),
      PPackageName.of(LEXICAL, TRUE, "b").toString());
  }

  @Test
  public void testHashCode()
  {
    Assertions.assertEquals(
      PPackageName.of(LEXICAL, TRUE, "a").hashCode(),
      PPackageName.of(LEXICAL, TRUE, "a").hashCode());
  }
}
