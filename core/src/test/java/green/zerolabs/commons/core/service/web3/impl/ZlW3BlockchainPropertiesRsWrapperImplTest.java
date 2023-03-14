package green.zerolabs.commons.core.service.web3.impl;

import green.zerolabs.commons.core.model.ZlBlockchainProperties;
import green.zerolabs.commons.core.service.CryptoService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/*
 * Created by triphon 18.08.22 г.
 */
@Getter
@Slf4j
class ZlW3BlockchainPropertiesRsWrapperImplTest {

  public static final String ENCRYPTED = "encrypted";
  public static final String DECRYPTED = "decrypted";
  CryptoService cryptoService;
  ZlW3BlockchainPropertiesRsWrapperImpl wrapper;

  @BeforeEach
  void beforeEach() {
    cryptoService = Mockito.spy(CryptoService.class);
    Mockito.doReturn(ENCRYPTED).when(getCryptoService()).encrypt(ArgumentMatchers.anyString());
    Mockito.doReturn(DECRYPTED).when(getCryptoService()).decrypt(ArgumentMatchers.anyString());

    wrapper = new ZlW3BlockchainPropertiesRsWrapperImpl(getCryptoService());
  }

  @Test
  void decryptPropsNullTest() {
    getWrapper().decryptW3BlockchainPropertiesRs(true, null);
    final ZlBlockchainProperties blockchainProperties = ZlBlockchainProperties.builder().build();
    getWrapper().decryptW3BlockchainPropertiesRs(null, blockchainProperties);
    Mockito.verify(getCryptoService(), Mockito.never())
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void decryptPropsDisabledTest() {
    final ZlBlockchainProperties blockchainProperties = ZlBlockchainProperties.builder().build();
    getWrapper().decryptW3BlockchainPropertiesRs(false, blockchainProperties);
    Mockito.verify(getCryptoService(), Mockito.never())
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void decryptPropsEmptyKeyTest() {
    final ZlBlockchainProperties w3BlockchainPropertiesRs =
        ZlBlockchainProperties.builder().build();
    w3BlockchainPropertiesRs.setPlatformOperatorPrivateKey("");
    w3BlockchainPropertiesRs.setMnemonicPhrase("");
    getWrapper().decryptW3BlockchainPropertiesRs(true, w3BlockchainPropertiesRs);
    Mockito.verify(getCryptoService(), Mockito.times(0))
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void decryptPropsTest() {
    final ZlBlockchainProperties w3BlockchainPropertiesRs =
        ZlBlockchainProperties.builder().build();
    w3BlockchainPropertiesRs.setPlatformOperatorPrivateKey(ENCRYPTED);
    w3BlockchainPropertiesRs.setMnemonicPhrase(ENCRYPTED);
    getWrapper().decryptW3BlockchainPropertiesRs(true, w3BlockchainPropertiesRs);
    Mockito.verify(getCryptoService(), Mockito.times(2))
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void encryptPropsNullTest() {
    getWrapper().encryptW3BlockchainPropertiesRs(true, null);
    Mockito.verify(getCryptoService(), Mockito.never())
        .encrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void encryptPropsDisabledTest() {
    final ZlBlockchainProperties blockchainProperties = ZlBlockchainProperties.builder().build();
    getWrapper().encryptW3BlockchainPropertiesRs(false, blockchainProperties);
    Mockito.verify(getCryptoService(), Mockito.never())
        .encrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void encryptPropsEmptyKeyTest() {
    final ZlBlockchainProperties w3BlockchainPropertiesRs =
        ZlBlockchainProperties.builder().build();
    w3BlockchainPropertiesRs.setPlatformOperatorPrivateKey("");
    w3BlockchainPropertiesRs.setMnemonicPhrase("");
    getWrapper().encryptW3BlockchainPropertiesRs(true, w3BlockchainPropertiesRs);
    Mockito.verify(getCryptoService(), Mockito.times(0))
        .encrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void encryptPropsTest() {
    final ZlBlockchainProperties w3BlockchainPropertiesRs =
        ZlBlockchainProperties.builder().build();
    w3BlockchainPropertiesRs.setPlatformOperatorPrivateKey(ENCRYPTED);
    w3BlockchainPropertiesRs.setMnemonicPhrase(ENCRYPTED);
    getWrapper().encryptW3BlockchainPropertiesRs(true, w3BlockchainPropertiesRs);
    Mockito.verify(getCryptoService(), Mockito.times(2))
        .encrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void decryptPrivateKeyNullTest() {
    Assertions.assertNull(getWrapper().decryptPrivateKey(null));
    Mockito.verify(getCryptoService(), Mockito.never())
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void decryptPrivateKeyEmptyTest() {
    final ZlBlockchainProperties blockchainProperties = ZlBlockchainProperties.builder().build();
    blockchainProperties.setPlatformOperatorPrivateKey("");
    Assertions.assertNull(getWrapper().decryptPrivateKey(blockchainProperties));
    Mockito.verify(getCryptoService(), Mockito.never())
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void decryptPrivateKeyTest() {
    final ZlBlockchainProperties blockchainProperties = ZlBlockchainProperties.builder().build();
    blockchainProperties.setPlatformOperatorPrivateKey(ENCRYPTED);
    Assertions.assertEquals(DECRYPTED, getWrapper().decryptPrivateKey(blockchainProperties));
    Mockito.verify(getCryptoService(), Mockito.times(1))
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void decryptMnemonicPhraseNullTest() {
    Assertions.assertNull(getWrapper().decryptMnemonicPhrase(null));
    Mockito.verify(getCryptoService(), Mockito.never())
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void decryptMnemonicPhraseEmptyTest() {
    final ZlBlockchainProperties blockchainProperties = ZlBlockchainProperties.builder().build();
    blockchainProperties.setMnemonicPhrase("");
    Assertions.assertNull(getWrapper().decryptMnemonicPhrase(blockchainProperties));
    Mockito.verify(getCryptoService(), Mockito.never())
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void decryptMnemonicPhraseTest() {
    final ZlBlockchainProperties blockchainProperties = ZlBlockchainProperties.builder().build();
    blockchainProperties.setMnemonicPhrase(ENCRYPTED);
    Assertions.assertEquals(DECRYPTED, getWrapper().decryptMnemonicPhrase(blockchainProperties));
    Mockito.verify(getCryptoService(), Mockito.times(1))
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void decryptFileClientApiKeyNullTest() {
    Assertions.assertNull(getWrapper().decryptFileClientApiKey(null));
    Mockito.verify(getCryptoService(), Mockito.never())
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void decryptFileClientApiKeyEmptyTest() {
    final ZlBlockchainProperties blockchainProperties = ZlBlockchainProperties.builder().build();
    blockchainProperties.setFileClientApiKey("");
    Assertions.assertNull(getWrapper().decryptFileClientApiKey(blockchainProperties));
    Mockito.verify(getCryptoService(), Mockito.never())
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void decryptFileClientApiKeyTest() {
    final ZlBlockchainProperties blockchainProperties = ZlBlockchainProperties.builder().build();
    blockchainProperties.setFileClientApiKey(ENCRYPTED);
    Assertions.assertEquals(DECRYPTED, getWrapper().decryptFileClientApiKey(blockchainProperties));
    Mockito.verify(getCryptoService(), Mockito.times(1))
        .decrypt(ArgumentMatchers.nullable(String.class));
  }

  @Test
  void coveragesTest() {
    Assertions.assertNotNull(getWrapper().getLog());
  }
}
