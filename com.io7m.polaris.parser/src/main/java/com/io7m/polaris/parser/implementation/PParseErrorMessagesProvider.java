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

import com.io7m.polaris.parser.api.PParseErrorMessagesProviderType;
import com.io7m.polaris.parser.api.PParseErrorMessagesType;

import java.util.Locale;
import java.util.Objects;

/**
 * An error message dictionary provider.
 */

public final class PParseErrorMessagesProvider
  implements PParseErrorMessagesProviderType
{
  /**
   * Instantiate a provider.
   */

  public PParseErrorMessagesProvider()
  {

  }

  @Override
  public PParseErrorMessagesType createWithLocale(
    final Locale locale)
  {
    Objects.requireNonNull(locale, "Locale");
    return PParseErrorMessages.createWithLocale(locale);
  }
}
