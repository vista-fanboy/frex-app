/*
 * Frex - a fractal image generator for Android mobile devices
 *
 * Copyright (C) 2012 by Norman Fomferra
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

/**
 * @author Norman Fomferra
 */
public abstract class Fractal {

    public static final Fractal MANDELBROT = new Fractal(new Region(-0.5, 0.0, 1.2), 100, 100.0) {
        @Override
        public int computeOrbit(double initX, double initY, double constX, double constY, int iterMax, double bailOut, double[] orbitX, double[] orbitY) {
            double cx = initX;
            double cy = initY;
            double cxx, cyy;
            for (int iter = 0; iter < iterMax; iter++) {
                cxx = cx * cx;
                cyy = cy * cy;
                if (cxx + cyy > bailOut) {
                    return iter;
                }
                cy = 2.0 * cx * cy + constY;
                cx = cxx - cyy + constX;
                orbitX[iter] = cx;
                orbitY[iter] = cy;
            }
            return iterMax;
        }
    };

    public static final Fractal BURNING_SHIP = new Fractal(new Region(-0.5, 0.0, 1.2), 100, 100.0) {
        @Override
        public int computeOrbit(double initX, double initY, double constX, double constY, int iterMax, double bailOut, double[] orbitX, double[] orbitY) {
            initY *= -1.0;
            constY *= -1.0;

            double cx = initX;
            double cy = initY;
            double cxy, cxx, cyy;

            for (int iter = 0; iter < iterMax; iter++) {
                cxx = cx * cx;
                cyy = cy * cy;
                if (cxx + cyy > bailOut) {
                    return iter;
                }
                cxy = cx * cy;
                if (cxy < 0.0) {
                    cxy *= -1.0;
                }
                cy = 2.0 * cxy + constY;
                cx = cxx - cyy + constX;
                orbitX[iter] = cx;
                orbitY[iter] = cy;
            }
            return iterMax;
        }
    };

    public static final Fractal ODD_ONION = new Fractal(new Region(-0.5, 0.0, 1.2), 150, 100.0) {
        @Override
        public int computeOrbit(double initX, double initY, double constX, double constY, int iterMax, double bailOut, double[] orbitX, double[] orbitY) {
            double cx = initX;
            double cy = initY;
            double cxx, cyy;
            double t;
            for (int iter = 0; iter < iterMax; iter++) {
                cxx = cx * cx;
                cyy = cy * cy;
                if (cxx + cyy > bailOut) {
                    return iter;
                }
                cy = 2.0 * cx * cy + constY;
                cx = cxx - cyy + constX;
                orbitX[iter] = cx;
                orbitY[iter] = cy;
                // This is the term that "disturbs" the Mandelbrot set
                if (cy < cx) {
                    t = cy;
                    cy = cx;
                    cx = t;
                }
            }
            return iterMax;
        }
    };

    public static final Fractal NOVA = new Fractal(new Region(0, 0, 1), 100, 0.001) {
        @Override
        public int computeOrbit(double initX, double initY, double constX, double constY, int iterMax, double bailOut, double[] orbitX, double[] orbitY) {

            final double rr = bailOut * bailOut;
            double zx = initX;
            double zy = initY;

            double dd, zzx = zx, zzy = zy;
            double t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15;

            for (int iter = 0; iter < iterMax; iter++) {
                t6 = zx + 1.0;
                t10 = zx - 1.0;
                t8 = zx * t10;
                t9 = zy * zy;
                t11 = zx * zy;
                t12 = t8 - t9;
                t14 = t10 * zy;
                t13 = t11 + t14;
                t1 = t12 * t6 - t13 * zy;
                t5 = 2.0 * zx - 1.0;
                t7 = 2.0 * zy;
                t2 = t5 * t6 - t7 * zy + t8 - t9;
                t3 = t12 * zy + t6 * t13;
                t4 = t5 * zy + t6 * t7 + t11 + t14;
                t15 = t2 * t2 + t4 * t4;
                zx = constX + zx - (t1 * t2 + t3 * t4) / t15;
                zy = constY + zy - (t2 * t3 - t1 * t4) / t15;

                dd = (zx - zzx) * (zx - zzx) + (zy - zzy) * (zy - zzy);
                if (dd < rr) {
                    return iter;
                }
                zzx = zx;
                zzy = zy;

                orbitX[iter] = zx;
                orbitY[iter] = zy;
            }
            return iterMax;
        }
    };


    private final Region defaultRegion;
    private final int defaultIterMax;
    private final double defaultBailOut;

    protected Fractal(Region defaultRegion, int defaultIterMax, double defaultBailOut) {
        this.defaultBailOut = defaultBailOut;
        this.defaultRegion = defaultRegion;
        this.defaultIterMax = defaultIterMax;
    }

    public abstract int computeOrbit(double initX, double initY,
                                     double constX, double constY,
                                     int iterMax,
                                     double bailOut,
                                     double[] orbitX, double[] orbitY);

    public double getDefaultBailOut() {
        return defaultBailOut;
    }

    public int getDefaultIterMax() {
        return defaultIterMax;
    }

    public Region getDefaultRegion() {
        return defaultRegion.clone();
    }
}
