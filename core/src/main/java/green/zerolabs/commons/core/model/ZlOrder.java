package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.ConsumptionUnit;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/*
 * Created by triphon 27.02.22 г.
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlOrder implements ZlInternal {
  private static final long serialVersionUID = -8290491811055702078L;

  private String id;
  private String agreements;
  private List<String> agreementIdList;
  private Instant deliveryDate;
  private String sellerId;
  private String supplierId;
  private String productType;
  private String energySources;
  private String countryRegionList;
  private Instant reportingStart;
  private Instant reportingEnd;
  private BigDecimal energyAmount;
  private ConsumptionUnit energyUnit;
  private BigDecimal price;
  private String currency;
  private String label;
  private String inventoryId;
  private String externalId;

  // internal fields
  @Builder.Default private ZlInternalData internal = ZlInternalData.builder().build();

}

