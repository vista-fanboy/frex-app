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

    private final DistanceFunction distanceFunction;
    private final double dilation;
    private final double translateX;
    private final double translateY;
    private final boolean turbulence;
    private final double turbulenceIntensity;
    private final double turbulenceScale;

    public OrbitFunction(DistanceFunction distanceFunction, double dilation, double translateX, double translateY, boolean turbulence, double turbulenceIntensity, double turbulenceScale) {
        this.dilation = dilation;
        this.translateX = translateX;
        this.translateY = translateY;
        this.distanceFunction = distanceFunction;
        this.turbulence = turbulence;
        this.turbulenceIntensity = turbulenceIntensity;
        this.turbulenceScale = turbulenceScale;
    }

    /**
     * @param numPoints The orbit's number of points
     * @param orbitX The orbit's X-values
     * @param orbitY The orbit's Y-values
     * @return a number x, with 0 <= x <= numPoints
     */
    public float processOrbit(int numPoints, double[] orbitX, double[] orbitY) {
        double vicinitySum = 0.0;
        if (numPoints > 0) {
            final DistanceFunction f = this.distanceFunction;
            final double a = 1.0 / dilation;
            final double tx = this.translateX;
            final double ty = this.translateY;
            double distance;
            if (turbulence) {
                final double ti =  turbulenceIntensity;
                final double ts =  turbulenceScale;
                double x, y, t;
                for (int i = 0; i < numPoints; i++) {
                    x = orbitX[i];
                    y = orbitY[i];
                    t = ti * Turbulence.computeTurbulence(x, y, ts, 3);
                    distance = a * f.evaluate(x  - tx + t, y - ty + t);
                    vicinitySum += 1.0 / (1.0 + distance * distance);
                }
            }  else {
                for (int i = 0; i < numPoints; i++) {
                    distance = a * f.evaluate(orbitX[i] - tx, orbitY[i] - ty);
                    vicinitySum += 1.0 / (1.0 + distance * distance);
                }
            }
        }
        return (float) vicinitySum;
    }
}
