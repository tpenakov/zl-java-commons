package green.zerolabs.commons.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import green.zerolabs.commons.core.error.GraphqlExpectedException;
import green.zerolabs.commons.core.model.GraphQlRequest;
import green.zerolabs.commons.core.model.graphql.generated.ErrorResponse;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.utils.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

@Getter(AccessLevel.PROTECTED)
@Slf4j
public class GraphQlWrapper {

  public static final String UNEXPECTED_ERROR_MESSAGE_PREFIX = "errorId: ";

  private final JsonUtils jsonUtils;

  public GraphQlWrapper(final JsonUtils jsonUtils) {
    this.jsonUtils = jsonUtils;
  }

  public static final BiFunction<ObjectMapper, Map<String, Object>, GraphQlRequest> INPUT_TO_RQ_FN =
      Unchecked.function((mapper, map) -> mapper.convertValue(map, GraphQlRequest.class));

  public Optional<String> getUsername(final GraphQlRequest input) {
    return Optional.ofNullable(input.getIdentity())
        .map(GraphQlRequest.Identity::getUsername)
        .filter(StringUtils::isNotBlank);
  }

  public <T> Optional<T> getBody(
      final GraphQlRequest input, final Function<Map<String, Object>, T> argumentsTransformer) {
    return Optional.ofNullable(input).map(GraphQlRequest::getArguments).map(argumentsTransformer);
  }

  public <T> Optional<T> getBody(
      final GraphQlRequest input, final String field, final Class<T> outputClass) {
    return getBody(input, map -> jsonUtils.fromMap((Map) map.get(field), outputClass));
  }

  public ErrorResponse handleError(final Throwable throwable, final ErrorResponse response) {
    response.setIsError(true);

    if (throwable instanceof GraphqlExpectedException) {
      response.setErrors(List.of(throwable.getMessage()));
    } else {
      final String errorId = UUID.randomUUID().toString();
      response.setErrors(List.of(UNEXPECTED_ERROR_MESSAGE_PREFIX + errorId));
    }

    log.error(response.getErrors().get(0), throwable);

    return response;
  }
}
