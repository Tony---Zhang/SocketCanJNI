package com.android.socketcan;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.init_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    final String device = "can0";
                    final int bitrate = 125000;
                    CanUtils.config(device, bitrate);
                    CanUtils.init(device);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        findViewById(R.id.send_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    CanUtils.sendData(0x05, new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
