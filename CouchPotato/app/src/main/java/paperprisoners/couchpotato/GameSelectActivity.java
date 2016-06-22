package paperprisoners.couchpotato;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameSelectActivity extends Activity implements View.OnClickListener {

    private int id = 1;
    private String username;

    private TextView nameText, playersText, playersText2;
    private Button backButton, infoButton, hostButton, joinButton, leftButton, rightButton;
    private RelativeLayout bg;

    private SetupDialog setup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Layout setup
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game_select);
        //Element setup
        nameText = (TextView) findViewById(R.id.select_name);
        playersText = (TextView) findViewById(R.id.select_players);
        playersText2 = (TextView) findViewById(R.id.select_players2);
        backButton = (Button) findViewById(R.id.select_back);
        infoButton = (Button) findViewById(R.id.select_info);
        hostButton = (Button) findViewById(R.id.select_host);
        joinButton = (Button) findViewById(R.id.select_join);
        leftButton = (Button) findViewById(R.id.select_left);
        rightButton = (Button) findViewById(R.id.select_right);
        bg = (RelativeLayout) findViewById(R.id.select_background_layout);
        //Setting fonts
        try {
            //Typeface light = Typeface.createFromAsset( getAssets(), "font/oswald/Oswald-Light.ttf" );
            Typeface regular = Typeface.createFromAsset( getAssets(), "font/oswald/Oswald-Regular.ttf" );
            Typeface bold = Typeface.createFromAsset( getAssets(), "font/oswald/Oswald-Bold.ttf" );
            nameText.setTypeface(regular);
            playersText.setTypeface(bold);
            playersText2.setTypeface(bold);
            hostButton.setTypeface(bold);
            joinButton.setTypeface(bold);
        }
        catch (Exception e) {}
        //Adding listeners
        backButton.setOnClickListener(this);
        infoButton.setOnClickListener(this);
        hostButton.setOnClickListener(this);
        joinButton.setOnClickListener(this);
        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        //Filling in values
        username = this.getIntent().getStringExtra("username");
        if (username != null)
            ((TextView)this.findViewById(R.id.select_name)).setText(username);
    }

    @Override
    public void onClick(View v) {
        if (v == backButton) {
            Intent toTitle = new Intent( this, TitleActivity.class );
            toTitle.putExtra("username", username);
            this.startActivity( toTitle );
        }
        else if ( v == infoButton ) {
            Intent toInfo = new Intent( this, InfoActivity.class );
            toInfo.putExtra( "gameID", 0 );
            this.startActivity( toInfo );
        }
        else if ( v == hostButton ) {
            setup = new SetupDialog(GameSelectActivity.this, true);
            setup.show();
        }
        else if ( v == joinButton ) {
            setup = new SetupDialog(GameSelectActivity.this, false);
            setup.show();
        }
        else if ( v == leftButton ) {
            id --;
            if ( id < 1 )
                id = 8;
            int col = getResources().getIdentifier("p"+id+"_col", "color", this.getPackageName());
            col = getBaseContext().getColor( col );
            bg.setBackgroundColor( col );
        }
        else {
            id ++;
            if ( id > 8 )
                id = 1;
            int col = getResources().getIdentifier("p"+id+"_col", "color", this.getPackageName());
            col = getBaseContext().getColor( col );
            bg.setBackgroundColor( col );
        }
    }
}
