package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

public class CustomEdit extends EditText {

    private OnEditTextImeBackListener mOnImeBack;
    private OnEditTextImeEnterListener mOnImeEnter;

    public CustomEdit(Context context) {
        super(context);
    }

    public CustomEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEdit(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeBack != null) mOnImeBack.onImeBack(this);
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeEnter != null) mOnImeEnter.onImeEnter(this);
        }
        Log.d("keyCode", "" + keyCode + " " + event.getDisplayLabel());
        return super.dispatchKeyEvent(event);
    }

    public void setOnEditTextImeBackListener(OnEditTextImeBackListener listener) {
        mOnImeBack = listener;
    }

    public void setOnEditTextImeEnterListener(OnEditTextImeEnterListener listener) {
        mOnImeEnter = listener;
    }

    public interface OnEditTextImeBackListener {
        public abstract void onImeBack(CustomEdit ctrl);
    }

    public interface OnEditTextImeEnterListener {
        public abstract void onImeEnter(CustomEdit ctrl);
    }


}
