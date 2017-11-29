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
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.model.PTermConstructorName;
import com.io7m.polaris.model.PTermConstructorNames;
import com.io7m.polaris.model.PTermName;
import com.io7m.polaris.model.PTermNames;
import com.io7m.polaris.model.PTypeConstructorName;
import com.io7m.polaris.model.PTypeConstructorNames;
import com.io7m.polaris.model.PTypeVariableName;
import com.io7m.polaris.model.PTypeVariableNames;
import com.io7m.polaris.model.PUnitName;
import com.io7m.polaris.model.PUnitNames;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.collection.HashMap;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.net.URI;
import java.util.Objects;
import java.util.function.Function;

import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TERM_CONSTRUCTOR_NAME;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TERM_NAME;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TYPE_CONSTRUCTOR_NAME;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TYPE_VARIABLE_NAME;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_UNIT_NAME;
import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;

/**
 * Functions for parsing names.
 */

public final class PParsingNames
{
  private PParsingNames()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as an unqualified term name.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return An unqualified term name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTermName<PParsed>>
  parseTermName(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      return parseTermNameRaw(m, es.lexical(), es.text());
    }
    return invalid(m.errorExpression(INVALID_TERM_NAME, e));
  }

  /**
   * Parse the given string as an unqualified term name.
   *
   * @param m       An error message provider
   * @param lexical Lexical information
   * @param text    The input text
   *
   * @return An unqualified term name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTermName<PParsed>>
  parseTermNameRaw(
    final PParseErrorMessagesType m,
    final LexicalPosition<URI> lexical,
    final String text)
  {
    if (PTermNames.isValid(text)) {
      return Validation.valid(PTermName.of(lexical, parsed(), text));
    }
    return invalid(m.errorLexical(INVALID_TERM_NAME, lexical, text));
  }

  /**
   * Parse the given expression as an unqualified constructor name.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return An unqualified constructor name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTermConstructorName<PParsed>>
  parseTermConstructorName(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      return parseTermConstructorNameRaw(m, es.lexical(), es.text());
    }
    return invalid(m.errorExpression(INVALID_TERM_CONSTRUCTOR_NAME, e));
  }

  /**
   * Parse the given string as an unqualified constructor name.
   *
   * @param m       An error message provider
   * @param lexical Lexical information
   * @param text    The input text
   *
   * @return An unqualified constructor name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTermConstructorName<PParsed>>
  parseTermConstructorNameRaw(
    final PParseErrorMessagesType m,
    final LexicalPosition<URI> lexical,
    final String text)
  {
    if (PTermConstructorNames.isValid(text)) {
      return Validation.valid(PTermConstructorName.of(lexical, parsed(), text));
    }
    return invalid(m.errorLexical(
      INVALID_TERM_CONSTRUCTOR_NAME,
      lexical,
      text));
  }

  /**
   * Parse the given expression as an unqualified unit name.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return An unqualified unit name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PUnitName<PParsed>>
  parseUnitName(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      return parseUnitNameRaw(m, es.lexical(), es.text());
    }
    return invalid(m.errorExpression(INVALID_UNIT_NAME, e));
  }

  /**
   * Parse the given string as an unqualified unit name.
   *
   * @param m       An error message provider
   * @param lexical Lexical information
   * @param text    The input text
   *
   * @return An unqualified unit name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PUnitName<PParsed>>
  parseUnitNameRaw(
    final PParseErrorMessagesType m,
    final LexicalPosition<URI> lexical,
    final String text)
  {
    if (PUnitNames.isValid(text)) {
      return Validation.valid(PUnitName.of(lexical, parsed(), text));
    }
    return invalid(m.errorLexical(INVALID_UNIT_NAME, lexical, text));
  }

  /**
   * Parse the given expression as a keyword.
   *
   * @param m    An error message provider
   * @param e    The expression
   * @param name The expected keyword
   *
   * @return {@code name}
   */

  public static Validation<Seq<PParseError>, String>
  parseKeyword(
    final PParseErrorMessagesType m,
    final SExpressionType e,
    final String name)
  {
    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      if (Objects.equals(es.text(), name)) {
        return Validation.valid(name);
      }
    }

    return invalid(m.errorExpectedKeyword(e, name));
  }

  /**
   * Require that the given vector of values contains no duplicates.
   *
   * @param values     The values
   * @param value_name A function to extract a name from a value
   * @param on_error   The function called with the list of duplicates if any
   *                   exist
   * @param <T>        The type of name
   *
   * @return A sequence of unique names, or a sequence of errors
   */

  public static <T, S> Validation<Seq<PParseError>, Vector<T>> requireUniqueNames(
    final Vector<T> values,
    final Function<T, S> value_name,
    final Function<Vector<S>, Seq<PParseError>> on_error)
  {
    HashMap<S, Integer> m = HashMap.empty();
    for (int index = 0; index < values.size(); ++index) {
      final S name = value_name.apply(values.get(index));
      final Integer next =
        Integer.valueOf(m.getOrElse(name, Integer.valueOf(0)).intValue() + 1);
      m = m.put(name, next);
    }

    final HashMap<S, Integer> duplicates =
      m.filter((name, count) -> count.intValue() > 1);

    if (duplicates.isEmpty()) {
      return Validation.valid(values);
    }

    return Validation.invalid(on_error.apply(duplicates.keySet().toVector()));
  }


  /**
   * Parse the given expression as an unqualified constructor name.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return An unqualified constructor name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTypeConstructorName<PParsed>>
  parseTypeConstructorName(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      return parseTypeConstructorNameRaw(m, es.lexical(), es.text());
    }
    return invalid(m.errorExpression(INVALID_TYPE_CONSTRUCTOR_NAME, e));
  }

  /**
   * Parse the given string as an unqualified constructor name.
   *
   * @param m       An error message provider
   * @param lexical Lexical information
   * @param text    The input text
   *
   * @return An unqualified constructor name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTypeConstructorName<PParsed>>
  parseTypeConstructorNameRaw(
    final PParseErrorMessagesType m,
    final LexicalPosition<URI> lexical,
    final String text)
  {
    if (PTypeConstructorNames.isValid(text)) {
      return Validation.valid(PTypeConstructorName.of(lexical, parsed(), text));
    }
    return invalid(m.errorLexical(
      INVALID_TYPE_CONSTRUCTOR_NAME,
      lexical,
      text));
  }


  /**
   * Parse the given expression as an unqualified variable name.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return An unqualified variable name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTypeVariableName<PParsed>>
  parseTypeVariableName(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      return parseTypeVariableNameRaw(m, es.lexical(), es.text());
    }
    return invalid(m.errorExpression(INVALID_TYPE_VARIABLE_NAME, e));
  }

  /**
   * Parse the given string as an unqualified variable name.
   *
   * @param m       An error message provider
   * @param lexical Lexical information
   * @param text    The input text
   *
   * @return An unqualified variable name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTypeVariableName<PParsed>>
  parseTypeVariableNameRaw(
    final PParseErrorMessagesType m,
    final LexicalPosition<URI> lexical,
    final String text)
  {
    if (PTypeVariableNames.isValid(text)) {
      return Validation.valid(PTypeVariableName.of(lexical, parsed(), text));
    }
    return invalid(m.errorLexical(INVALID_TYPE_VARIABLE_NAME, lexical, text));
  }
}
