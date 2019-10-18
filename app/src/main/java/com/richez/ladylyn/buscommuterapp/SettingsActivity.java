package com.richez.ladylyn.buscommuterapp;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;


/**
 * Created by Ladylyn on 26/03/2019.
 */

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainSettings()).commit();


    }

    public static class MainSettings extends PreferenceFragment {
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_screen);
            bindSummaryValue(findPreference("list_notify"));
            bindSummaryValue(findPreference("ringtone_sound"));
        }
    }

    private static void bindSummaryValue(Preference preference) {
        preference.setOnPreferenceChangeListener(listener);
        listener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    private static Preference.OnPreferenceChangeListener listener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String value = newValue.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(value);
                preference.setSummary(index > 0 ? listPreference.getEntries()[index] : null);

            } else if (preference instanceof RingtonePreference) {
                if (TextUtils.isEmpty(value)) {
                    preference.setSummary("Silent");
                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(value));
                    if (ringtone == null) {
                        preference.setSummary("Choose notification ringtone");
                    } else {
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(value);
                    }
                }
            }
            return true;
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
