package green.zerolabs.commons.core.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CronJobProcessorTest {

  CronJobProcessor processor;

  @BeforeEach
  void beforeEach() {
    processor = Mockito.spy(CronJobProcessor.class);
  }

  @Test
  void processorsTest() {
    Assertions.assertTrue(processor.PROCESSORS.isEmpty());
  }
}
