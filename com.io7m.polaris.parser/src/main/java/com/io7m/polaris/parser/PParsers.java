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

package com.io7m.polaris.parser;

import com.io7m.jsx.SExpressionType;
import com.io7m.jsx.api.lexer.JSXLexerComment;
import com.io7m.jsx.api.lexer.JSXLexerConfiguration;
import com.io7m.jsx.api.lexer.JSXLexerSupplierType;
import com.io7m.jsx.api.parser.JSXParserConfiguration;
import com.io7m.jsx.api.parser.JSXParserException;
import com.io7m.jsx.api.parser.JSXParserSupplierType;
import com.io7m.jsx.api.parser.JSXParserType;
import com.io7m.polaris.model.PExpressionOrDeclarationType;
import com.io7m.polaris.model.PPatternType;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParseErrorCode;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;
import com.io7m.polaris.parser.api.PParseErrorType;
import com.io7m.polaris.parser.api.PParsed;
import com.io7m.polaris.parser.api.PParserProviderType;
import com.io7m.polaris.parser.api.PParserType;
import com.io7m.polaris.parser.implementation.PParseErrorMessagesProvider;
import com.io7m.polaris.parser.implementation.PParsing;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

/**
 * The default parser implementation.
 */

public final class PParsers implements PParserProviderType
{
  private final JSXParserSupplierType sexpr_parsers;
  private final JSXLexerSupplierType sexpr_lexers;
  private final PParseErrorMessagesProvider error_messages;

  private PParsers(
    final JSXParserSupplierType in_sexpr_parsers,
    final JSXLexerSupplierType in_sexpr_lexers)
  {
    this.sexpr_parsers =
      Objects.requireNonNull(in_sexpr_parsers, "Parsers");
    this.sexpr_lexers =
      Objects.requireNonNull(in_sexpr_lexers, "Lexers");
    this.error_messages =
      new PParseErrorMessagesProvider();
  }

  /**
   * Create a parser provider, looking up dependencies from {@link
   * ServiceLoader}. This method is intended to be called from {@link
   * ServiceLoader}.
   *
   * @return A new parser provider
   */

  public static PParserProviderType provider()
  {
    final JSXParserSupplierType p_provider =
      ServiceLoader.load(JSXParserSupplierType.class).findFirst().orElseThrow(
        () -> {
          throw new ServiceConfigurationError(
            "No providers available for "
              + JSXParserSupplierType.class.getCanonicalName());
        });
    final JSXLexerSupplierType l_provider =
      ServiceLoader.load(JSXLexerSupplierType.class).findFirst().orElseThrow(
        () -> {
          throw new ServiceConfigurationError(
            "No providers available for "
              + JSXLexerSupplierType.class.getCanonicalName());
        });

    return new PParsers(p_provider, l_provider);
  }

  /**
   * Create a parser provider.
   *
   * @param in_sexpr_lexers  An s-expression lexer provider
   * @param in_sexpr_parsers An s-expression parser provider
   *
   * @return A new parser provider
   */

  public static PParserProviderType createWith(
    final JSXParserSupplierType in_sexpr_parsers,
    final JSXLexerSupplierType in_sexpr_lexers)
  {
    return new PParsers(in_sexpr_parsers, in_sexpr_lexers);
  }

  @Override
  public PParserType create(
    final URI uri,
    final InputStream stream)
  {
    Objects.requireNonNull(uri, "URI");
    Objects.requireNonNull(stream, "Stream");
    return new PParser(
      this.error_messages.create(),
      uri, stream,
      this.createSExpressionParser(uri, stream));
  }

  @Override
  public PParserType createWithErrors(
    final PParseErrorMessagesType errors,
    final URI uri,
    final InputStream stream)
  {
    Objects.requireNonNull(errors, "Errors");
    Objects.requireNonNull(uri, "URI");
    Objects.requireNonNull(stream, "Stream");

    return new PParser(
      errors,
      uri, stream,
      this.createSExpressionParser(uri, stream));
  }

  @Override
  public JSXParserType createSExpressionParser(
    final URI uri,
    final InputStream stream)
  {
    Objects.requireNonNull(uri, "URI");
    Objects.requireNonNull(stream, "Stream");

    final JSXLexerConfiguration lc =
      JSXLexerConfiguration.builder()
        .setStartAtLine(1)
        .setSquareBrackets(true)
        .setComments(EnumSet.of(JSXLexerComment.COMMENT_SEMICOLON))
        .setNewlinesInQuotedStrings(true)
        .setFile(uri)
        .build();

    final JSXParserConfiguration pc =
      JSXParserConfiguration.builder()
        .setPreserveLexical(true)
        .build();

    return this.sexpr_parsers.createFromStreamUTF8(
      pc, lc, this.sexpr_lexers, stream);
  }

  private static final class PParser implements PParserType
  {
    private final URI uri;
    private final InputStream stream;
    private final JSXParserType parser;
    private final PParseErrorMessagesType errors;

    PParser(
      final PParseErrorMessagesType in_errors,
      final URI in_uri,
      final InputStream in_stream,
      final JSXParserType in_parser)
    {
      this.errors = Objects.requireNonNull(in_errors, "Errors");
      this.uri = Objects.requireNonNull(in_uri, "URI");
      this.stream = Objects.requireNonNull(in_stream, "Stream");
      this.parser = Objects.requireNonNull(in_parser, "Parser");
    }

    @Override
    public void close()
      throws IOException
    {

    }

    @Override
    public Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>>
    parseExpressionOrDeclaration()
      throws IOException
    {
      try {
        final Optional<SExpressionType> opt = this.parser.parseExpressionOrEOF();
        if (opt.isPresent()) {
          final SExpressionType expr = opt.get();
          return PParsing.parseExpressionOrDeclaration(this.errors, expr)
            .map(Optional::of);
        }
        return Validation.valid(Optional.empty());
      } catch (final JSXParserException e) {
        return Validation.invalid(
          Vector.of(PParseError.builder()
                      .setException(e)
                      .setCode(PParseErrorCode.INVALID_S_EXPRESSION)
                      .setLexical(e.getLexicalInformation())
                      .setMessage(e.getMessage())
                      .setSeverity(PParseErrorType.Severity.ERROR)
                      .build()));
      }
    }

    @Override
    public Validation<Seq<PParseError>, Optional<PPatternType<PParsed>>> parsePattern()
      throws IOException
    {
      try {
        final Optional<SExpressionType> opt = this.parser.parseExpressionOrEOF();
        if (opt.isPresent()) {
          final SExpressionType expr = opt.get();
          return PParsing.parsePattern(this.errors, expr).map(Optional::of);
        }
        return Validation.valid(Optional.empty());
      } catch (final JSXParserException e) {
        return Validation.invalid(
          Vector.of(PParseError.builder()
                      .setException(e)
                      .setCode(PParseErrorCode.INVALID_S_EXPRESSION)
                      .setLexical(e.getLexicalInformation())
                      .setMessage(e.getMessage())
                      .setSeverity(PParseErrorType.Severity.ERROR)
                      .build()));
      }
    }
  }
}
