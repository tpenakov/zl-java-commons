package green.zerolabs.commons.core.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.List;

/*
 * Created by triphon 16.06.22 г.
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlSqsItem implements Serializable {
  private static final long serialVersionUID = -3105085485971156621L;

  private Store store;
  private ZlWeb3 web3;
  private Error error;
  private Crypt crypt;
  private List<GraphQlRequest> graphQlRequests;
  private String messageId;
  private String receiptHandle;
  private ZlSqsItem nextW3Item;
  private DbMigration dbMigration;

  @Builder
  @Data
  @Jacksonized
  @RegisterForReflection
  public static class Store implements Serializable {
    private static final long serialVersionUID = 3477499239498637570L;
    private String s3BucketName;
    private List<ZlBeneficiary> beneficiaryList;
    private List<ZlOrder> orderList;
    private List<ZlEventResult> eventResultList;
    private List<ZlCertificateData> certificateDataList;
    private List<ZlSeller> sellerList;
    private List<ZlSupplier> supplierList;
    private List<ZlUserDetails> userDetailsList;
    private List<ZlGenerator> generatorList;
    private List<ZlAgreement> agreementList;
    private List<ZlBlockchainProperties> blockchainPropertiesList;
    private List<ZlOffer> offerList;
  }

  @Builder
  @Data
  @Jacksonized
  @RegisterForReflection
  public static class Crypt implements Serializable {
    private static final long serialVersionUID = -5664055028959473266L;
    private String key;
  }

  @Builder
  @Data
  @Jacksonized
  @RegisterForReflection
  public static class DbMigration implements Serializable {
    private static final long serialVersionUID = 6460036359613875092L;
    private Boolean isEnabled;
    private String id;
  }

  @Builder
  @Data
  @Jacksonized
  @RegisterForReflection
  public static class Error implements Serializable {
    private static final long serialVersionUID = 619130839471355513L;
    private Boolean isError;
    private List<String> errors;
  }
}
