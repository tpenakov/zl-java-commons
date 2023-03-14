package green.zerolabs.commons.core.error;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GraphqlExpectedExceptionTest {

  private GraphqlExpectedException exception;

  @Test
  void testInit() {
    exception = new GraphqlExpectedException("");
    Assertions.assertNotNull(exception);
  }

  @Test
  void testInit2() {
    exception = new GraphqlExpectedException("", new RuntimeException());
    Assertions.assertNotNull(exception);
  }
}
