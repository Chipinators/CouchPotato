package paperprisoners.couchpotato;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by chris on 7/16/2016.
 */
public class BluetoothService {
    private static final String TAG = "BluetoothService";
    private static final UUID uuid = UUID.fromString("0b8c8517-39b8-4b97-a53f-821f76661ed6");
    private static final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

    private static AcceptThread mAcceptThread;
    private static ConnectThread mConnectThread;
    private static ConnectedThread mConnectedThread;
    private static ArrayList<ConnectedThread> mConnectedDevices = new ArrayList<>();

    public static ArrayList<MessageListener> listeners = new ArrayList<>();
    private static int maxPlayers = 7;
    public static String DELIM = "\\|/";

    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msgRead) {
            Log.i(TAG, "HANDLER handleMessage called");

            byte[] msg = (byte[]) msgRead.obj;
            //String msg = new String(Arrays.copyOfRange(buffer, 0, bytes)); //TODO: CHECK
            String[] split = TextUtils.split(new String(msg), Pattern.quote(BluetoothService.DELIM));
            Log.i(TAG, "Message: " + TextUtils.join("---", split));
            try {
                int player = Integer.parseInt(split[0]);
                Log.i(TAG, "Player: " + player);
                int type = Integer.parseInt(split[1]);
                Log.i(TAG, "Message Type: " + type);
                String[] content = Arrays.copyOfRange(split, 2, split.length);
                Log.i(TAG, "Message Content: " + content.toString());
                for (MessageListener m : BluetoothService.listeners) {
                    if (m != null)
                        m.onReceiveMessage(player, type, content);
                }
            } catch (NumberFormatException nf) {
                Log.e(TAG, "Could not parse message header");
            }
        }
    };

    //Constructor that gets the bluetooth adapter of the device
    private BluetoothService() {
    }

    public static BluetoothAdapter getmAdapter() {
        return mAdapter;
    }

    public static synchronized void start() {
        Log.d(TAG, "start");
        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    public static synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);
        // Start the thread to connect with the given device
        try {
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
        } catch (Exception e) {
        }
    }

    public static synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        if (mConnectedDevices.contains(mConnectedThread)) {
            mConnectedDevices.get(mConnectedDevices.indexOf(mConnectedThread)).cancel();
            Log.d(TAG, "Duplicate Device tried to connect");
            mConnectedDevices.remove(mConnectedThread);
            Log.d(TAG, "Number of Devices: " + mConnectedDevices.size());
        }
        mConnectedDevices.add(mConnectedThread);

        /*
        Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        msg.
        mHandler.sendMessage(msg);
        */
        Log.d(TAG, "Number of Devices: " + mConnectedDevices.size());
        mConnectedThread.start();

        //TODO: HANDLE TRANSMISSION BETWEEN DEVICES
    }

    public static synchronized void stop() {
        Log.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
    }

    public static void writeToServer(String player, int type, String[] content) {
        String output = player + DELIM + type + DELIM + TextUtils.join(DELIM, content);
        ConnectedThread r = mConnectedThread;
        r.write(output.getBytes());
    }

    public static void writeToClients(int type, String[] content) {
        String output;
        for (ConnectedThread device : mConnectedDevices) {
            output = 1 + DELIM + type + DELIM + TextUtils.join(DELIM, content);
            Log.i(TAG, "CREATING WRITE MESSAGE - "+ output);
            device.write(output.getBytes());
        }
    }

    private static class AcceptThread extends Thread {
        private final BluetoothServerSocket mServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord("Couch Potato", uuid);
            } catch (IOException e) {
            }
            mServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket = null;

            while (true) { //mState != STATE_CONNECTED
                try {
                    Log.i("Log", "SERVER SOCKET STARTED ");
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    Log.i("Log", "SERVER SOCKET FAILED");
                    break;
                }

                if (socket != null) {
                    //TODO: MANAGE CONNECTED SOCKETS
                    Log.i("Log", "SOCKET CONNECTED ");
                    connected(socket, socket.getRemoteDevice());


                    if (mConnectedDevices.size() >= maxPlayers) {
                        try {
                            Log.i("Log", "CLOSING SERVER SOCKET");
                            mServerSocket.close();
                        } catch (IOException e) {
                        }
                    }
                    socket = null;
                }
            }
            Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "cancel " + this);
            try {
                mServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    } //End Server

    private static class ConnectThread extends Thread {
        private final BluetoothDevice mDevice;
        private final BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mDevice = device;

            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            mAdapter.cancelDiscovery();

            try {
                Log.i("Log", "SOCKET TRYING TO CONNECT ");
                mSocket.connect();
            } catch (IOException connectionException) {
                try {
                    Log.i("Log", "SOCKET CONNECTION FAILED");
                    mSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                //connectionFailed();
                return;
            }

            synchronized (this) {
                mConnectThread = null;
            }

            Log.i("Log", "SOCKET CONNECTED");
            connected(mSocket, mDevice);
        }

        public void cancel() {
            try {
                mConnectedDevices.remove(mSocket.getRemoteDevice());
                mSocket.close();
            } catch (IOException e) {
            }
        }
    } //End ConnectThread


    private static class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mInStream = tmpIn;
            mOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    Log.i(TAG, "BEGIN try to read mConnectedThread");
                    bytes = mInStream.read(buffer);
                    //TODO: HANDLE DATA MESSAGES
                    Log.i(TAG, "MESSAGE RECEIVED FROM REMOTE: Attempting to process - " + bytes + " bytes");
                    if(mHandler == null) {
                        Log.e(TAG, "mHandler received a null");
                    }else {
                        mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    //connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] out) {
            try {
                mOutStream.write(out);
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mConnectedDevices.remove(mSocket.getRemoteDevice());
                mSocket.close();
            } catch (IOException e) {
            }
        }
    } //End ConnectedThread

    //Makes the device discoverable for 300 seconds
    public static void makeDiscoverable(Context context, BroadcastReceiver bCReciever) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        context.startActivity(discoverableIntent);
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        context.registerReceiver(bCReciever, intentFilter);
        Log.i("Log", "Discoverable ");
    }

    //Starts a search for nearby Bluetooth devices that are discoverable
    public static void startSearching(Context context, BroadcastReceiver bCReciever) {
        Log.i("Log", "in the start searching method");
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(bCReciever, intentFilter);
        mAdapter.startDiscovery();
    }


    public static void addMessageListener(MessageListener m) {
        listeners.add(m);
    }
}
