package green.zerolabs.commons.core.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class LambdaServiceTest {

  LambdaService lambdaService;

  @BeforeEach
  void setUp() {
    lambdaService = Mockito.spy(LambdaService.class);
  }

  @Test
  void servicesTest() {
    Assertions.assertTrue(lambdaService.SERVICES.isEmpty());
  }
}
