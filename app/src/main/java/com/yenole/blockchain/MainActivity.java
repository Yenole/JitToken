package com.yenole.blockchain;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.yenole.blockchain.wallet.ETHUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("TAG", ETHUtil.GenerateTokenData("0x84EfBF21b5a93F94C849D848366d6EcA83579393", 1000000));
    }

}
