package com.example.bluetoothserver;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothServer mBluetoothServer;
    TextView textView_Status;
    RemoteControlEventListener mRemoteControlListener;
    Handler mUiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_Status = findViewById(R.id.textView_Status);
        textView_Status.setText("Status:");

        mBluetoothServer = new BluetoothServer(this);
        int result = mBluetoothServer.initialize();
        if (result == mBluetoothServer.BT_SERVER_ERROR) {
            return;
        }

        mRemoteControlListener = new BtRemoteControllEventlistener();
        mUiHandler = new Handler(Looper.getMainLooper());
        mBluetoothServer.setRemoteControlEventListener(mRemoteControlListener, mUiHandler);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        switch (requestCode) {
//            case REQUEST_ENABLE_BT:
//                if (resultCode == Activity.RESULT_CANCELED) {
//                    Toast.makeText(this, "Bluetooth を使用できません。", Toast.LENGTH_LONG).show();
//                    // finish();
//                    return;
//                }
//                break;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    @Override
    protected void onResume() {
        super.onResume();

        mBluetoothServer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mBluetoothServer.stop();
    }

    public class BtRemoteControllEventlistener implements RemoteControlEventListener {
        @Override
        public void onCommandTakePicture() {
            textView_Status.setText("Status: TakePicture コマンドを受信しました。");
        }
        @Override
        public void onConnect() {
            textView_Status.setText("Status: TakePicture コマンドを受信しました。");
        }
    }
}
