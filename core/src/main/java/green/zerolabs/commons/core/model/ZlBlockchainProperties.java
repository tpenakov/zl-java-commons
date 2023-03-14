package green.zerolabs.commons.core.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/***
 * Created by Triphon Penakov 2023-02-17
 */
@Builder
@Data
@Jacksonized
@RegisterForReflection
public class ZlBlockchainProperties implements ZlInternal {

  private static final long serialVersionUID = 8026685022257724899L;

  String id;
  String userId;
  Long netId;
  Long topic;
  String batchFactory;
  String registry;
  String agreementFactory;
  String networkName;
  String networkBlockExplorerUrl;
  String rpcNode;
  String rpcNodeFallback;
  String platformOperatorPrivateKey;
  String mnemonicPhrase;
  String fileClientApiKey;
  String fileClientApiUrl;
  String fileClientApiUploadUrl;
  String fileClientApiDownloadUrl;
  String fileClientCollectionDefault;

  @Builder.Default private ZlInternalData internal = ZlInternalData.builder().build();
}
