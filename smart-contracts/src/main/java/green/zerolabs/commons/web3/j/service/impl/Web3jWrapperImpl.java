package green.zerolabs.commons.web3.j.service.impl;

import green.zerolabs.commons.core.model.ZlBlockchainProperties;
import green.zerolabs.commons.web3.j.service.Web3jWrapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.web3j.protocol.Web3j;

/*
 * Created by triphon 27.06.22 г.
 */
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class Web3jWrapperImpl implements Web3jWrapper {

  @Override
  public Web3j create(final ZlBlockchainProperties blockchainProperties) {
    return Web3jWrapper.super.create(blockchainProperties);
  }
}
