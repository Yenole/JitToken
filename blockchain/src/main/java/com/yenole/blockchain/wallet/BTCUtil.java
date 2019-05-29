package com.yenole.blockchain.wallet;

import com.yenole.blockchain.wallet.model.BIP44Util;
import com.yenole.blockchain.wallet.transaction.btc.SegWitBitcoinAddressCreator;
import com.yenole.blockchain.foundation.utils.MnemonicUtil;
import com.yenole.blockchain.foundation.utils.NumericUtil;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;

public class BTCUtil {

    public static NetworkParameters networkParameters = MainNetParams.get();


    public static void Test3Net(boolean isTest) {
        networkParameters = isTest ? TestNet3Params.get() : MainNetParams.get();
    }

    public static boolean IsTest3Net() {
        return networkParameters instanceof TestNet3Params;
    }

    public static String randomMnemonic() {
        String code = MnemonicUtil.randomMnemonicCodes().toString();
        return code.substring(1, code.length() - 1);
    }

    public static String segAddress(String code, String path) {
        try {
            DeterministicSeed seed = new DeterministicSeed(code, null, "", 0L);
            DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
            byte[] privKeys = keyChain.getKeyByPath(BIP44Util.generatePath(path + "/0/0"), true).getPrivKeyBytes();
            return new SegWitBitcoinAddressCreator(networkParameters).fromPrivateKey(privKeys);
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String xprv(String code, String path) {
        try {
            DeterministicSeed seed = new DeterministicSeed(code, null, "", 0L);
            DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
            return keyChain.getKeyByPath(BIP44Util.generatePath(path), true).serializePrivB58(networkParameters);
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static String scriptPubkey(String code, String path) {
        try {
            DeterministicSeed seed = new DeterministicSeed(code, null, "", 0L);
            DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
            DeterministicKey mainAdress = keyChain.getKeyByPath(BIP44Util.generatePath(path + "/0/0"), true);
            ECKey key = ECKey.fromPrivate(mainAdress.getPrivKeyBytes(), true);
            String redeemScript = String.format("0x0014%s", NumericUtil.bytesToHex(key.getPubKeyHash()));
            byte[] redeemScriptBytes = Utils.sha256hash160(NumericUtil.hexToBytes(redeemScript));
            return Integer.toHexString(169) + Integer.toHexString(redeemScriptBytes.length) + NumericUtil.bytesToHex(redeemScriptBytes) + Integer.toHexString(135);
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }
        return "";
    }

}
