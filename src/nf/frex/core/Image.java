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

import android.graphics.Bitmap;

/**
 * @author Norman Fomferra
 */
public class Image {
    private int width;
    private int height;
    private int[] colours;
    private float[] values;
    private boolean[] computed;

    public Image(int width, int height) {
        resize(width, height);
    }

    public boolean[] getComputed() {
        return computed;
    }

    public float[] getValues() {
        return values;
    }

    public int[] getColours() {
        return colours;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Bitmap createBitmap() {
        return Bitmap.createBitmap(getColours(), getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        this.colours = new int[width * height];
        this.values = new float[width * height];
        this.computed = new boolean[width * height];
    }
}
