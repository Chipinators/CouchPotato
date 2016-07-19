package paperprisoners.couchpotato;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GameSelectActivity extends Activity implements View.OnClickListener, ViewPager.OnPageChangeListener {

    protected String username;

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
        if (username != null)
            ((TextView) this.findViewById(R.id.select_name)).setText(username);

        Bitmap wcLogo = BitmapFactory.decodeResource(getResources(), R.drawable.wouldchuck_512);
        Bitmap moreLogo = BitmapFactory.decodeResource(getResources(), R.drawable.more_512);
        GameData wc = new GameData("Wouldchuck", wcLogo, 3, 8);
        GameData more = new GameData(moreLogo, 0, 0);
        pages.addPage(PagedGameAdapter.generateView(getBaseContext(), wc));
        pages.addPage(PagedGameAdapter.generateView(getBaseContext(), more));
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
            setup = new SetupDialog(this, true);
            setup.show();
        } else if (v == joinButton) {
            setup = new SetupDialog(this, false);
            setup.show();
            //Intent t = new Intent(this, GameActivity.class);
            //t.putExtra("username", username);
            //startActivity(t);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            bg.setBackgroundColor(getColor(R.color.wouldchuck));
            hostButton.setEnabled(true);
            hostButton.setBackgroundColor(getColor(R.color.main_black));
            joinButton.setEnabled(true);
            joinButton.setBackgroundColor(getColor(R.color.main_black));
        }
        else if (position == 1) {
            bg.setBackgroundColor(getColor(R.color.more));
            hostButton.setEnabled(false);
            hostButton.setBackgroundColor(getColor(R.color.main_black_faded));
            joinButton.setEnabled(false);
            joinButton.setBackgroundColor(getColor(R.color.main_black_faded));
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
