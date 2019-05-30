package com.yenole.blockchain.wallet.transaction.eth;


import com.yenole.blockchain.foundation.crypto.Hash;
import com.yenole.blockchain.foundation.rlp.RlpEncoder;
import com.yenole.blockchain.foundation.rlp.RlpList;
import com.yenole.blockchain.foundation.rlp.RlpString;
import com.yenole.blockchain.foundation.rlp.RlpType;
import com.yenole.blockchain.foundation.utils.ByteUtil;
import com.yenole.blockchain.foundation.utils.NumericUtil;
import com.yenole.blockchain.wallet.transaction.eos.SignatureData;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class EthereumTransaction {

    private BigInteger nonce;
    private BigInteger gasPrice;
    private BigInteger gasLimit;
    private String to;
    private BigInteger value;
    private String data;

    public EthereumTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to,
                               BigInteger value, String data) {
        this.nonce = nonce;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.to = to;
        this.value = value;

        if (data != null) {
            this.data = NumericUtil.cleanHexPrefix(data);
        }
    }

    public BigInteger getNonce() {
        return nonce;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public String getTo() {
        return to;
    }

    public BigInteger getValue() {
        return value;
    }

    public String getData() {
        return data;
    }

//    public String signTransaction(int chainID, byte[] privateKey) {
//
//        String signedTx = signTransaction(chainID, privateKey);
//        String txHash = this.calcTxHash(signedTx);
////        return new TxSignResult(signedTx, txHash);
//        return signedTx;
//    }

    public String signTransaction(int chainId, byte[] privateKey) {
        SignatureData signatureData = new SignatureData(chainId, new byte[]{}, new byte[]{});
        byte[] encodedTransaction = encodeToRLP(signatureData);
        signatureData = EthereumSign.signMessage(encodedTransaction, privateKey);

        SignatureData eip155SignatureData = createEip155SignatureData(signatureData, chainId);
        byte[] rawSignedTx = encodeToRLP(eip155SignatureData);
        return NumericUtil.bytesToHex(rawSignedTx);
    }

    public String calcTxHash(String signedTx) {
        return NumericUtil.prependHexPrefix(Hash.keccak256(signedTx));
    }

    private static SignatureData createEip155SignatureData(SignatureData signatureData, int chainId) {
        int v = signatureData.getV() + (chainId * 2) + 8;

        return new SignatureData(v, signatureData.getR(), signatureData.getS());
    }

    byte[] encodeToRLP(SignatureData signatureData) {
        List<RlpType> values = asRlpValues(signatureData);
        RlpList rlpList = new RlpList(values);
        return RlpEncoder.encode(rlpList);
    }

    List<RlpType> asRlpValues(SignatureData signatureData) {
        List<RlpType> result = new ArrayList<>();

        result.add(RlpString.create(getNonce()));
        result.add(RlpString.create(getGasPrice()));
        result.add(RlpString.create(getGasLimit()));

        String to = getTo();
        if (to != null && to.length() > 0) {
            result.add(RlpString.create(NumericUtil.hexToBytes(to)));
        } else {
            result.add(RlpString.create(""));
        }

        result.add(RlpString.create(getValue()));

        // value field will already be hex encoded, so we need to convert into binary first
        byte[] data = NumericUtil.hexToBytes(getData());
        result.add(RlpString.create(data));

        if (signatureData != null && signatureData.getV() > 0) {
            result.add(RlpString.create(signatureData.getV()));
            result.add(RlpString.create(ByteUtil.trimLeadingZeroes(signatureData.getR())));
            result.add(RlpString.create(ByteUtil.trimLeadingZeroes(signatureData.getS())));
        }

        return result;
    }
}