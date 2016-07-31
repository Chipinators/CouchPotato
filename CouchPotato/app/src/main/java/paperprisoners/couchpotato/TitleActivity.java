package paperprisoners.couchpotato;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * The title screen Activity of Couch Potato.
 *
 * @author Ian Donovan
 */
public class TitleActivity extends Activity implements View.OnClickListener, TextWatcher {

    private int easter = 0;

    private RelativeLayout root;
    private Button logo;
    private TextView titleText;
    private EditText nameField;
    private Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Layout setup
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_title);
        //Element setup
        root = (RelativeLayout) this.findViewById(R.id.title_root);
        logo = (Button) this.findViewById(R.id.title_image);
        titleText = (TextView) this.findViewById(R.id.title_text);
        nameField = (EditText) this.findViewById(R.id.title_name_field);
        submit = (Button) this.findViewById(R.id.title_submit);
        //Setting fonts
        try {
            Typeface regular = TypefaceManager.get("Oswald-Regular");
            Typeface bold = TypefaceManager.get("Oswald-Bold");
            titleText.setTypeface(bold);
            nameField.setTypeface(regular);
            submit.setTypeface(bold);
        }
        catch (Exception e) {}
        //Adding listeners
        root.setOnClickListener(new KeyboardHidingListener(this,root));
        logo.setOnClickListener(this);
        nameField.addTextChangedListener(this);
        nameField.setOnFocusChangeListener(new KeyboardHidingListener(this,root));
        submit.setOnClickListener(this);
        submit.setEnabled(false);

        if (BluetoothService.getmAdapter()== null) {
            // No Bluetooth on device.
            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth", Toast.LENGTH_LONG).show();
        }

        //Setting values
        String username = this.getIntent().getStringExtra("username");
        if (username != null)
            ((EditText)findViewById(R.id.title_name_field)).setText(username);
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.title_submit) {
            boolean canPlay = true;
            try {
                if (BluetoothAdapter.getDefaultAdapter() == null)
                    canPlay = false;
                else {
                    Intent toSelect = new Intent(this, GameSelectActivity.class);
                    toSelect.putExtra("username", ((EditText) findViewById(R.id.title_name_field)).getText().toString());
                    this.startActivity(toSelect);
                }
            }
            catch (NoClassDefFoundError e) {
                canPlay = false;    //Compatibility fallback
            }
            if (!canPlay) {
                MessageDialog sorryMsg = new MessageDialog(this, getString(R.string.title_sorry));
                sorryMsg.show();
            }
        }
        else {
            //Easter egg
            easter ++;
            if (easter == 4) {
                logo.setEnabled(false);
                logo.setBackground(ContextCompat.getDrawable(this,R.drawable.cool_512));
                titleText.setText(getString(R.string.easter));
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s != null && s.length() >= 1) {
            submit.setEnabled(true);
            submit.setBackgroundColor( ContextCompat.getColor(this, R.color.main_accept) );
            submit.setTextColor( ContextCompat.getColor(this, R.color.main_black) );
        }
        else {
            submit.setEnabled(false);
            submit.setBackgroundColor( ContextCompat.getColor(this, R.color.main_black) );
            submit.setTextColor( ContextCompat.getColor(this, R.color.main_white) );
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
