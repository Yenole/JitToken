package com.yenole.blockchain.foundation.crypto;



interface KDFParams {
    int DK_LEN = 32;

    int getDklen();

    String getSalt();

    void validate();
}
