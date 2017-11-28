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

package com.io7m.polaris.tests;

import com.io7m.polaris.parser.api.PParseError;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.slf4j.Logger;

public final class PTestUtilities
{
  private PTestUtilities()
  {

  }

  public static <T> void dump(
    final Logger log,
    final Validation<Seq<PParseError>, T> r)
  {
    if (r.isValid()) {
      log.debug("valid: {}", r.get());
    } else {
      r.getError().forEach(e -> log.error("invalid: {}", e));
    }
  }
}
