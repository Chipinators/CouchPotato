package paperprisoners.couchpotato;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;

/**
 * Created by Ian on 7/13/2016.
 */
public class UserData {

    protected String username;
    protected int gameID;
    protected int player = -1;
    protected Color color;
    protected BluetoothDevice device;
    protected BluetoothAdapter adapter;

    public UserData(String username, int gameID, BluetoothDevice device, BluetoothAdapter adapter ) {
        this.username = username;
        this.gameID = gameID;
        this.device = device;
        this.adapter = adapter;
    }

}
