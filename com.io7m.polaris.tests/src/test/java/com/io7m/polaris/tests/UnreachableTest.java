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

import com.io7m.junreachable.UnreachableCodeException;
import com.io7m.polaris.model.PPackageNames;
import com.io7m.polaris.model.PTermNames;
import com.io7m.polaris.model.PTypeNames;
import com.io7m.polaris.model.PUnitNames;
import com.io7m.polaris.parser.PParsing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public final class UnreachableTest
{
  private static void checkUnreachable(
    final Class<?> c)
  {
    Assertions.assertThrows(UnreachableCodeException.class, () -> {
      try {
        final Constructor<?> cc = c.getDeclaredConstructor();
        cc.setAccessible(true);
        cc.newInstance();
      } catch (final InvocationTargetException e) {
        throw e.getCause();
      }
    });
  }

  @Test
  public void testUnreachable()
  {
    Assertions.assertAll(
      List.of(
        PParsing.class,
        PPackageNames.class,
        PTermNames.class,
        PTypeNames.class,
        PUnitNames.class)
        .stream()
        .map(c -> (Executable) () -> checkUnreachable(c)));
  }
}
