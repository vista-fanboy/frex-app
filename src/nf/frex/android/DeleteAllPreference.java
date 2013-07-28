
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
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.Toast;

import java.io.File;
import java.util.List;

/**
 * @author Norman Fomferra
 */
public class DeleteAllPreference extends DialogPreference {

    public DeleteAllPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DeleteAllPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setDialogLayoutResource(R.layout.delete_all);
        setNegativeButtonText(android.R.string.cancel);
        setPositiveButtonText(android.R.string.ok);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            deleteAllFiles();

            // Simply change value in order to force preference change notification
            boolean oldValue = getSharedPreferences().getBoolean(getKey(), false);
            SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putBoolean(getKey(), !oldValue);
            editor.commit();
        }
    }

    private int deleteAllFiles() {
        FrexIO frexIO = new FrexIO(getContext());
        List<File> fileList = frexIO.getFileList();
        int n = 0;
        for (File file : fileList) {
            if (file.delete()) {
                n++;
            }
        }
        return n;
    }
}
