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
import com.io7m.polaris.model.PTermName;
import com.io7m.polaris.model.PTermNames;
import com.io7m.polaris.model.PTypeName;
import com.io7m.polaris.model.PTypeNames;
import com.io7m.polaris.model.PUnitName;
import com.io7m.polaris.model.PUnitNames;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.polaris.parser.api.PParseErrorCode.EXPECTED_TERM_NAME_UNQUALIFIED_GOT_EXPRESSION;
import static com.io7m.polaris.parser.api.PParseErrorCode.EXPECTED_TERM_REFERENCE_GOT_EXPRESSION;
import static com.io7m.polaris.parser.api.PParseErrorCode.EXPECTED_TYPE_NAME_UNQUALIFIED_GOT_EXPRESSION;
import static com.io7m.polaris.parser.api.PParseErrorCode.EXPECTED_TYPE_REFERENCE_GOT_EXPRESSION;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TERM_NAME;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TYPE_NAME;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_UNIT_NAME;
import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PValidation.cast;
import static com.io7m.polaris.parser.implementation.PValidation.errorsFlatten;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;
import static com.io7m.polaris.parser.implementation.PValidation.sequence;

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
  parseTermNameUnqualified(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      final String text = es.text();
      if (PTermNames.isValid(text)) {
        return Validation.valid(PTermName.of(es.lexical(), parsed(), text));
      }
      return invalid(m.errorExpression(INVALID_TERM_NAME, e));
    }
    return invalid(m.errorExpression(
      EXPECTED_TERM_NAME_UNQUALIFIED_GOT_EXPRESSION, e));
  }

  /**
   * Parse the given expression as an unqualified type name.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return An unqualified term name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTypeName<PParsed>>
  parseTypeNameUnqualified(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(e, "Expression");

    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      final String text = es.text();
      if (PTypeNames.isValid(text)) {
        return Validation.valid(PTypeName.of(es.lexical(), parsed(), text));
      }
      return invalid(m.errorExpression(INVALID_TYPE_NAME, e));
    }
    return invalid(m.errorExpression(
      EXPECTED_TYPE_NAME_UNQUALIFIED_GOT_EXPRESSION, e));
  }

  /**
   * Parse the given expression as a term reference.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return A term reference, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTermReference>
  parseTermReference(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      final String et = es.text();

      /*
       * If the reference contains a ":", then treat it as a path prefixed by
       * a unit name.
       */

      if (et.contains(":")) {
        final int colon = et.indexOf(':');
        final String t_unit = et.substring(0, colon);
        final String t_rest = et.substring(colon + 1);

        final Validation<Seq<PParseError>, PUnitName<PParsed>> r_unit =
          cast(parseUnitName(m, es, colon, t_unit));
        final Validation<Seq<PParseError>, Vector<PTermName<PParsed>>> r_terms =
          parseTermNames(m, es, colon, t_rest);
        final Validation<Seq<Seq<PParseError>>, PTermReference> r_result =
          Validation.combine(r_unit, r_terms)
            .ap((unit, path) -> new PTermReference(
              Optional.of(unit),
              path.get(0),
              path.tail()));

        return errorsFlatten(r_result);
      }

      /*
       * Otherwise, treat the reference as a simple path of record accesses.
       */

      final Validation<Seq<PParseError>, Vector<PTermName<PParsed>>> r_terms =
        parseTermNames(m, es, 0, et);

      return r_terms.map(
        terms -> new PTermReference(
          Optional.empty(),
          terms.get(0),
          terms.tail()));
    }

    return invalid(m.errorExpression(
      EXPECTED_TERM_REFERENCE_GOT_EXPRESSION, e));
  }

  private static Validation<Seq<PParseError>, Vector<PTermName<PParsed>>> parseTermNames(
    final PParseErrorMessagesType m,
    final SExpressionSymbolType es,
    final int offset,
    final String text)
  {
    final Vector<String> pieces = Vector.of(text.split("\\."));
    if (pieces.isEmpty()) {
      return invalid(m.errorExpression(INVALID_TERM_NAME, es));
    }

    Vector<Tuple2<Integer, String>> pieces_offsets = Vector.empty();
    int current_offset = offset;
    for (final String piece : pieces) {
      pieces_offsets =
        pieces_offsets.append(Tuple.of(Integer.valueOf(current_offset), piece));
      current_offset += piece.length();
    }

    return sequence(
      pieces_offsets,
      pair -> parseTermName(m, es, pair._1.intValue(), pair._2));
  }

  private static Validation<Seq<PParseError>, PTermName<PParsed>> parseTermName(
    final PParseErrorMessagesType m,
    final SExpressionSymbolType es,
    final int offset,
    final String name)
  {
    final LexicalPosition<URI> lex = es.lexical().withColumn(offset);
    if (PTermNames.isValid(name)) {
      return Validation.valid(PTermName.of(lex, parsed(), name));
    }
    return invalid(m.errorLexical(INVALID_TERM_NAME, lex, name));
  }

  private static Validation<Seq<PParseError>, PTypeName<PParsed>> parseTypeName(
    final PParseErrorMessagesType m,
    final SExpressionSymbolType es,
    final int offset,
    final String name)
  {
    final LexicalPosition<URI> lex = es.lexical().withColumn(offset);
    if (PTypeNames.isValid(name)) {
      return Validation.valid(PTypeName.of(lex, parsed(), name));
    }
    return invalid(m.errorLexical(INVALID_TYPE_NAME, lex, name));
  }

  private static Validation<Seq<PParseError>, PUnitName<PParsed>> parseUnitName(
    final PParseErrorMessagesType m,
    final SExpressionSymbolType es,
    final int offset,
    final String name)
  {
    final LexicalPosition<URI> lex = es.lexical().withColumn(offset);
    if (PUnitNames.isValid(name)) {
      return Validation.valid(PUnitName.of(lex, parsed(), name));
    }
    return invalid(m.errorLexical(INVALID_UNIT_NAME, lex, name));
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
   * Parse the given expression as a type reference.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return A type reference, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTypeReference> parseTypeReference(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      final String et = es.text();

      /*
       * If the reference contains a ":", then treat it as a name prefixed by
       * a unit name.
       */

      if (et.contains(":")) {
        final int colon = et.indexOf(':');
        final String t_unit = et.substring(0, colon);
        final String t_rest = et.substring(colon + 1);

        final Validation<Seq<PParseError>, PUnitName<PParsed>> r_unit =
          cast(parseUnitName(m, es, colon, t_unit));
        final Validation<Seq<PParseError>, PTypeName<PParsed>> r_base =
          parseTypeName(m, es, colon, t_rest);

        final Validation<Seq<Seq<PParseError>>, PTypeReference> r_result =
          Validation.combine(r_unit, r_base)
            .ap((unit, base) -> new PTypeReference(Optional.of(unit), base));

        return errorsFlatten(r_result);
      }

      /*
       * Otherwise, treat the reference as a simple type name.
       */

      final Validation<Seq<PParseError>, PTypeName<PParsed>> r_base =
        parseTypeName(m, es, 0, et);

      return r_base.map(base -> new PTypeReference(Optional.empty(), base));
    }

    return invalid(m.errorExpression(
      EXPECTED_TYPE_REFERENCE_GOT_EXPRESSION, e));
  }

  /**
   * A term reference.
   */

  public static final class PTermReference
  {
    private final Optional<PUnitName<PParsed>> unit;
    private final PTermName<PParsed> base;
    private final Vector<PTermName<PParsed>> path;

    /**
     * Construct a term path.
     *
     * @param in_unit The unit, if any
     * @param in_base The term
     * @param in_path The series of record accesses
     */

    public PTermReference(
      final Optional<PUnitName<PParsed>> in_unit,
      final PTermName<PParsed> in_base,
      final Vector<PTermName<PParsed>> in_path)
    {
      this.unit = Objects.requireNonNull(in_unit, "Unit");
      this.base = Objects.requireNonNull(in_base, "Base");
      this.path = Objects.requireNonNull(in_path, "Path");
    }

    /**
     * @return The unit, if any
     */

    public Optional<PUnitName<PParsed>> unit()
    {
      return this.unit;
    }

    /**
     * @return The base name
     */

    public PTermName<PParsed> base()
    {
      return this.base;
    }

    /**
     * @return The series of record accesses
     */

    public Vector<PTermName<PParsed>> path()
    {
      return this.path;
    }
  }

  /**
   * A term path.
   */

  public static final class PTypeReference
  {
    private final Optional<PUnitName<PParsed>> unit;
    private final PTypeName<PParsed> base;

    /**
     * Construct a type reference.
     *
     * @param in_unit The unit, if any
     * @param in_base The term
     */

    public PTypeReference(
      final Optional<PUnitName<PParsed>> in_unit,
      final PTypeName<PParsed> in_base)
    {
      this.unit = Objects.requireNonNull(in_unit, "Unit");
      this.base = Objects.requireNonNull(in_base, "Base");
    }

    /**
     * @return The unit, if any
     */

    public Optional<PUnitName<PParsed>> unit()
    {
      return this.unit;
    }

    /**
     * @return The base name
     */

    public PTypeName<PParsed> base()
    {
      return this.base;
    }
  }
}
