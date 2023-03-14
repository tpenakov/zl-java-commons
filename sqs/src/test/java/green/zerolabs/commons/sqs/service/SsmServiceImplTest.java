package green.zerolabs.commons.sqs.service;

import green.zerolabs.commons.core.model.ZlSqsItem;
import green.zerolabs.commons.core.service.SqsProcessor;
import green.zerolabs.commons.core.service.SsmService;
import io.smallrye.mutiny.Uni;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

/*
 * Created by triphon 14.06.22 г.
 */
@Getter
@Slf4j
class SsmServiceImplTest {
  public static final String SOME_SECRET_VALUE = "some_secret_value";

  SqsProcessor sqsProcessor;
  SsmService ssmService;

  @BeforeEach
  void beforeEach() {
    sqsProcessor = Mockito.spy(Mockito.mock(SqsProcessor.class));
    ssmService = new SsmServiceImpl(sqsProcessor);
  }

  @Test
  void getEncryptionKey() {
    Mockito.doReturn(
            Uni.createFrom()
                .item(
                    List.of(
                        ZlSqsItem.builder()
                            .crypt(ZlSqsItem.Crypt.builder().key(SOME_SECRET_VALUE).build())
                            .build())))
        .when(getSqsProcessor())
        .receive();

    Assertions.assertEquals(
        SOME_SECRET_VALUE, getSsmService().getEncryptionKey().await().indefinitely());
    Assertions.assertEquals(
        SOME_SECRET_VALUE, getSsmService().getEncryptionKey().await().indefinitely());

    Mockito.verify(getSqsProcessor(), Mockito.times(1)).receive();
  }
}
