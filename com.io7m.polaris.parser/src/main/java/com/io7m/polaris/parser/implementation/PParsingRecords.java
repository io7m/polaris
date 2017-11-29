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
import com.io7m.polaris.model.PDeclarationRecord;
import com.io7m.polaris.model.PRecordField;
import com.io7m.polaris.model.PTermName;
import com.io7m.polaris.model.PTermNameType;
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

import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_RECORD;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_RECORD_DUPLICATE_FIELD;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_RECORD_FIELD;
import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_RECORD_TYPE_PARAMETERS;
import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PValidation.errorsFlatten;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;
import static com.io7m.polaris.parser.implementation.PValidation.sequence;

/**
 * Functions to parse record declarations.
 */

public final class PParsingRecords
{
  private PParsingRecords()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as a record declaration.
   *
   * @param m An error message provider
   * @param e The expression
   *
   * @return A value and radix, or a sequence of parse errors
   */

  public static Validation<Seq<PParseError>, PDeclarationRecord<PParsed>> parseRecord(
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
      c -> "Record declaration must begin with record keyword");

    if (e.size() >= 3) {
      final Validation<Seq<PParseError>, PTypeName<PParsed>> r_name =
        PParsingNames.parseTypeName(m, e.get(1));
      final Validation<Seq<PParseError>, RecordParameters> r_rest =
        parseForAllAndFields(m, Vector.ofAll(e).tail().tail());

      final Validation<Seq<Seq<PParseError>>, PDeclarationRecord<PParsed>> r_result =
        Validation.combine(r_name, r_rest)
          .ap((name, params) ->
                PDeclarationRecord.of(
                  e.lexical(),
                  parsed(),
                  name,
                  PVectors.vectorCast(params.parameters),
                  PVectors.vectorCast(params.fields)));

      return errorsFlatten(r_result);
    }

    return invalid(m.errorExpression(INVALID_RECORD, e));
  }

  private static Validation<Seq<PParseError>, RecordParameters> parseForAllAndFields(
    final PParseErrorMessagesType messages,
    final Vector<SExpressionType> exprs)
  {
    if (hasForAll(exprs)) {
      final Validation<Seq<PParseError>, Vector<PTypeName<PParsed>>> r_forall =
        parseForAll(messages, exprs.get(0));
      final Validation<Seq<PParseError>, Vector<PRecordField<PParsed>>> r_fields =
        parseFields(messages, exprs.tail());
      final Validation<Seq<Seq<PParseError>>, RecordParameters> r_result =
        Validation.combine(r_forall, r_fields).ap(RecordParameters::new);
      return errorsFlatten(r_result);
    }

    return parseFields(messages, exprs)
      .map(fields -> new RecordParameters(Vector.empty(), fields));
  }

  private static Validation<Seq<PParseError>, Vector<PRecordField<PParsed>>> parseFields(
    final PParseErrorMessagesType m,
    final Vector<SExpressionType> exprs)
  {
    return sequence(exprs, f -> parseField(m, f))
      .flatMap(fields -> requireFieldsUnique(m, fields));
  }

  private static Validation<Seq<PParseError>, Vector<PRecordField<PParsed>>> requireFieldsUnique(
    final PParseErrorMessagesType messages,
    final Vector<PRecordField<PParsed>> fields)
  {
    HashMap<PTermNameType<PParsed>, Integer> m = HashMap.empty();
    for (int index = 0; index < fields.size(); ++index) {
      final PRecordField<PParsed> field = fields.get(index);
      final PTermNameType<PParsed> name = field.name();
      final Integer next =
        Integer.valueOf(m.getOrElse(name, Integer.valueOf(0)).intValue() + 1);
      m = m.put(name, next);
    }

    final HashMap<PTermNameType<PParsed>, Integer> duplicates =
      m.filter((name, count) -> count.intValue() > 1);

    if (duplicates.isEmpty()) {
      return Validation.valid(fields);
    }

    return Validation.invalid(duplicates.map(
      pair -> messages.errorLexical(
        INVALID_RECORD_DUPLICATE_FIELD, pair._1.lexical(), pair._1.value())));
  }

  private static Validation<Seq<PParseError>, PRecordField<PParsed>> parseField(
    final PParseErrorMessagesType m,
    final SExpressionType ex)
  {
    if (ex instanceof SExpressionListType) {
      final SExpressionListType exs = (SExpressionListType) ex;
      if (exs.size() == 3) {
        final Validation<Seq<PParseError>, String> r_keyword =
          PParsingNames.parseKeyword(m, exs.get(0), "field");
        final Validation<Seq<PParseError>, PTermName<PParsed>> r_name =
          PParsingNames.parseTermName(m, exs.get(1));
        final Validation<Seq<PParseError>, PTypeExpressionType<PParsed>> r_expr =
          PParsingTypeExpressions.parseTypeExpression(m, exs.get(2));
        final Validation<Seq<Seq<PParseError>>, PRecordField<PParsed>> r_result =
          Validation.combine(r_keyword, r_name, r_expr)
            .ap((keyword, name, expr) ->
                  PRecordField.of(exs.lexical(), parsed(), name, expr));
        return errorsFlatten(r_result);
      }
    }

    return invalid(m.errorExpression(INVALID_RECORD_FIELD, ex));
  }

  private static Validation<Seq<PParseError>, Vector<PTypeName<PParsed>>> parseForAll(
    final PParseErrorMessagesType m,
    final SExpressionType e)
  {
    if (e instanceof SExpressionListType) {
      final SExpressionListType es = (SExpressionListType) e;
      final SExpressionSymbolType keyword = (SExpressionSymbolType) es.get(0);

      Preconditions.checkPrecondition(
        keyword,
        Objects.equals(keyword.text(), "for-all")
          || Objects.equals(keyword.text(), "∀"),
        k -> "Expecting a for-all");

      return sequence(
        Vector.ofAll(es).tail(),
        name -> PParsingNames.parseTypeName(m, name));
    }

    return invalid(m.errorExpression(INVALID_RECORD_TYPE_PARAMETERS, e));
  }

  private static boolean hasForAll(
    final Vector<SExpressionType> exprs)
  {
    Preconditions.checkPrecondition(
      exprs,
      exprs.size() > 0,
      e -> "Must be non-empty list of expressions");

    final SExpressionType ex = exprs.get(0);
    if (ex instanceof SExpressionListType) {
      final SExpressionListType exs = (SExpressionListType) ex;
      if (exs.size() > 0) {
        final SExpressionType ex_sym = exs.get(0);
        if (ex_sym instanceof SExpressionSymbolType) {
          final SExpressionSymbolType ex_syms = (SExpressionSymbolType) ex_sym;
          final String t = ex_syms.text();
          return Objects.equals(t, "∀") || Objects.equals(t, "for-all");
        }
      }
    }

    return false;
  }

  private static final class RecordParameters
  {
    private final Vector<PTypeName<PParsed>> parameters;
    private final Vector<PRecordField<PParsed>> fields;

    RecordParameters(
      final Vector<PTypeName<PParsed>> in_parameters,
      final Vector<PRecordField<PParsed>> in_fields)
    {
      this.parameters =
        Objects.requireNonNull(in_parameters, "Parameters");
      this.fields =
        Objects.requireNonNull(in_fields, "Fields");
    }
  }
}
