package green.zerolabs.commons.core.service;

import green.zerolabs.commons.core.model.GraphQlRequest;
import green.zerolabs.commons.core.model.ZlLambdaRqContext;
import io.smallrye.mutiny.Uni;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * Created by triphon 27.02.22 г.
 */
public interface GraphQlProcessor {

  Collection<GraphQlProcessor> PROCESSORS = new ConcurrentLinkedQueue<>();

  Boolean isSupported(
      GraphQlRequest input, Map<String, Object> data, final ZlLambdaRqContext context);

  Uni<Object> handle(
      GraphQlRequest input, Map<String, Object> data, final ZlLambdaRqContext context);

  default Optional<String> getUserName(final GraphQlRequest input) {
    return Optional.ofNullable(input)
        .map(GraphQlRequest::getIdentity)
        .map(GraphQlRequest.Identity::getUsername);
  }

  Object logInput(
      Map<String, Object> input, ZlLambdaRqContext context, GraphQlRequest graphQlRequest);

  Object logOutput(Object output);
}
