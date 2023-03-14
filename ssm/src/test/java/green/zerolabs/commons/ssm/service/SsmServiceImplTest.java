package green.zerolabs.commons.ssm.service;

import green.zerolabs.commons.core.service.SsmService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/*
 * Created by triphon 14.06.22 г.
 */
@Getter
@Slf4j
class SsmServiceImplTest {
  public static final String SOME_SECRET_VALUE = "some_secret_value";

  SsmAsyncClient ssm;
  SsmService ssmService;
  String encryptionKeyPath;

  @BeforeEach
  void beforeEach() {
    encryptionKeyPath = "/path";
    ssm = Mockito.spy(Mockito.mock(SsmAsyncClient.class));
    ssmService = new SsmServiceImpl(encryptionKeyPath, ssm);
  }

  @Test
  void getEncryptionKey() {
    Mockito.doReturn(
            CompletableFuture.completedFuture(
                GetParameterResponse.builder()
                    .parameter(Parameter.builder().value(SOME_SECRET_VALUE).build())
                    .build()))
        .when(getSsm())
        .getParameter(ArgumentMatchers.any(Consumer.class));

    Assertions.assertEquals(
        SOME_SECRET_VALUE, getSsmService().getEncryptionKey().await().indefinitely());
    Assertions.assertEquals(
        SOME_SECRET_VALUE, getSsmService().getEncryptionKey().await().indefinitely());

    Mockito.verify(getSsm(), Mockito.times(1)).getParameter(ArgumentMatchers.any(Consumer.class));
  }
}
