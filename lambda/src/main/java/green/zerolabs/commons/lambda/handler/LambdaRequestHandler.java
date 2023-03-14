package green.zerolabs.commons.lambda.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import green.zerolabs.commons.core.model.ZlLambdaRqContext;
import green.zerolabs.commons.core.service.LambdaService;
import green.zerolabs.commons.core.utils.JsonUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@Slf4j
public class LambdaRequestHandler implements RequestHandler<Map<String, Object>, Object> {

  private final JsonUtils jsonUtils;

  public LambdaRequestHandler(final JsonUtils jsonUtils) {
    this.jsonUtils = jsonUtils;
  }

  @Override
  public Object handleRequest(final Map<String, Object> input, final Context awsContext) {

    final ZlLambdaRqContext context = toZlLambdaRqContext(awsContext);

    return getLambdaServiceStream()
        .filter(lambdaService -> lambdaService.isSupported(input, context))
        .findFirst()
        .map(
            lambdaService -> {
              // log.info("lambdaService: {}", lambdaService);
              return lambdaService
                  .handle(input, context)
                  // todo - does handle logging of the result - may contains sensitive data
                  .onItem()
                  .invoke(
                      map -> {
                        if (log.isDebugEnabled()) {
                          Optional.ofNullable(lambdaService.logOutput(map))
                              .ifPresent(o -> log.debug("result: {}", o));
                        }
                      })
                  .await()
                  .atMost(Duration.ofMinutes(1));
            })
        .orElseGet(
            () -> {
              if (log.isInfoEnabled()) {
                final String jsonString = getJsonUtils().toString(input);
                log.info("{} unhandled input: {}", context.getRequestId(), jsonString);
              }
              return Map.of(
                  "result",
                  false,
                  "reason",
                  "Not Processed: " + getJsonUtils().toStringLazy(input).toString());
            });
  }

  public Stream<LambdaService> getLambdaServiceStream() {
    return LambdaService.SERVICES.stream()
        .sorted(Comparator.comparing(LambdaService::getPriority).reversed());
  }

  public ZlLambdaRqContext toZlLambdaRqContext(final Context context) {
    return ZlLambdaRqContext.builder().requestId(context.getAwsRequestId()).build();
  }
}
