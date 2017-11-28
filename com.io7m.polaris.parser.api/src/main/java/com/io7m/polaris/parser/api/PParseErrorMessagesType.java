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

package com.io7m.polaris.parser.api;

import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jlexing.core.LexicalType;
import com.io7m.jsx.SExpressionType;

import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A dictionary of error messages.
 */

public interface PParseErrorMessagesType
{
  /**
   * Retrieve an error value for the given error code.
   *
   * @param lex       The lexical information
   * @param code      The error code
   * @param received  A supplier of text for the "Received: " section of the
   *                  error message
   * @param exception The exception raised, if any
   *
   * @return An error value
   */

  PParseError error(
    LexicalType<URI> lex,
    PParseErrorCode code,
    Supplier<String> received,
    Optional<Exception> exception);

  /**
   * Retrieve an error value for the given error code.
   *
   * @param lex      The lexical information
   * @param code     The error code
   * @param received The text for the "Received: " section of the error message
   *
   * @return An error value
   */

  PParseError errorLexical(
    PParseErrorCode code,
    LexicalPosition<URI> lex,
    String received);

  /**
   * Retrieve an error value for the given error code.
   *
   * @param code     The error code
   * @param received The received expression
   *
   * @return An error value
   */

  PParseError errorExpression(
    PParseErrorCode code,
    SExpressionType received);

  /**
   * Retrieve an error value for the given error code.
   *
   * @param code      The error code
   * @param received  The received expression
   * @param exception The exception raised
   *
   * @return An error value
   */

  PParseError errorExpressionException(
    PParseErrorCode code,
    SExpressionType received,
    Exception exception);

  /**
   * Retrieve an error value indicating that a keyword was expected but a
   * different type of expression was received.
   *
   * @param received The received expression
   * @param keyword  The expected keyword
   *
   * @return An error value
   */

  PParseError errorExpectedKeyword(
    SExpressionType received,
    String keyword);
}
