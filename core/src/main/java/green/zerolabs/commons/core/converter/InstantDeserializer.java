package green.zerolabs.commons.core.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class InstantDeserializer extends StdDeserializer<Instant> {
  public InstantDeserializer() {
    super(Instant.class);
  }

  @Override
  public Instant deserialize(final JsonParser p, final DeserializationContext ctxt)
      throws IOException, JsonProcessingException {
    final String text = p.getText();
    return toInstant(text);
  }

  public static Instant toInstant(final String text) {
    try {
      return Instant.parse(text);
    } catch (final DateTimeParseException e) {
      return LocalDate.from(DateTimeFormatter.ISO_DATE.parse(text))
          .atStartOfDay()
          .toInstant(ZoneOffset.UTC);
    }
  }
}
