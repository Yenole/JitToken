package com.yenole.blockchain.wallet.transaction.eos;

import com.yenole.blockchain.foundation.crypto.Hash;
import com.yenole.blockchain.foundation.utils.ByteUtil;
import com.yenole.blockchain.foundation.utils.NumericUtil;
import com.yenole.blockchain.wallet.model.TokenException;

import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.spongycastle.crypto.digests.RIPEMD160Digest;
import org.spongycastle.crypto.params.ECDomainParameters;
import org.spongycastle.crypto.params.ECPrivateKeyParameters;
import org.spongycastle.math.ec.ECPoint;

import java.math.BigInteger;
import java.util.Arrays;

public class EOSSign {

    private static class SigChecker {
        BigInteger e;
        BigInteger privKey;

        BigInteger r;
        BigInteger s;

        SigChecker(byte[] hash, BigInteger privKey) {
            this.e = new BigInteger(1, hash);
            this.privKey = privKey;
        }

        public static ECPoint multiply(ECPoint p, BigInteger k) {
            BigInteger e = k;
            BigInteger h = e.multiply(BigInteger.valueOf(3));

            ECPoint neg = p.negate();
            ECPoint R = p;

            for (int i = h.bitLength() - 2; i > 0; --i) {
                R = R.twice();

                boolean hBit = h.testBit(i);
                boolean eBit = e.testBit(i);

                if (hBit != eBit) {
                    R = R.add(hBit ? p : neg);
                }
            }

            return R;
        }

        boolean checkSignature(ECDomainParameters curveParam, BigInteger k) {

            ECPoint Q = multiply(curveParam.getG(), k);// Secp256k1Param.G, k);
            if (Q.isInfinity()) return false;

            r = Q.getX().toBigInteger().mod(curveParam.getN());// Secp256k1Param.n );
            if (r.signum() == 0) return false;


            s = k.modInverse(curveParam.getN())// Secp256k1Param.n)
                    .multiply(e.add(privKey.multiply(r)))
                    .mod(curveParam.getN());// Secp256k1Param.n);

            if (s.signum() == 0) return false;

            return true;
        }

        public boolean isRSEachLength(int length) {
            return (r.toByteArray().length == length) && (s.toByteArray().length == length);
        }
    }

    public static String sign(byte[] dataSha256, String wif) {
        SignatureData signatureData = signAsRecoverable(dataSha256, EOSKey.fromWIF(wif).getECKey());
        byte[] sigResult = ByteUtil.concat(NumericUtil.intToBytes(signatureData.getV()), signatureData.getR());
        sigResult = ByteUtil.concat(sigResult, signatureData.getS());
        return serialEOSSignature(sigResult);
    }

    public static String sign(byte[] dataSha256, byte[] prvKey) {
        ECKey ecKey = EOSKey.fromPrivate(prvKey).getECKey();
        SignatureData signatureData = signAsRecoverable(dataSha256, ecKey);
        byte[] sigResult = ByteUtil.concat(NumericUtil.intToBytes(signatureData.getV()), signatureData.getR());
        sigResult = ByteUtil.concat(sigResult, signatureData.getS());
        return serialEOSSignature(sigResult);
    }


