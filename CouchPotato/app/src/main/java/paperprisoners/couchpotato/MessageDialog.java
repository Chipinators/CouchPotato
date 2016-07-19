package paperprisoners.couchpotato;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * The dialog that appears upon creating
 *
 * @author Ian
 */
public class MessageDialog extends AlertDialog implements View.OnClickListener {

    private String text;
    private TextView message;
    private Button okButton;

    public MessageDialog(Context context, String message) {
        super(context);
        this.text = message;
    }


    //INHERITED METHODS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_message);
        //Gets elements
        message = (TextView) findViewById(R.id.message_text);
        okButton = (Button) findViewById(R.id.message_ok);
        //Setting fonts
        try {
            Typeface regular = TypefaceManager.get("Oswald-Regular");
            Typeface bold = TypefaceManager.get("Oswald-Bold");
            message.setTypeface(regular);
            okButton.setTypeface(bold);
        } catch (Exception e) { }
        //Adding message text
        if (text != null)
            message.setText(text);
        //Adding listeners
        okButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == okButton) {
            cancel();
        }
    }
}
