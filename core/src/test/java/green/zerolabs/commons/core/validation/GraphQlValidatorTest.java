package green.zerolabs.commons.core.validation;

import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class GraphQlValidatorTest {

  @Test
  void graphQlValidatorsCollectionTest() {
    Assertions.assertTrue(GraphQlValidator.VALIDATORS.isEmpty());
  }

  @Test
  void getUserId() {
    final DummyValidator validator = new DummyValidator();

    Assertions.assertNull(validator.getUserId().await().atMost(Duration.ofSeconds(5)));
  }

  private static class DummyValidator implements GraphQlValidator<String> {

    @Override
    public Uni<Void> validate(final String s) {
      return null;
    }

    @Override
    public Class<String> getDataClass() {
      return null;
    }

    @Override
    public Uni<String> getUserId() {
      return Uni.createFrom().nullItem();
    }
  }
}
