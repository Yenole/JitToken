package com.yenole.blockchain.wallet.transaction.btc;

import com.yenole.blockchain.wallet.model.TokenException;
import com.yenole.blockchain.foundation.utils.NumericUtil;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Utils;

public class SegWitBitcoinAddressCreator {
    private NetworkParameters networkParameters;

    public SegWitBitcoinAddressCreator(NetworkParameters networkParameters) {
        this.networkParameters = networkParameters;
    }

    public String fromPrivateKey(String prvKeyHex) {
        ECKey key;
        if (prvKeyHex.length() == 51 || prvKeyHex.length() == 52) {
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(networkParameters, prvKeyHex);
            key = dumpedPrivateKey.getKey();
            if (!key.isCompressed()) {
                throw new TokenException("");
            }
        } else {
            key = ECKey.fromPrivate(NumericUtil.hexToBytes(prvKeyHex), true);
        }
        return calcSegWitAddress(key.getPubKeyHash());
    }

    public String fromPrivateKey(byte[] prvKeyBytes) {
        ECKey key = ECKey.fromPrivate(prvKeyBytes, true);
        return calcSegWitAddress(key.getPubKeyHash());
    }

    private String calcSegWitAddress(byte[] pubKeyHash) {
        String redeemScript = String.format("0x0014%s", NumericUtil.bytesToHex(pubKeyHash));
        return Address.fromP2SHHash(networkParameters, Utils.sha256hash160(NumericUtil.hexToBytes(redeemScript))).toBase58();
    }

    public Address fromPrivateKey(ECKey ecKey) {
        String redeemScript = String.format("0x0014%s", NumericUtil.bytesToHex(ecKey.getPubKeyHash()));
        return Address.fromP2SHHash(networkParameters, Utils.sha256hash160(NumericUtil.hexToBytes(redeemScript)));
    }

}
