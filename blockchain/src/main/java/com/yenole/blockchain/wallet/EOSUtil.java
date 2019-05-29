package com.yenole.blockchain.wallet;

import com.yenole.blockchain.foundation.utils.NumericUtil;
import com.yenole.blockchain.wallet.transaction.eos.EOSKey;

public class EOSUtil {
    public static String randomPrivate() {
        return EOSKey.fromPrivate(NumericUtil.generateRandomBytes(32)).toBase58();
    }

    public static String private2Public(String prikey) {
        return EOSKey.fromWIF(prikey).getPublicKeyAsHex();
    }
}
