package green.zerolabs.commons.dynamo.db.service.impl;

import green.zerolabs.commons.dynamo.db.model.ZlDbItem;
import green.zerolabs.commons.dynamo.db.service.DynamoDbFinderFactory;
import green.zerolabs.commons.dynamo.db.utils.DynamoDbFinder;
import green.zerolabs.commons.dynamo.db.utils.DynamoDbUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/*
 * Created by Triphon Penakov 2022-09-23
 */
@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class DynamoDbFinderFactoryImpl implements DynamoDbFinderFactory {

  private final DynamoDbUtils dynamoDbUtils;

  @Override
  public DynamoDbFinder createPkFinder() {
    return new DynamoDbFinder(getDynamoDbUtils());
  }

  @Override
  public DynamoDbFinder createTextFinder() {
    return new DynamoDbFinder(ZlDbItem.GSI_NAME, dynamoDbUtils);
  }

  @Override
  public DynamoDbFinder createNumericFinder() {
    return new DynamoDbFinder(ZlDbItem.GSI_NUMERIC_NAME, dynamoDbUtils);
  }
}
