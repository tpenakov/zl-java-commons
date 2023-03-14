package green.zerolabs.commons.core.service.web3.impl;

import green.zerolabs.commons.core.model.ZlBlockchainProperties;
import green.zerolabs.commons.core.service.CryptoService;
import green.zerolabs.commons.core.service.web3.ZlW3BlockchainPropertiesRsWrapper;
import lombok.AccessLevel;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/*
 * Created by triphon 27.06.22 г.
 */
@Getter(AccessLevel.PROTECTED)
public class ZlW3BlockchainPropertiesRsWrapperImpl implements ZlW3BlockchainPropertiesRsWrapper {

  protected final Logger log = LoggerFactory.getLogger(getClass());
  private final CryptoService cryptoService;

  public ZlW3BlockchainPropertiesRsWrapperImpl(final CryptoService cryptoService) {
    this.cryptoService = cryptoService;
  }

  @Override
  public String decryptPrivateKey(final ZlBlockchainProperties blockchainProperties) {
    return runCryptActionOnKey(
        blockchainProperties,
        ZlBlockchainProperties::getPlatformOperatorPrivateKey,
        getCryptoService()::decrypt);
  }

  @Override
  public String decryptMnemonicPhrase(final ZlBlockchainProperties blockchainProperties) {
    return runCryptActionOnKey(
        blockchainProperties,
        ZlBlockchainProperties::getMnemonicPhrase,
        getCryptoService()::decrypt);
  }

  @Override
  public String decryptFileClientApiKey(final ZlBlockchainProperties blockchainProperties) {
    return runCryptActionOnKey(
        blockchainProperties,
        ZlBlockchainProperties::getFileClientApiKey,
        getCryptoService()::decrypt);
  }

  @Override
  public void decryptW3BlockchainPropertiesRs(
      final Boolean decrypt, final ZlBlockchainProperties w3BlockchainPropertiesRs) {
    runCryptoActionOnW3BlockchainPropertiesRs(
        decrypt, w3BlockchainPropertiesRs, getCryptoService()::decrypt);
  }

  @Override
  public void encryptW3BlockchainPropertiesRs(
      final Boolean encrypt, final ZlBlockchainProperties w3BlockchainPropertiesRs) {
    runCryptoActionOnW3BlockchainPropertiesRs(
        encrypt, w3BlockchainPropertiesRs, getCryptoService()::encrypt);
  }

  void setW3BlockchainPropertiesRsKey(
      final ZlBlockchainProperties w3BlockchainPropertiesRs,
      final Function<ZlBlockchainProperties, String> getKey,
      final Consumer<String> setKey,
      final Function<String, String> cryptAction) {
    Optional.ofNullable(w3BlockchainPropertiesRs)
        .map(getKey)
        .filter(key -> !key.isBlank())
        .ifPresent(
            key -> {
              final String result = cryptAction.apply(key);
              setKey.accept(result);
            });
  }

  String runCryptActionOnKey(
      final ZlBlockchainProperties w3BlockchainPropertiesRs,
      final Function<ZlBlockchainProperties, String> getKey,
      final Function<String, String> cryptAction) {
    return Optional.ofNullable(w3BlockchainPropertiesRs)
        .map(getKey)
        .filter(key -> !key.isBlank())
        .map(cryptAction::apply)
        .orElse(null);
  }

  void runCryptoActionOnW3BlockchainPropertiesRs(
      final Boolean action,
      final ZlBlockchainProperties w3BlockchainPropertiesRs,
      final Function<String, String> cryptAction) {
    if (Objects.isNull(w3BlockchainPropertiesRs) || Objects.isNull(action)) {
      return;
    }
    Optional.ofNullable(action)
        .filter(Boolean::booleanValue)
        .ifPresent(
            unused -> {
              setW3BlockchainPropertiesRsKey(
                  w3BlockchainPropertiesRs,
                  ZlBlockchainProperties::getPlatformOperatorPrivateKey,
                  w3BlockchainPropertiesRs::setPlatformOperatorPrivateKey,
                  cryptAction);
              setW3BlockchainPropertiesRsKey(
                  w3BlockchainPropertiesRs,
                  ZlBlockchainProperties::getMnemonicPhrase,
                  w3BlockchainPropertiesRs::setMnemonicPhrase,
                  cryptAction);
              setW3BlockchainPropertiesRsKey(
                  w3BlockchainPropertiesRs,
                  ZlBlockchainProperties::getFileClientApiKey,
                  w3BlockchainPropertiesRs::setFileClientApiKey,
                  cryptAction);
            });
  }
}
