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

import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.model.PTermName;
import com.io7m.polaris.model.PTermNames;
import com.io7m.polaris.model.PUnitName;
import com.io7m.polaris.model.PUnitNames;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorExpectedTermNameGotExpression;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorExpectedTermReferenceGotExpression;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorInvalidExpression;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorMessageTermNameNotValid;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorMessageUnitNameNotValid;

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
   * @param e The input expression
   *
   * @return An unqualified term name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTermName<PParsed>>
  parseTermNameUnqualified(
    final SExpressionType e)
  {
    Objects.requireNonNull(e, "Expression");

    if (e instanceof SExpressionSymbolType) {
      final SExpressionSymbolType es = (SExpressionSymbolType) e;
      final String text = es.text();
      if (PTermNames.isValid(text)) {
        return Validation.valid(PTermName.of(es.lexical(), parsed(), text));
      }
      return errorInvalidExpression(
        e, () -> errorMessageTermNameNotValid(text));
    }

    return errorInvalidExpression(
      e, () -> errorExpectedTermNameGotExpression(e));
  }

  /**
   * Parse the given expression as a term reference.
   *
   * @param e The input expression
   *
   * @return A term reference, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTermReferencePath>
  parseTermPath(
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
          parseUnitName(es, colon, t_unit).map(Function.identity());
        final Validation<Seq<PParseError>, Vector<PTermName<PParsed>>> r_terms =
          parseTermNames(es, colon, t_rest);
        final Validation<Seq<Seq<PParseError>>, PTermReferencePath> r_result =
          Validation.combine(r_unit, r_terms)
            .ap((unit, path) -> new PTermReferencePath(
              Optional.of(unit),
              path.get(0),
              path.tail()));

        return PValidation.errorsFlatten(r_result);
      }

      /*
       * Otherwise, treat the reference as a simple path of record accesses.
       */

      final Validation<Seq<PParseError>, Vector<PTermName<PParsed>>> r_terms =
        parseTermNames(es, 0, et);

      return r_terms.map(
        terms ->
          new PTermReferencePath(
            Optional.empty(),
            terms.get(0),
            terms.tail()));
    }

    return errorInvalidExpression(
      e, () -> errorExpectedTermReferenceGotExpression(e));
  }

  private static Validation<Seq<PParseError>, Vector<PTermName<PParsed>>> parseTermNames(
    final SExpressionSymbolType es,
    final int offset,
    final String text)
  {
    final Vector<String> pieces = Vector.of(text.split("\\."));
    if (pieces.isEmpty()) {
      return errorInvalidExpression(
        es, () -> errorMessageTermNameNotValid(text));
    }

    Vector<Tuple2<Integer, String>> pieces_offsets = Vector.empty();
    int current_offset = offset;
    for (final String piece : pieces) {
      pieces_offsets =
        pieces_offsets.append(Tuple.of(Integer.valueOf(current_offset), piece));
      current_offset += piece.length();
    }

    return PValidation.sequence(
      pieces_offsets, pair -> parseTermName(es, pair._1.intValue(), pair._2));
  }

  private static Validation<Seq<PParseError>, PTermName<PParsed>> parseTermName(
    final SExpressionSymbolType es,
    final int offset,
    final String name)
  {
    if (PTermNames.isValid(name)) {
      return Validation.valid(
        PTermName.of(
          es.lexical().map(lex -> lex.withColumn(offset)),
          parsed(),
          name));
    }
    return errorInvalidExpression(es, () -> errorMessageTermNameNotValid(name));
  }

  private static Validation<Seq<PParseError>, PUnitName<PParsed>> parseUnitName(
    final SExpressionSymbolType es,
    final int offset,
    final String unit)
  {
    if (PUnitNames.isValid(unit)) {
      return Validation.valid(
        PUnitName.of(
          es.lexical().map(lex -> lex.withColumn(lex.column() + offset)),
          parsed(),
          unit));
    }
    return errorInvalidExpression(es, () -> errorMessageUnitNameNotValid(unit));
  }

  /**
   * A term path.
   */

  public static final class PTermReferencePath
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

    public PTermReferencePath(
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

}
