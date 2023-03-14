package green.zerolabs.commons.dynamo.db.service.impl;

import green.zerolabs.commons.dynamo.db.utils.UnitTestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DynamoDbFinderFactoryImplTest {

  DynamoDbFinderFactoryImpl dynamoDbFinderFactory;

  @BeforeEach
  void setUp() {
    dynamoDbFinderFactory = new DynamoDbFinderFactoryImpl(UnitTestUtils.of().getDynamoDbUtils());
  }

  @Test
  void createPkFinder() {
    Assertions.assertNotNull(dynamoDbFinderFactory.createPkFinder());
  }

  @Test
  void createTextFinder() {
    Assertions.assertNotNull(dynamoDbFinderFactory.createTextFinder());
  }

  @Test
  void createNumericFinder() {
    Assertions.assertNotNull(dynamoDbFinderFactory.createNumericFinder());
  }
}
