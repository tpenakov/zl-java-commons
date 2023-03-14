package green.zerolabs.commons.core.service.web3;

import green.zerolabs.commons.core.model.ZlBlockchainProperties;

/*
 * Created by triphon 27.06.22 г.
 */
public interface ZlW3BlockchainPropertiesRsWrapper {

  String decryptPrivateKey(ZlBlockchainProperties blockchainProperties);

  String decryptMnemonicPhrase(ZlBlockchainProperties blockchainProperties);

  String decryptFileClientApiKey(ZlBlockchainProperties blockchainProperties);

  void decryptW3BlockchainPropertiesRs(
      final Boolean decrypt, final ZlBlockchainProperties w3BlockchainPropertiesRs);

  void encryptW3BlockchainPropertiesRs(
      final Boolean encrypt, final ZlBlockchainProperties w3BlockchainPropertiesRs);
}
