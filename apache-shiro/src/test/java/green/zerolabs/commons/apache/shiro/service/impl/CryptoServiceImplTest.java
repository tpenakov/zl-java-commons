package green.zerolabs.commons.apache.shiro.service.impl;

import green.zerolabs.commons.core.service.CryptoService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.crypto.hash.*;
import org.apache.shiro.util.SimpleByteSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

/*
 * Created by triphon 21.10.21 г.
 */
@Getter
@Slf4j
class CryptoServiceImplTest {

  public static final String DATA = "The Matrix has you!";

  private final CryptoService cryptoService = new CryptoServiceImpl(UUID.randomUUID().toString());

  @Test
  void encryptDecryptTest() {

    final String data = DATA;
    log.info("data: {}", data);

    final String encryptedData = getCryptoService().encrypt(data);
    log.info("encryptedData: {}", encryptedData);
    final String decryptedData = getCryptoService().decrypt(encryptedData);
    log.info("decryptedData: {}", decryptedData);
    Assertions.assertEquals(data, decryptedData);
  }

  @Test
  public void nullValuesTest() {
    Assertions.assertNull(getCryptoService().encrypt(null));
    Assertions.assertNull(getCryptoService().decrypt(null));
  }

  @Test
  public void hashTest() {

    final String pk = DATA;

    final String result = getCryptoService().hash(pk);
    log.info("hash: {}", result);
    Assertions.assertEquals(result, getCryptoService().hash(pk));
  }

  @Test
  public void hashTestRaw() {
    final HashService hashService = new DefaultHashService();

    final String pk = "b8a58f6f-b51a-4537-b472-399e5ebbf68f";
    final String salt = "salt";

    final String result = hashToHex(getHash(hashService, pk, salt));
    log.info("hash: {}", result);
    Assertions.assertEquals(result, hashToHex(getHash(hashService, pk, salt)));
    Assertions.assertEquals(
        "77ca4ba716a9f2151ae41691bf39cb0bbcec28e35bc7536414bcf4460d2b994d1d35601b2a9b646291f4f53670412b5a3bf4c7b7f6ef68afc0704c283c131b45",
        hashToHex(getHash(hashService, pk, salt)));
  }

  private static String hashToHex(final Hash hash) {
    return hash.toHex();
  }

  private static Hash getHash(final HashService hashService, final String pk, final String salt) {
    return hashService.computeHash(
        new SimpleHashRequest(
            Sha512Hash.ALGORITHM_NAME, new SimpleByteSource(pk), new SimpleByteSource(salt), 3));
  }
}
