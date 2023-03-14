package green.zerolabs.commons.core.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.time.Instant;

@Getter
@Setter
public class InstantSerializer extends StdSerializer<Instant> {

  public InstantSerializer() {
    super(Instant.class);
  }

  @Override
  public void serialize(
      final Instant value, final JsonGenerator gen, final SerializerProvider provider)
      throws IOException {
    gen.writeString(value.toString());
  }
}
