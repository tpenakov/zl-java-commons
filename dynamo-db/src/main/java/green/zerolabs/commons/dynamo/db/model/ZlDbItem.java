package green.zerolabs.commons.dynamo.db.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

/*
 * Created by triphon 27.02.22 г.
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlDbItem implements Serializable, Cloneable {
  private static final long serialVersionUID = -5436379863867419767L;

  public static final String ID = "id";
  public static final String SORT = "sort";
  public static final String GSI_SORT = "gsiSort";
  public static final String GSI_NUMERIC_SORT = "gsiNumericSort";
  public static final String LOCK_VERSION = "lockVersion";
  public static final String DATA_KEY = "#data";
  public static final String DATA = "data";
  public static final String GSI_NAME = "gsi01";
  public static final String GSI_NUMERIC_NAME = "gsi02";

  // DynamoDb partition key
  private String id;
  // DynamoDb partition sort key and GSI key
  private String sort;
  // DynamoDb GSI sort key
  private String gsiSort;
  // DynamoDb GSI numeric sort key
  private Long gsiNumericSort;
  // The non indexed data. E.g - ZlContract (not indexed)
  private Object data;

  @Override
  protected Object clone() {
    return ZlDbItem.builder()
        .id(getId())
        .sort(getSort())
        .gsiSort(getGsiSort())
        .gsiNumericSort(getGsiNumericSort())
        .data(getData());
  }
}
