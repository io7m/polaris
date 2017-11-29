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
import com.io7m.polaris.model.PTypeName;
import com.io7m.polaris.model.PTypeReference;
import com.io7m.polaris.model.PTypeReferenceType;
import com.io7m.polaris.model.PUnitName;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParsed;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

import static com.io7m.polaris.parser.api.PParseErrorCode.INVALID_TYPE_REFERENCE;
import static com.io7m.polaris.parser.api.PParsed.parsed;
import static com.io7m.polaris.parser.implementation.PValidation.errorsFlatten;
import static com.io7m.polaris.parser.implementation.PValidation.invalid;

/**
 * Functions for parsing type references.
 */

public final class PParsingTypeReferences
{
  private PParsingTypeReferences()
  {
    throw new UnreachableCodeException();
  }

  /**
   * Parse the given expression as a type reference.
   *
   * @param m An error message provider
   * @param e The input expression
   *
   * @return An unqualified term name, or a list of parse errors
   */

  public static Validation<Seq<PParseError>, PTypeReferenceType<PParsed>>
  parseTypeReference(
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

        final Validation<Seq<PParseError>, PUnitName<PParsed>> r_unit =
          PParsingNames.parseUnitNameRaw(m, lex_before, text_before);
        final Validation<Seq<PParseError>, PTypeName<PParsed>> r_type =
          PParsingNames.parseTypeNameRaw(m, lex_after, text_after);
        final Validation<Seq<Seq<PParseError>>, PTypeReferenceType<PParsed>> r_result =
          Validation.combine(r_unit, r_type)
            .ap((t_unit, t_type) -> PTypeReference.of(
              lex_before, parsed(), Optional.of(t_unit), t_type));

        return errorsFlatten(r_result);
      }

      return PParsingNames.parseTypeName(m, e).map(
        name -> PTypeReference.of(
          lex_before, parsed(), Optional.empty(), name));
    }

    return invalid(m.errorExpression(INVALID_TYPE_REFERENCE, e));
  }
}
