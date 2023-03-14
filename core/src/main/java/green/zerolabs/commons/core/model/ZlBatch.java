package green.zerolabs.commons.core.model;

import green.zerolabs.commons.core.model.graphql.generated.W3BatchRs;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/***
 * Created by Triphon Penakov 2022-10-09
 */
@RegisterForReflection
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ZlBatch extends W3BatchRs implements ZlInternal {
  private static final long serialVersionUID = 5051287561906137727L;

  public enum State {
    INITIAL,
    CREATED,
    STORAGE_POINTER_REQUEST_BLOCKCHAIN_PROPERTIES,
    STORAGE_POINTER_STORE_ONCHAIN,
    STORAGE_POINTER_STORE_IN_DB,
    STORAGE_POINTER_STORED_IN_DB,
    MINTED,
    ERROR,
  }

  private State state;
  private S3EventMessage.Record s3Record;
  private Map<String, W3Certificate> certificateIdMintCertificatesTxHashMap;
  private ZlInternalData internal = ZlInternalData.builder().build();

  @RegisterForReflection
  @Builder
  @Data
  @Jacksonized
  public static class W3Certificate {
    private static final long serialVersionUID = 1179592098449045919L;

    private String w2Id;
    private String w3Id;
    private String w3BatchId;
    private String mintCertificatesTxHash;
  }
}
