package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import green.zerolabs.commons.core.model.graphql.generated.EnergyType;
import green.zerolabs.commons.core.model.graphql.generated.W3ProductType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlOffer implements ZlInternal {
  private static final long serialVersionUID = 5959095636104249870L;

  private String id;
  private BigDecimal unitPrice;
  private ConsumptionUnit energyUnit;
  private String description;
  private String image;
  private Boolean disabled;
  private IndexedOfferData indexedData;

  @Builder
  @Data
  @Jacksonized
  public static class IndexedOfferData implements Serializable {
    private static final long serialVersionUID = -1301317675853550124L;

    public static final String REPORTING_START_FIELD = "reportingStart";

    private String inventoryId;
    private W3ProductType productType;
    private List<EnergyType> energyTypes;
    private Long reportingStart;
    private Long reportingEnd;
    private List<ZlCountryRegion> countryRegions;
    private List<String> countryRegionIndexes;
  }

  @Builder.Default private ZlInternalData internal = ZlInternalData.builder().build();
}
