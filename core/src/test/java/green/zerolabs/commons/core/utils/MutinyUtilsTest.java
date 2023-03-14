package green.zerolabs.commons.core.utils;

import io.smallrye.mutiny.Context;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.utils.CollectionUtils;
import software.amazon.awssdk.utils.StringUtils;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/*
 * Created by triphon 24.08.22 г.
 */
class MutinyUtilsTest {

  public static final String ERROR = "error";
  public static final String OK = "ok";
  public static final String ERROR_1 = "error1";
  public static final String ERROR_2 = "error2";

  @Test
  void instantiationTest() {
    Assertions.assertNotNull(new MutinyUtils());
  }

  @Test
  void runWithContextNullTest() {
    Assertions.assertNull(MutinyUtils.runWithContext(null, null));
  }

  @Test
  void runWithContextNullContextTest() {
    Assertions.assertTrue(
        MutinyUtils.runWithContext(() -> Uni.createFrom().item(true), null)
            .await()
            .atMost(Duration.ofMillis(100)));
  }

  @Test
  void runWithContextErrorTest() {
    final Uni<Boolean> uni =
        Uni.createFrom()
            .context(
                context ->
                    Uni.createFrom()
                        .<Boolean>failure(new RuntimeException(ERROR))
                        .onSubscription()
                        .invoke(() -> context.put(ERROR, ERROR)));

    final AtomicReference<Context> context = new AtomicReference(Context.empty());
    Assertions.assertTrue(
        MutinyUtils.runWithContext(() -> uni, context)
            .onFailure()
            .recoverWithItem(throwable -> StringUtils.equals(ERROR, throwable.getMessage()))
            .await()
            .atMost(Duration.ofMillis(100)));
    Assertions.assertEquals(ERROR, context.get().get(ERROR));
  }

  @Test
  void runWithContextTest() {
    final Uni<Boolean> uni =
        Uni.createFrom()
            .context(
                context ->
                    Uni.createFrom().item(true).onSubscription().invoke(() -> context.put(OK, OK)));

    final AtomicReference<Context> context = new AtomicReference(Context.empty());
    Assertions.assertTrue(
        MutinyUtils.runWithContext(() -> uni, context).await().atMost(Duration.ofMillis(100)));
    Assertions.assertEquals(OK, context.get().get(OK));
  }

  @Test
  void contextTest() {
    final Context context = Context.empty();
    Assertions.assertNull(MutinyUtils.get(context, String.class));
    Assertions.assertNull(MutinyUtils.delete(context, String.class));

    MutinyUtils.put(context, ERROR);
    Assertions.assertEquals(ERROR, MutinyUtils.get(context, String.class));
    Assertions.assertEquals(ERROR, MutinyUtils.delete(context, String.class));
    Assertions.assertNull(MutinyUtils.get(context, String.class));
    Assertions.assertNull(MutinyUtils.delete(context, String.class));
    Assertions.assertNull(MutinyUtils.delete(context, String.class));

    final String data1 = "data1";
    final String id1 = "id1";
    MutinyUtils.put(context, data1, id1);
    Assertions.assertEquals(data1, MutinyUtils.get(context, String.class, id1));

    final String data2 = "data2";
    final String id2 = "id2";
    MutinyUtils.put(context, data2, id2);
    Assertions.assertEquals(data2, MutinyUtils.get(context, String.class, id2));
    Assertions.assertEquals(data2, MutinyUtils.delete(context, String.class, id2));
    Assertions.assertNull(MutinyUtils.get(context, String.class, id2));
  }

  @Test
  void mutinyExceptionTest() {

    try {
      Uni.createFrom()
          .item(true)
          .flatMap(
              aBoolean -> {
                throw new RuntimeException(ERROR_1);
              })
          .map(o -> o)
          .map(o -> o)
          .onFailure()
          .transform(throwable -> new RuntimeException(throwable))
          .map(o -> o)
          .map(o -> o)
          .map(o -> o)
          .onItem()
          .ifNull()
          .continueWith(() -> false)
          .onFailure()
          .recoverWithItem(throwable -> false)
          .await()
          .indefinitely();
    } catch (final RuntimeException e) {
      Assertions.assertEquals(ERROR_2, e.getMessage());
    }
  }

  @Test
  void mutinyMultiNullItemsTest() {
    Assertions.assertTrue(
        CollectionUtils.isNullOrEmpty(
            Multi.createFrom()
                .items(1, 2, 3)
                .onItem()
                .transformToUni(integer -> Uni.createFrom().nullItem())
                .concatenate()
                .filter(Objects::nonNull)
                .collect()
                .asList()
                .await()
                .atMost(Duration.ofSeconds(1))));
  }

  @Test
  void combiningNullUnisTest() {
    final int item1 = 1;
    final Tuple2<Integer, Object> tuple2 =
        Uni.combine()
            .all()
            .unis(Uni.createFrom().item(item1), Uni.createFrom().nullItem())
            .asTuple()
            .await()
            .atMost(Duration.ofMillis(10));
    Assertions.assertEquals(item1, tuple2.getItem1());
    Assertions.assertNull(tuple2.getItem2());
  }
}

