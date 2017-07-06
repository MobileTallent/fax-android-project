package co.faxapp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.parse.ParseUser;
import com.parse.ui.Countries;

import co.faxapp.App;
import co.faxapp.R;
import co.faxapp.dialogs.Dialogs;

public class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = PrefsFragment.class.getSimpleName();
    private SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        CheckBoxPreference custom = (CheckBoxPreference) findPreference("customHeader");
        App app = (App) getActivity().getApplication();
        if (app.getPremiumPagesCount() > 0) {
            custom.setEnabled(true);
        } else {
            custom.setEnabled(false);
        }
        EditTextPreference textHeader = (EditTextPreference) findPreference("textHeader");
        textHeader.setSummary(sp.getString("textHeader", ""));

//        EditTextPreference emailForFax = (EditTextPreference) findPreference("emailForFax");
//        emailForFax.setSummary(sp.getString("emailForFax", ""));
//        if (app.isFaxToEmail() && sp.getString("emailForFax", "").equals("")) {
//            emailForFax.setEnabled(true);
//        } else {
//            emailForFax.setEnabled(false);
//        }

        final Preference countryText = findPreference("userCountry");
        final String countryName = ParseUser.getCurrentUser().getString("country");
        countryText.setSummary(countryName);
        countryText.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Dialogs.setUserCountry(getActivity(), Countries.get().getCountryPositionByName(countryName), new Dialogs.CountryListener() {
                    @Override
                    public void updateCountry() {
                        countryText.setSummary(ParseUser.getCurrentUser().getString("country"));
                    }
                });
                return true;
            }
        });

//        updateNumber();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (preference instanceof EditTextPreference) {
            preference.setSummary(sharedPreferences.getString(key, ""));
        }
//        if (key.equals("emailForFax")) {
//            FaxToEmailService faxToEmailService = new FaxToEmailService(getActivity());
//            String phone = sharedPreferences.getString("phoneForFax", null);
//            if (phone != null) {
//                faxToEmailService.removeUserRequest(phone,true);
//            }
//            faxToEmailService.createUserRequest(sharedPreferences.getString(key, null));
//            preference.setEnabled(false);
//        }
    }


    @Override
    public void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

//    public void updateNumber() {
//        Preference phoneForFax = findPreference("phoneForFax");
//        phoneForFax.setSummary(sp.getString("phoneForFax", ""));
//
//
//    }
}
