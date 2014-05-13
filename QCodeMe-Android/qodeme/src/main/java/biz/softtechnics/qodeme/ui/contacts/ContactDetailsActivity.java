package biz.softtechnics.qodeme.ui.contacts;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.utils.AnalyticsHelper;

/**
 * Created by Alex on 10/30/13.
 */
public class ContactDetailsActivity extends Activity implements ColorPicker.OnColorChangedListener {

    private ColorPicker mPicker;
    private SVBar mSvBar;
    private OpacityBar mOpacityBar;
    private Button mButton;
    private TextView mText;
    private Button mApplyButton;
//    private EditText mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsHelper.onCreateActivity(this);
        setContentView(R.layout.activity_contact_details);

        mPicker = (ColorPicker) findViewById(R.id.picker);
        mSvBar = (SVBar) findViewById(R.id.svbar);
        mOpacityBar = (OpacityBar) findViewById(R.id.opacitybar);
        mButton = (Button) findViewById(R.id.button1);
        mText = (TextView) findViewById(R.id.textView1);
        mApplyButton = (Button) findViewById(R.id.apply);
//        mName = (EditText) findViewById(R.id.name);

        mPicker.addSVBar(mSvBar);
        mPicker.addOpacityBar(mOpacityBar);
        mPicker.setOnColorChangedListener(this);
        int color = getIntent().getIntExtra(QodemeContract.Contacts.CONTACT_COLOR, 0);
        color = color == 0 ? Color.GRAY : color;

        mPicker.setOldCenterColor(color);
        mPicker.setNewCenterColor(color);
        mPicker.setColor(color);
//        mName.setText("");
//        mName.append(getIntent().getStringExtra(QodemeContract.Contacts.CONTACT_TITLE));

        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.putExtra(QodemeContract.Contacts._ID, getIntent().getLongExtra(QodemeContract.Contacts._ID, -1));
                i.putExtra(QodemeContract.Contacts.UPDATED, getIntent().getIntExtra(QodemeContract.Contacts.UPDATED, QodemeContract.Contacts.Sync.UPDATED));
                i.putExtra(QodemeContract.Contacts.CONTACT_COLOR, mPicker.getColor());
//                String sName = mName.getText().toString();
                //i.putExtra(QodemeContract.Contacts.CONTACT_TITLE, TextUtils.isEmpty(sName) ? "User" : sName);
                setResult(RESULT_OK, i);
                finish();
            }
        });
    }

    @Override
    public void onColorChanged(int color) {
        //gives the color when it's changed.
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnalyticsHelper.onStartActivity(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        AnalyticsHelper.onStopActivity(this);
    }
}