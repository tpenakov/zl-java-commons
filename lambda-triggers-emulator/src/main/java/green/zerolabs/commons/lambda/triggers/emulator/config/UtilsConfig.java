package green.zerolabs.commons.lambda.triggers.emulator.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import green.zerolabs.commons.core.utils.ConverterUtils;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.sqs.utils.AllSqsUtils;
import io.quarkus.runtime.StartupEvent;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;

/*
 * Created by triphon 15.06.22 г.
 */
@Slf4j
public class UtilsConfig {
  void startup(@Observes final StartupEvent event) throws Exception {
    log.info("UtilsConfig.startup");
  }

  @Produces
  @ApplicationScoped
  JsonUtils jsonUtils(final ObjectMapper objectMapper) {
    objectMapper
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    return new JsonUtils(objectMapper);
  }

  @Produces
  @ApplicationScoped
  AllSqsUtils allSqsUtils(final JsonUtils jsonUtils) {
    return new AllSqsUtils(jsonUtils);
  }

  @Produces
  @ApplicationScoped
  ConverterUtils converterUtils() {
    return new ConverterUtils();
  }
}
