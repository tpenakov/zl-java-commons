package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.EnergyType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlGenerator implements ZlInternal {
  private static final long serialVersionUID = -2446046381932984967L;

  private String id;
  private String generatorName;
  private EnergyType energySource;
  private String country;
  private String region;
  private BigDecimal latitude;
  private BigDecimal longitude;
  private BigDecimal nameplateCapacity;
  private String capacityUnit;
  private Instant commissioningDate;
  private String impactStory;
  private String images;
  private String links;
  private List<String> imagesTransformed;
  private List<String> linksTransformed;
  private String inventoryId;

  @Builder.Default private ZlInternalData internal = ZlInternalData.builder().build();
}
