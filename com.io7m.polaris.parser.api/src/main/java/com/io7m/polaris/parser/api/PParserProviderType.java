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

import com.io7m.jsx.api.parser.JSXParserType;

import java.io.InputStream;
import java.net.URI;

/**
 * The type of parser providers.
 */

public interface PParserProviderType
{
  /**
   * Create a parser.
   *
   * @param uri    The URI, for diagnostic messages
   * @param stream The input stream
   *
   * @return A parser
   */

  PParserType create(
    URI uri,
    InputStream stream);

  /**
   * Create a configured s-expression parser.
   *
   * @param uri    The URI, for diagnostic messages
   * @param stream The input stream
   *
   * @return A parser
   */

  JSXParserType createSExpressionParser(
    URI uri,
    InputStream stream);
}
