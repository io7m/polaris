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

package com.io7m.polaris.parser.implementation;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionType;
import com.io7m.jsx.prettyprint.JSXPrettyPrinterCodeStyle;
import com.io7m.jsx.prettyprint.JSXPrettyPrinterType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.model.PTermNames;
import com.io7m.polaris.model.PUnitNames;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorType;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Functions to construct parse errors.
 */

// CHECKSTYLE:OFF

public final class PParseErrors
{
  private PParseErrors()
  {
    throw new UnreachableCodeException();
  }

  public static PParseError parseError(
    final SExpressionType e,
    final Supplier<String> messages)
  {
    return PParseError.builder()
      .setSeverity(PParseErrorType.Severity.ERROR)
      .setMessage(messages.get())
      .setLexical(lex(e.lexical()))
      .build();
  }

  private static LexicalPosition<URI> lex(
    final Optional<LexicalPosition<URI>> lexical)
  {
    return lexical.orElse(LexicalPosition.of(1, 0, Optional.empty()));
  }

  private static String errorMessageFormat(
    final String message,
    final String expected,
    final String received)
  {
    return new StringBuilder(128)
      .append(message)
      .append(System.lineSeparator())
      .append("  Expected: ")
      .append(expected)
      .append(System.lineSeparator())
      .append("  Received: ")
      .append(received)
      .append(System.lineSeparator())
      .toString();
  }

  public static String errorMessageTermNameNotValid(
    final String text)
  {
    return errorMessageFormat(
      "The given term name is not valid",
      "A name matching " + PTermNames.PATTERN.pattern(),
      text);
  }

  public static String errorMessageUnitNameNotValid(
    final String text)
  {
    return errorMessageFormat(
      "The given unit name is not valid",
      "A name matching " + PUnitNames.PATTERN.pattern(),
      text);
  }

  public static String errorUnparseableInteger(
    final String text)
  {
    return errorMessageFormat(
      "Could not parse the given value as an integer",
      "<integer>",
      text);
  }

  public static String errorUnparseableReal(
    final String text)
  {
    return errorMessageFormat(
      "Could not parse the given value as a real number",
      "<real>",
      text);
  }

  public static <T> Validation<Seq<PParseError>, T> errorInvalid(
    final PParseError p)
  {
    return Validation.invalid(Vector.of(p));
  }

  public static <T> Validation<Seq<PParseError>, T> errorInvalidExpression(
    final SExpressionType e,
    final Supplier<String> message)
  {
    return errorInvalid(parseError(e, message));
  }

  public static String errorMessageEmptyExpression(
    final SExpressionListType e)
  {
    return errorMessageFormat(
      "Received an empty expression",
      "A valid expression",
      "()");
  }

  public static String errorExpectedExpressionGotDeclaration(
    final SExpressionType ex)
  {
    return errorMessageFormat(
      "A declaration was given where an expression was expected",
      "A term-level expression",
      prettyPrint(ex)
    );
  }

  public static String errorExpectedTermNameGotExpression(
    final SExpressionType ex)
  {
    return errorMessageFormat(
      "An expression was provided where a term name was expected",
      "A term name",
      prettyPrint(ex)
    );
  }

  private static String prettyPrint(
    final SExpressionType ex)
  {
    try (StringWriter writer = new StringWriter()) {
      try (JSXPrettyPrinterType printer =
             JSXPrettyPrinterCodeStyle.newPrinterWithWidthIndent(
               writer, 80, 2)) {
        printer.print(ex);
        return writer.toString();
      }
    } catch (final IOException e) {
      throw new UnreachableCodeException(e);
    }
  }

  public static String errorMessageInvalidLambda(
    final SExpressionListType e)
  {
    return errorMessageFormat(
      "Invalid lambda expression",
      "(lambda (<term-name> ...) <expression>) | (λ (<term-name> ...) <expression>)",
      prettyPrint(e));
  }

  public static String errorMessageInvalidPattern(
    final SExpressionType e)
  {
    return errorMessageFormat(
      "Invalid pattern",
      "<integer> | <real> | _ | <term-name> | (<term-name> <term-name>)",
      prettyPrint(e));
  }

  public static String errorMessageInvalidMatchCase(
    final SExpressionType e)
  {
    return errorMessageFormat(
      "Invalid match case",
      "(case <pattern> <expression>)",
      prettyPrint(e));
  }

  public static String errorMessageInvalidMatch(
    final SExpressionType e)
  {
    return errorMessageFormat(
      "Invalid match expression",
      "(match <expression> <case> { <case> } )",
      prettyPrint(e));
  }

  public static String errorMessageInvalidLocal(
    final SExpressionType e)
  {
    return errorMessageFormat(
      "Invalid local expression",
      "(local { <expression> | <declaration> } <expression>)",
      prettyPrint(e));
  }

  public static String errorMessageExpectedKeyword(
    final SExpressionType e,
    final String name)
  {
    return errorMessageFormat(
      "Expected a keyword",
      name,
      prettyPrint(e));
  }

  public static String errorExpectedTermReferenceGotExpression(
    final SExpressionType e)
  {
    return errorMessageFormat(
      "An expression was provided where a term reference was expected",
      "A term reference",
      prettyPrint(e)
    );
  }

  public static String errorMessagePatternInvalidExpectedNoPath(
    final SExpressionType e)
  {
    return errorMessageFormat(
      "A reference to a constructor was expected",
      "A constructor reference",
      prettyPrint(e));
  }

  public static String errorMessagePatternInvalidExpectedNoPathNoUnit(
    final SExpressionType e)
  {
    return errorMessageFormat(
      "An unqualified term name was expected",
      "An unqualified term name",
      prettyPrint(e));
  }
}
