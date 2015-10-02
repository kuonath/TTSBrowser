package com.example.kev94.ttsbrowserjs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.webkit.WebView;

/**
 * Created by Kev94 on 31.08.2015.
 */
public class CustomWebView extends WebView {

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*public ActionMode startActionMode(ActionMode.Callback callback)
    {
        ActionMode.Callback customActionModeCallback = new CustomActionModeCallback(this);
        return super.startActionMode(customActionModeCallback);
    }*/

}
