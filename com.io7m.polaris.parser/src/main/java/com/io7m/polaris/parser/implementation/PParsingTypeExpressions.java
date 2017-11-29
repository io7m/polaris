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
import com.io7m.jsx.SExpressionMatcherType;
import com.io7m.jsx.SExpressionQuotedStringType;
import com.io7m.jsx.SExpressionSymbolType;
import com.io7m.jsx.SExpressionType;
import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.model.PTypeExprApplication;
import com.io7m.polaris.model.PTypeExprArrow;
import com.io7m.polaris.model.PTypeExprForAll;
import com.io7m.polaris.model.PTypeExprReference;
import com.io7m.polaris.model.PTypeExpressionType;
import com.io7m.polaris.model.PTypeName;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.collection.HashMap;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.util.Objects;

import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TYPE_EXPRESSION;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TYPE_EXPRESSION_ARROW;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TYPE_EXPRESSION_FORALL;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TYPE_EXPRESSION_FORALL_DUPLICATE_NAME;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TYPE_EXPRESSION_UNEXPECTED_VARIADIC;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TYPE_EXPRESSION_VARIADIC;
import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PValidation.cast;
import static com.io7m.polaris.parser.implementation.PValidation.errorsFlatten;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;
import static com.io7m.polaris.parser.implementation.PValidation.sequence;

/**
 * Functions to parse type expressions.
 */

public final class PParsingTypeExpressions
{
  private PParsingTypeExpressions()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as a type expression.
   *
   * @param m An error message provider
   * @param e The expression
   *
   * @return A parsed type expression, or a sequence of errors
   */

  public static Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> parseTypeExpression(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    Objects.requireNonNull(e, "Expression");

    return e.matchExpression(
      new SExpressionMatcherType<Validation<Seq<PParseError>, PTypeExpressionType<PParsed>>, RuntimeException>()
      {
        @Override
        public Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> list(
          final SExpressionListType el)
        {
          return parseTypeExpressionList(m, el);
        }

        @Override
        public Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> quotedString(
          final SExpressionQuotedStringType eq)
        {
          return parseTypeExpressionQuotedString(m, eq);
        }

        @Override
        public Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> symbol(
          final SExpressionSymbolType es)
        {
          return parseTypeExpressionSymbol(m, es);
        }
      });
  }

  private static Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> parseTypeExpressionSymbol(
    final PParseErrorMessagesType m,
    final SExpressionSymbolType es)
  {
    return PParsingNames.parseTypeReference(m, es)
      .map(ref -> PTypeExprReference.of(
        es.lexical(), parsed(), ref.unit(), ref.base()));
  }

  private static Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> parseTypeExpressionQuotedString(
    final PParseErrorMessagesType m,
    final SExpressionQuotedStringType eq)
  {
    return invalid(m.errorExpression(INVALID_TYPE_EXPRESSION, eq));
  }

  private static Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> parseTypeExpressionList(
    final PParseErrorMessagesType m,
    final SExpressionListType el)
  {
    /*
     * Empty applications are always syntactically invalid.
     */

    if (el.size() == 0) {
      return invalid(m.errorExpression(INVALID_TYPE_EXPRESSION, el));
    }

    /*
     * Check the first symbol to see if this expression is a special form.
     */

    if (el.get(0) instanceof SExpressionSymbolType) {
      final SExpressionSymbolType sym = (SExpressionSymbolType) el.get(0);
      switch (sym.text()) {

        case "for-all": {
          return cast(parseTypeExpressionForAll(m, el));
        }
        case "∀": {
          return cast(parseTypeExpressionForAll(m, el));
        }

        case "->": {
          return cast(parseTypeExpressionArrow(m, el));
        }
        case "→": {
          return cast(parseTypeExpressionArrow(m, el));
        }

        case "variadic": {
          return invalid(m.errorExpression(
            INVALID_TYPE_EXPRESSION_UNEXPECTED_VARIADIC, el));
        }
        case "…": {
          return invalid(m.errorExpression(
            INVALID_TYPE_EXPRESSION_UNEXPECTED_VARIADIC, el));
        }

        default: {
          break;
        }
      }
    }

    Preconditions.checkPreconditionI(
      el.size(),
      el.size() > 0,
      c -> "Expression size must be > 0");

    return sequence(el, se -> parseTypeExpression(m, se))
      .flatMap(es -> Validation.valid(
        PTypeExprApplication.of(el.lexical(), parsed(), es.head(), es.tail())));
  }

