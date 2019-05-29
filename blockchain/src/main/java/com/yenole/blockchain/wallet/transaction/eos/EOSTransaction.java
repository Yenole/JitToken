package com.yenole.blockchain.wallet.transaction.eos;


import com.yenole.blockchain.wallet.transaction.eos.chain.Action;
import com.yenole.blockchain.wallet.transaction.eos.chain.PackedTransaction;
import com.yenole.blockchain.wallet.transaction.eos.chain.SignedTransaction;

import java.util.ArrayList;

public class EOSTransaction {

    SignedTransaction txnBeforeSign;
    String mContract, mAaction, mMessage, mPermission, mPrivateKey, mBinargs;
    String mBlockId, mExpiration;

    public EOSTransaction() {
    }


    public EOSTransaction action(String contract, String action, String message, String permission,String binargs) {
        mMessage = message;
        mContract = contract;
        mAaction = action;
        mPermission = permission;
        mBinargs = binargs;
        return this;
    }

    public String execute(String privKey, String blockId, String expiration, String chainId) {
        mPrivateKey = privKey;
        mBlockId = blockId;
        mExpiration = expiration;
        txnBeforeSign = createTransaction(mContract, mAaction, mBinargs, mPermission);
        txnBeforeSign.sign(mPrivateKey, chainId);
        return new PackedTransaction(txnBeforeSign).toJSON();
    }


    private SignedTransaction createTransaction(String contract, String actionName, String dataAsHex, String permissions) {
        Action action = new Action(contract, actionName);
        action.setAuthorization(permissions);
        action.setData(dataAsHex);


        SignedTransaction txn = new SignedTransaction();
        txn.addAction(action);
        txn.putSignatures(new ArrayList<String>());

        txn.setReferenceBlock(mBlockId);
        txn.setExpiration(mExpiration);
        return txn;
    }

}
