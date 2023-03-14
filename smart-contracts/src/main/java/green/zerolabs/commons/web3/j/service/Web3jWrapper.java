package green.zerolabs.commons.web3.j.service;

import green.zerolabs.commons.core.model.ZlBlockchainProperties;
import green.zerolabs.commons.core.model.graphql.generated.W3BlockchainPropertiesRs;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

/*
 * Created by triphon 27.06.22 г.
 */
public interface Web3jWrapper {
  default Web3j create(final ZlBlockchainProperties blockchainProperties) {
    if (blockchainProperties == null) {
      return null;
    }

    final HttpService httpService = new HttpService(blockchainProperties.getRpcNode(), false);
    return Web3j.build(httpService);
  }
}
