package green.zerolabs.commons.web3.j.utils;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.nio.ByteBuffer;

public class SignatureUtils {

  public static byte[] sign(final String message, final Credentials credentials) {
    final String hashedAddress = Hash.sha3(message);
    final byte[] messageBytes = Numeric.hexStringToByteArray(hashedAddress);

    final Sign.SignatureData signatureData =
        Sign.signPrefixedMessage(messageBytes, credentials.getEcKeyPair());
    return SignatureUtils.toBytes(signatureData);
  }

  static byte[] toBytes(final Sign.SignatureData data) {
    final ByteBuffer bb = ByteBuffer.allocate(65);
    bb.put(data.getR());
    bb.put(data.getS());
    bb.put(data.getV());

    return bb.array();
  }


}
