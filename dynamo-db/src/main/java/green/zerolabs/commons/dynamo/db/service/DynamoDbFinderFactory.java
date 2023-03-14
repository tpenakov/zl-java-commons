package green.zerolabs.commons.dynamo.db.service;

import green.zerolabs.commons.dynamo.db.utils.DynamoDbFinder;

/*
 * Created by Triphon Penakov 2022-09-23
 */
public interface DynamoDbFinderFactory {
  DynamoDbFinder createPkFinder();

  DynamoDbFinder createTextFinder();

  DynamoDbFinder createNumericFinder();
}
