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

import android.os.Bundle;
import nf.frex.core.PropertySet;

/**
 * @author Norman Fomferra
 */
public class BundlePropertySet implements PropertySet {
    private final Bundle bundle;

    public BundlePropertySet() {
        this(new Bundle());
    }

    public BundlePropertySet(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return bundle.getBoolean(key, defaultValue);
    }

    @Override
    public void setBoolean(String key, boolean value) {
        bundle.putBoolean(key, value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return bundle.getInt(key, defaultValue);
    }

    @Override
    public void setInt(String key, int value) {
        bundle.putInt(key, value);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return bundle.getDouble(key, defaultValue);
    }

    @Override
    public void setDouble(String key, double value) {
        bundle.putDouble(key, value);
    }

    @Override
    public String getString(String key, String defaultValue) {
        return bundle.getString(key, defaultValue);
    }

    @Override
    public void setString(String key, String value) {
        bundle.putString(key, value);
    }
}
