package com.example.bluetoothserver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class BluetoothServer {
    private AppCompatActivity mActivity;
    private BluetoothAdapter mBluetoothAdapter;
    BTServerThread btServerThread;

    RemoteControlEventListener mRemoteControlEventListener;
    Handler mUiHandler;


    BluetoothServer(AppCompatActivity activity) {
        mActivity = activity;
    }

    final int REQUEST_ENABLE_BT = 1;

    public static final int BT_SERVER_OK = 0;
    public static final int BT_SERVER_ERROR = -1;

    int initialize() {
        BluetoothManager bluetoothManager =
                (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.d(MainActivity.class.getName(), "Device does not support Bluetooth");
            //textView_Status.setText("Status: Device does not support Bluetooth.");
            return BT_SERVER_ERROR;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            return BT_SERVER_ERROR;
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            mActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        return BT_SERVER_OK;
    }

    void setRemoteControlEventListener(RemoteControlEventListener listener, Handler uiHandler){
        mRemoteControlEventListener = listener;
        mUiHandler = uiHandler;
    }

    int start() {
        btServerThread = new BTServerThread();
        btServerThread.start();
        return BT_SERVER_OK;
    }

    int stop() {
        if( btServerThread != null){
            btServerThread.cancel();
        }
        return BT_SERVER_OK;
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
                        processConnect();

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

    }

    private void processBtCommand(){
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mRemoteControlEventListener.onCommandTakePicture();
            }
        });
    }

    private void processConnect(){
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mRemoteControlEventListener.onConnect();
            }
        });
    }

    private void setStatusTextView(final String str){
//        mUiHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                textView_Status.setText(str);
//            }
//        });
    }
}

