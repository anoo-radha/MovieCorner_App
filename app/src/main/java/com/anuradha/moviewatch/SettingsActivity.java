package com.anuradha.moviewatch;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.util.Set;

/* This is the activity that opens when settings option is selected from the menu
*  It displays three options to sort the movies - by most-popular, by highest-rated and by favorites*/
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);
        Preference preference = findPreference(getString(R.string.pref_genre_key));
        preference.setOnPreferenceChangeListener(this);
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getStringSet(preference.getKey(), null));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        MultiSelectListPreference mlPreference = (MultiSelectListPreference) preference;
        String summary="";
        if(newValue != null ) {
            Set<String> ValueArray = (Set<String>)newValue;
            for (String e : ValueArray) {
                int prefIndex = mlPreference.findIndexOfValue(e);
                summary = summary + mlPreference.getEntries()[prefIndex] + "  ";
            }
        }
        if(summary.equals("")) {
            mlPreference.setSummary(getString(R.string.genre_summary));
        } else{
            mlPreference.setSummary(summary);
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}