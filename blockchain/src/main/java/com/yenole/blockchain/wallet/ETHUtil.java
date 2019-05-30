package com.yenole.blockchain.wallet;

import com.yenole.blockchain.foundation.utils.MnemonicUtil;
import com.yenole.blockchain.wallet.model.BIP44Util;
import com.yenole.blockchain.wallet.transaction.btc.SegWitBitcoinAddressCreator;
import com.yenole.blockchain.wallet.transaction.eth.EthereumAddressCreator;

import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;

public class ETHUtil {

    public static byte[] privateKey(String mnemonic) {
        try {
            DeterministicSeed seed = new DeterministicSeed(mnemonic, null, "", 0L);
            DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
            return keyChain.getKeyByPath(BIP44Util.generatePath(BIP44Util.ETHEREUM_PATH), true).getPrivKeyBytes();
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String Address(String mnemonic) {
        return new EthereumAddressCreator().fromPrivateKey(privateKey(mnemonic));
    }
}
