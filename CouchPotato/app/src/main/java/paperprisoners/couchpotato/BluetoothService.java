package paperprisoners.couchpotato;

import android.app.Activity;
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
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by Chris Potter on 7/16/2016.
 * Creates a Bluetooth service that allows a one-to-many Bluetooth connection up to 7 devices
 */
public class BluetoothService {
    private static final String TAG = "BluetoothService";
    private static final UUID uuid = UUID.fromString("0b8c8517-39b8-4b97-a53f-821f76661ed6");
    private static final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private static final String defaultDeviceName = mAdapter.getName();

    private static AcceptThread mAcceptThread;
    private static ConnectThread mConnectThread;
    private static ConnectedThread mConnectedThread;
    private static HashMap<String, ConnectedThread> mConnectedDevices = new HashMap<>();

    public static HashSet<MessageListener> listeners = new HashSet<>();
    private static int maxPlayers = 7;
    public static String DELIM = "\\|/";

    //Handles messages received over Bluetooth and passes it to the proper MessageListener
    public static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msgRead) {
            if(Constants.debug) Log.i(TAG, "HANDLER handleMessage called");

            byte[] msg = (byte[]) msgRead.obj;
            String str = new String(Arrays.copyOfRange(msg, 0, msgRead.arg1));
            String[] split = TextUtils.split(str, Pattern.quote(BluetoothService.DELIM));
            if(Constants.debug) Log.i(TAG, "Message: " + TextUtils.join("---", split));
            try {
                int player = Integer.parseInt(split[0]);
                if(Constants.debug) Log.i(TAG, "Player: " + player);
                int type = Integer.parseInt(split[1]);
                if(Constants.debug) Log.i(TAG, "Message Type: " + type);
                String[] content = Arrays.copyOfRange(split, 2, split.length);
                if(Constants.debug) Log.i(TAG, "Message Content: " + Arrays.toString(split));
                if(Constants.debug) Log.i(TAG, "NUMBER OF MESSAGE LISTENERS = " + listeners.size());
                scanMessageListeners(player,type,content);
            } catch (NumberFormatException nf) {
                if(Constants.debug) Log.e(TAG, "Could not parse message header");
            }
        }
    };

    //Constructor that gets the bluetooth adapter of the device
    private BluetoothService() {
    }

    public static BluetoothAdapter getmAdapter() {
        return mAdapter;
    }

    //Returns the device name that the device had on start-up
    public static String getDefaultDeviceName() {
        return defaultDeviceName;
    }

    //Host - starts a server listening thread
    public static synchronized void start() {
        if(Constants.debug) Log.d(TAG, "start");
        //If there is currently an accept thread open close it
        if(mAcceptThread != null){
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        // Start the thread to listen on a BluetoothServerSocket
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    //Client - attempts to connect to host device
    public static synchronized void connect(BluetoothDevice device) {
        if(Constants.debug) Log.d(TAG, "connect to: " + device);
        // Start the thread to connect with the given device
        try {
            mConnectThread = new ConnectThread(device);
            mConnectThread.start();
        } catch (Exception e) {
        }
    }

    //Connects Host and Client
    public static synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if(Constants.debug) Log.d(TAG, "connected");
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        /*
        if (mConnectedDevices.contains(mConnectedThread)) {
            mConnectedThread.cancel();
            if(Constants.debug) Log.d(TAG, "Duplicate Device tried to connect");
            mConnectedDevices.remove(mConnectedThread);
            if(Constants.debug) Log.d(TAG, "Number of Devices: " + mConnectedDevices.size());
        } else {
            if(Constants.debug) Log.d(TAG, "No Duplicate Found, Adding Connection To List");
            */
            mConnectedDevices.put(device.getAddress(), mConnectedThread);
        //}
        if(Constants.debug) Log.d(TAG, "Number of Devices: " + mConnectedDevices.size());
        mConnectedThread.start();

        //TODO: HANDLE TRANSMISSION BETWEEN DEVICES
    }

    //Stops BluetoothService
    public static synchronized void stop() {
        if(Constants.debug) Log.d(TAG, "stop");
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
        mConnectedDevices.clear();
    }
    
    //(Players ID, Message Type, Conent)
    public static void writeToServer(String player, int type, String[] content) {
        String output = player + DELIM + type + DELIM + TextUtils.join(DELIM, content);
        if(Constants.debug) Log.i(TAG, "CLIENT WRITING TO HOST: " + output);
        ConnectedThread r = mConnectedThread;
        r.write(output.getBytes());
        if(Constants.debug) Log.i(TAG, "CLIENT WRITING TO HOST: " + r.getName() + "    ---   OUTPUT:  "+ output);
    }

    //(Message Type, Content)
    public static void writeToClients(int type, String[] content) {
        String output;
        if(Constants.debug) Log.i(TAG, "NUMBER OF CONNECTED DEVICES: " + mConnectedDevices.size());
        for(Map.Entry<String, ConnectedThread> device : mConnectedDevices.entrySet()) {
            try {
                output = "0" + DELIM + type + DELIM + TextUtils.join(DELIM, content);
                if(Constants.debug) Log.i(TAG, "CREATING WRITE MESSAGE - " + output);
                device.getValue().write(output.getBytes());
            } catch(Exception e){
                device.getValue().cancel();
                mConnectedDevices.remove(device);
            }
        }
    }

    //(MAC Address, Player ID, Message Type, Content)
    public static void write(String macAddress, String player, int type, String[] content){
        try {
            if(Constants.debug) Log.i(TAG, "NUMBER OF CONNECTED DEVICES: " + mConnectedDevices.size());
            String output = player + DELIM + type + DELIM + TextUtils.join(DELIM, content);
            if(Constants.debug) Log.i(TAG, "SINGLE WRITE CALLED: " + output);
            mConnectedDevices.get(macAddress).write(output.getBytes());
        } catch (Exception e){
            if(Constants.debug) Log.e(TAG, "COULD NOT WRITE TO CLIENT WITH MAC ADDRESS: " + macAddress);
        }
    }

    //Server Thread
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
                    if(Constants.debug) Log.i(TAG, "SERVER SOCKET STARTED ");
                    socket = mServerSocket.accept();
                } catch (IOException e) {
                    if(Constants.debug) Log.i(TAG, "SERVER SOCKET FAILED");
                    break;
                }

                if (socket != null) {
                    //TODO: MANAGE CONNECTED SOCKETS
                    if(Constants.debug) Log.i(TAG, "SOCKET CONNECTED ");
                    connected(socket, socket.getRemoteDevice());


                    if (mConnectedDevices.size() >= maxPlayers) {
                        try {
                            if(Constants.debug) Log.i(TAG, "CLOSING SERVER SOCKET");
                            mServerSocket.close();
                        } catch (IOException e) {
                        }
                    }
                    socket = null;
                }
            }
            if(Constants.debug) Log.i(TAG, "END mAcceptThread");
        }

        public void cancel() {
            if(Constants.debug) Log.d(TAG, "cancel " + this);
            try {
                mServerSocket.close();
            } catch (IOException e) {
                if(Constants.debug) Log.e(TAG, "close() of server failed", e);
            }
        }
    } //End Server

    //Client Thread
    private static class ConnectThread extends Thread {
        private final BluetoothDevice mDevice;
        private final BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mDevice = device;

            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(uuid);
            } catch (IOException e) {
                if(Constants.debug) Log.e(TAG, "create() failed", e);
            }
            mSocket = tmp;
        }

        public void run() {
            if(Constants.debug) Log.i(TAG, "BEGIN mConnectThread");
            mAdapter.cancelDiscovery();

            try {
                if(Constants.debug) Log.i(TAG, "SOCKET TRYING TO CONNECT ");
                mSocket.connect();
            } catch (IOException connectionException) {
                try {
                    if(Constants.debug) Log.i(TAG, "SOCKET CONNECTION FAILED");
                    mSocket.close();
                } catch (IOException e2) {
                    if(Constants.debug) Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                //connectionFailed();
                return;
            }

            synchronized (this) {
                mConnectThread = null;
            }

            if(Constants.debug) Log.i(TAG, "SOCKET CONNECTED");
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

    //Connection Thread
    private static class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mInStream;
        private final OutputStream mOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            if(Constants.debug) Log.d(TAG, "create ConnectedThread");
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                if(Constants.debug) Log.e(TAG, "temp sockets not created", e);
            }
            mInStream = tmpIn;
            mOutStream = tmpOut;
        }

        public void run() {
            if(Constants.debug) Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    if(Constants.debug) Log.i(TAG, "BEGIN try to read mConnectedThread");
                    bytes = mInStream.read(buffer);
                    if(Constants.debug) Log.i(TAG, "MESSAGE RECEIVED FROM REMOTE: Attempting to process - " + bytes + " bytes");
                    if(mHandler == null) {
                        if(Constants.debug) Log.e(TAG, "mHandler received a null");
                    }else {
                        if(Constants.debug) Log.i(TAG, "Obtain Message Reached");
                        /*Message msg = mHandler.obtainMessage(100, bytes, -1, buffer).;
                        mHandler.sendMessage(msg);
                        */
                        mHandler.obtainMessage(Constants.MESSAGE_READ, bytes,-1,buffer).sendToTarget();
                        if(Constants.debug) Log.i(TAG, "Obtain Message Finished");
                    }

                } catch (IOException e) {
                    if(Constants.debug) Log.e(TAG, "disconnected", e);
                    //connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] out) {
            try {
                mOutStream.write(out);
            } catch (IOException e) {
                if(Constants.debug) Log.e(TAG, "COULD NOT WRITE TO CLIENT - REMOVING CLIENT: " + this);
                cancel();
            }
        }

        public void cancel() {
            try {
                if(Constants.debug) Log.e(TAG, "CANCELING SOCKET - " + this);
                //mConnectedDevices.remove(this);
                mSocket.close();
            } catch (IOException e) {
            }
        }
    } //End ConnectedThread

    //Host - gets permissions to make itself discoverable
    public static void getDiscoverablePermissions(Activity activity){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 180);
        //context.startActivity(discoverableIntent);
        activity.startActivityForResult(discoverableIntent, Constants.REQUEST_DISCOVERABILITY);
    }

    //Host - Makes the device discoverable for 300 seconds
    public static void startDiscoverable(Context context, BroadcastReceiver bCReciever) {
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        context.registerReceiver(bCReciever, intentFilter);
        IntentFilter disconnect = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(bCReciever,disconnect);
        if(Constants.debug) Log.i(TAG, "DEVICE IS NOW DISCOVERABLE");
    }

    //Host - Stops host
    public static void stopDiscoverable(Context context, BroadcastReceiver bCReciever){
        mAcceptThread.cancel();
        context.unregisterReceiver(bCReciever);
        mAdapter.setName(getDefaultDeviceName());
        if(Constants.debug) Log.i(TAG, "DISCOVERY ENDED");
    }

    //Client - Starts a search for nearby Bluetooth devices that are discoverable
    public static void startSearching(Context context, BroadcastReceiver bCReciever) {
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(bCReciever, intentFilter);
        IntentFilter disconnect = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(bCReciever,disconnect);
        mAdapter.startDiscovery();
        if(Constants.debug) Log.i(TAG, "SEARCHING FOR NEARBY DEVICES");
    }

    //Client - Stops client from searching for nearby devices
    public static void stopSearching(Context context, BroadcastReceiver bcReceiver){
        mAdapter.cancelDiscovery();
        if(Constants.debug) Log.i(TAG, "SEARCHING FOR DEVICES ENDED");
    }

    public static void addMessageListener(MessageListener m) {
        listeners.add(m);
    }

    public static void scanMessageListeners(int player, int type, String[] content){
        for (MessageListener m : BluetoothService.listeners) {
            if (m != null) {
                m.onReceiveMessage(player, type, content);
                if(Constants.debug) Log.i(TAG, "Message sent to: " + m.toString());
            }
        }
    }
}
