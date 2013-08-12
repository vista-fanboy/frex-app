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

import java.util.Arrays;
import java.util.Comparator;

import static java.lang.Math.abs;

/**
 * Color quantization based on k-means.
 *
 * @author Norman Fomferra
 */
public class ColorQuantizer {

    private  final int clusterCount;
    private  final int maxDistance;
    private  final int maxIterCount;

    private boolean canceled;

    public ColorQuantizer() {
        this(8, 3, 25);
    }

    public ColorQuantizer(int clusterCount, int maxDistance, int maxIterCount) {
        this.clusterCount = clusterCount;
        this.maxDistance = maxDistance;
        this.maxIterCount = maxIterCount;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public int getClusterCount() {
        return clusterCount;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public int getMaxIterCount() {
        return maxIterCount;
    }

    public void cancel() {
        this.canceled = canceled;
    }

    public ColorScheme quantize(Bitmap bitmap, ProgressListener progressListener) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        AccuColor[] newCenterColors = new AccuColor[clusterCount];
        for (int i = 0; i < newCenterColors.length; i++) {
            newCenterColors[i] = new AccuColor();
        }
        int[] pixels = new int[width * height];

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        final Color[] centerColors = new Color[clusterCount];
        for (int i = 0; i < centerColors.length; i++) {
            int rgb = pixels[(int) (pixels.length * Math.random())];
            centerColors[i] = new Color(rgb);
        }

        int iterCount;
        for (iterCount = 1; iterCount <= maxIterCount; iterCount++) {

            for (int rgb : pixels) {
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = (rgb) & 0xFF;
                int minDist = Integer.MAX_VALUE;
                int minIndex = 0;
                for (int k = 0; k < centerColors.length; k++) {
                    int dist = centerColors[k].mdist(r, g, b);
                    if (dist < minDist) {
                        minDist = dist;
                        minIndex = k;
                    }
                }
                newCenterColors[minIndex].add(r, g, b);
            }

            //System.out.println("Iteration " + i);
            int maxDistance = Integer.MIN_VALUE;
            for (int k = 0; k < centerColors.length; k++) {
                Color oldColor = centerColors[k];
                Color newColor = newCenterColors[k].createPoint();
                centerColors[k] = newColor;
                int dist = oldColor.mdist(newColor);
                //System.out.printf("dist[%d] = %s\n", k, dist);
                newCenterColors[k].reset();
                maxDistance = Math.max(maxDistance, dist);
            }

            if (canceled) {
                return null;
            }

            progressListener.progress("iter " + iterCount + ", " + maxDistance, iterCount, maxIterCount);

            if (maxDistance <= this.maxDistance) {
                break;
            }
        }

        //System.out.println(iter + " iterations");

        Arrays.sort(centerColors, new Comparator<Color>() {
            @Override
            public int compare(Color o1, Color o2) {
                return o1.len() - o2.len();
            }
        });

        ColorScheme.TiePoint[] tiePoints = new ColorScheme.TiePoint[clusterCount];
        for (int i = 0; i < tiePoints.length; i++) {
            tiePoints[i] = new ColorScheme.TiePoint(i / (clusterCount - 1.0), centerColors[i].getRGB());
        }

        return new ColorScheme(tiePoints);

    }

    public interface ProgressListener {
        void progress(String msg, int iter, int maxIter);
    }

    private final static class Color {

        final int r;
        final int g;
        final int b;

        public Color(int rgb) {
            this((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
        }

        public Color(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        public int mdist(Color p) {
            return abs(r - p.r) + abs(g - p.g) + abs(b - p.b);
        }

        public int mdist(int r, int g, int b) {
            return abs(this.r - r) + abs(this.g - g) + abs(this.b - b);
        }

        public int getRGB() {
            int a = 255;
            return ((a & 0xFF) << 24) |
                    ((r & 0xFF) << 16) |
                    ((g & 0xFF) << 8) |
                    (b & 0xFF);
        }

        public int len() {
            return r + g + b;
        }
    }

    private final static class AccuColor {
        double r;
        double g;
        double b;
        int n;

        public void add(int r, int g, int b) {
            this.r += r / 255.0;
            this.g += g / 255.0;
            this.b += b / 255.0;
            n++;
        }

        public Color createPoint() {
            return new Color((int) (255.0 * r / n + 0.5),
                             (int) (255.0 * g / n + 0.5),
                             (int) (255.0 * b / n + 0.5));
        }

        public void reset() {
            r = g = b = 0.0;
            n = 0;
        }
    }
}
