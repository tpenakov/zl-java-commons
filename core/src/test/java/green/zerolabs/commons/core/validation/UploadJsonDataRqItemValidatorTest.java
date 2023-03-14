package green.zerolabs.commons.core.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UploadJsonDataRqItemValidatorTest {

  @Test
  void validatorsCollectionTest() {
    Assertions.assertTrue(UploadJsonDataRqItemValidator.VALIDATORS.isEmpty());
  }
}
