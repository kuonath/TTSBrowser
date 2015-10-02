package com.example.kev94.ttsbrowserjs;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

/**
 * Created by Kev94 on 31.08.2015.
 */
public class JSInterface {
    Context mContext;
    AppCompatActivity mActivity;
    CustomWebView mWebView;

    public interface iOnTextSelected {
        void onTextSelected(String id, String text);
    }

    /** Instantiate the interface and set the context */
    JSInterface(Context c, AppCompatActivity a, CustomWebView w) {
        mContext = c;
        mActivity = a;
        mWebView = w;
    }

    /** Show a toast from the web page */
    @JavascriptInterface
    public void onTextSelected(final String id, final String text) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                iOnTextSelected activity = (iOnTextSelected) mActivity;
                activity.onTextSelected(id, text);

                /*ActionMode.Callback customActionModeCallback = new CustomActionModeCallback(mActivity, mWebView);
                mActivity.startActionMode(customActionModeCallback);*/
            }
        });
    }
}
