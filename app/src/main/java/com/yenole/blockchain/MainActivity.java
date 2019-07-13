package com.yenole.blockchain;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.yenole.blockchain.wallet.BTCUtil;
import com.yenole.blockchain.wallet.DCRUtil;
import com.yenole.blockchain.wallet.transaction.dcr.DecredTransaction;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        BTCUtil.randomMnemonic();

        String mnemonic = DCRUtil.randomMnemonic();
        Log.i("TAG", mnemonic);
//        Log.i("TAG", DCRUtil.address(getFilesDir() + "/wallet", mnemonic, "testnet3"));

    }

    public void Click(View view) {
        DecredTransaction tx = new DecredTransaction(getFilesDir().toString());
        tx.setAddress("TsTLP4k6hopq8Ye6AU3kXxwmATd2ngpC6Nh");
        tx.setAmount(5);
        tx.unspent("2de1cd3d525894e10ca69e826f3e14e66257d962fadb8be1fe4797f348317865", "76a914657272bf4e959035fce56900f431ab1912a458d188ac", 0, 23.08884055);
        String raw = tx.signTransaction("tempest borderline crowfoot document chisel combustion pupil molecule bison resistor bookshelf Wilmington shamrock trombonist ringbolt maritime apple document concert microwave checkup company choking Eskimo classroom applicant trauma aftermath snowcap graduate gazelle unravel payday", "testnet3");
        Log.i("TAG", raw);
    }
}
