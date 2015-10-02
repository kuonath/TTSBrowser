package com.example.kev94.ttsbrowserjs;

import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.ValueCallback;

/**
 * Created by Kev94 on 31.08.2015.
 */
public class CustomActionModeCallback implements ActionMode.Callback {
    // Called when the action mode is created; startActionMode() was called

    private AppCompatActivity mActivity;
    private CustomWebView mCustomWebView;

    public CustomActionModeCallback(AppCompatActivity activity, CustomWebView customWebView) {
        mActivity = activity;
        mCustomWebView = customWebView;
    }

    public interface iOnMenuSelected {
        void onMenuSelected(String menu);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        // Inflate a menu resource providing context menu items
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_ttsbrowser, menu);
        return true;
    }

    // Called each time the action mode is shown. Always called after onCreateActionMode, but
    // may be called multiple times if the mode is invalidated.
    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false; // Return false if nothing is done
    }

    // Called when the user selects a contextual menu item
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_read:

                iOnMenuSelected activity = (iOnMenuSelected) mActivity;
                activity.onMenuSelected("read");

                mode.finish(); // Action picked, so close the CAB
                return true;
            default:
                return false;
        }
    }

    // Called when the user exits the action mode
    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mode = null;
    }

    /*@TargetApi(21)
    public void speakSelection(String selection) {
        if(android.os.Build.VERSION.SDK_INT < 19) {
            mTTS.speak(selection, TextToSpeech.QUEUE_FLUSH, null);
        } else {
            mTTS.speak(selection, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }*/
}

