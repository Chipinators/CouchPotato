package paperprisoners.couchpotato;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Text;

import java.util.Arrays;

/**
 * Created by Ian on 7/13/2016.
 */
public class UserData {
    private static final String TAG = "UserData";
    protected String username;
    protected String address;
    protected int score;
    protected int playerID = -1;
    protected BluetoothDevice device;
    protected BluetoothAdapter adapter;

    public UserData(String username) {
        this.username = username;
    }

    public UserData (BluetoothDevice device,String address, String username){
        this.device = device;
        this.address = address;
        this.username = username;
    }

    public UserData(String username, int score){
        this.username = username;
        this.score = score;
    }
    public UserData(String[] data){
        Log.i(TAG, "USER DATA STRING ARRAY CONSTRUCTOR CALLED:   " + TextUtils.join(",",data));
        username = data[0];
        address = data[1];
        score = Integer.parseInt(data[2]);
        playerID = Integer.parseInt(data[3]);
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public void setAdapter(BluetoothAdapter adapter) {
        this.adapter = adapter;
    }

    public String getUsername() {

        return username;
    }

    public int getScore() {
        return score;
    }

    public int getPlayerID() {
        return playerID;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public BluetoothAdapter getAdapter() {
        return adapter;
    }

    public String[] toArray() {
        String[] output = new String[2];
        output[0] = username;
        output[1] = ""+score;
        return output;
    }

    public static String toString(UserData u){
        String delim = "||||";
        String temp = u.username + delim + u.address + delim + u.score + delim + u.playerID;
        Log.i(TAG, "TO STRING CALLED - OUTPUT: " + temp);
        return temp;
    }
}
