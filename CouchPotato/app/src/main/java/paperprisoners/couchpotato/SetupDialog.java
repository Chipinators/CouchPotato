package paperprisoners.couchpotato;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * The dialog that appears upon creating
 *
 * @author Ian Base Code / Chris Bluetooth
 */
public class SetupDialog extends AlertDialog implements View.OnClickListener, AdapterView.OnItemClickListener, DialogInterface.OnCancelListener, MessageListener{
    private static final String TAG = "SetupDialog";
    private int minPlayers = 1, maxPlayers = 8;
    private boolean isHost = false;
    private boolean joined = false;

    private UserData userData;

    private TextView messageText, countText;
    private Button cancelButton, startButton;
    private LinearLayout buttonArea;

    private ListView userList;
    private SetupAdapter adapter;

    private ArrayList<UserData> finalUserList;

    private Context ownerContext;

    public SetupDialog(Context context, int minPlayers, int maxPlayers, boolean isHost, UserData userData) {
        super(context);
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.isHost = isHost;
        this.ownerContext = context;
        this.userData = userData;
        BluetoothService.listeners.add(this);
        finalUserList = new ArrayList<>();
    }

    public SetupDialog(Context context, boolean isHost, UserData userData) {
        this(context, 1, 8, isHost, userData);
    }

    //CUSTOM METHODS

    public void addUser(UserData data) {
        if (!isHost || (isHost && adapter.getCount() + 1 < maxPlayers)) {
            adapter.add(data);
            adjustContent();
            //Then adds listener if needed
            if (isHost) {
                int pos = adapter.getCount() - 1;
                Log.v("SETUPD",""+pos);
                //Button b = (Button) userList.getChildAt(pos).findViewById(R.id.item_setup_kick);
                //b.setOnClickListener(this);
            }
        }
    }

    public UserData getUser(int index) {
        return adapter.getItem(index);
    }

    public void removeUser(UserData data) {
        adapter.remove(data);
        adjustContent();
    }

    //Updates what the list is displaying based on content
    private void adjustContent() {
        int count = adapter.getCount();
        ProgressBar loader = (ProgressBar) findViewById(R.id.setup_loader);
        //Toggling loader visibility
        if (count == 0 || joined) {
            loader.setVisibility(View.VISIBLE);
            userList.setVisibility(View.INVISIBLE);
        } else {
            loader.setVisibility(View.INVISIBLE);
            userList.setVisibility(View.VISIBLE);
        }
        //Adjusting host/client specific content
        if (isHost) {
            countText.setText((adapter.getCount() + 1) + "/" + maxPlayers);
            if (count + 1 >= minPlayers) {
                countText.setTextColor(ContextCompat.getColor(getContext(),R.color.main_accept));
                if (count + 1 >= maxPlayers) {
                    messageText.setText(getContext().getString(R.string.setup_host3));
                } else {
                    messageText.setText(getContext().getString(R.string.setup_host2));
                }
                startButton.setEnabled(true);
                startButton.setTextColor(ContextCompat.getColor(getContext(),R.color.main_black));
            } else {
                countText.setTextColor(ContextCompat.getColor(getContext(),R.color.main_white));
                messageText.setText(getContext().getString(R.string.setup_host1));
                startButton.setEnabled(false);
                startButton.setTextColor(ContextCompat.getColor(getContext(),R.color.main_black_faded));
            }
        } else {
            if (joined) {
                countText.setText(getContext().getString(R.string.setup_joining));
                messageText.setText(getContext().getString(R.string.setup_join3));
            } else {
                countText.setText(getContext().getString(R.string.setup_searching));
                if (count > 0) {
                    messageText.setText(getContext().getString(R.string.setup_join2));
                } else {
                    messageText.setText(getContext().getString(R.string.setup_join1));
                }
            }
        }
        //Then invalidates the list
        userList.invalidate();
    }

    private UserData getUserDataFromButton(Button btn) {
        if (!isHost)
            return null;
        for (int i = 0; i < adapter.getCount(); i++) {
            View v = userList.getChildAt(i);
            if (isHost && v.findViewById(R.id.item_setup_kick) == btn)
                return adapter.getItem(i);
        }
        return null;
    }

    private void setupHost() {
        userData.setPlayer(0);
        isHost = true;
        setTitle(getContext().getString(R.string.select_host));
        adjustContent();
        //BLUETOOTH
        BluetoothService.getmAdapter().setName(Constants.app_name + " - " + userData.getUsername());
        BluetoothService.makeDiscoverable(ownerContext, bCReciever);
        BluetoothService.start();
    }

    private void setupClient() {
        isHost = false;
        setTitle(getContext().getString(R.string.select_join));
        userList.setOnItemClickListener(this);
        buttonArea.removeView(startButton);
        adjustContent();
        //BLUETOOTH
        BluetoothService.getmAdapter().setName(userData.getUsername());
        BluetoothService.getmAdapter().cancelDiscovery();
        adapter.clear();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        ownerContext.registerReceiver(bCReciever, filter);

        BluetoothService.startSearching(ownerContext, bCReciever);
    }


