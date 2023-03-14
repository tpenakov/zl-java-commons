package green.zerolabs.commons.core.converter;

import com.fasterxml.jackson.core.JsonParser;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/***
 * Created by Triphon Penakov 2022-11-14
 */
@Getter(AccessLevel.PACKAGE)
class InstantDeserializerTest {

  InstantDeserializer deserializer;
  JsonParser jsonParser;

  @BeforeEach
  void beforeEach() {
    deserializer = new InstantDeserializer();
    jsonParser = spy(JsonParser.class);
  }

  @Test
  void deserializeDateOnlyTest() throws IOException {
    final String dateString = "2022-11-14";
    doReturn(dateString).when(getJsonParser()).getText();

    final Instant instant = getDeserializer().deserialize(getJsonParser(), null);
    assertNotNull(instant);
    assertTrue(instant.toString().startsWith(dateString));
  }

  @Test
  void deserializeDateTimeTest() throws IOException {
    final Instant now = Instant.now();
    doReturn(now.toString()).when(getJsonParser()).getText();

    final Instant instant = getDeserializer().deserialize(getJsonParser(), null);
    assertNotNull(instant);
    assertEquals(now, instant);
  }
}
