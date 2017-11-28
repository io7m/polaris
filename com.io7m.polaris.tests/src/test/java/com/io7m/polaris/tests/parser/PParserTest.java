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

package com.io7m.polaris.tests.parser;

import com.io7m.jsx.lexer.JSXLexerSupplier;
import com.io7m.jsx.parser.JSXParserSupplier;
import com.io7m.polaris.parser.PParsers;
import com.io7m.polaris.parser.api.PParserProviderType;
import com.io7m.polaris.parser.api.PParserType;
import com.io7m.polaris.tests.parser.api.PParserContractType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public final class PParserTest implements PParserContractType
{
  @Override
  public PParserType parserForString(
    final String text)
  {
    final PParserProviderType parsers =
      PParsers.createWith(new JSXParserSupplier(), new JSXLexerSupplier());
    return parsers.create(
      URI.create("urn:test"),
      new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
  }

  @Override
  public Logger log()
  {
    return LoggerFactory.getLogger(PParserTest.class);
  }
}
