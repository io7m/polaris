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

import com.io7m.jaffirm.core.Preconditions;
import com.io7m.jlexing.core.LexicalPosition;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.model.PTermConstructorName;
import com.io7m.polaris.model.PTermName;
import com.io7m.polaris.model.PTermNameType;
import com.io7m.polaris.model.PTermReferenceConstructor;
import com.io7m.polaris.model.PTermReferenceType;
import com.io7m.polaris.model.PTermReferenceVariable;
import com.io7m.polaris.model.PUnitName;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_CONSTRUCTOR_REFERENCE;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TERM_NAME;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TERM_REFERENCE;
import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PValidation.errorsFlatten;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;

/**
 * Functions for parsing term references.
 */

public final class PParsingTermReferences
{
  private PParsingTermReferences()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as a term reference.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return A term reference, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTermReferenceType<PParsed>>
  parseTermReference(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      final String text = es.text();

      final LexicalPosition<URI> lex_before = es.lexical();
      if (text.contains(":")) {
        final int colon = text.indexOf(':');
        final String text_before = text.substring(0, colon);
        final String text_after = text.substring(colon + 1);
        final LexicalPosition<URI> lex_after =
          lex_before.withColumn(lex_before.column() + colon);

        if (text_after.isEmpty()) {
          return invalid(m.errorLexical(
            INVALID_TERM_NAME,
            lex_after,
            text_after));
        }

        if (Character.isUpperCase(text_after.codePointAt(0))) {
          return parseTermReferenceAsConstructorQualified(
            m, lex_before, text_before, lex_after, text_after);
        }
        return parseTermReferenceAsVariableQualified(
          m, lex_before, text_before, lex_after, text_after);
      }

      if (Character.isUpperCase(text.codePointAt(0))) {
        return parseTermReferenceAsConstructorUnqualified(m, lex_before, text);
      }
      return parseTermPathReferenceAsVariableUnqualified(m, lex_before, text);
    }

    return invalid(m.errorExpression(INVALID_TERM_REFERENCE, e));
  }

  /**
   * Parse the given expression as a constructor reference.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return A constructor reference, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTermReferenceConstructor<PParsed>>
  parseConstructorReference(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    return parseTermReference(m, e).flatMap(ref -> {
      switch (ref.referenceKind()) {
        case REFERENCE_CONSTRUCTOR:
          return Validation.valid((PTermReferenceConstructor<PParsed>) ref);
        case REFERENCE_VARIABLE:
          return invalid(m.errorExpression(INVALID_CONSTRUCTOR_REFERENCE, e));
      }
      throw new UnreachableCodeException();
    });
  }

  private static Validation<Seq<PParseError>, PTermReferenceType<PParsed>>
  parseTermReferenceAsConstructorQualified(
    final PParseErrorMessagesType m,
    final LexicalPosition<URI> lexi_before,
    final String text_before,
    final LexicalPosition<URI> lexi_after,
    final String text_after)
  {
    final Validation<Seq<PParseError>, PUnitName<PParsed>> r_unit =
      PParsingNames.parseUnitNameRaw(m, lexi_before, text_before);
    final Validation<Seq<PParseError>, PTermConstructorName<PParsed>> r_term =
      PParsingNames.parseTermConstructorNameRaw(m, lexi_after, text_after);
    final Validation<Seq<Seq<PParseError>>, PTermReferenceType<PParsed>> r_result =
      Validation.combine(r_unit, r_term)
        .ap((t_unit, t_term) -> PTermReferenceConstructor.of(
          parsed(), Optional.of(t_unit), t_term));
    return errorsFlatten(r_result);
  }

  private static Validation<Seq<PParseError>, PTermReferenceType<PParsed>>
  parseTermReferenceAsVariableQualified(
    final PParseErrorMessagesType m,
    final LexicalPosition<URI> lexi_before,
    final String text_before,
    final LexicalPosition<URI> lexi_after,
    final String text_after)
  {
    final Validation<Seq<PParseError>, PUnitName<PParsed>> r_unit =
      PParsingNames.parseUnitNameRaw(m, lexi_before, text_before);
    final Validation<Seq<PParseError>, Vector<PTermName<PParsed>>> r_term =
      parseTermPathRaw(m, lexi_after, text_after);
    final Validation<Seq<Seq<PParseError>>, PTermReferenceType<PParsed>> r_result =
      Validation.combine(r_unit, r_term)
        .ap((t_unit, t_term) -> makeReferenceVariable(
          lexi_before, Optional.of(t_unit), t_term));
    return errorsFlatten(r_result);
  }

  private static Validation<Seq<PParseError>, PTermReferenceType<PParsed>>
  parseTermReferenceAsConstructorUnqualified(
    final PParseErrorMessagesType m,
    final LexicalPosition<URI> lexical,
    final String text)
  {
    return PParsingNames.parseTermConstructorNameRaw(m, lexical, text)
      .map(name -> PTermReferenceConstructor.of(
        parsed(), Optional.empty(), name));
  }

  private static Validation<Seq<PParseError>, PTermReferenceType<PParsed>>
  parseTermPathReferenceAsVariableUnqualified(
    final PParseErrorMessagesType m,
    final LexicalPosition<URI> lexical,
    final String text)
  {
    return parseTermPathRaw(m, lexical, text)
      .map(params -> makeReferenceVariable(lexical, Optional.empty(), params));
  }

  private static Validation<Seq<PParseError>, Vector<PTermName<PParsed>>>
  parseTermPathRaw(
    final PParseErrorMessagesType m,
    final LexicalPosition<URI> lexical,
    final String text)
  {
    final Vector<String> components = Vector.of(text.split("\\."));

    if (components.isEmpty()) {
      return invalid(m.errorLexical(INVALID_TERM_NAME, lexical, text));
    }

    Vector<Validation<Seq<PParseError>, PTermName<PParsed>>> checks = Vector.empty();
    int offset = lexical.column();
    for (int index = 0; index < components.size(); ++index) {
      final String component = components.get(index);
      final int offset_now = offset;
      checks = checks.append(PParsingNames.parseTermNameRaw(
        m, lexical.withColumn(offset_now), component));
      offset += component.length();
    }

    return Validation.sequence(checks).map(Vector::ofAll);
  }

  private static PTermReferenceType<PParsed>
  makeReferenceVariable(
    final LexicalPosition<URI> lex,
    final Optional<PUnitName<PParsed>> unit,
    final Vector<PTermName<PParsed>> params)
  {
    Preconditions.checkPrecondition(
      params,
      !params.isEmpty(),
      p -> "Parameters must not be empty");

    final Vector<PTermNameType<PParsed>> path;
    if (params.size() > 1) {
      path = PVectors.vectorCast(params.tail());
    } else {
      path = Vector.empty();
    }

    return PTermReferenceVariable.of(parsed(), unit, params.get(0), path);
  }
}
