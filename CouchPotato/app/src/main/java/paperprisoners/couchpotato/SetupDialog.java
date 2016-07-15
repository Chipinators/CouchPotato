package paperprisoners.couchpotato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * The dialog that appears upon creating
 *
 * @author Ian
 */
public class SetupDialog extends AlertDialog implements View.OnClickListener {

    private boolean isHost = false;

    private TextView messageText, countText;
    private Button cancelButton, startButton, addUsr;
    private LinearLayout buttonArea;

    private ListView userList;
    private SetupAdapter adapter;

    private int players = 0;
    private int gameID;


    public SetupDialog(Context context, boolean isHost, int id) {
        super(context);
        this.isHost = isHost;
        gameID = id;
    }


    //INHERITED METHODS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_setup);
        //Gets elements
        messageText = (TextView) findViewById( R.id.setup_message );
        countText = (TextView) findViewById( R.id.setup_count );
        cancelButton = (Button) findViewById( R.id.setup_cancel );
        startButton = (Button) findViewById( R.id.setup_start );
        buttonArea = (LinearLayout) findViewById(R.id.setup_buttons);
        userList = (ListView) findViewById(R.id.setup_list);
        addUsr = (Button) findViewById(R.id.addUsr);
        //Setting fonts
        try {
            Typeface light = Typeface.createFromAsset( getContext().getAssets(), "font/oswald/Oswald-Light.ttf" );
            Typeface regular = Typeface.createFromAsset( getContext().getAssets(), "font/oswald/Oswald-Regular.ttf" );
            Typeface bold = Typeface.createFromAsset( getContext().getAssets(), "font/oswald/Oswald-Bold.ttf" );
            messageText.setTypeface(regular);
            countText.setTypeface(light);
            cancelButton.setTypeface(bold);
            startButton.setTypeface(bold);
        }
        catch (Exception e) {}
        //List setup stuff
        adapter = new SetupAdapter( getContext(), 0, isHost );
        userList.setAdapter( adapter );
        //Adding listeners
        cancelButton.setOnClickListener( this );
        startButton.setOnClickListener( this );

        if (isHost) {
            setupHost();
        }
        else {
            setupClient();
        }
    }


    @Override
    public void onClick(View v) {
        if (v == addUsr) {
            ProgressBar loader = (ProgressBar) findViewById(R.id.setup_loader);
            loader.setVisibility(View.INVISIBLE);
            if(players >= 8){

            }
            else {
                adapter.add( new UserData("User "+((int)(Math.random()*256)),1,null,null) );
                userList.invalidate();
                players ++;
                countText.setText((players + "/8"));
            }
        }
        else if(v == startButton){
            Intent game = new Intent(this.getContext(), Game.class);
            game.putExtra("host", isHost);
            game.putExtra("players", players);
            game.putExtra("gameNum", gameID);
            this.closeOptionsMenu();
            this.getContext().startActivity(game);
        }
        else {
            adapter.clear();
            userList.invalidate();
            cancel();
            new KickDialog(getContext()).show();
        }
    }


    //CUSTOM METHODS

    private void setupHost() {
        setTitle( getContext().getString(R.string.select_host) );
        messageText.setText( getContext().getString(R.string.setup_host1) );
        players ++;
        countText.setText(players + "/8");
    }

    private void setupClient() {
        setTitle( getContext().getString(R.string.select_join) );
        messageText.setText( getContext().getString(R.string.setup_join1) );
        countText.setText( getContext().getString(R.string.setup_searching) );
        buttonArea.removeView(startButton);
    }

}
