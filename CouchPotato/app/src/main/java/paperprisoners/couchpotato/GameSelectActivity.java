package paperprisoners.couchpotato;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameSelectActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener, MessageListener {
    private static final String TAG = "GameSelectActivity";
    protected String username;

    private UserData userData;

    private TextView nameText;
    private Button backButton, infoButton, hostButton, joinButton;
    private RelativeLayout bg;
    private PagedFragment pages;

    private SetupDialog setup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Layout setup
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_select);
        //Element setup
        nameText = (TextView) findViewById(R.id.select_name);
        //playersText = (TextView) findViewById(R.id.select_players);
        //playersText2 = (TextView) findViewById(R.id.select_players2);
        backButton = (Button) findViewById(R.id.select_back);
        infoButton = (Button) findViewById(R.id.select_info);
        hostButton = (Button) findViewById(R.id.select_host);
        joinButton = (Button) findViewById(R.id.select_join);
        bg = (RelativeLayout) findViewById(R.id.select_background_layout);
        pages = (PagedFragment) getFragmentManager().findFragmentById(R.id.select_pager);
        //Setting fonts
        try {
            Typeface regular = TypefaceManager.get("Oswald-Regular");
            Typeface bold = TypefaceManager.get("Oswald-Bold");
            nameText.setTypeface(regular);
            hostButton.setTypeface(bold);
            joinButton.setTypeface(bold);
        } catch (Exception e) {
        }
        //Adding listeners
        backButton.setOnClickListener(this);
        infoButton.setOnClickListener(this);
        hostButton.setOnClickListener(this);
        joinButton.setOnClickListener(this);
        pages.setListener(this);
        //Filling in values
        username = this.getIntent().getStringExtra("username");
        if (username != null) {
            ((TextView) this.findViewById(R.id.select_name)).setText(username);
            userData = new UserData(username);
        }

        Bitmap wcLogo = BitmapFactory.decodeResource(getResources(), R.drawable.wouldchuck_512);
        Bitmap moreLogo = BitmapFactory.decodeResource(getResources(), R.drawable.more_512);
        GameData wc = new GameData("Wouldchuck", wcLogo, 3, 8);
        GameData more = new GameData(moreLogo, 0, 0);
        pages.addPage(PagedGameAdapter.generateView(getBaseContext(), wc));
        pages.addPage(PagedGameAdapter.generateView(getBaseContext(), more));

        //Bluetooth
        BluetoothService.listeners.add(this);
       // BluetoothService.setHandler(mHandler);
    }

    @Override
    public void onClick(View v) {
        if (v == backButton) {
            Intent toTitle = new Intent(this, TitleActivity.class);
            toTitle.putExtra("username", username);
            this.startActivity(toTitle);
        } else if (v == infoButton) {
            Intent toInfo = new Intent(this, InfoActivity.class);
            toInfo.putExtra("gameID", 0);
            this.startActivity(toInfo);
        } else if (v == hostButton) {
            setup = new SetupDialog(this, true, userData);
            setup.show();
            BluetoothService.start();
        } else if (v == joinButton) {
            requestLocation();
            setup = new SetupDialog(this, false, userData);
            setup.show();
            //Intent t = new Intent(this, GameActivity.class);
            //t.putExtra("username", username);
            //startActivity(t);
        }
    }

    public void requestLocation(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
            ActivityCompat.requestPermissions(GameSelectActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        BluetoothService.addMessageListener(this);
        if (!BluetoothService.getmAdapter().isEnabled()) {
            //Bluetooth not enabled on device, request it.
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, Constants.REQUEST_BLUETOOTH);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            bg.setBackgroundColor(ContextCompat.getColor(this,R.color.wouldchuck));
            hostButton.setEnabled(true);
            hostButton.setBackgroundColor(ContextCompat.getColor(this,R.color.main_black));
            joinButton.setEnabled(true);
            joinButton.setBackgroundColor(ContextCompat.getColor(this,R.color.main_black));
        }
        else if (position == 1) {
            bg.setBackgroundColor(ContextCompat.getColor(this,R.color.more));
            hostButton.setEnabled(false);
            hostButton.setBackgroundColor(ContextCompat.getColor(this,R.color.main_black_faded));
            joinButton.setEnabled(false);
            joinButton.setBackgroundColor(ContextCompat.getColor(this,R.color.main_black_faded));
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothService.getmAdapter().cancelDiscovery();
    }

    //BLUETOOTH METHODS


    @Override
    public void onReceiveMessage(int player, int messageType, Object[] content) {
        if (messageType == 1) {
            Log.i(TAG, "Message received from remote device! - " + content.toString());
        }
    }
}
