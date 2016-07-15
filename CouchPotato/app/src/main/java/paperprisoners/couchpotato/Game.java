package paperprisoners.couchpotato;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class Game extends AppCompatActivity {

    private int gameID;
    private boolean host;
    private int numPlayers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);

        Intent intent = getIntent();
        gameID = intent.getIntExtra("gameNum", -1);
        host = intent.getBooleanExtra("host", false);
        numPlayers = intent.getIntExtra("players", -1);

        loadData();
    }

    private void loadData(){

    }
}
