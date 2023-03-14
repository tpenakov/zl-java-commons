package green.zerolabs.commons.web3.j.utils;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Bip44WalletUtils;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.MnemonicUtils;

import static org.web3j.crypto.Bip32ECKeyPair.HARDENED_BIT;

/*
 * Created by triphon 18.08.22 г.
 */
public class ZlWalletUtils {
  public static Credentials create(final Integer index, final String mnemonicPhrase) {
    final byte[] seed = MnemonicUtils.generateSeed(mnemonicPhrase, null);
    final Bip32ECKeyPair masterKeypair = Bip32ECKeyPair.generateKeyPair(seed);
    final int[] path = {44 | HARDENED_BIT, 60 | HARDENED_BIT, 0 | HARDENED_BIT, 0, index};
    final Bip32ECKeyPair derivedKeyPair = Bip32ECKeyPair.deriveKeyPair(masterKeypair, path);
    return Credentials.create(derivedKeyPair);
  }

  public static Credentials create(final String password, final String mnemonicPhrase) {
    return Bip44WalletUtils.loadBip44Credentials(password, mnemonicPhrase);
  }
}
