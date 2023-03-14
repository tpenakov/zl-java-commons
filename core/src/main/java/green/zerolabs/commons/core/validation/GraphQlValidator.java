package green.zerolabs.commons.core.validation;

import io.smallrye.mutiny.Uni;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("unused")
public interface GraphQlValidator<T> {
  Collection<GraphQlValidator> VALIDATORS = new ConcurrentLinkedQueue<>();

  Uni<Void> validate(final T t);

  Class<T> getDataClass();

  Uni<String> getUserId();
}
