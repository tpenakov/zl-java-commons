package green.zerolabs.commons.core.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphQlValidatorContextTest {
  private static final String USER_ID = "user-1";

  GraphQlValidatorContext context;

  @BeforeEach
  void beforeEach() {
    context = new GraphQlValidatorContext(USER_ID);
  }

  @Test
  void setUserId() {
    context.setUserId(USER_ID);
    Assertions.assertEquals(USER_ID, context.getUserId());
  }
}
