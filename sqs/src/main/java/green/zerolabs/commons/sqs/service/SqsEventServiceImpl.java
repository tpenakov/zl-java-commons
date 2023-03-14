package green.zerolabs.commons.sqs.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import green.zerolabs.commons.core.model.SqsEventMessage;
import green.zerolabs.commons.core.model.ZlLambdaRqContext;
import green.zerolabs.commons.core.model.ZlSqsItem;
import green.zerolabs.commons.core.service.SqsEventProcessor;
import green.zerolabs.commons.core.service.SqsEventService;
import green.zerolabs.commons.core.utils.JsonUtils;
import green.zerolabs.commons.sqs.utils.AllSqsUtils;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/*
 * Created by triphon 26.02.22 г.
 */
@Getter(AccessLevel.PROTECTED)
public class SqsEventServiceImpl implements SqsEventService {

  private final Logger log = LoggerFactory.getLogger(getClass());
  public static final long PRIORITY = 10L;
  final Function<Map<String, Object>, SqsEventMessage> INPUT_TO_EVENT_FN =
      Unchecked.function(
          map ->
              getObjectMapper()
                  .readValue(getObjectMapper().writeValueAsString(map), SqsEventMessage.class));

  private final AllSqsUtils allUtils;

  public SqsEventServiceImpl(final AllSqsUtils allUtils) {
    this.allUtils = allUtils;
    SERVICES.add(this);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Boolean isSupported(final Map<String, Object> input, final ZlLambdaRqContext context) {
    return Optional.ofNullable(input)
        .filter(map -> map.containsKey(SqsEventMessage.RECORDS))
        .filter(map -> null != map.get(SqsEventMessage.RECORDS))
        .filter(map -> map.get(SqsEventMessage.RECORDS) instanceof List)
        .map(map -> map.get(SqsEventMessage.RECORDS))
        .map(
            records ->
                ((List<Map<String, Object>>) records)
                    .stream()
                        .map(
                            record ->
                                record.containsKey(SqsEventMessage.Record.MESSAGE_ID)
                                    && StringUtils.equals(
                                        SqsEventMessage.Record.AWS_SQS,
                                        (String) record.get(SqsEventMessage.Record.EVENT_SOURCE)))
                        .reduce((aBoolean, aBoolean1) -> aBoolean && aBoolean1)
                        .orElse(false))
        .orElse(false);
  }

  @Override
  public Uni<Object> handle(final Map<String, Object> input, final ZlLambdaRqContext context) {

    log.debug("input: {}", getJsonUtils().toStringLazy(input));

    final SqsEventMessage eventMessage = INPUT_TO_EVENT_FN.apply(input);

    return Multi.createFrom()
        .iterable(eventMessage.getRecords())
        .map(
            record ->
                Optional.ofNullable(record.getBody())
                    .flatMap(
                        body -> {
                          final Optional<ZlSqsItem> zlSqsItem =
                              getJsonUtils().readValue(body, ZlSqsItem.class);
                          zlSqsItem.ifPresent(
                              zlSqsItem1 -> {
                                zlSqsItem1.setMessageId(record.getMessageId());
                                zlSqsItem1.setReceiptHandle(record.getReceiptHandle());
                              });
                          return zlSqsItem;
                        }))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .onItem()
        .transformToUni(
            rq ->
                getAllUtils()
                    .getProcessors()
                    .filter(processor -> processor.isSupported(rq, context))
                    .findFirst()
                    .map(processor -> handleWithProcessor(context, rq, processor))
                    .orElseGet(() -> processUnhandled(rq, context)))
        .concatenate()
        .collect()
        .asList()
        .map(
            booleans ->
                booleans.stream()
                    .reduce((aBoolean, aBoolean2) -> aBoolean && aBoolean2)
                    .orElse(true))
        .map(aBoolean -> Map.<String, Object>of("result", aBoolean))
        .onFailure()
        .recoverWithItem(
            throwable -> {
              log.error("Unable to process sqs item", throwable);
              return Map.of("result", false);
            })
        .onItem()
        .invoke(aBoolean -> log.info("{} completed with {}", context.getRequestId(), aBoolean))
        .onItem()
        .castTo(Object.class);
  }

  @SuppressWarnings("unused")
  protected Uni<Boolean> processUnhandled(final ZlSqsItem rq, final ZlLambdaRqContext context) {
    return Uni.createFrom().item(true);
  }

  protected Uni<Boolean> handleWithProcessor(
      final ZlLambdaRqContext context, final ZlSqsItem rq, final SqsEventProcessor processor) {
    return processor.handle(rq, context);
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
}
