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

package nf.frex.core;

import java.util.Properties;

/**
 * @author Norman Fomferra
 */
public class DefaultPropertySet implements PropertySet {
    private final Properties properties;

    public DefaultPropertySet() {
        this.properties = new Properties();
    }

    public DefaultPropertySet(Properties properties) {
        this.properties = properties;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        String property = properties.getProperty(key);
        if (property != null) {
            return Boolean.parseBoolean(property);
        }
        return defaultValue;
    }

    @Override
    public void setBoolean(String key, boolean value) {
        properties.setProperty(key, value + "");
    }

    @Override
    public int getInt(String key, int defaultValue) {
        String property = properties.getProperty(key);
        if (property != null) {
            try {
                return Integer.parseInt(property);
            } catch (NumberFormatException e) {
                // warn
            }
        }
        return defaultValue;
    }

    @Override
    public void setInt(String key, int value) {
        properties.setProperty(key, value + "");
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        String property = properties.getProperty(key);
        if (property != null) {
            try {
                return Double.parseDouble(property);
            } catch (NumberFormatException e) {
                // warn
            }
        }
        return defaultValue;
    }

    @Override
    public void setDouble(String key, double value) {
        properties.setProperty(key, value + "");
    }

    @Override
    public String getString(String key, String defaultValue) {
        String property = properties.getProperty(key);
        if (property != null) {
            return property;
        }
        return defaultValue;
    }

    @Override
    public void setString(String key, String value) {
        properties.setProperty(key, value);
    }
}
