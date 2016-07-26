package paperprisoners.couchpotato;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * @Auther Ian Donovan
 */
public class GameActivity extends Activity implements View.OnClickListener {

    private String username;

    private TextView name;
    private ImageView rank;
    private Button menu;
    private RelativeLayout container;

    private Fragment screen;
    private FragmentManager manager;

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
}
