package green.zerolabs.commons.dynamo.db.utils;

import green.zerolabs.commons.core.utils.MutinyUtils;
import io.smallrye.mutiny.Uni;
import software.amazon.awssdk.utils.StringUtils;

import java.util.Optional;
import java.util.function.Consumer;

/***
 * Created by Triphon Penakov 2022-11-04
 */
public class DynamoDbFillDetailsUtils {

  public static <T> Uni<Void> fill(
      final DynamoDbFinder finder,
      final String id,
      final String dataKey,
      final Class<T> clazz,
      final Consumer<T> setter) {
    return Uni.createFrom()
        .optional(Optional.ofNullable(id).filter(StringUtils::isNotBlank))
        .onItem()
        .ifNotNull()
        .transformToUni(
            s ->
                Uni.createFrom()
                    .context(
                        context ->
                            Uni.createFrom()
                                .item(MutinyUtils.get(context, clazz, s))
                                .onItem()
                                .ifNull()
                                .switchTo(() -> finder.findOne(s, dataKey, clazz))
                                .onItem()
                                .ifNotNull()
                                .invoke(t -> MutinyUtils.put(context, t, s))))
        .onItem()
        .invoke(setter)
        .replaceWithVoid();
  }
}
