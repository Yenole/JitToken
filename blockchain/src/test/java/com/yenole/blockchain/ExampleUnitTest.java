 package com.yenole.blockchain;

import com.yenole.blockchain.wallet.BTCUtil;
import com.yenole.blockchain.wallet.ETHUtil;
import com.yenole.blockchain.wallet.model.BIP44Util;
import com.yenole.blockchain.wallet.model.ChainID;
import com.yenole.blockchain.wallet.transaction.eos.chain.Transaction;
import com.yenole.blockchain.wallet.transaction.eth.EthereumTransaction;

import org.bitcoinj.utils.BtcAutoFormat;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void EthTest() {
        BigInteger nonce = BigInteger.valueOf(1);
        BigInteger gasPrice = BigInteger.valueOf(1000000000);
        BigInteger gasLimit = BigInteger.valueOf(21000);
        BigInteger value = BigInteger.valueOf(1000000);
        EthereumTransaction transaction = new EthereumTransaction(nonce, gasPrice, gasLimit, "0x687422eea2cb73b5d3e242ba5456b782919afc85", value, "0x");
        byte[] privateKey= ETHUtil.privateKey("");
        String rawTx = transaction.signTransaction(ChainID.ETHEREUM_ROPSTEN,privateKey);
        System.out.println( transaction.calcTxHash(rawTx));
        System.out.println(rawTx);
    }
}