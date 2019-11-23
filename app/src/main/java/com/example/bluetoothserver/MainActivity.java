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

    private BluetoothAdapter mBluetoothAdapter;
    final int REQUEST_ENABLE_BT = 1;
    final int REQUEST_PERMISSION = 1000;
    TextView textView_Status;
    BTServerThread btServerThread;
    Handler mUiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView_Status = findViewById(R.id.textView_Status);
        textView_Status.setText("Status:");

        BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.d(MainActivity.class.getName(), "Device does not support Bluetooth");
            textView_Status.setText("Status: Device does not support Bluetooth.");
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        mUiHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, "Bluetooth を使用できません。", Toast.LENGTH_LONG).show();
                    // finish();
                    return;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mBluetoothAdapter == null) {
            return;
        }

        if(btServerThread == null){
            btServerThread = new BTServerThread();
            btServerThread.start();
        }else{
//            btServerThread.cancel();
//            btServerThread = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(btServerThread == null){
            btServerThread.cancel();
            btServerThread = null;
        }
    }

    public class BTServerThread extends Thread {
        static final String TAG = "BTTest1Server";
        static final String BT_NAME = "BTTEST1";
        UUID BT_UUID = UUID.fromString(
                "41eb5f39-6c3a-4067-8bb9-bad64e6e0908");

        BluetoothServerSocket bluetoothServerSocket;
        BluetoothSocket bluetoothSocket;
        InputStream inputStream;
        OutputStream outputStream;

        public void run() {

            byte[] incomingBuff = new byte[64];

            try {
                while (true) {

                    if (Thread.interrupted()) {
                        break;
                    }

                    try {

                        bluetoothServerSocket
                                = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                                BT_NAME,
                                BT_UUID);

                        bluetoothSocket = bluetoothServerSocket.accept();
                        setStatusTextView("Status: クライアントが接続しました。");

                        bluetoothServerSocket.close();
                        bluetoothServerSocket = null;

                        inputStream = bluetoothSocket.getInputStream();
                        outputStream = bluetoothSocket.getOutputStream();

                        while (true) {

                            if (Thread.interrupted()) {
                                break;
                            }

                            int incomingBytes = inputStream.read(incomingBuff);
                            byte[] buff = new byte[incomingBytes];
                            System.arraycopy(incomingBuff, 0, buff, 0, incomingBytes);
                            processBtCommand();
                            //String cmd = new String(buff, StandardCharsets.UTF_8);

                            //String resp = processCommand(cmd);
                            //outputStream.write(resp.getBytes());
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (bluetoothSocket != null) {
                        try {
                            bluetoothSocket.close();
                            bluetoothSocket = null;
                        } catch (IOException e) {
                        }
                        setStatusTextView("Status: クライアントが切断されました。");
                    }

                    // Bluetooth connection broke. Start Over in a few seconds.
                    Thread.sleep(3 * 1000);
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Cancelled ServerThread");
            }

            Log.d(TAG, "ServerThread exit");
        }

        public void cancel() {
            if (bluetoothServerSocket != null) {
                try {
                    bluetoothServerSocket.close();
                    bluetoothServerSocket = null;
                    super.interrupt();
                } catch (IOException e) {}
            }
        }

        private void processBtCommand(){
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    textView_Status.setText("Status: コマンドを受信しました。");
                }
            });
        }

        private void setStatusTextView(final String str){
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    textView_Status.setText(str);
                }
            });
        }
    }
}
