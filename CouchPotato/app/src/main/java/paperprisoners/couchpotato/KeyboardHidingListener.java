package paperprisoners.couchpotato;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
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
            InputMethodManager imm =  (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == root) {
            InputMethodManager imm =  (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
