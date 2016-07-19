package paperprisoners.couchpotato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
public class SetupDialog extends AlertDialog implements View.OnClickListener, AdapterView.OnItemClickListener {

    private int minPlayers = 3, maxPlayers = 8;
    private boolean isHost = false;
    private boolean joined = false;

    private TextView messageText, countText;
    private Button cancelButton, startButton;
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
        this(context, 3, 8, isHost);
    }

    //CUSTOM METHODS

    public void addUser(UserData data) {
        if (!isHost || (isHost && adapter.getCount() + 1 < maxPlayers)) {
            adapter.add(data);
            adjustContent();
            //Then adds listener if needed
            if (isHost) {
                int pos = adapter.getCount() - 1;
                Log.v("SETUPD",""+pos);
                //Button b = (Button) userList.getChildAt(pos).findViewById(R.id.item_setup_kick);
                //b.setOnClickListener(this);
            }
        }
    }

    public UserData getUser(int index) {
        return adapter.getItem(index);
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
        } else {
            loader.setVisibility(View.INVISIBLE);
            userList.setVisibility(View.VISIBLE);
        }
        //Adjusting host/client specific content
        if (isHost) {
            countText.setText((adapter.getCount() + 1) + "/" + maxPlayers);
            if (count + 1 >= minPlayers) {
                countText.setTextColor(getContext().getColor(R.color.main_accept));
                if (count + 1 >= maxPlayers) {
                    messageText.setText(getContext().getString(R.string.setup_host3));
                } else {
                    messageText.setText(getContext().getString(R.string.setup_host2));
                }
                startButton.setEnabled(true);
                startButton.setTextColor(getContext().getColor(R.color.main_black));
            } else {
                countText.setTextColor(getContext().getColor(R.color.main_white));
                messageText.setText(getContext().getString(R.string.setup_host1));
                startButton.setEnabled(false);
                startButton.setTextColor(getContext().getColor(R.color.main_black_faded));
            }
        } else {
            if (joined) {
                countText.setText(getContext().getString(R.string.setup_joining));
                messageText.setText(getContext().getString(R.string.setup_join3));
            } else {
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

    private UserData getUserDataFromButton(Button btn) {
        if (!isHost)
            return null;
        for (int i = 0; i < adapter.getCount(); i++) {
            View v = userList.getChildAt(i);
            if (isHost && v.findViewById(R.id.item_setup_kick) == btn)
                return adapter.getItem(i);
        }
        return null;
    }

    private void setupHost() {
        setTitle(getContext().getString(R.string.select_host));
        adjustContent();
    }

    private void setupClient() {
        setTitle(getContext().getString(R.string.select_join));
        userList.setOnItemClickListener(this);
        buttonArea.removeView(startButton);
        adjustContent();
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
        adapter = new SetupAdapter(this, 0, isHost);
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
            Intent toGame = new Intent(ownerContext, GameActivity.class);
            ownerContext.startActivity(toGame);
            cancel();
        } else if (v == cancelButton) {
            addUser(new UserData("dude", 0, null, null));
            //adapter.clear();
            userList.invalidate();
            //cancel();
        } else {
            try {
                //This should only run for the host
                UserData user = getUserDataFromButton((Button) v);
                // TODO: Host sends kick to selected client as described in user object
                removeUser(user);
            } catch (ClassCastException e) {
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Should only fire off for client.
        UserData user = getUser(position);
        // TODO: Connect client to selected host as described in user object

        if (true)
            joined = true;
        adjustContent();
    }
}