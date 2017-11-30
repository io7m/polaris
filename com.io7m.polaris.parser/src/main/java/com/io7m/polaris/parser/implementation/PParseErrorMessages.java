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
import com.io7m.jlexing.core.LexicalType;
import com.io7m.jsx.SExpressionType;
import com.io7m.jsx.prettyprint.JSXPrettyPrinterCodeStyle;
import com.io7m.jsx.prettyprint.JSXPrettyPrinterType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.ast.PPackageNames;
import com.io7m.polaris.ast.PTermConstructorNames;
import com.io7m.polaris.ast.PTermVariableNames;
import com.io7m.polaris.ast.PTypeConstructorNames;
import com.io7m.polaris.ast.PTypeVariableNames;
import com.io7m.polaris.ast.PUnitNames;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorCode;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParseErrorType;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public final class PParseErrorMessages implements PParseErrorMessagesType
{
  private final ResourceBundle messages;
  private final ResourceBundle expected;

  private PParseErrorMessages(
    final ResourceBundle in_messages,
    final ResourceBundle in_expected)
  {
    this.messages = Objects.requireNonNull(in_messages, "Messages");
    this.expected = Objects.requireNonNull(in_expected, "Expected");
  }

  public static PParseErrorMessagesType create()
  {
    return createWithLocale(Locale.getDefault());
  }

  public static PParseErrorMessagesType createWithLocale(
    final Locale locale)
  {
    Objects.requireNonNull(locale, "Locale");

    return new PParseErrorMessages(
      ResourceBundle.getBundle(
        "com/io7m/polaris/parser/implementation/PParseErrorMessages", locale),
      ResourceBundle.getBundle(
        "com/io7m/polaris/parser/implementation/PParseErrorExpected", locale));
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

  @Override
  public PParseError error(
    final LexicalType<URI> lex,
    final PParseErrorCode code,
    final Supplier<String> received,
    final Optional<Exception> exception)
  {
    Objects.requireNonNull(lex, "Lex");
    Objects.requireNonNull(code, "Code");
    Objects.requireNonNull(received, "Received");
    Objects.requireNonNull(exception, "Exception");

    final String text_message =
      this.codeToMessage(code);
    final String text_expected =
      this.codeToExpected(code);

    return PParseError.builder()
      .setCode(code)
      .setSeverity(PParseErrorType.Severity.ERROR)
      .setLexical(lex.lexical())
      .setMessage(errorMessageFormat(
        text_message,
        text_expected,
        received.get()))
      .setException(exception)
      .build();
  }

  @Override
  public PParseError errorLexical(
    final PParseErrorCode code,
    final LexicalPosition<URI> lex,
    final String received)
  {
    Objects.requireNonNull(code, "Code");
    Objects.requireNonNull(lex, "Lexical");
    Objects.requireNonNull(received, "Name");
    return this.error(() -> lex, code, () -> received, Optional.empty());
  }

  @Override
  public PParseError errorExpression(
    final PParseErrorCode code,
    final SExpressionType received)
  {
    Objects.requireNonNull(code, "Code");
    Objects.requireNonNull(received, "Expression");
    return this.errorExpressionExceptionOpt(code, received, Optional.empty());
  }

  private PParseError errorExpressionExceptionOpt(
    final PParseErrorCode code,
    final SExpressionType e,
    final Optional<Exception> exception)
  {
    return this.error(e, code, () -> prettyPrint(e), exception);
  }

  @Override
  public PParseError errorExpressionException(
    final PParseErrorCode code,
    final SExpressionType received,
    final Exception exception)
  {
    Objects.requireNonNull(code, "Code");
    Objects.requireNonNull(received, "Expression");
    Objects.requireNonNull(exception, "Exception");
    return this.errorExpressionExceptionOpt(
      code, received, Optional.of(exception));
  }

  @Override
  public PParseError errorExpectedKeyword(
    final SExpressionType received,
    final String keyword)
  {
    Objects.requireNonNull(received, "Received");
    Objects.requireNonNull(keyword, "Keyword");

    final String text_message =
      this.codeToMessage(PParseErrorCode.EXPECTED_KEYWORD);
    final String text_expected =
      this.codeToExpected(PParseErrorCode.EXPECTED_KEYWORD) + " " + keyword;

    return PParseError.builder()
      .setCode(PParseErrorCode.EXPECTED_KEYWORD)
      .setSeverity(PParseErrorType.Severity.ERROR)
      .setLexical(received.lexical())
      .setMessage(errorMessageFormat(
        text_message,
        text_expected,
        prettyPrint(received)))
      .build();
  }

  private String codeToExpected(
    final PParseErrorCode code)
  {
    final String base = this.expected.getString(code.name());

    switch (code) {
      case INVALID_PATTERN:
        return base;
      case INVALID_PATTERN_EXPECTED_CONSTRUCTOR_REFERENCE:
        return base;
      case INVALID_PATTERN_EXPECTED_CONSTRUCTOR_UNQUALIIFIED_ARGUMENT_NAME:
        return base;
      case INVALID_LOCAL:
        return base;
      case INVALID_MATCH:
        return base;
      case INVALID_MATCH_CASE:
        return base;
      case INVALID_LAMBDA:
        return base;
      case INVALID_LAMBDA_DUPLICATE_PARAMETER:
        return base;
      case INVALID_FUNCTION:
        return base;
      case INVALID_FUNCTION_DUPLICATE_PARAMETER:
        return base;
      case INVALID_VALUE:
        return base;
      case INVALID_REAL:
        return base;
      case INVALID_INTEGER:
        return base;
      case INVALID_TERM_VARIABLE_NAME:
        return base + " " + PTermVariableNames.PATTERN.pattern();
      case INVALID_UNIT_NAME:
        return base + " " + PUnitNames.PATTERN.pattern();
      case INVALID_TERM_CONSTRUCTOR_NAME:
        return base + " " + PTermConstructorNames.PATTERN.pattern();
      case INVALID_TYPE_CONSTRUCTOR_NAME:
        return base + " " + PTypeConstructorNames.PATTERN.pattern();
      case INVALID_TYPE_VARIABLE_NAME:
        return base + " " + PTypeVariableNames.PATTERN.pattern();
      case INVALID_PACKAGE_NAME:
        return base + " " + PPackageNames.PATTERN.pattern();
      case INVALID_RECORD:
        return base;
      case INVALID_RECORD_DUPLICATE_FIELD:
        return base;
      case INVALID_RECORD_FIELD:
        return base;
      case INVALID_RECORD_TYPE_PARAMETERS:
        return base;
      case INVALID_VARIANT:
        return base;
      case INVALID_VARIANT_DUPLICATE_CASE:
        return base;
      case INVALID_VARIANT_CASE:
        return base;
      case INVALID_VARIANT_TYPE_PARAMETERS:
        return base;
      case INVALID_UNIT:
        return base;
      case INVALID_UNIT_IMPORT:
        return base;
      case INVALID_UNIT_IMPORT_QUALIFIED:
        return base;
      case INVALID_UNIT_EXPORT_TERMS:
        return base;
      case INVALID_UNIT_EXPORT_TYPES:
        return base;
      case INVALID_UNIT_EXPORT_TERMS_DUPLICATE_NAME:
        return base;
      case INVALID_UNIT_EXPORT_TYPES_DUPLICATE_NAME:
        return base;
      case INVALID_APPLICATION:
        return base;
      case INVALID_TYPE_EXPRESSION:
        return base;
      case INVALID_TYPE_EXPRESSION_ARROW:
        return base;
      case INVALID_TYPE_EXPRESSION_UNEXPECTED_VARIADIC:
        return base;
      case INVALID_TYPE_EXPRESSION_VARIADIC:
        return base;
      case INVALID_TYPE_EXPRESSION_FORALL:
        return base;
      case INVALID_TYPE_EXPRESSION_FORALL_DUPLICATE_NAME:
        return base;
      case INVALID_S_EXPRESSION:
        return base;
      case INVALID_TERM_REFERENCE:
        return base;
      case INVALID_TYPE_REFERENCE:
        return base;
      case INVALID_CONSTRUCTOR_REFERENCE:
        return base;
      case INVALID_TYPE_SIGNATURE:
        return base;
      case EXPECTED_TERM_NAME_UNQUALIFIED_GOT_EXPRESSION:
        return base;
      case EXPECTED_TYPE_NAME_UNQUALIFIED_GOT_EXPRESSION:
        return base;
      case EXPECTED_TERM_REFERENCE_GOT_EXPRESSION:
        return base;
      case EXPECTED_EXPRESSION_BUT_GOT_DECLARATION:
        return base;
      case EXPECTED_KEYWORD:
        return base;
      case EXPECTED_TYPE_REFERENCE_GOT_EXPRESSION:
        return base;
    }

    throw new UnreachableCodeException();
  }

  private String codeToMessage(
    final PParseErrorCode code)
  {
    return this.messages.getString(code.name());
  }
}
