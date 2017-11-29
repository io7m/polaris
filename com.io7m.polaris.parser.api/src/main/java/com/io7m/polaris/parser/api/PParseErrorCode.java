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

/**
 * Parsing error codes.
 */

public enum PParseErrorCode
{
  /**
   * The given pattern is invalid.
   */

  INVALID_PATTERN,

  /**
   * The given pattern is invalid - a constructor reference was expected.
   */

  INVALID_PATTERN_EXPECTED_CONSTRUCTOR_REFERENCE,

  /**
   * The given pattern is invalid - expected an unqualified name as a
   * constructor argument.
   */

  INVALID_PATTERN_EXPECTED_CONSTRUCTOR_UNQUALIIFIED_ARGUMENT_NAME,

  /**
   * The given local expression is invalid.
   */

  INVALID_LOCAL,

  /**
   * The given match expression is invalid.
   */

  INVALID_MATCH,

  /**
   * The given match case is invalid.
   */

  INVALID_MATCH_CASE,

  /**
   * The given lambda expression is invalid.
   */

  INVALID_LAMBDA,

  /**
   * The given function declaration is invalid.
   */

  INVALID_FUNCTION,

  /**
   * The given value declaration is invalid.
   */

  INVALID_VALUE,

  /**
   * The given real number is invalid.
   */

  INVALID_REAL,

  /**
   * The given integer is invalid.
   */

  INVALID_INTEGER,

  /**
   * The given term name is invalid.
   */

  INVALID_TERM_NAME,

  /**
   * The given type name is invalid.
   */

  INVALID_TYPE_NAME,

  /**
   * The given unit name is invalid.
   */

  INVALID_UNIT_NAME,

  /**
   * The given constructor name is invalid.
   */

  INVALID_CONSTRUCTOR_NAME,

  /**
   * The given record declaration is invalid.
   */

  INVALID_RECORD,

  /**
   * The given record declaration is invalid due to having duplicate field
   * names.
   */

  INVALID_RECORD_DUPLICATE_FIELD,

  /**
   * The given record declaration is invalid due to having an invalid field.
   */

  INVALID_RECORD_FIELD,

  /**
   * The given record type parameter declaration is invalid.
   */

  INVALID_RECORD_TYPE_PARAMETERS,

  /**
   * The given variant declaration is invalid.
   */

  INVALID_VARIANT,

  /**
   * The given variant declaration is invalid due to having duplicate field
   * names.
   */

  INVALID_VARIANT_DUPLICATE_CASE,

  /**
   * The given variant declaration is invalid due to having an invalid case.
   */

  INVALID_VARIANT_CASE,

  /**
   * The given variant type parameter declaration is invalid.
   */

  INVALID_VARIANT_TYPE_PARAMETERS,

  /**
   * The given function application is invalid.
   */

  INVALID_APPLICATION,

  /**
   * The given type expression is invalid.
   */

  INVALID_TYPE_EXPRESSION,

  /**
   * The given type arrow expression is invalid.
   */

  INVALID_TYPE_EXPRESSION_ARROW,

  /**
   * Only the last parameter of an arrow type is allowed to be variadic.
   */

  INVALID_TYPE_EXPRESSION_UNEXPECTED_VARIADIC,

  /**
   * The given variadic type expression is invalid.
   */

  INVALID_TYPE_EXPRESSION_VARIADIC,

  /**
   * The given type forall expression is invalid.
   */

  INVALID_TYPE_EXPRESSION_FORALL,

  /**
   * The given type forall expression is invalid due to a duplicate parameter
   * name.
   */

  INVALID_TYPE_EXPRESSION_FORALL_DUPLICATE_NAME,

  /**
   * The given text could not be parsed as an S-expression.
   */

  INVALID_S_EXPRESSION,

  /**
   * The given term reference is invalid.
   */

  INVALID_TERM_REFERENCE,

  /**
   * The given type reference is invalid.
   */

  INVALID_TYPE_REFERENCE,

  /**
   * The given constructor reference is invalid.
   */

  INVALID_CONSTRUCTOR_REFERENCE,

  /**
   * Expected a term name but got some other kind of expression.
   */

  EXPECTED_TERM_NAME_UNQUALIFIED_GOT_EXPRESSION,

  /**
   * Expected a type name but got some other kind of expression.
   */

  EXPECTED_TYPE_NAME_UNQUALIFIED_GOT_EXPRESSION,

  /**
   * Expected a term reference but got some other kind of expression.
   */

  EXPECTED_TERM_REFERENCE_GOT_EXPRESSION,

  /**
   * Expected an expression but got a declaration.
   */

  EXPECTED_EXPRESSION_BUT_GOT_DECLARATION,

  /**
   * Expected a keyword but got something else.
   */

  EXPECTED_KEYWORD,

  /**
   * Expected a type reference but got some other kind of expression.
   */

  EXPECTED_TYPE_REFERENCE_GOT_EXPRESSION
}
