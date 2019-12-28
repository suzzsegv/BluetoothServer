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

    final int REQUEST_ENABLE_BT = 1;

    BluetoothServer(AppCompatActivity activity) {
        mActivity = activity;
    }

    void setRemoteControlEventListener(RemoteControlEventListener listener, Handler uiHandler){
        mRemoteControlEventListener = listener;
        mUiHandler = uiHandler;
    }

    synchronized void start() throws IOException {
        if (mBluetoothAdapter == null) {
            BluetoothManager bluetoothManager =
                    (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                Log.d(MainActivity.class.getName(), "Device does not support Bluetooth");
                throw new IOException("Device does not support Bluetooth.");
            }
        }
        btServerThread = new BTServerThread();
        btServerThread.start();
    }

    synchronized void stop() {
        if( btServerThread != null){
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

        BTServerThread(){
            BluetoothServerSocket socket = null;
            try {
                socket = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(
                           BT_NAME, BT_UUID);
            } catch (IOException e) {
                Log.e(TAG, "listen() failed", e);
            }
            bluetoothServerSocket = socket;
        }

        public void run() {
            byte[] incomingBuff = new byte[64];

            try {
                while (true) {
                    if(bluetoothServerSocket == null){
                        break;
                    }
                    try {
                        bluetoothSocket = bluetoothServerSocket.accept();
                        processConnect();

                        inputStream = bluetoothSocket.getInputStream();
                        outputStream = bluetoothSocket.getOutputStream();

                        while (true) {
                            int incomingBytes = inputStream.read(incomingBuff);
                            byte[] buff = new byte[incomingBytes];
                            System.arraycopy(incomingBuff, 0, buff, 0, incomingBytes);
                            processBtCommand();
                            //String cmd = new String(buff, StandardCharsets.UTF_8);

                            //String resp = processCommand(cmd);
                            //outputStream.write(resp.getBytes());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "accept() failed", e);
                    }

                    if (bluetoothSocket != null) {
                        try {
                            bluetoothSocket.close();
                            bluetoothSocket = null;
                        } catch (IOException e) {
                        }
                        Log.d(TAG, "Close socket.");
                    }

                    if (Thread.interrupted()) {
                        break;
                    }

                    // Bluetooth connection broke. Start Over in a few seconds.
                    Thread.sleep(3 * 1000);
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Cancelled ServerThread");
            }

            Log.d(TAG, "ServerThread exit");
        }

        void cancel() {
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "bluetoothServerSocket close() failed", e);
            }
            interrupt();
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

