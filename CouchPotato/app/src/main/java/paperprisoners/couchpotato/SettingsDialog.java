package paperprisoners.couchpotato;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * The dialog that appears upon creating
 *
 * @author Ian
 */
public class SettingsDialog extends AlertDialog implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public enum MenuState {
        MAIN, CLIENT, HOST
    }

    private MenuState state = MenuState.MAIN;

    private ColorStateList active, muted;

    private LinearLayout root;
    private TextView title, voiceLabel, sfxLabel;
    private Button voiceMute, sfxMute;
    private Button quitButton, backButton;
    private SeekBar voiceSlider, sfxSlider;

    public SettingsDialog(Context context, MenuState state) {
        super(context);
        this.state = state;
    }


    //CUSTOM METHODS BELOW

    private void adjustContent() {
        Log.v("AudioManager",AudioManager.isVoiceMuted()+" "+AudioManager.isSFXMuted());
        //Voice volume
        if (AudioManager.isVoiceMuted()){
            //voiceMute.setImageBitmap(mute);
            voiceMute.setBackgroundResource(R.drawable.ic_volume_off_white_48dp);
            voiceMute.setBackgroundTintList(muted);
        }
        else {
            voiceMute.setBackgroundResource(R.drawable.ic_volume_up_white_48dp);
            voiceMute.setBackgroundTintList(active);
        }
        //SFX volume
        if (AudioManager.isSFXMuted()){
            sfxMute.setBackgroundResource(R.drawable.ic_volume_off_white_48dp);
            sfxMute.setBackgroundTintList(muted);
        }
        else {
            sfxMute.setBackgroundResource(R.drawable.ic_volume_up_white_48dp);
            sfxMute.setBackgroundTintList(active);
        }
    }


    //INHERITED METHODS

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_settings);
        //Gets elements
        root = (LinearLayout) findViewById(R.id.settings_root);
        title = (TextView) findViewById(R.id.settings_title);
        voiceLabel = (TextView) findViewById(R.id.settings_voice_label);
        sfxLabel = (TextView) findViewById(R.id.settings_sfx_label);
        voiceMute = (Button) findViewById(R.id.settings_voice_mute);
        sfxMute = (Button) findViewById(R.id.settings_sfx_mute);
        quitButton = (Button) findViewById(R.id.settings_exit);
        backButton = (Button) findViewById(R.id.settings_close);
        voiceSlider = (SeekBar) findViewById(R.id.settings_voice_slider);
        sfxSlider = (SeekBar) findViewById(R.id.settings_sfx_slider);
        //Setting fonts
        try {
            Typeface regular = TypefaceManager.get("Oswald-Regular");
            Typeface bold = TypefaceManager.get("Oswald-Bold");
            title.setTypeface(bold);
            voiceLabel.setTypeface(regular);
            sfxLabel.setTypeface(regular);
            quitButton.setTypeface(bold);
            backButton.setTypeface(bold);
        } catch (Exception e) {
        }
        //Sets up values
        active = new ColorStateList(new int[1][0], new int[]{ContextCompat.getColor(getContext(),R.color.main_white)});
        muted = new ColorStateList(new int[1][0], new int[]{ContextCompat.getColor(getContext(),R.color.main_deny)});
        voiceSlider.setProgress((int)(AudioManager.getVoiceVolume()*100));
        sfxSlider.setProgress((int)(AudioManager.getSFXVolume()*100));
        adjustContent();
        //Adding listeners
        voiceMute.setOnClickListener(this);
        sfxMute.setOnClickListener(this);
        quitButton.setOnClickListener(this);
        backButton.setOnClickListener(this);
        voiceSlider.setOnSeekBarChangeListener(this);
        sfxSlider.setOnSeekBarChangeListener(this);
        //Tweaking the quit button
        if (state == MenuState.HOST)
            quitButton.setText(getContext().getString(R.string.close_room));
        else if (state == MenuState.MAIN)
            root.removeView(quitButton);
        adjustContent();
    }

    @Override
    public void onClick(View v) {
        if (v == voiceMute) {
            if (!AudioManager.isVoiceMuted())
                AudioManager.muteVoice();
            else {
                AudioManager.unmuteVoice();
            }
            adjustContent();
        }
        else if (v == sfxMute) {
            if (!AudioManager.isSFXMuted())
                AudioManager.muteSFX();
            else {
                AudioManager.unmuteSFX();
            }
            adjustContent();
        }
        else if (v == quitButton) {
            //TODO: Leave game
            AudioManager.playVoice(getContext(),"audio/testVoice.wav",false);
        }
        else if (v == backButton) {
            cancel();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (seekBar == voiceSlider && AudioManager.isVoiceMuted())
            AudioManager.unmuteVoice();
        else if (seekBar == sfxSlider && AudioManager.isSFXMuted())
            AudioManager.unmuteSFX();
        adjustContent();
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        float newVolume = seekBar.getProgress()/100f;
        if (seekBar == voiceSlider)
            AudioManager.setVoiceVolume(newVolume);
        else if (seekBar == sfxSlider)
            AudioManager.setSFXVolume(newVolume);
    }
}
