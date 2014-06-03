package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by Alex on 1/31/14.
 */
public class CustomEdit extends EditText{

    private OnEditTextImeBackListener mOnImeBack;

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
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnEditTextImeBackListener(OnEditTextImeBackListener listener) {
        mOnImeBack = listener;
    }

    public interface OnEditTextImeBackListener {
        public abstract void onImeBack(CustomEdit ctrl);
    }


}
