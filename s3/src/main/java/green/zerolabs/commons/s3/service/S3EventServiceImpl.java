package green.zerolabs.commons.s3.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import green.zerolabs.commons.core.model.S3EventMessage;
import green.zerolabs.commons.core.model.ZlLambdaRqContext;
import green.zerolabs.commons.core.service.S3EventProcessor;
import green.zerolabs.commons.core.service.S3EventService;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.s3.utils.AllS3Utils;
import green.zerolabs.commons.s3.utils.S3Utils;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.BytesWrapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/*
 * Created by triphon 26.02.22 г.
 */
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class S3EventServiceImpl implements S3EventService {
  public static final long PRIORITY = 10L;
  final Function<Map<String, Object>, S3EventMessage> INPUT_TO_EVENT_FN =
      Unchecked.function(
          map ->
              getObjectMapper()
                  .readValue(getObjectMapper().writeValueAsString(map), S3EventMessage.class));

  private final AllS3Utils allUtils;

  public S3EventServiceImpl(final AllS3Utils allUtils) {
    this.allUtils = allUtils;
    SERVICES.add(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Boolean isSupported(final Map<String, Object> input, final ZlLambdaRqContext context) {
    return Optional.ofNullable(input)
        .filter(map -> map.containsKey(S3EventMessage.RECORDS))
        .filter(map -> null != map.get(S3EventMessage.RECORDS))
        .filter(map -> map.get(S3EventMessage.RECORDS) instanceof List)
        .map(map -> map.get(S3EventMessage.RECORDS))
        .map(
            records ->
                ((List<Map<String, Object>>) records)
                    .stream()
                        .filter(record -> record.containsKey(S3EventMessage.Record.S3_NAME))
                        .map(record -> record.get(S3EventMessage.Record.S3_NAME))
                        .map(
                            s3 ->
                                ((Map<String, Object>) s3)
                                    .containsKey(S3EventMessage.Record.S3.BUCKET))
                        .reduce((aBoolean, aBoolean1) -> aBoolean && aBoolean1)
                        .orElse(false))
        .orElse(false);
  }

  @Override
  public Uni<Object> handle(final Map<String, Object> input, final ZlLambdaRqContext context) {

    log.debug("input: {}", getJsonUtils().toStringLazy(input));

    final S3EventMessage eventMessage = INPUT_TO_EVENT_FN.apply(input);

    return Multi.createFrom()
        .iterable(eventMessage.getRecords())
        .onItem()
        .transformToUni(
            record ->
                getS3Utils()
                    .readObject(record)
                    .map(BytesWrapper::asByteArray)
                    .onItem()
                    .ifNotNull()
                    .transform(bytes -> Tuple2.of(record, bytes)))
        .merge(5)
        .flatMap(
            objects ->
                S3EventProcessor.PROCESSORS.stream()
                    .filter(
                        s3EventProcessor ->
                            s3EventProcessor.isSupported(
                                objects.getItem1(), objects.getItem2(), context))
                    .findFirst()
                    .map(
                        s3EventProcessor ->
                            s3EventProcessor.handle(
                                objects.getItem1(), objects.getItem2(), context))
                    .orElseGet(() -> Uni.createFrom().item(true))
                    .convert()
                    .toPublisher())
        .collect()
        .asList()
        .map(
            booleans ->
                booleans.stream()
                    .reduce((aBoolean, aBoolean2) -> aBoolean && aBoolean2)
                    .orElse(true))
        .map(aBoolean -> Map.<String, Object>of("result", aBoolean))
        .onFailure()
        .recoverWithItem(Map.of("result", false))
        .onItem()
        .invoke(aBoolean -> log.info("{} completed with {}", context.getRequestId(), aBoolean))
        .onItem()
        .castTo(Object.class);
  }

  @Override
  public Long getPriority() {
    return PRIORITY;
  }

  @Override
  public Object logOutput(final Object output) {
    return getJsonUtils().toStringLazy(output);
  }

  private ObjectMapper getObjectMapper() {
    return getAllUtils().getJsonUtils().getObjectMapper();
  }

  private JsonUtils getJsonUtils() {
    return getAllUtils().getJsonUtils();
  }

  private S3Utils getS3Utils() {
    return getAllUtils().getS3Utils();
  }
}
