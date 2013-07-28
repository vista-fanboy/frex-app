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

/**
 * @author Norman Fomferra
 */
public final class OrbitFunction {

    private final double dilation;
    private final double translateX;
    private final double translateY;
    private final DistanceFunction distanceFunction;

    public OrbitFunction(DistanceFunction distanceFunction, double dilation, double translateX, double translateY) {
        this.dilation = dilation;
        this.translateX = translateX;
        this.translateY = translateY;
        this.distanceFunction = distanceFunction;
    }

    public double getDilation() {
        return dilation;
    }

    public DistanceFunction getDistanceFunction() {
        return distanceFunction;
    }

    public double getTranslateX() {
        return translateX;
    }

    public double getTranslateY() {
        return translateY;
    }

    /**
     * @param numPoints
     * @param orbitX
     * @param orbitY
     * @return a number x, with 0 <= x <= n
     */
    public float processOrbitNew(int numPoints, double[] orbitX, double[] orbitY) {
        double vicinitySum = 0.0;
        if (numPoints > 0) {
            final double a = dilation;
            final double aa = a * a;
            final double tx = this.translateX;
            final double ty = this.translateY;
            final DistanceFunction f = this.distanceFunction;
            double distance;
            for (int i = 0; i < numPoints; i++) {
                distance = f.evaluate(orbitX[i] - tx, orbitY[i] - ty);
                distance = distance * distance;
                if (distance < aa) {
                    vicinitySum += Math.sqrt(aa - distance) / a;
                }
            }
        }
        return (float) vicinitySum;
    }

    /**
     * @param numPoints
     * @param orbitX
     * @param orbitY
     * @return a number x, with 0 <= x <= n
     */
    public float processOrbit(int numPoints, double[] orbitX, double[] orbitY) {
        double vicinitySum = 0.0;
        if (numPoints > 0) {
            final double a = 1.0 / dilation;
            final double tx = this.translateX;
            final double ty = this.translateY;
            final DistanceFunction f = this.distanceFunction;
            double distance;
            for (int i = 0; i < numPoints; i++) {
                distance = a * f.evaluate(orbitX[i] - tx, orbitY[i] - ty);
                vicinitySum += 1.0 / (1.0 + distance * distance);
            }
        }
        return (float) vicinitySum;
    }
}
