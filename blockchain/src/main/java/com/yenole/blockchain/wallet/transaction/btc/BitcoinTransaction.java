package com.yenole.blockchain.wallet.transaction.btc;

import com.yenole.blockchain.foundation.utils.ByteUtil;
import com.yenole.blockchain.foundation.utils.NumericUtil;
import com.yenole.blockchain.wallet.BTCUtil;
import com.yenole.blockchain.wallet.model.Metadata;
import com.yenole.blockchain.wallet.model.TokenException;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.UnsafeByteArrayOutputStream;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VarInt;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.script.ScriptBuilder;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class BitcoinTransaction {
    private String mScritPubKey;
    private String mFrom;
    private String mTo;
    private long mAmount;
    private List<UTXO> mOutputs = new ArrayList<>();
    private long mFee;
    private String mChainId = "0";
    private String mXprvKey;
    private long mLocktime = 0;

    private Address changeAddress;
    private List<BigInteger> prvKeys;
    private NetworkParameters network = BTCUtil.networkParameters;


    // 2730 sat
    private final static long DUST_THRESHOLD = 0;

    public BitcoinTransaction() {

    }

    public BitcoinTransaction sendto(String from, String scriptPubKey, String to, long amount, long fee) {
        mTo = to;
        mFrom = from;
        mAmount = amount;
        mFee = fee;
        mScritPubKey = scriptPubKey;
        if (amount < DUST_THRESHOLD) {
            throw new TokenException("AMOUNT_LESS_THAN_MINIMUM");
        }
        return this;
    }

    public String execute(String xprv, ArrayList<String> utxos) {
        mXprvKey = xprv;
        mChainId = network instanceof MainNetParams ? "0" : "1";
        for (String utxo : utxos) {
            String[] list = utxo.split(",");
            mOutputs.add(new UTXO(list[0], Integer.parseInt(list[1]), Long.parseLong(list[2]), mFrom, mScritPubKey, list[3]));
        }
        return signSegWitTransaction();
    }


    public static class UTXO {
        private String txHash;
        private int vout;
        private long amount;
        private String address;
        private String scriptPubKey;
        private String derivedPath;
        private long sequence = 4294967295L;

        public UTXO(String txHash, int vout, long amount, String address, String scriptPubKey, String derivedPath) {
            this.txHash = txHash;
            this.vout = vout;
            this.amount = amount;
            this.address = address;
            this.scriptPubKey = scriptPubKey;
            this.derivedPath = derivedPath;
        }

        public UTXO(String txHash, int vout, long amount, String address, String scriptPubKey, String derivedPath, long sequence) {
            this.txHash = txHash;
            this.vout = vout;
            this.amount = amount;
            this.address = address;
            this.scriptPubKey = scriptPubKey;
            this.derivedPath = derivedPath;
            this.sequence = sequence;
        }

        public int getVout() {
            return vout;
        }

        public void setVout(int vout) {
            this.vout = vout;
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(long amount) {
            this.amount = amount;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public String getTxHash() {
            return txHash;
        }

        public void setTxHash(String txHash) {
            this.txHash = txHash;
        }

        public String getScriptPubKey() {
            return scriptPubKey;
        }

        public void setScriptPubKey(String scriptPubKey) {
            this.scriptPubKey = scriptPubKey;
        }

        public String getDerivedPath() {
            return derivedPath;
        }

        public void setDerivedPath(String derivedPath) {
            this.derivedPath = derivedPath;
        }

        public long getSequence() {
            return sequence;
        }

        public void setSequence(long sequence) {
            this.sequence = sequence;
        }
    }

    public String signSegWitTransaction() {
        collectPrvKeysAndAddress(Metadata.P2WPKH, mXprvKey);

        long totalAmount = 0L;
        boolean hasChange = false;

        for (UTXO output : mOutputs) {
            totalAmount += output.getAmount();
        }

        if (totalAmount < mAmount) {
            throw new TokenException("INSUFFICIENT_FUNDS");
        }

        long changeAmount = totalAmount - (mAmount + mFee);
        Address toAddress = Address.fromBase58(network, mTo);
        byte[] targetScriptPubKey;
        if (toAddress.isP2SHAddress()) {
            targetScriptPubKey = ScriptBuilder.createP2SHOutputScript(toAddress.getHash160()).getProgram();
        } else {
            targetScriptPubKey = ScriptBuilder.createOutputScript(toAddress).getProgram();
        }

        byte[] changeScriptPubKey = ScriptBuilder.createP2SHOutputScript(changeAddress.getHash160()).getProgram();

        byte[] hashPrevouts;
        byte[] hashOutputs;
        byte[] hashSequence;

        try {
            // calc hash prevouts
            UnsafeByteArrayOutputStream stream = new UnsafeByteArrayOutputStream();
            for (UTXO utxo : mOutputs) {
                TransactionOutPoint outPoint = new TransactionOutPoint(this.network, utxo.vout, Sha256Hash.wrap(utxo.txHash));
                outPoint.bitcoinSerialize(stream);
            }
            hashPrevouts = Sha256Hash.hashTwice(stream.toByteArray());

            // calc hash outputs
            stream = new UnsafeByteArrayOutputStream();

            TransactionOutput targetOutput = new TransactionOutput(this.network, null, Coin.valueOf(mAmount), toAddress);
            targetOutput.bitcoinSerialize(stream);

            if (changeAmount >= DUST_THRESHOLD) {
                hasChange = true;
                TransactionOutput changeOutput = new TransactionOutput(this.network, null, Coin.valueOf(changeAmount), changeAddress);
                changeOutput.bitcoinSerialize(stream);
            }

//

            hashOutputs = Sha256Hash.hashTwice(stream.toByteArray());

            // calc hash sequence
            stream = new UnsafeByteArrayOutputStream();

            for (UTXO utxo : mOutputs) {
                Utils.uint32ToByteStreamLE(utxo.getSequence(), stream);
            }
            hashSequence = Sha256Hash.hashTwice(stream.toByteArray());

            // calc witnesses and redemScripts
            List<byte[]> witnesses = new ArrayList<>();
            List<String> redeemScripts = new ArrayList<>();
            for (int i = 0; i < mOutputs.size(); i++) {
                UTXO utxo = mOutputs.get(i);
//                BigInteger prvKey = Metadata.FROM_WIF.equals(wallet.getMetadata().getSource()) ? prvKeys.get(0) : prvKeys.get(i);
                BigInteger prvKey = prvKeys.get(i);
                ECKey key = ECKey.fromPrivate(prvKey, true);
                String redeemScript = String.format("0014%s", NumericUtil.bytesToHex(key.getPubKeyHash()));
                redeemScripts.add(redeemScript);

                // calc outpoint
                stream = new UnsafeByteArrayOutputStream();
                TransactionOutPoint txOutPoint = new TransactionOutPoint(this.network, utxo.vout, Sha256Hash.wrap(utxo.txHash));
                txOutPoint.bitcoinSerialize(stream);
                byte[] outpoint = stream.toByteArray();

                // calc scriptCode
                byte[] scriptCode = NumericUtil.hexToBytes(String.format("0x1976a914%s88ac", NumericUtil.bytesToHex(key.getPubKeyHash())));

                // before sign
                stream = new UnsafeByteArrayOutputStream();
                Utils.uint32ToByteStreamLE(2L, stream);
                stream.write(hashPrevouts);
                stream.write(hashSequence);
                stream.write(outpoint);
                stream.write(scriptCode);
                Utils.uint64ToByteStreamLE(BigInteger.valueOf(utxo.getAmount()), stream);
                Utils.uint32ToByteStreamLE(utxo.getSequence(), stream);
                stream.write(hashOutputs);
                Utils.uint32ToByteStreamLE(mLocktime, stream);
                // hashType 1 = all
                Utils.uint32ToByteStreamLE(1L, stream);

                byte[] hashPreimage = stream.toByteArray();
                byte[] sigHash = Sha256Hash.hashTwice(hashPreimage);
                ECKey.ECDSASignature signature = key.sign(Sha256Hash.wrap(sigHash));
                byte hashType = 0x01;
                // witnesses
                byte[] sig = ByteUtil.concat(signature.encodeToDER(), new byte[]{hashType});
                witnesses.add(sig);
            }


            // the second stream is used to calc the traditional txhash
            UnsafeByteArrayOutputStream[] serialStreams = new UnsafeByteArrayOutputStream[]{
                    new UnsafeByteArrayOutputStream(), new UnsafeByteArrayOutputStream()
            };
            for (int idx = 0; idx < 2; idx++) {
                stream = serialStreams[idx];
                Utils.uint32ToByteStreamLE(2L, stream); // version
                if (idx == 0) {
                    stream.write(0x00); // maker
                    stream.write(0x01); // flag
                }
                // inputs
                stream.write(new VarInt(mOutputs.size()).encode());
                for (int i = 0; i < mOutputs.size(); i++) {
                    UTXO utxo = mOutputs.get(i);
                    stream.write(NumericUtil.reverseBytes(NumericUtil.hexToBytes(utxo.txHash)));
                    Utils.uint32ToByteStreamLE(utxo.getVout(), stream);

                    // the length of byte array that follows, and this length is used by OP_PUSHDATA1
                    stream.write(0x17);
                    // the length of byte array that follows, and this length is used by cutting array
                    stream.write(0x16);
                    stream.write(NumericUtil.hexToBytes(redeemScripts.get(i)));
                    Utils.uint32ToByteStreamLE(utxo.getSequence(), stream);
                }

                // outputs
                // outputs size
                int outputSize = hasChange ? 2 : 1;
                stream.write(new VarInt(outputSize).encode());
                Utils.uint64ToByteStreamLE(BigInteger.valueOf(mAmount), stream);
                stream.write(new VarInt(targetScriptPubKey.length).encode());
                stream.write(targetScriptPubKey);
                if (hasChange) {
                    Utils.uint64ToByteStreamLE(BigInteger.valueOf(changeAmount), stream);
                    stream.write(new VarInt(changeScriptPubKey.length).encode());
                    stream.write(changeScriptPubKey);
                }

                // the first stream is used to calc the segwit hash
                if (idx == 0) {
                    for (int i = 0; i < witnesses.size(); i++) {
//                        BigInteger prvKey = Metadata.FROM_WIF.equals(wallet.getMetadata().getSource()) ? prvKeys.get(0) : prvKeys.get(i);
                        BigInteger prvKey = prvKeys.get(i);

                        ECKey ecKey = ECKey.fromPrivate(prvKey);
                        byte[] wit = witnesses.get(i);
                        stream.write(new VarInt(2).encode());
                        stream.write(new VarInt(wit.length).encode());
                        stream.write(wit);
                        stream.write(new VarInt(ecKey.getPubKey().length).encode());
                        stream.write(ecKey.getPubKey());
                    }
                }

                Utils.uint32ToByteStreamLE(mLocktime, stream);
            }
            byte[] signed = serialStreams[0].toByteArray();
            String signedHex = NumericUtil.bytesToHex(signed);
            String wtxID = NumericUtil.bytesToHex(Sha256Hash.hashTwice(signed));
            wtxID = NumericUtil.beBigEndianHex(wtxID);
            String txHash = NumericUtil.bytesToHex(Sha256Hash.hashTwice(serialStreams[1].toByteArray()));
            txHash = NumericUtil.beBigEndianHex(txHash);
//            return new TxSignResult(signedHex, txHash, wtxID);
            return txHash + "," + signedHex;
        } catch (IOException ex) {
            throw new TokenException("OutputStream error");
        }
    }


    private void collectPrvKeysAndAddress(String segWit, String xprv) {
        prvKeys = new ArrayList<>(mOutputs.size());
        DeterministicKey xprvKey = DeterministicKey.deserializeB58(xprv, network);
        DeterministicKey changeKey = HDKeyDerivation.deriveChildKey(xprvKey, ChildNumber.ZERO);
        DeterministicKey indexKey = HDKeyDerivation.deriveChildKey(changeKey, new ChildNumber(0, false));
        if (Metadata.P2WPKH.equals(segWit)) {
            changeAddress = new SegWitBitcoinAddressCreator(network).fromPrivateKey(indexKey);
        } else {
            changeAddress = indexKey.toAddress(network);
        }

        for (UTXO output : mOutputs) {
            String derivedPath = output.getDerivedPath().trim();
            String[] pathIdxs = derivedPath.replace('/', ' ').split(" ");
            int accountIdx = Integer.parseInt(pathIdxs[0]);
            int changeIdx = Integer.parseInt(pathIdxs[1]);

            DeterministicKey accountKey = HDKeyDerivation.deriveChildKey(xprvKey, new ChildNumber(accountIdx, false));
            DeterministicKey externalChangeKey = HDKeyDerivation.deriveChildKey(accountKey, new ChildNumber(changeIdx, false));
            prvKeys.add(externalChangeKey.getPrivKey());
        }
    }

}
