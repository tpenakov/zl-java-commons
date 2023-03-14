package green.zerolabs.commons.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Function;

/*
 * Created by triphon 19.03.22 г.
 */
@Getter
@Setter
@Slf4j
public class UnitTestUtils {
  public static final Function<InputStream, String> READ_TEXT_FILE_FN =
      Unchecked.<InputStream, String>function(s -> IOUtils.toString(s, StandardCharsets.UTF_8));
  final ObjectMapper objectMapper;
  final JsonUtils jsonUtils;
  final ConverterUtils converterUtils;

  protected UnitTestUtils() {
    objectMapper = new ObjectMapper();
    jsonUtils = new JsonUtils(objectMapper);
    converterUtils = new ConverterUtils();
  }

  public static UnitTestUtils of() {
    return new UnitTestUtils();
  }

  public void waitFor(final Duration duration) {
    Multi.createFrom()
        .deferred(() -> Multi.createFrom().ticks().every(duration))
        .skip()
        .first()
        .toUni()
        .onFailure()
        .retry()
        .indefinitely()
        .await()
        .indefinitely();
  }

  public <T> T readFromFile(final String path, final Class<T> clazz) {
    final String data =
        READ_TEXT_FILE_FN.apply(getClass().getClassLoader().getResourceAsStream(path));
    return getJsonUtils().readValue(data, clazz).get();
  }
}
