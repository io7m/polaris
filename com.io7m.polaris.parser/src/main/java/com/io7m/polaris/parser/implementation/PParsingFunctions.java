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
import com.io7m.jsx.SExpressionListType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.model.PDeclarationFunction;
import com.io7m.polaris.model.PExpressionType;
import com.io7m.polaris.model.PTermName;
import com.io7m.polaris.model.PTermNameType;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.util.Objects;
import java.util.function.Function;

import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_FUNCTION;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_FUNCTION_DUPLICATE_PARAMETER;
import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PValidation.errorsFlatten;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;
import static com.io7m.polaris.parser.implementation.PValidation.sequence;

/**
 * Functions to parse function declarations.
 */

public final class PParsingFunctions
{
  private PParsingFunctions()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as a function declaration.
   *
   * @param m An error message provider
   * @param e The expression
   *
   * @return A value and radix, or a sequence of parse errors
   */

  public static Validation<Seq<PParseError>, PDeclarationFunction<PParsed>> parseFunction(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    Objects.requireNonNull(m, "Messages");
    Objects.requireNonNull(e, "Expression");

    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    final SExpressionType e_keyword = e.get(0);

    Preconditions.checkPrecondition(
      e_keyword,
      e_keyword instanceof SExpressionSymbolType,
      c -> "Function declaration must begin with function keyword");

    if (e.size() == 4) {
      final SExpressionType e_name = e.get(1);
      final SExpressionType e_params = e.get(2);
      final SExpressionType e_body = e.get(3);

      if (e_params instanceof SExpressionListType) {
        final Iterable<SExpressionType> el_params =
          (SExpressionListType) e_params;

        final Validation<Seq<PParseError>, PTermName<PParsed>> r_name =
          PParsingNames.parseTermName(m, e_name);
        final Validation<Seq<PParseError>, Vector<PTermNameType<PParsed>>> r_params =
          sequence(el_params, x -> PParsingNames.parseTermName(m, x))
            .flatMap(names -> requireUniqueNames(m, names))
            .map(PVectors::vectorCast);

        final Validation<Seq<PParseError>, PExpressionType<PParsed>> r_body =
          PParsing.parseExpression(m, e_body);

        final Validation<Seq<Seq<PParseError>>, PDeclarationFunction<PParsed>> r_result =
          Validation.combine(r_name, r_params, r_body)
            .ap((name, params, body) ->
                  PDeclarationFunction.of(
                    e.lexical(), parsed(), name, params, body));

        return errorsFlatten(r_result);
      }
    }

    return invalid(m.errorExpression(INVALID_FUNCTION, e));
  }

  private static Validation<Seq<PParseError>, Vector<PTermName<PParsed>>> requireUniqueNames(
    final PParseErrorMessagesType m,
    final Vector<PTermName<PParsed>> names)
  {
    return PParsingNames.requireUniqueNames(
      names,
      Function.identity(),
      dups -> dups.map(dup -> m.errorLexical(
        INVALID_FUNCTION_DUPLICATE_PARAMETER, dup.lexical(), dup.value())));
  }
}
