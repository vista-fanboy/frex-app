/*
 * Frex - a fractal image generator for Android mobile devices
 *
 * Copyright (C) 2013 by Norman Fomferra
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package nf.frex.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import java.io.File;
import java.util.List;

/**
 * @author Norman Fomferra
 */
public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String NUM_TASKS_PREF_KEY = "num_tasks";
    public static final int NUM_CORES = Runtime.getRuntime().availableProcessors();
    public static final int NUM_TASKS_DEFAULT = 2 * NUM_CORES;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        ListPreference numTasksPref = (ListPreference) getPreferenceManager().findPreference(NUM_TASKS_PREF_KEY);
        int numTasksMax = NUM_TASKS_DEFAULT + 2;
        String[] numTasksValues = new String[numTasksMax];
        for (int i = 0; i < numTasksMax; i++) {
            numTasksValues[i] = (1 + i) + "";
        }
        numTasksPref.setDefaultValue(NUM_TASKS_DEFAULT + "");
        numTasksPref.setEntries(numTasksValues);
        numTasksPref.setEntryValues(numTasksValues);
        setNumTasksSummary(PreferenceManager.getDefaultSharedPreferences(this), numTasksPref);

        updateStorageStatusSummary();

        String versionName;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?";
        }
        Preference version = getPreferenceManager().findPreference("version");
        version.setSummary(getString(R.string.version_summary, versionName));

        // Only if a preference changes, we return RESULT_OK
        setResult(RESULT_CANCELED);
    }

    private void setNumTasksSummary(SharedPreferences preferences, Preference preference) {
        int numTasks = getNumTasks(preferences);
        preference.setSummary(getResources().getQuantityString(R.plurals.num_tasks_summary, NUM_CORES, NUM_CORES, numTasks));
    }

    public static int getNumTasks(Context context) {
        return getNumTasks(PreferenceManager.getDefaultSharedPreferences(context));
    }

    public static int getNumTasks(SharedPreferences preferences) {
        String string = preferences.getString(NUM_TASKS_PREF_KEY, NUM_TASKS_DEFAULT + "");
        return Integer.parseInt(string);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(NUM_TASKS_PREF_KEY)) {
            setNumTasksSummary(sharedPreferences, findPreference(key));
        } else if (key.equals("delete_all")) {
            updateStorageStatusSummary();
        }
        setResult(RESULT_OK);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void updateStorageStatusSummary() {
        FrexIO frexIO = new FrexIO(this);
        List<File> fileList = frexIO.getFileList();

        long bytes = 0;
        for (File file : fileList) {
            bytes += file.length();
        }
        String storageUsed;
        long mibs = Math.round(bytes / (1000.0 * 1000.0));
        if (mibs > 0) {
            storageUsed = mibs + " MiB";
        } else {
            long kibs = Math.round(bytes / (1024.0));
            if (kibs > 0) {
                storageUsed = kibs + " KiB";
            } else {
                storageUsed = bytes + " Byte";
            }
        }
        Preference preference = findPreference("storage_status");
        String summary = getResources().getQuantityString(R.plurals.storage_summary, fileList.size(), fileList.size(), storageUsed, frexIO.getAppStorageDir().getPath());
        preference.setSummary(summary);
    }

}