    private static SignatureData signAsRecoverable(byte[] value, ECKey ecKey) {
        int recId = -1;
        int nonce = 0;
        SigChecker checker = new SigChecker(value, ecKey.getPrivKey());
        ECDomainParameters curveParam = new ECPrivateKeyParameters(ecKey.getPrivKey(), ECKey.CURVE).getParameters();
        BigInteger halfCurveOrder = curveParam.getN().shiftRight(1);
        while (true) {
            deterministicGenerateK(curveParam, value, ecKey.getPrivKey(), checker, nonce++);

            if (checker.s.compareTo(halfCurveOrder) > 0) {//  Secp256k1Param.HALF_CURVE_ORDER) > 0) {
                checker.s = curveParam.getN().subtract(checker.s);//   Secp256k1Param.n.subtract(checker.s);
            }

            if (checker.isRSEachLength(32)) {
                break;
            }
        }
//        ECKey.ECDSASignature sig = (value, ecKey.getPrivKey());
        ECKey.ECDSASignature sig = new ECKey.ECDSASignature(checker.r, checker.s).toCanonicalised();
        for (int i = 0; i < 4; i++) {
            ECKey recoverKey = ECKey.recoverFromSignature(i, sig, Sha256Hash.wrap(value), false);
            if (recoverKey != null && recoverKey.getPubKeyPoint().equals(ecKey.getPubKeyPoint())) {
                recId = i;
                break;
            }
        }

        if (recId == -1) {
            throw new TokenException("Could not construct a recoverable key. This should never happen.");
        }
        int headerByte = recId + 27 + 4;
        // 1 header + 32 bytes for R + 32 bytes for S
        byte v = (byte) headerByte;
        byte[] r = NumericUtil.bigIntegerToBytesWithZeroPadded(sig.r, 32);
        byte[] s = NumericUtil.bigIntegerToBytesWithZeroPadded(sig.s, 32);

        return new SignatureData(v, r, s);

    }

//    private static ECKey.ECDSASignature eosSign(byte[] input, BigInteger privateKeyForSigning) {
//    EOSECDSASigner signer = new EOSECDSASigner(new MyHMacDSAKCalculator(new SHA256Digest()));
//    ECPrivateKeyParameters privKey = new ECPrivateKeyParameters(privateKeyForSigning, CURVE);
//    signer.init(true, privKey);
//    BigInteger[] components = signer.generateSignature(input);
//    }

    private static BigInteger deterministicGenerateK(ECDomainParameters curveParam, byte[] hash, BigInteger d, SigChecker checker, int nonce) {
        if (nonce > 0) {
            byte[] bytes = NumericUtil.intToBytes(nonce);
            EOSByteWriter writer = new EOSByteWriter(hash.length + bytes.length);
            writer.putBytes(hash);
            writer.putBytes(bytes);
            hash = Hash.sha256(writer.toBytes());
        }

        byte[] dBytes = d.toByteArray();

        // Step b
        byte[] v = new byte[32];
        Arrays.fill(v, (byte) 0x01);

        // Step c
        byte[] k = new byte[32];
        Arrays.fill(k, (byte) 0x00);

        // Step d
        EOSByteWriter bwD = new EOSByteWriter(32 + 1 + 32 + 32);
        bwD.putBytes(v);
        bwD.put((byte) 0x00);
        bwD.putBytes(dBytes);
        bwD.putBytes(hash);
        k = Hash.hmacSHA256(k, bwD.toBytes());

        // Step e
        v = Hash.hmacSHA256(k, v);

        // Step f
        EOSByteWriter bwF = new EOSByteWriter(32 + 1 + 32 + 32);
        bwF.putBytes(v);
        bwF.put((byte) 0x01);
        bwF.putBytes(dBytes);
        bwF.putBytes(hash);
        k = Hash.hmacSHA256(k, bwF.toBytes());

        // Step g
        v = Hash.hmacSHA256(k, v);

        // Step H2b
        v = Hash.hmacSHA256(k, v);

        BigInteger t = new BigInteger(1, v);

        // Step H3, repeat until T is within the interval [1, Secp256k1Param.n - 1]
        while ((t.signum() <= 0) || (t.compareTo(curveParam.getN()) >= 0) || !checker.checkSignature(curveParam, t)) {
            EOSByteWriter bwH = new EOSByteWriter(32 + 1);
            bwH.putBytes(v);
            bwH.put((byte) 0x00);
            k = Hash.hmacSHA256(k, bwH.toBytes());
            v = Hash.hmacSHA256(k, v);

            // Step H1/H2a, again, ignored as tlen === qlen (256 bit)
            // Step H2b again
            v = Hash.hmacSHA256(k, v);

            t = new BigInteger(v);
        }
        return t;
    }

    private static String serialEOSSignature(byte[] data) {
        byte[] toHash = ByteUtil.concat(data, "K1".getBytes());
        RIPEMD160Digest digest = new RIPEMD160Digest();
        digest.update(toHash, 0, toHash.length);
        byte[] out = new byte[20];
        digest.doFinal(out, 0);
        byte[] checksumBytes = Arrays.copyOfRange(out, 0, 4);
        data = ByteUtil.concat(data, checksumBytes);
        return "SIG_K1_" + Base58.encode(data);
    }
}
