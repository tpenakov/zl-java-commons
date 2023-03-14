package green.zerolabs.commons.core.utils;

import io.smallrye.mutiny.Context;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.operators.multi.processors.UnicastProcessor;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/*
 * Created by triphon 24.08.22 г.
 */
public class MutinyUtils {

  public static <T> Uni<T> runWithContext(
      final Supplier<Uni<T>> uniSupplier, final AtomicReference<Context> context) {

    if (Objects.isNull(context)) {
      return Optional.ofNullable(uniSupplier).map(Supplier::get).orElse(null);
    }

    final UnicastProcessor<T> processor = UnicastProcessor.create();

    uniSupplier.get().subscribe().with(context.get(), processor::onNext, processor::onError);

    return processor.toUni();
  }

  public static <T> String toContextKey(final Class<T> clazz) {
    return clazz.getSimpleName();
  }

  public static <T> String toContextKey(final String id, final Class<T> clazz) {
    return id + toContextKey(clazz);
  }

  public static <T> T get(final Context context, final Class<T> clazz) {
    return get(context, toContextKey(clazz));
  }

  public static <T> T get(final Context context, final Class<T> clazz, final String id) {
    return get(context, toContextKey(id, clazz));
  }

  private static <T> T get(final Context context, final String key) {
    return Optional.ofNullable(context)
        .map(context1 -> context1.<T>getOrElse(key, () -> null))
        .orElse(null);
  }

  public static <T> T delete(final Context context, final Class<T> clazz) {
    return delete(context, toContextKey(clazz));
  }

  public static <T> T delete(final Context context, final Class<T> clazz, final String id) {
    return delete(context, toContextKey(id, clazz));
  }

  private static <T> T delete(final Context context, final String key) {
    return Optional.<T>ofNullable(get(context, key))
        .map(
            t -> {
              context.delete(key);
              return t;
            })
        .orElse(null);
  }

  public static <T> void put(final Context context, final T value) {
    Optional.ofNullable(value).ifPresent(t -> context.put(toContextKey(t.getClass()), t));
  }

  public static <T> void put(final Context context, final T value, final String id) {
    Optional.ofNullable(value).ifPresent(t -> context.put(toContextKey(id, t.getClass()), t));
  }
}
