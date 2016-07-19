package paperprisoners.couchpotato;

import android.app.Activity;
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

    private int minPlayers=3, maxPlayers=8;
    private boolean isHost = false;
    private boolean joined = false;

    private TextView messageText, countText;
    private Button cancelButton, startButton, addUsr;
    private LinearLayout buttonArea;

    private ListView userList;
    private SetupAdapter adapter;

    private Context ownerContext;

    public SetupDialog(Context context, int minPlayers, int maxPlayers, boolean isHost) {
        super(context);
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.isHost = isHost;
        this.ownerContext = context;
    }

    public SetupDialog(Context context, boolean isHost) {
        this(context,3,8,isHost);
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
            Intent toGame = new Intent(ownerContext,GameActivity.class);
            ownerContext.startActivity(toGame);
            cancel();
        } else {
            addUser(new UserData("dude",0,null,null));
            //adapter.clear();
            userList.invalidate();
            //cancel();
        }
    }


    //CUSTOM METHODS

    public void addUser(UserData data) {
        if (!isHost || (isHost && adapter.getCount()+1 < maxPlayers) ) {
            adapter.add(data);
            adjustContent();
        }
    }

    public void removeUser(UserData data) {
        adapter.remove(data);
        adjustContent();
    }

    //Updates what the list is displaying based on content
    private void adjustContent() {
        int count = adapter.getCount();
        ProgressBar loader = (ProgressBar) findViewById(R.id.setup_loader);
        //Toggling loader visibility
        if (count == 0 || joined) {
            loader.setVisibility(View.VISIBLE);
            userList.setVisibility(View.INVISIBLE);
        }
        else {
            loader.setVisibility(View.INVISIBLE);
            userList.setVisibility(View.VISIBLE);
        }
        //Adjusting host/client specific content
        if (isHost) {
            countText.setText((adapter.getCount() + 1) + "/" + maxPlayers);
            if (count+1 >= minPlayers) {
                countText.setTextColor(getContext().getColor(R.color.main_accept));
                if (count+1 >= maxPlayers) {
                    messageText.setText(getContext().getString(R.string.setup_host3));
                }
                else {
                    messageText.setText(getContext().getString(R.string.setup_host2));
                }
                startButton.setEnabled(true);
                startButton.setTextColor(getContext().getColor(R.color.main_black));
            }
            else {
                countText.setTextColor(getContext().getColor(R.color.main_white));
                messageText.setText(getContext().getString(R.string.setup_host1));
                startButton.setEnabled(false);
                startButton.setTextColor(getContext().getColor(R.color.main_black_faded));
            }
        }
        else {
            if (joined) {
                countText.setText(getContext().getString(R.string.setup_joining));
                messageText.setText(getContext().getString(R.string.setup_join3));
            }
            else {
                countText.setText(getContext().getString(R.string.setup_searching));
                if (count > 0) {
                    messageText.setText(getContext().getString(R.string.setup_join2));
                } else {
                    messageText.setText(getContext().getString(R.string.setup_join1));
                }
            }
        }
        //Then invalidates the list
        userList.invalidate();
    }

    private void setupHost() {
        setTitle(getContext().getString(R.string.select_host));
        messageText.setText(getContext().getString(R.string.setup_host1));
        adjustContent();
    }

    private void setupClient() {
        setTitle(getContext().getString(R.string.select_join));
        messageText.setText(getContext().getString(R.string.setup_join1));
        countText.setText(getContext().getString(R.string.setup_searching));
        buttonArea.removeView(startButton);
        adjustContent();
    }

}
