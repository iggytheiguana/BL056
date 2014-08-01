package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.one2one.ChatListGroupItem;


public class EditTextPreIme extends EditText {

    private MainActivity parent;

    public EditTextPreIme(Context context) {
        super(context);
    }

    public EditTextPreIme(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextPreIme(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            parent.deleteChat(null);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void setParent(MainActivity _parent) {
        parent = _parent;
    }

}
