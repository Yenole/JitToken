package com.yenole.blockchain.wallet;

import com.yenole.blockchain.wallet.model.BIP44Util;
import com.yenole.blockchain.wallet.transaction.eth.EthereumAddressCreator;

import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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

    public static String GenerateTokenData(String address, long amount) {
        List inputParameters = Arrays.asList(new Address(address), new Uint256(BigInteger.valueOf(amount)));
        List outputParameters = Arrays.asList(new TypeReference<Type>() {
        });
        Function function = new Function("transfer", inputParameters, outputParameters);
        String encodedFunction = FunctionEncoder.encode(function);
        return encodedFunction;
    }
}