  private static Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> parseTypeExpressionArrow(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    final SExpressionType e_keyword = e.get(0);

    Preconditions.checkPrecondition(
      e_keyword,
      e_keyword instanceof SExpressionSymbolType,
      c -> "Arrow expression must begin with -> or →");

    /*
     * An arrow expression must be at least an arrow and one argument.
     */

    if (e.size() >= 2) {
      final Vector<SExpressionType> es = Vector.ofAll(e).tail();
      final SExpressionType es_return = es.last();
      final Vector<SExpressionType> es_params = es.init();

      if (es_params.isEmpty()) {
        return parseTypeExpression(m, es_return)
          .map(t_return -> PTypeExprArrow.of(
            e.lexical(), parsed(), Vector.empty(), false, t_return));
      }

      /*
       * If there's more than one argument to the arrow, then the second-to-last
       * argument is allowed to be variadic.
       */

      final Vector<SExpressionType> es_init = es_params.init();
      final SExpressionType es_last = es_params.last();

      final Validation<Seq<PParseError>, Vector<PTypeExpressionType<PParsed>>> r_params =
        sequence(es_init, ex -> parseTypeExpression(m, ex));
      final Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> r_return =
        parseTypeExpression(m, es_return);

      /*
       * If the last parameter looks variadic, then parse it as if it is.
       */

      if (looksVariadic(es_last)) {
        return makeArrow(
          e, r_params, r_return, parseVariadicTypeExpression(m, es_last), true);
      }

      return makeArrow(
        e, r_params, r_return, parseTypeExpression(m, es_last), false);
    }

    return invalid(m.errorExpression(INVALID_TYPE_EXPRESSION_ARROW, e));
  }

  private static Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> makeArrow(
    final SExpressionListType e,
    final Validation<Seq<PParseError>, Vector<PTypeExpressionType<PParsed>>> r_params,
    final Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> r_return,
    final Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> r_last,
    final boolean var)
  {
    final Validation<Seq<Seq<PParseError>>, PTypeExpressionType<PParsed>> r_result =
      Validation.combine(r_params, r_last, r_return).ap(
        (params, last, t_return) ->
          PTypeExprArrow.of(
            e.lexical(),
            parsed(),
            params.append(last),
            var,
            t_return));

    return errorsFlatten(r_result);
  }

  private static Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> parseVariadicTypeExpression(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    if (e instanceof SExpressionListType) {
      final SExpressionListType es = (SExpressionListType) e;
      if (es.size() == 2) {
        final SExpressionType e_key = es.get(0);
        if (e_key instanceof SExpressionSymbolType) {
          if (symbolIsVariadic((SExpressionSymbolType) e_key)) {
            return parseTypeExpression(m, es.get(1));
          }
        }
      }
    }

    return invalid(m.errorExpression(INVALID_TYPE_EXPRESSION_VARIADIC, e));
  }

  private static boolean symbolIsVariadic(
    final SExpressionSymbolType e)
  {
    return Objects.equals(e.text(), "variadic")
      || Objects.equals(e.text(), "…");
  }

  private static boolean looksVariadic(
    final SExpressionType e)
  {
    if (e instanceof SExpressionListType) {
      final SExpressionListType es = (SExpressionListType) e;
      if (es.size() >= 2) {
        final SExpressionType e_key = es.get(0);
        if (e_key instanceof SExpressionSymbolType) {
          return symbolIsVariadic((SExpressionSymbolType) e_key);
        }
      }
    }
    return false;
  }

  private static Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> parseTypeExpressionForAll(
    final PParseErrorMessagesType m,
    final SExpressionListType e)
  {
    Preconditions.checkPreconditionI(
      e.size(),
      e.size() > 0,
      c -> "Expression size must be > 0");

    final SExpressionType e_keyword = e.get(0);

    Preconditions.checkPrecondition(
      e_keyword,
      e_keyword instanceof SExpressionSymbolType,
      c -> "Forall expression must begin with for-all or ∀");

    if (e.size() >= 3) {
      final Vector<SExpressionType> es = Vector.ofAll(e).tail();
      final Vector<SExpressionType> e_params = es.init();
      final SExpressionType e_last = es.last();

      final Validation<Seq<PParseError>, Vector<PTypeName<PParsed>>> r_params =
        sequence(e_params, n -> PParsingNames.parseTypeNameUnqualified(m, n));
      final Validation<Seq<PParseError>, Vector<PTypeName<PParsed>>> r_params_unique =
        r_params.flatMap(params -> requireUnique(m, params));
      final Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> r_last =
        parseTypeExpression(m, e_last);

      final Validation<Seq<Seq<PParseError>>, PTypeExpressionType<PParsed>> r_result =
        Validation.combine(r_params_unique, r_last)
          .ap((t_params, t_last) ->
                PTypeExprForAll.of(
                  e.lexical(),
                  parsed(),
                  PVectors.vectorCast(t_params),
                  t_last));

      return errorsFlatten(r_result);
    }

    return invalid(m.errorExpression(INVALID_TYPE_EXPRESSION_FORALL, e));
  }

  private static Validation<Seq<PParseError>, Vector<PTypeName<PParsed>>> requireUnique(
    final PParseErrorMessagesType messages,
    final Vector<PTypeName<PParsed>> params)
  {
    HashMap<PTypeName<PParsed>, Integer> m = HashMap.empty();
    for (int index = 0; index < params.size(); ++index) {
      final PTypeName<PParsed> name = params.get(index);
      final Integer next =
        Integer.valueOf(m.getOrElse(name, Integer.valueOf(0)).intValue() + 1);
      m = m.put(name, next);
    }

    final HashMap<PTypeName<PParsed>, Integer> duplicates =
      m.filter((name, count) -> count.intValue() > 1);

    if (duplicates.isEmpty()) {
      return Validation.valid(params);
    }

    return Validation.invalid(duplicates.map(
      pair -> messages.errorLexical(
        INVALID_TYPE_EXPRESSION_FORALL_DUPLICATE_NAME,
        pair._1.lexical(),
        pair._1.value())));
  }
}
