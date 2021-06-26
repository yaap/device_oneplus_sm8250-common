/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2019 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.doze;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.provider.Settings;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

public class DozeSettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener {


    private ListPreference mPickUpPreference;
    private SwitchPreference mPocketPreference;
    CharSequence[] mPickupprefentries;
    private Handler mHandler = new Handler();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.doze_settings);

        SharedPreferences prefs = getActivity().getSharedPreferences("doze_settings",
                Activity.MODE_PRIVATE);
        if (savedInstanceState == null && !prefs.getBoolean("first_help_shown", false)) {
            showHelp();
        }

        mPickUpPreference = (ListPreference) findPreference(Utils.GESTURE_PICK_UP_KEY);
        mPickUpPreference.setOnPreferenceChangeListener(this);
        mPickupprefentries = mPickUpPreference.getEntries();

        mPocketPreference = (SwitchPreference) findPreference(Utils.GESTURE_POCKET_KEY);
        mPocketPreference.setOnPreferenceChangeListener(this);
        updateEnablement();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateEnablement();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        mHandler.post(() -> Utils.checkDozeService(getActivity()));
        mHandler.post(() -> updateEnablement());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }

    private static class HelpDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.doze_settings_help_title)
                    .setMessage(R.string.doze_settings_help_text)
                    .setNegativeButton(R.string.dialog_ok, (dialog, which) -> dialog.cancel())
                    .create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            getActivity().getSharedPreferences("doze_settings", Activity.MODE_PRIVATE)
                    .edit()
                    .putBoolean("first_help_shown", true)
                    .commit();
        }
    }

    private void showHelp() {
        HelpDialogFragment fragment = new HelpDialogFragment();
        fragment.show(getFragmentManager(), "help_dialog");
    }

    private void updatePref()
    {
        int currentpickuprefvalue = Integer.parseInt(mPickUpPreference.getValue());
        mPickUpPreference.setSummary(mPickupprefentries[currentpickuprefvalue]);
        mPocketPreference.setSummary(R.string.pocket_gesture_summary);
    }

    private void updateEnablement() {
        boolean isAODEnabled = Settings.Secure.getIntForUser(getContext().getContentResolver(),
                Settings.Secure.DOZE_ALWAYS_ON, 0, UserHandle.USER_CURRENT) == 1;
        boolean isDozeEnabled = Settings.Secure.getIntForUser(getContext().getContentResolver(),
        Settings.Secure.DOZE_ENABLED, 1, UserHandle.USER_CURRENT) != 0;
        boolean enabled = isDozeEnabled && !isAODEnabled;
        mPickUpPreference.setEnabled(enabled);
        mPocketPreference.setEnabled(enabled);
        if(enabled)
            updatePref();
        else if(isAODEnabled) {
            mPickUpPreference.setSummary(R.string.disabled_for_aod);
            mPocketPreference.setSummary(R.string.disabled_for_aod);
        }
        else {
            mPickUpPreference.setSummary(R.string.disabled_for_doze);
            mPocketPreference.setSummary(R.string.disabled_for_doze);
        } 

    }
}
