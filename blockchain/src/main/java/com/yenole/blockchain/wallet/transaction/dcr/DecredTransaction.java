package com.yenole.blockchain.wallet.transaction.dcr;

import android.util.Log;

import java.util.LinkedList;

import dcrlibwallet.Dcrlibwallet;
import dcrlibwallet.LibWallet;

public class DecredTransaction {
    private String dir;
    private LinkedList<UTXO> utxos = new LinkedList<>();
    private String address;
    private double amount;
    private int account;


    public DecredTransaction(String dir) {
        this.dir = dir;
    }

    public void unspent(String txid, String scriptPubKey, int vout, double amount) {
        this.utxos.add(new UTXO(txid, scriptPubKey, vout, amount));
    }

    public String signTransaction(String mnemonic, String netparam) {
        LibWallet lw = null;
        try {
            lw = Dcrlibwallet.newLibWallet(String.format("%s/wallet", dir), "", netparam);
            lw.createWallet("DCR", mnemonic);
            Log.i("TAG", lw.currentAddress(0));
            for (UTXO utxo : this.utxos) {
                lw.pushUnspent(utxo.getTxid(), utxo.getPubkey(), utxo.getVout(), utxo.getAmount());
            }
            String raw = lw.pushTransaction(amount, account, address, "DCR".getBytes());
            lw.deleteWallet("DCR".getBytes());
            return raw;
        } catch (Exception ex) {
            try {
                if (lw != null && lw.walletExists()) {
                    lw.deleteWallet("DCR".getBytes());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public int getAccount() {
        return account;
    }

    public void setAccount(int account) {
        this.account = account;
    }
}


class UTXO {
    String Txid;
    String Pubkey;
    int Vout;
    double Amount;

    public UTXO(String txid, String pubkey, int vout, double amount) {
        Txid = txid;
        Pubkey = pubkey;
        Vout = vout;
        Amount = amount;
    }

    public String getTxid() {
        return Txid;
    }

    public void setTxid(String txid) {
        Txid = txid;
    }

    public String getPubkey() {
        return Pubkey;
    }

    public void setPubkey(String pubkey) {
        Pubkey = pubkey;
    }

    public int getVout() {
        return Vout;
    }

    public void setVout(int vout) {
        Vout = vout;
    }

    public double getAmount() {
        return Amount;
    }

    public void setAmount(double amount) {
        Amount = amount;
    }
}