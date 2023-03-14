package green.zerolabs.commons.cron.job.service.impl;

import green.zerolabs.commons.core.model.ZlLambdaRqContext;
import green.zerolabs.commons.core.service.CronJobProcessor;
import green.zerolabs.commons.core.service.CronJobService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Map;
import java.util.Optional;

/***
 * Created by Triphon Penakov 2022-11-21
 */
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class CronJobServiceImpl implements CronJobService {
  public static final long PRIORITY = 10L;
  public static final String DETAIL_TYPE = "detail-type";
  public static final String SCHEDULED_EVENT = "Scheduled Event";

  @Override
  public Boolean isSupported(final Map<String, Object> input, final ZlLambdaRqContext context) {
    return Optional.ofNullable(input)
        .filter(map -> map.containsKey(DETAIL_TYPE))
        .map(map -> StringUtils.equals(SCHEDULED_EVENT, (String) map.get(DETAIL_TYPE)))
        .orElse(false);
  }

  @Override
  public Uni<Object> handle(final Map<String, Object> input, final ZlLambdaRqContext context) {
    return Multi.createFrom()
        .iterable(CronJobProcessor.PROCESSORS)
        .onSubscription()
        .invoke(() -> log.debug("handle start"))
        .onItem()
        .transformToUni(processor -> processor.handle(context))
        .merge(5)
        .collect()
        .asList()
        .map(
            booleans ->
                booleans.stream()
                    .reduce((aBoolean, aBoolean2) -> aBoolean && aBoolean2)
                    .orElse(true))
        .onFailure()
        .recoverWithItem(this::handleError)
        .onItem()
        .castTo(Object.class)
        .onTermination()
        .invoke((o, throwable, aBoolean) -> log.debug("handle end with {}", o));
  }

  boolean handleError(final Throwable throwable) {
    log.error("handle error", throwable);
    return false;
  }

  @Override
  public Long getPriority() {
    return PRIORITY;
  }

  @Override
  public Object logOutput(final Object output) {
    return output;
  }
}
