package green.zerolabs.commons.apache.shiro.service.impl;

import green.zerolabs.commons.core.service.CryptoService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.crypto.BlowfishCipherService;
import org.apache.shiro.crypto.CipherService;
import org.apache.shiro.crypto.hash.DefaultHashService;
import org.apache.shiro.crypto.hash.HashService;
import org.apache.shiro.crypto.hash.Sha512Hash;
import org.apache.shiro.crypto.hash.SimpleHashRequest;
import org.apache.shiro.util.ByteSource;
import org.apache.shiro.util.SimpleByteSource;
import software.amazon.awssdk.utils.StringUtils;

/*
 * Created by triphon 21.10.21 г.
 */
@Getter(AccessLevel.PROTECTED)
@Slf4j
public class CryptoServiceImpl implements CryptoService {

  public static final int ITERATIONS = 3;
  private final String salt;
  private final byte[] keyBytes;
  private final CipherService cipher;
  private final HashService hashService;

  public CryptoServiceImpl(final String key) {
    salt = key;
    keyBytes = CodecSupport.toBytes(key);
    cipher = new BlowfishCipherService();
    hashService = new DefaultHashService();
  }

  @Override
  public String encrypt(final String value) {
    return encrypt(value, getKeyBytes());
  }

  @Override
  public String decrypt(final String value) {
    return decrypt(value, getKeyBytes());
  }

  @Override
  public String hash(final String value) {
    return hashService
        .computeHash(
            new SimpleHashRequest(
                Sha512Hash.ALGORITHM_NAME,
                new SimpleByteSource(value),
                new SimpleByteSource(getSalt()),
                ITERATIONS))
        .toHex();
  }

  private String encrypt(final String value, final byte[] keyBytes1) {
    if (StringUtils.isBlank(value)) {
      return null;
    }

    final byte[] secretBytes = CodecSupport.toBytes(value);
    final ByteSource encrypted = getCipher().encrypt(secretBytes, keyBytes1);
    return Base64.encodeToString(encrypted.getBytes());
  }

  private String decrypt(final String value, final byte[] keyBytes1) {
    if (StringUtils.isBlank(value)) {
      return null;
    }

    final byte[] encryptedBytes = Base64.decode(value);
    final ByteSource decrypted = getCipher().decrypt(encryptedBytes, keyBytes1);
    return CodecSupport.toString(decrypted.getBytes());
  }
}
