package green.zerolabs.commons.sqs.service;

import green.zerolabs.commons.core.model.ZlSqsItem;
import green.zerolabs.commons.core.service.SqsProcessor;
import green.zerolabs.commons.core.service.SsmService;
import io.smallrye.mutiny.Uni;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Optional;

/*
 * Created by triphon 21.06.22 г.
 */
@Getter
@Slf4j
public class SsmServiceImpl implements SsmService {
  String encryptionKeyValue;
  private final SqsProcessor sqsProcessor;

  public SsmServiceImpl(final SqsProcessor sqsProcessor) {
    this.sqsProcessor = sqsProcessor;
  }

  @Override
  public Uni<String> getEncryptionKey() {
    return Optional.ofNullable(getEncryptionKeyValue())
        .map(value -> Uni.createFrom().item(value))
        .orElseGet(
            () ->
                getSqsProcessor()
                    .receive()
                    .map(
                        keys ->
                            Optional.ofNullable(keys).stream()
                                .flatMap(Collection::stream)
                                .findFirst()
                                .flatMap(
                                    rs ->
                                        Optional.ofNullable(rs.getCrypt())
                                            .map(ZlSqsItem.Crypt::getKey))
                                .map(
                                    key -> {
                                      encryptionKeyValue = key;
                                      return getEncryptionKeyValue();
                                    })
                                .orElse(null)));
  }
}
