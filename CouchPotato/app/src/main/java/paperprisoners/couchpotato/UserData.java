package paperprisoners.couchpotato;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.text.TextUtils;

/**
 * Created by Ian on 7/13/2016.
 */
public class UserData {

    protected String username;
    protected int score;
    protected int player = -1;
    protected BluetoothDevice device;
    protected BluetoothAdapter adapter;

    public UserData(String username) {
        this.username = username;
    }

    public UserData (BluetoothDevice device, String username){
        this.device = device;
        this.username = username;
    }

    public UserData(String username, int score){
        this.username = username;
        this.score = score;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setPlayer(int player) {
        this.player = player;
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

    public int getPlayer() {
        return player;
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
}
