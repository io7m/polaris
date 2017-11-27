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

import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionMatcherType;
import com.io7m.jsx.SExpressionQuotedStringType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.model.PPatternConstantInteger;
import com.io7m.polaris.model.PPatternConstantReal;
import com.io7m.polaris.model.PPatternConstantString;
import com.io7m.polaris.model.PPatternConstructor;
import com.io7m.polaris.model.PPatternType;
import com.io7m.polaris.model.PPatternWildcard;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParsed;
import com.io7m.polaris.parser.implementation.PParsingNames.PTermReferencePath;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.util.Objects;
import java.util.Optional;

import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorInvalidExpression;
import static com.io7m.polaris.parser.implementation.PParseErrors.errorMessageInvalidPattern;

/**
 * Functions to parse patterns.
 */

public final class PParsingPatterns
{
  private PParsingPatterns()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as a pattern.
   *
   * @param e The expression
   *
   * @return A parsed pattern, or a sequence of errors
   */

  public static Validation<Seq<PParseError>, PPatternType<PParsed>> parseAsPattern(
    final SExpressionType e)
  {
    Objects.requireNonNull(e, "Expression");

    return e.matchExpression(
      new SExpressionMatcherType<Validation<Seq<PParseError>, PPatternType<PParsed>>, RuntimeException>()
      {
        @Override
        public Validation<Seq<PParseError>, PPatternType<PParsed>> list(
          final SExpressionListType el)
        {
          return parsePatternList(el);
        }

        @Override
        public Validation<Seq<PParseError>, PPatternType<PParsed>> quotedString(
          final SExpressionQuotedStringType eq)
        {
          return parsePatternQuotedString(eq);
        }

        @Override
        public Validation<Seq<PParseError>, PPatternType<PParsed>> symbol(
          final SExpressionSymbolType es)
        {
          return parsePatternSymbol(es);
        }
      });
  }

  private static Validation<Seq<PParseError>, PPatternType<PParsed>> parsePatternList(
    final SExpressionListType e)
  {
    if (e.size() == 2) {
      final SExpressionType e0 = e.get(0);
      final SExpressionType e1 = e.get(1);

      final Validation<Seq<PParseError>, PTermReferencePath> r_constructor =
        PParsingNames.parseTermPath(e0)
          .flatMap(path -> requireEmptyPath(e0, path));

      final Validation<Seq<PParseError>, PTermReferencePath> r_arg =
        PParsingNames.parseTermPath(e1)
          .flatMap(path -> requireEmptyPathAndUnit(e1, path));

      final Validation<Seq<Seq<PParseError>>, PPatternType<PParsed>> r_result =
        Validation.combine(r_constructor, r_arg)
          .ap((cons_path, arg_path) ->
                PPatternConstructor.of(
                  e.lexical(),
                  parsed(),
                  cons_path.unit(),
                  cons_path.base(),
                  Optional.of(arg_path.base())));

      return PValidation.errorsFlatten(r_result);
    }

    return errorInvalidExpression(e, () -> errorMessageInvalidPattern(e));
  }

  private static Validation<Seq<PParseError>, PTermReferencePath> requireEmptyPath(
    final SExpressionType e,
    final PTermReferencePath path)
  {
    if (path.path().isEmpty()) {
      return Validation.valid(path);
    }

    return errorInvalidExpression(
      e, () -> PParseErrors.errorMessagePatternInvalidExpectedNoPath(e));
  }

  private static Validation<Seq<PParseError>, PTermReferencePath> requireEmptyPathAndUnit(
    final SExpressionType e,
    final PTermReferencePath path)
  {
    if (path.path().isEmpty() && !path.unit().isPresent()) {
      return Validation.valid(path);
    }

    return errorInvalidExpression(
      e, () -> PParseErrors.errorMessagePatternInvalidExpectedNoPathNoUnit(e));
  }

  private static Validation<Seq<PParseError>, PPatternType<PParsed>> parsePatternSymbol(
    final SExpressionSymbolType e)
  {
    final String text = e.text();

    if (Objects.equals(text, "_")) {
      return Validation.valid(PPatternWildcard.of(e.lexical(), parsed()));
    }

    if (PParsingIntegers.appearsToBeNumeric(text)) {
      if (PParsingReals.appearsToBeReal(text)) {
        return PParsingReals.parseReal(e)
          .map(value -> PPatternConstantReal.of(e.lexical(), parsed(), value));
      }
      return PParsingIntegers.parseInteger(e)
        .map(pair -> PPatternConstantInteger.of(
          e.lexical(),
          parsed(),
          pair._2.intValue(),
          pair._1));
    }

    return PParsingNames.parseTermPath(e)
      .flatMap(path -> requireEmptyPath(e, path))
      .map(path -> PPatternConstructor.of(
        e.lexical(),
        parsed(),
        path.unit(),
        path.base(),
        Optional.empty()));
  }

  private static Validation<Seq<PParseError>, PPatternType<PParsed>> parsePatternQuotedString(
    final SExpressionQuotedStringType e)
  {
    return Validation.valid(
      PPatternConstantString.of(e.lexical(), parsed(), e.text()));
  }
}
