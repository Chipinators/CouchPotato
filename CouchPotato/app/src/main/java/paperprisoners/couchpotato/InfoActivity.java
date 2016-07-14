package paperprisoners.couchpotato;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class InfoActivity extends Activity implements View.OnClickListener {

    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Layout setup
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN );
        setContentView( R.layout.activity_info );
        back = (Button) findViewById( R.id.info_back );
        back.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        if ( v == back ) {
            Intent toSelect = new Intent( this, GameSelectActivity.class );
            this.startActivity( toSelect );
        }
    }
}
