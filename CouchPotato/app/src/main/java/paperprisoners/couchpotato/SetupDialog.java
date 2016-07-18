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
    private Button cancelButton, startButton;
    private LinearLayout buttonArea;

    private ListView userList;
    private SetupAdapter adapter;

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
        messageText = (TextView) findViewById(R.id.setup_message);
        countText = (TextView) findViewById(R.id.setup_count);
        cancelButton = (Button) findViewById(R.id.setup_cancel);
        startButton = (Button) findViewById(R.id.setup_start);
        buttonArea = (LinearLayout) findViewById(R.id.setup_buttons);
        userList = (ListView) findViewById(R.id.setup_list);
        //Setting fonts
        Typeface light = TypefaceManager.get("Oswald-Light");
        Typeface regular = TypefaceManager.get("Oswald-Regular");
        Typeface bold = TypefaceManager.get("Oswald-Bold");
        messageText.setTypeface(regular);
        countText.setTypeface(light);
        cancelButton.setTypeface(bold);
        startButton.setTypeface(bold);
        //List setup stuff
        adapter = new SetupAdapter(getContext(), 0, isHost);
        userList.setAdapter(adapter);
        //Adding listeners
        cancelButton.setOnClickListener(this);
        startButton.setOnClickListener(this);

        if (isHost) {
            setupHost();
        } else {
            setupClient();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == startButton) {
            //TODO: Start the game
            ProgressBar loader = (ProgressBar) findViewById(R.id.setup_loader);
            loader.setVisibility(View.INVISIBLE);
            adapter.add(new UserData("User " + ((int) (Math.random() * 256)), 1, null, null));
            userList.invalidate();
        } else {
            adapter.clear();
            userList.invalidate();
            cancel();
        }
    }


    //CUSTOM METHODS

    private void setupHost() {
        setTitle(getContext().getString(R.string.select_host));
        messageText.setText(getContext().getString(R.string.setup_host1));
        countText.setText("1/8");
    }

    private void setupClient() {
        setTitle(getContext().getString(R.string.select_join));
        messageText.setText(getContext().getString(R.string.setup_join1));
        countText.setText(getContext().getString(R.string.setup_searching));
        buttonArea.removeView(startButton);
    }

}
