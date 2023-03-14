package green.zerolabs.commons.core.service;

/*
 * Created by triphon 21.10.21 г.
 */
public interface CryptoService {
  String encrypt(String value);

  String decrypt(String value);

  String hash(String value);
}
