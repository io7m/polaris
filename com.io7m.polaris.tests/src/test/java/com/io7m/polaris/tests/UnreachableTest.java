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
import com.io7m.polaris.ast.PPackageNames;
import com.io7m.polaris.ast.PTermConstructorNames;
import com.io7m.polaris.ast.PTermVariableNames;
import com.io7m.polaris.ast.PTypeConstructorNames;
import com.io7m.polaris.ast.PUnitNames;
import com.io7m.polaris.parser.implementation.PParsing;
import com.io7m.polaris.parser.implementation.PParsingFunctions;
import com.io7m.polaris.parser.implementation.PParsingIntegers;
import com.io7m.polaris.parser.implementation.PParsingNames;
import com.io7m.polaris.parser.implementation.PParsingPatterns;
import com.io7m.polaris.parser.implementation.PParsingReals;
import com.io7m.polaris.parser.implementation.PParsingRecords;
import com.io7m.polaris.parser.implementation.PParsingTypeExpressions;
import com.io7m.polaris.parser.implementation.PParsingUnits;
import com.io7m.polaris.parser.implementation.PParsingValues;
import com.io7m.polaris.parser.implementation.PParsingVariants;
import com.io7m.polaris.parser.implementation.PValidation;
import com.io7m.polaris.parser.implementation.PVectors;
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
        PParsingNames.class,
        PParsingRecords.class,
        PParsingTypeExpressions.class,
        PParsingFunctions.class,
        PParsingValues.class,
        PParsingPatterns.class,
        PParsingIntegers.class,
        PParsingReals.class,
        PParsingVariants.class,
        PParsingUnits.class,
        PParsing.class,
        PVectors.class,
        PValidation.class,
        PPackageNames.class,
        PTypeConstructorNames.class,
        PTermConstructorNames.class,
        PTermVariableNames.class,
        PUnitNames.class)
        .stream()
        .map(c -> (Executable) () -> checkUnreachable(c)));
  }
}
