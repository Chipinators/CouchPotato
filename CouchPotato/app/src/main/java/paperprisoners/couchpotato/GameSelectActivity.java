package paperprisoners.couchpotato;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameSelectActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener, MessageListener {
    private static final String TAG = "GameSelectActivity";

    protected String username;
    private UserData userData;
    private Bitmap wcLogo, moreLogo;

    private TextView nameText, infoText;
    private Button backButton, menuButton, hostButton, joinButton;
    private LinearLayout infoButton;
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
        infoText = (TextView) findViewById(R.id.select_info_subtitle);
        //playersText = (TextView) findViewById(R.id.select_players);
        //playersText2 = (TextView) findViewById(R.id.select_players2);
        backButton = (Button) findViewById(R.id.select_back);
        menuButton = (Button) findViewById(R.id.select_menu);
        infoButton = (LinearLayout) findViewById(R.id.select_info);
        hostButton = (Button) findViewById(R.id.select_host);
        joinButton = (Button) findViewById(R.id.select_join);
        bg = (RelativeLayout) findViewById(R.id.select_background_layout);
        pages = (PagedFragment) getFragmentManager().findFragmentById(R.id.select_pager);
        //Setting fonts
        try {
            Typeface regular = TypefaceManager.get("Oswald-Regular");
            Typeface bold = TypefaceManager.get("Oswald-Bold");
            nameText.setTypeface(regular);
            infoText.setTypeface(regular);
            hostButton.setTypeface(bold);
            joinButton.setTypeface(bold);
        } catch (Exception e) {
        }
        //Adding listeners
        backButton.setOnClickListener(this);
        menuButton.setOnClickListener(this);
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

        if (wcLogo == null)
            wcLogo = BitmapFactory.decodeResource(getResources(), R.drawable.wouldchuck_512);
        if (moreLogo == null)
            moreLogo = BitmapFactory.decodeResource(getResources(), R.drawable.more_512);
        GameData wc = new GameData("Wouldchuck", wcLogo, 3, 8);
        GameData more = new GameData(moreLogo, 0, 0);
        pages.addPage(PagedGameAdapter.generateView(getBaseContext(), wc));
        pages.addPage(PagedGameAdapter.generateView(getBaseContext(), more));

        //Bluetooth
        BluetoothService.listeners.add(this);
    }

    @Override
    public void onClick(View v) {
        if (v == backButton) {
            Intent toTitle = new Intent(this, TitleActivity.class);
            toTitle.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            toTitle.putExtra("username", username);
            this.startActivity(toTitle);
            finish();
        } else if (v == menuButton) {
            new SettingsDialog(this, -1).show();
            //new SettingsDialog(this, SettingsDialog.MenuState.MAIN).show();
        } else if (v == infoButton) {
            Intent toInfo = new Intent(this, InfoActivity.class);
            toInfo.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            toInfo.putExtra("username", username);
            toInfo.putExtra("gameID", 0);
            this.startActivity(toInfo);
            finish();
        } else if (v == hostButton) {
            BluetoothService.getDiscoverablePermissions(this);

        } else if (v == joinButton) {
            if(ContextCompat.checkSelfPermission(this.getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                setup = new SetupDialog(this, false, userData);
                setup.show();
            }
            else{
                requestLocation();
            }
        }
    }

    public void requestLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Constants.debug)Log.i(TAG, "BUILD IS GREATER OR EQUAL TO VERSION M, CHECKING PERMISSIONS");
            if (ContextCompat.checkSelfPermission(this.getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if(Constants.debug)Log.i(TAG, "PERMISSION NOT GRANTED, REQUESTING COARSE LOCATION PERMISSION");
                ActivityCompat.requestPermissions(GameSelectActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, Constants.REQUEST_COARSE_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Constants.REQUEST_COARSE_LOCATION: {
                if(Constants.debug)Log.i(TAG, "REQUEST_COARSE_LOCATION REQUEST RESULT");
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(Constants.debug)Log.i(TAG, "REQUEST_COARSE_LOCATION PERMISSION WAS GRANTED, GOING TO SETUP DIALOG");
                    setup = new SetupDialog(this, false, userData);
                    setup.show();
                }
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        new MessageToast(this, "You MUST grant permissions to use Couch Potato!").show();
                        goToSettings();
                        break;
                    }
                    else {
                        if(Constants.debug)Log.i(TAG, "REQUEST_COARSE_LOCATION PERMISSION WAS NOT GRANTED, RETURNING TO GAME SELECT ACTIVITY");
                        new MessageToast(this, "You must grant permissions to use Bluetooth!").show();
                    }
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void goToSettings() {
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        this.startActivity(i);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(Constants.debug)Log.i(TAG, "REQUEST RECEIVED - Request Code: " + requestCode + "  Result Code: " + resultCode);
        if (requestCode == Constants.REQUEST_DISCOVERABILITY) {
            if(resultCode == 180){
                setup = new SetupDialog(this, true, userData);
                setup.show();
            }
            if (resultCode == 0) {
                new MessageToast(this, "You must grant permissions to use Bluetooth!").show();
            }
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
            bg.setBackgroundColor(ContextCompat.getColor(this, R.color.wouldchuck));
            hostButton.setEnabled(true);
            hostButton.setBackgroundColor(ContextCompat.getColor(this, R.color.main_black));
            joinButton.setEnabled(true);
            joinButton.setBackgroundColor(ContextCompat.getColor(this, R.color.main_black));
            infoButton.setEnabled(true);
            infoButton.setBackgroundColor(ContextCompat.getColor(this, R.color.main_black));
        } else if (position == 1) {
            bg.setBackgroundColor(ContextCompat.getColor(this, R.color.more));
            hostButton.setEnabled(false);
            hostButton.setBackgroundColor(ContextCompat.getColor(this, R.color.main_black_faded));
            joinButton.setEnabled(false);
            joinButton.setBackgroundColor(ContextCompat.getColor(this, R.color.main_black_faded));
            infoButton.setEnabled(false);
            infoButton.setBackgroundColor(ContextCompat.getColor(this, R.color.main_black_faded));
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        BluetoothService.listeners.remove(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BluetoothService.getmAdapter().setName(BluetoothService.getDefaultDeviceName());
        BluetoothService.getmAdapter().cancelDiscovery();
        if (wcLogo != null)
            wcLogo.recycle();
        wcLogo = null;
        if (moreLogo != null)
            moreLogo.recycle();
        moreLogo = null;
    }


    //BLUETOOTH METHODS

    @Override
    public void onReceiveMessage(int player, int messageType, Object[] content) {
        if (messageType == 1) {
            if(Constants.debug)Log.i(TAG, "Message received from remote device! - " + content.toString());
        }
    }
}