    //INHERITED METHODS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_setup);
        //Gets elements
        messageText = (TextView) findViewById(R.id.setup_message);
        countText = (TextView) findViewById(R.id.setup_count);
        cancelButton = (Button) findViewById(R.id.setup_cancel);
        startButton = (Button) findViewById(R.id.setup_start);
        buttonArea = (LinearLayout) findViewById(R.id.setup_buttons);
        userList = (ListView) findViewById(R.id.setup_list);
        //Setting fonts
        Typeface light = TypefaceManager.get("Oswald-Light");
        Typeface regular = TypefaceManager.get("Oswald-Regular");
        Typeface bold = TypefaceManager.get("Oswald-Bold");
        messageText.setTypeface(regular);
        countText.setTypeface(light);
        cancelButton.setTypeface(bold);
        startButton.setTypeface(bold);
        //List setup stuff
        adapter = new SetupAdapter(this, 0, isHost);
        userList.setAdapter(adapter);
        //Adding listeners
        cancelButton.setOnClickListener(this);
        startButton.setOnClickListener(this);
        if (isHost) {
            setupHost();
        } else {
            setupClient();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == startButton) {
            createFinalPlayerList();
            //Send Player ID To Clients
            for(UserData user : finalUserList){
                BluetoothService.write(Integer.toString(user.getPlayer()), Constants.USER_ID, new String[] {"Player ID Sent"});
            }
            //TODO: Send Final Player List to all devices in the START message
            String[] startMsg = new String[1];
            startMsg[0] = "This is a Start Game Message!";
            BluetoothService.writeToClients(Constants.START, startMsg);
            Intent toGame = new Intent(ownerContext, GameActivity.class);
            ownerContext.startActivity(toGame);
            cancel();
        } else if (v == cancelButton) {
            //addUser(new UserData("dude", 0, null, null));
            BluetoothService.stop();
            adapter.clear();
            userList.invalidate();
            cancel();
        } else {
            try {
                //This should only run for the host
                UserData user = getUserDataFromButton((Button) v);
                // TODO: Host sends kick to selected client as described in user object
                removeUser(user);
            } catch (ClassCastException e) {
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Should only fire off for client.
        // TODO: Connect client to selected host as described in user object

        UserData selected = adapter.getItem(position);
        Log.i("Log", "Item clicked trying to connect");
        try {
            BluetoothService.connect(selected.getDevice());
        } catch (Exception e) {
            Log.e(TAG, "Cannot connect to server, server not available?");
            adapter.clear();
        }
        BluetoothService.getmAdapter().cancelDiscovery();
        BluetoothService.writeToClients(Constants.USER_CONNECTED,userData.toArray());
        if (true)
            joined = true;
        adjustContent();
    }


    //BLUETOOTH METHODS
    private final BroadcastReceiver bCReciever = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, action);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG,"Device Name: " + device.getName());
                UserData foundDevice = new UserData(device, device.getAddress(), device.getName());
                if (!adapter.getItems().contains(foundDevice) && device.getName() != null) {
                    Log.i(TAG, "USER IS NOT ON LIST");
                    //TODO: MAKE IT SO THAT IT LOOKS FOR THE GAME NAME, NOT APP NAME.
                    if (foundDevice.getUsername().contains(Constants.app_name)) {
                        Log.i(TAG, "USERNAME CONTAINS APP NAME - ADDED TO AVAILABLE DEVICES");
                        addUser(foundDevice);
                    }
                }
            }
            if(BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){
                Log.i(TAG, "USER CONNECTED TO SERVER - adding user to list");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                UserData connectedDevice = new UserData(device,device.getAddress(), device.getName());
                addUser(connectedDevice);
            }
            if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                Log.i(TAG, "USER DISCONNECTED");
                if(isHost){
                    //TODO: REMOVE DISCONNECTED USER FROM USER LIST
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    for(UserData user : adapter.getItems()){
                        if(device.getAddress().equals(user.getAddress())){
                            removeUser(user);
                        }
                    }
                }else{
                    BluetoothService.stop();
                    adapter.clear();
                    userList.invalidate();
                    Toast.makeText(ownerContext,"You were disconnected from the host.",Toast.LENGTH_LONG).show();
                    cancel();
                }
            }
        }
    };

    @Override
    public void onCancel(DialogInterface dialog) {
        ownerContext.unregisterReceiver(bCReciever);
        BluetoothService.getmAdapter().cancelDiscovery();
        BluetoothService.stop();
    }

    @Override
    public void onReceiveMessage(int player, int messageType, Object[] content) {
        switch(messageType){
            case Constants.START:
                Log.i(TAG, content.toString());
                ownerContext.unregisterReceiver(bCReciever);
                BluetoothService.getmAdapter().cancelDiscovery();
                //TODO: ADD BUNDLED DATA TO SEND TO GAME ACTIVITY
                Intent toGame = new Intent(ownerContext, GameActivity.class);
                ownerContext.startActivity(toGame);
                break;
            case Constants.USER_CONNECTED:
                Log.i(TAG, "USER_CONNECTED: PROCESSING USER DATA");
                UserData user = new UserData((String)content[0], Integer.getInteger((String)content[1]));
                adapter.add(user);
                break;
            case Constants.USER_ID:
                userData.setPlayer(player);
                break;
        }
    }

    public void createFinalPlayerList(){
        finalUserList.add(userData);
        finalUserList.addAll(adapter.getItems());
        int i = 0;
        for(UserData user : finalUserList){
            user.setPlayer(i);
            i++;
        }
    }
}