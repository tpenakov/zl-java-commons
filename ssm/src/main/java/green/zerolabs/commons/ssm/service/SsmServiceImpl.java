package green.zerolabs.commons.ssm.service;

import green.zerolabs.commons.core.service.SsmService;
import io.smallrye.mutiny.Uni;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;

import java.util.Optional;

/*
 * Created by triphon 14.06.22 г.
 */
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class SsmServiceImpl implements SsmService {

  String encryptionKeyValue;
  private final String encryptionKeyPath;
  private final SsmAsyncClient ssm;

  public SsmServiceImpl(final String encryptionKeyPath, final SsmAsyncClient ssm) {
    this.encryptionKeyPath = encryptionKeyPath;
    this.ssm = ssm;
    encryptionKeyValue = null;
  }

  @Override
  public Uni<String> getEncryptionKey() {
    return Optional.ofNullable(getEncryptionKeyValue())
        .map(value -> Uni.createFrom().item(value))
        .orElseGet(
            () ->
                Uni.createFrom()
                    .completionStage(
                        () ->
                            getSsm()
                                .getParameter(
                                    builder ->
                                        builder.name(getEncryptionKeyPath()).withDecryption(true)))
                    .map(
                        getParameterResponse -> {
                          encryptionKeyValue = getParameterResponse.parameter().value();
                          return getEncryptionKeyValue();
                        }));
  }
}
