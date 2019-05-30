package com.yenole.blockchain.wallet;

import com.yenole.blockchain.foundation.utils.MnemonicUtil;
import com.yenole.blockchain.wallet.model.BIP44Util;
import com.yenole.blockchain.wallet.transaction.btc.SegWitBitcoinAddressCreator;
import com.yenole.blockchain.wallet.transaction.eth.EthereumAddressCreator;

import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;

public class ETHUtil {

    public static String Address(String mnemonic) {
        try {
//            MnemonicUtil.validateMnemonics(mnemonic);
//            MnemonicUtil.validateMnemonics(mnemonicCodes);
//            DeterministicSeed seed = new DeterministicSeed(mnemonicCodes, null, "", 0L);
//            DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
//
//            this.mnemonicPath = path;
//            List<ChildNumber> zeroPath = BIP44Util.generatePath(path);
//
//            byte[] prvKeyBytes = keyChain.getKeyByPath(zeroPath, true).getPrivKeyBytes();
//            this.crypto = Crypto.createPBKDF2CryptoWithKDFCached(password, prvKeyBytes);
//            this.encMnemonic = crypto.deriveEncPair(password, Joiner.on(" ").join(mnemonicCodes).getBytes());
//            this.crypto.clearCachedDerivedKey();
//
//            this.address = AddressCreatorManager.getInstance(metadata.getChainType(), metadata.isMainNet(), metadata.getSegWit()).fromPrivateKey(prvKeyBytes);
//            metadata.setTimestamp(DateUtil.getUTCTime());
//            metadata.setWalletType(Metadata.V3);
//            this.metadata = metadata;
//            this.version = VERSION;
//            this.id = Strings.isNullOrEmpty(id) ? UUID.randomUUID().toString() : id;

            DeterministicSeed seed = new DeterministicSeed(mnemonic, null, "", 0L);
            DeterministicKeyChain keyChain = DeterministicKeyChain.builder().seed(seed).build();
            byte[] prvKeyBytes = keyChain.getKeyByPath(BIP44Util.generatePath(BIP44Util.ETHEREUM_PATH), true).getPrivKeyBytes();
            return new EthereumAddressCreator().fromPrivateKey(prvKeyBytes);
        } catch (UnreadableWalletException e) {
            e.printStackTrace();
        }
        return "";
    }
}
