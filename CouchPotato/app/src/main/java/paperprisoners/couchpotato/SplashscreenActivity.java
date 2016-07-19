package paperprisoners.couchpotato;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SplashscreenActivity extends Activity {

    private static final int SPLASH_LENGTH_MILLIS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Layout setup
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splashscreen);
        //Sets a timer to show the Paper Prisoners logo for a couple seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent toTitle = new Intent(SplashscreenActivity.this, TitleActivity.class);
                startActivity(toTitle);
                finish();
            }
        }, SPLASH_LENGTH_MILLIS);
        //Loads in game's fonts
        TypefaceManager.manualLoadFonts(getAssets());
        TypefaceManager.loadFonts(getAssets(),"fonts");
    }
}
