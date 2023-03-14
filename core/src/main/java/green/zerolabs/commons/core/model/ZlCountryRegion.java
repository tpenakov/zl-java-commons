package green.zerolabs.commons.core.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;

@Builder
@Data
@Jacksonized
public class ZlCountryRegion implements Serializable {
  private static final long serialVersionUID = -4369760900912296315L;

  private String country;
  private String regionName;
}
