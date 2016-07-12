package paperprisoners.couchpotato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

/**
 * The dialog that appears upon creating
 *
 * @author Ian
 */
public class SetupDialog extends AlertDialog implements View.OnClickListener {

    private boolean isHost = false;

    private ImageView statusImage;
    private TextView messageText, countText;
    private Button cancelButton, startButton;
    private LinearLayout buttonArea;

    public SetupDialog(Context context, boolean isHost) {
        super(context);
        this.isHost = isHost;
    }


    //INHERITED METHODS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_setup);
        //Gets elements
        statusImage = (ImageView) findViewById( R.id.setup_status );
        messageText = (TextView) findViewById( R.id.setup_message );
        countText = (TextView) findViewById( R.id.setup_count );
        cancelButton = (Button) findViewById( R.id.setup_cancel );
        startButton = (Button) findViewById( R.id.setup_start );
        buttonArea = (LinearLayout) findViewById(R.id.setup_buttons);
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
        if (v == startButton) {
            if(countText.getText().toString().toCharArray()[0] == '1'){

            }
            else {
                switchToComplete();
            }
        }
        else
            cancel();
    }


    //CUSTOM METHODS

    private void setupHost() {
        setTitle( getContext().getString(R.string.select_host) );
        messageText.setText( getContext().getString(R.string.setup_host1) );
        countText.setText( "1/8" );
    }

    private void setupClient() {
        setTitle( getContext().getString(R.string.select_join) );
        messageText.setText( getContext().getString(R.string.setup_join1) );
        countText.setText( getContext().getString(R.string.setup_searching) );
        buttonArea.removeView(startButton);
    }

    private void switchToWaiting() {
        statusImage.setImageResource( R.drawable.ic_autorenew_white_48dp );
        statusImage.setColorFilter( getContext().getColor( R.color.main_deny ) );
    }

    private void switchToComplete() {
        statusImage.setImageResource( R.drawable.ic_done_white_48dp );
        statusImage.setColorFilter( getContext().getColor( R.color.main_accept ) );
    }

}
