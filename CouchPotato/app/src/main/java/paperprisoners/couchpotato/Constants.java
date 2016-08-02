package paperprisoners.couchpotato;

/**
 * Created by chris on 7/16/2016.
 */
public interface Constants {
    public String app_name = "Couch Potato";

    final int range = 14;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    //CORE - Message Types Sent Over Bluetooth
    public static final int DENY = 0;
    public static final int REQUEST = 1;
    public static final int ACKNOWLEDGEMENT = 2;
    public static final int READY = 3;                  //Client to Server - lets the server know the client is ready to move to next state
    public static final int NEXT = 4;                   //Server to Client - instructs client to go to next state
    public static final int EDIT_SCORE = 5;             //Server to Client - tells the client to change the UserData.Score of a user in the UserData Array
    public static final int USER_CONNECTED = 6;         //Client to Server - Client send its own UserData to the host
    public static final int USER_DISCONNECTED = 7;      //Not sure what used for yet
    public static final int USER_KICKED = 8;            //Host to Client - Tells the client it was kicked
    public static final int USER_ID = 9;                //Server to Client - sends client their user ID
    public static final int START = 10;                 //Server to Client - Tells Clients to start the game
    public static final int RESTART = 11;               //Server to Client - Restart the game
    public static final int EXIT = 12;                  //Server to Client - Tells Client to Exit the current game

    //REQUIRE TYPE - Used When A REQ Type Call Is Made - Stored on the content part of the Write call
    public static final int USER = 0;
    public static final int NAME = 1;
    public static final int SCORE = 2;
    public static final int ID = 3;
    public static final int LIST = 4;

    //WOULDCHUCK - Specific Message Types For The Game Wouldchuck
    public static final int WC_QUESTION = range + 0;
    public static final int WC_VOTE = range + 1;
    public static final int WC_RESULTS = range + 2;
    public static final int WC_SUBMISSION = range + 3;

    //BLUETOOTH
    public static final int REQUEST_BLUETOOTH = 100;
    public static final int REQUEST_DISCOVERABILITY = 200;
    public static final int REQUEST_COARSE_LOCATION = 300;
}
