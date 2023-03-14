package green.zerolabs.commons.core.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class S3EventProcessorTest {

  private S3EventProcessor s3EventProcessor;

  @BeforeEach
  void setUp() {
    s3EventProcessor = Mockito.spy(S3EventProcessor.class);
  }

  @Test
  void getAllProcessorsTest() {
    Assertions.assertTrue(s3EventProcessor.getAllProcessors().isEmpty());
  }
}
