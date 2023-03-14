package green.zerolabs.commons.core.service;

import green.zerolabs.commons.core.model.ZlLambdaRqContext;
import io.smallrye.mutiny.Uni;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/*
 * Created by triphon 26.02.22 г.
 */
public interface LambdaService {
  Collection<LambdaService> SERVICES = new ConcurrentLinkedQueue<>();

  Boolean isSupported(final Map<String, Object> input, final ZlLambdaRqContext context);

  Uni<Object> handle(final Map<String, Object> input, final ZlLambdaRqContext context);

  /**
   * The higher priority should get the higher precedence
   *
   * @return
   */
  Long getPriority();

  Object logOutput(Object output);
}
