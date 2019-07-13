package com.yenole.blockchain.wallet;


import dcrlibwallet.Dcrlibwallet;
import dcrlibwallet.LibWallet;

public class DCRUtil {
    static final String PASS = "DCR";

    public static String randomMnemonic() {
        try {
            return Dcrlibwallet.generateSeed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String address(String dir, String mnemonic, String netparam) {
        LibWallet lw = null;
        try {
            lw= Dcrlibwallet.newLibWallet(dir, "", netparam);
            lw.createWallet(PASS, mnemonic);
            String address = lw.currentAddress(0);
            lw.deleteWallet(PASS.getBytes());
            return address;
        } catch (Exception ex) {
            try {
                if (lw!=null && lw.walletExists()){
                    lw.deleteWallet(PASS.getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

}
