package paperprisoners.couchpotato;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Ian on 7/31/2016.
 */
public class KeyboardHidingListener implements View.OnFocusChangeListener, View.OnClickListener {

    private Activity activity;
    private ViewGroup root;

    public KeyboardHidingListener(Activity activity, ViewGroup root) {
        this.activity = activity;
        this.root = root;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
            try {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            } catch (NullPointerException n) {
                Log.i("KeyboardHidingListener", "Keyboard not available to hide. onFocusChange failed.");
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == root) {
            try {
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
            } catch (NullPointerException n) {
                Log.i("KeyboardHidingListener", "Keyboard not available to hide. onClick failed.");
            }
        }
    }
}
