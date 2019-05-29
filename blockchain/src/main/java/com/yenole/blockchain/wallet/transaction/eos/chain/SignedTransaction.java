package com.yenole.blockchain.wallet.transaction.eos.chain;

import com.yenole.blockchain.foundation.crypto.Hash;
import com.yenole.blockchain.foundation.utils.NumericUtil;
import com.yenole.blockchain.wallet.transaction.eos.EOSKey;
import com.yenole.blockchain.wallet.transaction.eos.EOSSign;
import com.yenole.blockchain.wallet.transaction.eos.types.EosByteWriter;

import java.util.ArrayList;
import java.util.List;


public class SignedTransaction extends Transaction {

    private List<String> signatures = null;

    private List<String> context_free_data = new ArrayList<>();


    public SignedTransaction() {
        super();
    }

    public SignedTransaction(SignedTransaction anotherTxn) {
        super(anotherTxn);
        this.signatures = deepCopyOnlyContainer(anotherTxn.signatures);
        this.context_free_data = context_free_data;
    }

    public List<String> getSignatures() {
        return signatures;
    }

    public void putSignatures(List<String> signatures) {
        this.signatures = signatures;
    }

    public int getCtxFreeDataCount() {
        return (context_free_data == null) ? 0 : context_free_data.size();
    }

    public List<String> getCtxFreeData() {
        return context_free_data;
    }


    private byte[] getDigestForSignature(String chainId) {
        EosByteWriter writer = new EosByteWriter(255);

        writer.putBytes(NumericUtil.hexToBytes(chainId));
        pack(writer);
        if (context_free_data.size() > 0) {
        } else {
            writer.putBytes(new byte[32]);
        }
        return Hash.sha256(writer.toBytes());
    }

    public void sign(String prikey, String chainId) {
        if (null == this.signatures) {
            this.signatures = new ArrayList<>();
        }
        this.signatures.add(EOSSign.sign(getDigestForSignature(chainId), EOSKey.fromWIF(prikey).getPrivateKey()));
    }
}

