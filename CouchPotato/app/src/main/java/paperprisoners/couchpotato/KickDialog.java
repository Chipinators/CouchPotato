package paperprisoners.couchpotato;

import android.app.AlertDialog;
import android.content.Context;
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
public class KickDialog extends AlertDialog implements View.OnClickListener {

    private TextView message;
    private Button okButton;

    public KickDialog(Context context) {
        super(context);
    }


    //INHERITED METHODS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_kick);
        //Gets elements
        message = (TextView) findViewById(R.id.kick_message);
        okButton = (Button) findViewById(R.id.kick_ok);
        //Setting fonts
        try {
            Typeface regular = Typeface.createFromAsset( getContext().getAssets(), "font/oswald/Oswald-Regular.ttf" );
            Typeface bold = Typeface.createFromAsset( getContext().getAssets(), "font/oswald/Oswald-Bold.ttf" );
            message.setTypeface(regular);
            okButton.setTypeface(bold);
        }
        catch (Exception e) {}
        //Adding listeners
        okButton.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        if (v == okButton) {
            cancel();
        }
    }
}
