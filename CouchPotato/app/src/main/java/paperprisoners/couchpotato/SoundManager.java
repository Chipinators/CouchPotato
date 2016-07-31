package paperprisoners.couchpotato;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Ian on 7/28/2016.
 */
public class SoundManager {

    private static boolean voiceMute = false;
    private static boolean sfxMute = false;
    private static float voiceVolume = 0.75f;
    private static float sfxVolume = 0.5f;

    private static ArrayList<MediaPlayer> voice = new ArrayList<>();
    private static ArrayList<MediaPlayer> sfx = new ArrayList<>();


    //PRIVATE CONSTRUCTOR BELOW TO KEEP STATIC

    private SoundManager() {
    }


    //CUSTOM METHODS BELOW

    public static boolean playVoice(Context context, String path, boolean loop) {
        return runAudio(context, path, loop, voice);
    }

    public static boolean playSFX(Context context, String path, boolean loop) {
        return runAudio(context, path, loop, sfx);
    }

    private static boolean runAudio(Context context, final String path, boolean loop, ArrayList<MediaPlayer> list ) {
        try {
            AssetFileDescriptor file = context.getAssets().openFd(path);
            MediaPlayer sound = new MediaPlayer();
            list.add(sound);
            sound.reset();
            sound.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            sound.setAudioStreamType(AudioManager.STREAM_MUSIC);
            sound.setLooping(loop);
            sound.prepare();
            sound.start();
            updateVolumes(list);
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void updateVolumes(ArrayList<MediaPlayer> list) {
        for (int i = 0; i < list.size(); i++) {
            MediaPlayer mp = list.get(i);
            if (!mp.isPlaying()) {
                mp.stop();
                mp.release();
                list.remove(i);
                i--;
            } else {
                if (list == voice) {
                    if (voiceMute)
                        mp.setVolume(0f, 0f);
                    else
                        mp.setVolume(voiceVolume, voiceVolume);
                }
                else if (list == sfx) {
                    if (sfxMute)
                        mp.setVolume(0f, 0f);
                    else
                        mp.setVolume(sfxVolume, sfxVolume);
                }
            }
        }
    }


    //GETTERS AND SETTERS BELOW

    public static final boolean isVoiceMuted() {
        return voiceMute;
    }

    public static final boolean isSFXMuted() {
        return sfxMute;
    }

    public static final float getVoiceVolume() {
        return voiceVolume;
    }

    public static final float getSFXVolume() {
        return sfxVolume;
    }

    public static final void muteVoice() {
        voiceMute = true;
        updateVolumes(voice);
    }

    public static final void unmuteVoice() {
        voiceMute = false;
        updateVolumes(voice);
    }

    public static final void muteSFX() {
        sfxMute = true;
        updateVolumes(sfx);
    }

    public static final void unmuteSFX() {
        sfxMute = false;
        updateVolumes(sfx);
    }

    public static final void setVoiceVolume(float val) {
        if (val >= 0 && val <= 1) {
            voiceVolume = val;
            updateVolumes(voice);
        }
    }

    public static final void setSFXVolume(float val) {
        if (val >= 0 && val <= 1) {
            sfxVolume = val;
            updateVolumes(sfx);
        }
    }
}
