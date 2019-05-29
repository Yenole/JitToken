package com.yenole.blockchain.foundation.crypto;

import com.google.common.base.Strings;
import com.yenole.blockchain.wallet.model.TokenException;

public class CipherParams {
    private String iv;

    CipherParams() {
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public void validate() {
        if (Strings.isNullOrEmpty(iv)) {
            throw new TokenException("CIPHER_FAIL");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CipherParams)) {
            return false;
        }

        CipherParams that = (CipherParams) o;

        return getIv() != null
                ? getIv().equals(that.getIv()) : that.getIv() == null;
    }

    @Override
    public int hashCode() {
        return getIv() != null ? getIv().hashCode() : 0;
    }
}
