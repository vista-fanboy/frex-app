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

import android.widget.SeekBar;

/**
 * @author Norman Fomferra
 */
public class SeekBarConfigurer {
    final SeekBar seekBar;
    final double minValue;
    final double maxValue;
    final boolean log10Scaled;

    public SeekBarConfigurer(SeekBar seekBar, double minValue, double maxValue, boolean log10Scaled) {
        this.seekBar = seekBar;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.log10Scaled = log10Scaled;
    }

    public static SeekBarConfigurer create(SeekBar seekBar, double minValue, double maxValue, boolean log10Scaled, double initValue) {
        final SeekBarConfigurer configurer = new SeekBarConfigurer(seekBar, minValue, maxValue, log10Scaled);
        configurer.setValue(initValue);
        return configurer;
    }

    public double getValue() {
        return log10Scaled ? Math.pow(10, getRawValue()) : getRawValue();
    }
    public int getValueInt() {
        return (int) Math.round(getValue());
    }

    public void setValue(double value) {
        setRawValue(log10Scaled ? Math.log10(value) : value);
    }

    public void setRandomValue() {
        setRawValue(minValue + Math.random() * (maxValue - minValue));
    }

    private double getRawValue() {
        double scale = (maxValue - minValue) / seekBar.getMax();
        return minValue + scale * seekBar.getProgress();
    }

    private void setRawValue(double value) {
        double ratio = (value - minValue) / (maxValue - minValue);
        seekBar.setProgress((int) Math.round(seekBar.getMax() * ratio));
    }
}
