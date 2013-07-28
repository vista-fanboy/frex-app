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
import android.graphics.Color;

/**
 * @author Norman Fomferra
 */
public class ColorScheme {

    private final static int _ORANGE = Color.parseColor("#ff8000");
    private final static int _GREEN = Color.parseColor("#008000");
    private final static int _INDIGO = Color.parseColor("#4b0082");
    private final static int _VIOLET = Color.parseColor("#9400d3");
    private final static int _DARK_GREY = Color.parseColor("#aaaaaa");
    private final static int _LIGHT_GREY = Color.parseColor("#444444");

    public final static ColorScheme CLOUDS = new ColorScheme(
            Color.parseColor("#19191d"),
            Color.parseColor("#605f67"),
            Color.parseColor("#737486"),
            Color.parseColor("#b8a3a0"),
            Color.parseColor("#8d94b0"),
            Color.parseColor("#ffc8a0"),
            Color.parseColor("#fefc8d"),
            Color.parseColor("#fefec2")
            );
    public final static ColorScheme BRICKS = new ColorScheme(
            Color.parseColor("#120e05"),
            Color.parseColor("#363531"),
            Color.parseColor("#6d6960"),
            Color.parseColor("#6d6960"),
            Color.parseColor("#d2c9ac"),
            Color.parseColor("#d9c7a1"),
            Color.parseColor("#6d6960"),
            Color.parseColor("#7d6b47"),
            Color.parseColor("#f5f2ed")
    );
    public final static ColorScheme EARTH = new ColorScheme(Color.BLACK, Color.rgb(94, 51, 31), Color.rgb(240, 214, 171), Color.WHITE);
    public final static ColorScheme OCEAN = new ColorScheme(Color.BLACK, Color.rgb(3, 20, 46), Color.rgb(135, 148, 166), Color.rgb(133, 166, 122), Color.WHITE);
    public final static ColorScheme RAINBOW = new ColorScheme(Color.RED, Color.YELLOW, _GREEN, Color.BLUE, _INDIGO, _VIOLET);
    public final static ColorScheme FIRE = new ColorScheme(Color.BLACK, Color.RED, Color.YELLOW, Color.WHITE, Color.rgb(127, 127, 255));
    public final static ColorScheme HEAT = new ColorScheme(Color.BLACK, Color.RED, Color.YELLOW, Color.WHITE);
    public final static ColorScheme BLACK_AND_WHITE = new ColorScheme(Color.BLACK, Color.WHITE);
    public final static ColorScheme METAL = new ColorScheme(Color.BLACK, Color.WHITE, _DARK_GREY,  Color.WHITE, _LIGHT_GREY, Color.WHITE);
    public final static ColorScheme SUNSET = new ColorScheme(Color.BLACK, _ORANGE, Color.WHITE, Color.rgb(255, 90, 100), Color.WHITE);
    public final static ColorScheme ORANGE = new ColorScheme(Color.BLACK, _ORANGE, Color.WHITE);
    public final static ColorScheme YELLOW = new ColorScheme(Color.BLACK, Color.YELLOW, Color.WHITE);
    public final static ColorScheme RED = new ColorScheme(Color.BLACK, Color.RED, Color.WHITE);
    public final static ColorScheme GREEN = new ColorScheme(Color.BLACK, Color.GREEN, Color.WHITE);
    public final static ColorScheme BLUE = new ColorScheme(Color.BLACK, Color.BLUE, Color.WHITE);

    public static class TiePoint {
        private double position;
        private int color;

        public TiePoint(double position, int color) {
            this.color = color;
            this.position = position;
        }

        public double getPosition() {
            return position;
        }

        public int getColor() {
            return color;
        }

        public String getColorString() {
            return "#"
                    + (Color.alpha(color) < 16 ? "0" : "") + Integer.toHexString(Color.alpha(color))
                    + (Color.red(color) < 16 ? "0" : "") + Integer.toHexString(Color.red(color))
                    + (Color.green(color) < 16 ? "0" : "") + Integer.toHexString(Color.green(color))
                    + (Color.blue(color) < 16 ? "0" : "") + Integer.toHexString(Color.blue(color));
        }
    }

    private final TiePoint[] tiePoints;

    public ColorScheme(TiePoint... tiePoints) {
        this.tiePoints = tiePoints.clone();
    }

    public ColorScheme(int... colors) {
        this.tiePoints = new TiePoint[colors.length];
        for (int i = 0; i < colors.length; i++) {
            double position = (double) i / (double) (colors.length - 1);
            tiePoints[i] = new TiePoint(position, colors[i]);
        }
    }

    public String asText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tiePoints.length; i++) {
            TiePoint tiePoint = tiePoints[i];
            if (i > 0) {
                sb.append(";");
            }
            sb.append(tiePoint.position);
            sb.append(',');
            sb.append(tiePoint.getColorString());
        }
        return sb.toString();
    }

    public static ColorScheme parse(String text) throws IllegalArgumentException {
        String[] tiePointValues = text.split(";");
        if (tiePointValues.length < 2) {
            throw new IllegalArgumentException("Invalid color scheme");
        }
        TiePoint[] tiePoints = new TiePoint[tiePointValues.length];
        for (int i = 0; i < tiePointValues.length; i++) {
            String tiePointValue = tiePointValues[i];
            String[] posCol = tiePointValue.split(",");
            if (posCol.length != 2) {
                   throw new IllegalArgumentException("Invalid color scheme tie-point");
            }
            tiePoints[i] = new TiePoint(Double.parseDouble(posCol[0].trim()),
                                        Color.parseColor(posCol[1].trim()));
        }
        return new ColorScheme(tiePoints);
    }

    public TiePoint[] getTiePoints() {
        return tiePoints;
    }

    public int[] createGradient(int colorCount) {
        int[] gradient = new int[colorCount];
        int index = 0;
        for (int i = 0; i < colorCount; i++) {
            double position = i / (double) colorCount;
            if (index < tiePoints.length - 1) {
                if (position > tiePoints[index + 1].getPosition()) {
                    index++;
                }
            }
            if (index < tiePoints.length - 1) {
                gradient[i] = getColor(tiePoints[index], tiePoints[index + 1], position);
            } else {
                gradient[i] = tiePoints[index].getColor();
            }
        }
        return gradient;
    }

    public Bitmap createGradientIcon() {
        int width = 256;
        int height = 48;
        int[] colors = new int[width * height];
        int[] palette = createGradient(width);
        for (int j = 0; j < height; j++) {
            System.arraycopy(palette, 0, colors, j * width, width);
        }
        return Bitmap.createBitmap(colors, width, height, Bitmap.Config.ARGB_8888);
    }

    private int getColor(TiePoint tp1, TiePoint tp2, double position) {
        double w = (position - tp1.getPosition()) / (tp2.getPosition() - tp1.getPosition());
        int c1 = tp1.getColor();
        int c2 = tp2.getColor();
        int a = crop8((int) (Color.alpha(c1) + w * (Color.alpha(c2) - Color.alpha(c1))));
        int r = crop8((int) (Color.red(c1) + w * (Color.red(c2) - Color.red(c1))));
        int g = crop8((int) (Color.green(c1) + w * (Color.green(c2) - Color.green(c1))));
        int b = crop8((int) (Color.blue(c1) + w * (Color.blue(c2) - Color.blue(c1))));
        return Color.argb(a, r, g, b);
    }

    private static int crop8(int iv) {
        if (iv < 0) {
            iv = 0;
        } else if (iv > 255) {
            iv = 255;
        }
        return iv;
    }
}
