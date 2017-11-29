package com.io7m.polaris.tests.parser;

import com.io7m.jsx.lexer.JSXLexerSupplier;
import com.io7m.jsx.parser.JSXParserSupplier;
import com.io7m.polaris.ast.PExpressionOrDeclarationType;
import com.io7m.polaris.parser.PParsers;
import com.io7m.polaris.parser.api.PParseError;
import com.io7m.polaris.parser.api.PParsed;
import com.io7m.polaris.parser.api.PParserProviderType;
import com.io7m.polaris.parser.api.PParserType;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;

public final class PParserDemo
{
  private static final Logger LOG = LoggerFactory.getLogger(PParserDemo.class);

  private PParserDemo()
  {

  }

  public static void main(
    final String[] args)
    throws IOException
  {
    final PParserProviderType parsers =
      PParsers.createWith(new JSXParserSupplier(), new JSXLexerSupplier());

    try (PParserType parser =
           parsers.create(URI.create("urn:stdin"), System.in)) {
      while (true) {
        try {
          final Validation<Seq<PParseError>, Optional<PExpressionOrDeclarationType<PParsed>>> result =
            parser.parseExpressionOrDeclaration();

          if (result.isValid()) {
            LOG.debug("{}", result.get());

            if (!result.get().isPresent()) {
              return;
            }
          } else {
            result.getError().forEach(error -> LOG.error("{}", error));
          }
        } catch (final Exception e) {
          LOG.error("error: ", e);
        }
      }
    }
  }
}
