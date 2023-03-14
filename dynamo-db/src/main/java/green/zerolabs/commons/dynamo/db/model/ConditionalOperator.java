package green.zerolabs.commons.dynamo.db.model;

/*
 * Created by Triphon Penakov 2022-09-22
 */
public enum ConditionalOperator {
  EQUAL,
  NOT_EQUAL,
  GREATER,
  GREATER_THAN_OR_EQUAL,
  LESS,
  LESS_THAN_OR_EQUAL,
  BETWEEN,
  CONTAINS,
  BEGINS_WITH,
  IN,
  EXISTS,
  NOT_EXISTS,
  BEGIN_GROUP,
  END_GROUP,
  ERROR,
}
