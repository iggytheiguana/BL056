package biz.softtechnics.qodeme.ui.preferences;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.sync.SyncHelper;
import biz.softtechnics.qodeme.utils.AnalyticsHelper;

/**
 * Created by Alex on 11/19/13.
 */
public class SettingsActivity extends PreferenceActivity{

    private boolean mPreferencesChanged = false;
    private SharedPreferences.OnSharedPreferenceChangeListener mSpChanged;
    private EditTextPreference mPrefEditName;
    private EditTextPreference mPrefEditMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsHelper.onCreateActivity(this);
        getPreferenceManager().setSharedPreferencesName(QodemePreferences.getInstance().getName());
        addPreferencesFromResource(R.xml.preference_main);

        mPrefEditName = (EditTextPreference) findPreference("pref_edit_name");
        final SharedPreferences prefs = getSharedPreferences(QodemePreferences.getInstance().getName(), MODE_PRIVATE);
        ;
        String publicName = prefs.getString("pref_edit_name", "User");
        mPrefEditName.setSummary(publicName);
        mPrefEditName.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newName = (String) newValue;
                mPrefEditName.setSummary(newName);
                mPrefEditName.setText(newName);

                return false;
            }
        });

        mPrefEditMessage = (EditTextPreference) findPreference("pref_edit_message");
        String message = prefs.getString("pref_edit_message", "");
        mPrefEditMessage.setSummary(message);
        mPrefEditMessage.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newMessage = (String) newValue;
                mPrefEditMessage.setSummary(newMessage);
                mPrefEditMessage.setText(newMessage);

                return false;
            }
        });

        initActionBar();

        mSpChanged = new
                SharedPreferences.OnSharedPreferenceChangeListener() {

                    @Override
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        mPreferencesChanged = true;
                    }
                };

    }

    @Override
    protected void onStart() {
        super.onStart();
        QodemePreferences.getInstance().getSharedPreferences().registerOnSharedPreferenceChangeListener(mSpChanged);
        AnalyticsHelper.onStartActivity(this);
    }

    @Override
    protected void onStop() {
        QodemePreferences.getInstance().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mSpChanged);
        if (mPreferencesChanged){
            QodemePreferences.getInstance().setUserSettingsUpToDate(false);
            SyncHelper.requestManualSync();
            mPreferencesChanged = false;
        }
        super.onStop();
        AnalyticsHelper.onStopActivity(this);
    }

    private void initActionBar() {


    }
}
