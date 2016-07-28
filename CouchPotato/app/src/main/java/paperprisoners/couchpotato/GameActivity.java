package paperprisoners.couchpotato;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @Auther Ian Donovan
 */
public class GameActivity extends Activity implements View.OnClickListener {
    private static String DELIM1 = "\\|/";
    private static String DELIM2 = "||||";
    private static final String TAG = "GameActivity";
    private boolean host;
    private String username;
    private TextView name;
    private ImageView rank;
    private Button menu;
    private RelativeLayout container;
    private Fragment screen;
    private FragmentManager manager;

    private UserData me;
    private ArrayList<UserData> players;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Setup layout
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);
        //Gets necessary elements
        name = (TextView) findViewById(R.id.game_name);
        rank = (ImageView) findViewById(R.id.game_rank);
        menu = (Button) findViewById(R.id.game_menu);
        container = (RelativeLayout) findViewById(R.id.game_container);
        //Sets fonts
        name.setTypeface(TypefaceManager.get("Oswald-Bold"));
        //Adds listeners
        menu.setOnClickListener(this);
        //Sets top bar content
        username = this.getIntent().getStringExtra("username");
        if (username != null)
            name.setText(username);
        //Other junk

        Bundle extras = getIntent().getExtras();

        ArrayList<String> tmp = extras.getStringArrayList("PlayerArray");
        players = new ArrayList<UserData>();

        for(int i = 0; i < tmp.size(); i++){
            String[] split = TextUtils.split(tmp.get(i), Pattern.quote(GameActivity.DELIM2));
            players.add(new UserData(split));
        }
        Log.i(TAG,"FINISHED GETTING PLAYER ARRAY DATA FROM BUNDLE");
        me = new UserData( TextUtils.split(extras.getString("me"), Pattern.quote(GameActivity.DELIM2) ));
        host = extras.getBoolean("host");
        Log.i(TAG,"FINISHED GETTING USER DATA FROM BUNDLE");
        Log.i(TAG, "PLAYER DATA ----- " + TextUtils.join(",",players));
        manager = getFragmentManager();
        Fragment wcFrag = new WouldChuckFragment();
        setFragment(wcFrag);
    }


    //CUSTOM METHODS

    public void setFragment(Fragment fragment) {
        FragmentTransaction swapper = manager.beginTransaction();
        swapper.replace(R.id.game_container, fragment);
        swapper.addToBackStack(null);
        swapper.commit();
    }


    //INHERITED METHODS BELOW

    @Override
    public void onClick(View v) {
        if (v == menu) {

        }
    }

    public ArrayList<UserData> getPlayers() {
        return players;
    }
    public UserData getMe(){
        return me;
    }
    public boolean getHost(){
        return  host;
    }
}
