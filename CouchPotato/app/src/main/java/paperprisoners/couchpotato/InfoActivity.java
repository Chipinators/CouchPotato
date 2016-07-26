package paperprisoners.couchpotato;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class InfoActivity extends Activity implements View.OnClickListener {

    private String username;            //saved to be referenced on return
    private Button back;
    private TextView text, barTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Layout setup
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView( R.layout.activity_info );
        //Retrieves username
        username = getIntent().getStringExtra("username");
        //Gets  elements
        back = (Button) findViewById( R.id.info_back );
        text = (TextView) findViewById( R.id.info_text );
        barTitle = (TextView) findViewById( R.id.info_bar_title ) ;
        //sets fonts
        Typeface regular = TypefaceManager.get( "Oswald-Regular" );
        Typeface bold = TypefaceManager.get( "Oswald-Bold" );
        text.setTypeface(regular);
        barTitle.setTypeface(bold);
        //adds listener
        back.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        if ( v == back ) {
            Intent toSelect = new Intent( this, GameSelectActivity.class );
            toSelect.putExtra("username",username);
            this.startActivity( toSelect );
        }
    }
}
