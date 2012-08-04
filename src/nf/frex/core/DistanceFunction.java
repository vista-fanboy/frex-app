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

import static java.lang.Math.*;

/**
 * @author Norman Fomferra
 */
public interface DistanceFunction {
    DistanceFunction X = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return x;
        }
    };
    DistanceFunction Y = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return y;
        }
    };
    DistanceFunction MIN_XY = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return min(x, y);
        }
    };
    DistanceFunction MAX_XY = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return max(x, y);
        }
    };
    DistanceFunction ADD_XY = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return x + y;
        }
    };
    DistanceFunction SUB_XY = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return x - y;
        }
    };
    DistanceFunction MUL_XY = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return x * y;
        }
    };
    DistanceFunction DIV_XY = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return abs(y) > 0 ? x / y : Double.POSITIVE_INFINITY;
        }
    };
    DistanceFunction LOG_XX = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return log(x * x);
        }
    };

    DistanceFunction LINEAR_1 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return min(abs(x), abs(y));
        }
    };
    DistanceFunction LINEAR_2 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return min(abs(y - x), abs(y + x));
        }
    };
    DistanceFunction LINEAR_3 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return min(min(abs(x), abs(y - x)), min(abs(y), abs(y + x)));
        }
    };
    DistanceFunction CUBIC_1 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return abs((x + 1) * x * (x - 1));
        }
    };
    DistanceFunction CUBIC_2 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            double v = (x + 1) * x * (x - 1);
            return min(abs(y - v), abs(y + v));
        }
    };
    DistanceFunction RING_1 = new DistanceFunction() {
        @Override
        public final double evaluate(double x, double y) {
            double v = x * x + y * y;
            return v > 1.0 ? v - 1.0 : 1.0 - v;
        }
    };
    DistanceFunction RING_2 = new DistanceFunction() {
        @Override
        public final double evaluate(double x, double y) {
            double v = x * x + y * y;
            return min(v > 1.0 ? v - 1.0 : 1.0 - v, v > 2.0 ? v - 2.0 : 2.0 - v);
        }
    };
    DistanceFunction RING_3 = new DistanceFunction() {
        @Override
        public final double evaluate(double x, double y) {
            double v = x * x + y * y;
            return min(min(v > 1.0 ? v - 1.0 : 1.0 - v, v > 2.0 ? v - 2.0 : 2.0 - v), v > 3.0 ? v - 3.0 : 3.0 - v);
        }
    };
    DistanceFunction RING_4 = new DistanceFunction() {
        @Override
        public final double evaluate(double x, double y) {
            double v = log(x * x + y * y);
            return 1.0 + sin(v);
        }
    };
    DistanceFunction SIN_1 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return y - sin(x);
        }
    };
    DistanceFunction SIN_2 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            double v = 1.0 + sin(x);
            return min(y - v, y + v);
        }
    };
    DistanceFunction SIN_3 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return 1.0 + min(sin(x), sin(y));
        }
    };
    DistanceFunction SIN_4= new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return sin(x * y);
        }
    };
    DistanceFunction DIST_1 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return x * x + y * y;
        }
    };
    DistanceFunction DIST_2 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return x * x - y * y;
        }
    };
    DistanceFunction DIST_3 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return (x - y) * (x - y);
        }
    };
    DistanceFunction LOG_DIST_1 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return log(x * x + y * y);
        }
    };
    DistanceFunction LOG_DIST_2 = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            double dx1 = (x - 1) * (x - 1);
            double dx2 = (x + 1) * (x + 1);
            double dy1 = (y - 1) * (y - 1);
            double dy2 = (y + 1) * (y + 1);
            return log(min(min(dx1 + dy1, dx1 + dy2), min(dx2 + dy1, dx2 + dy2)));
        }
    };

    DistanceFunction FLOOR_DIST = new DistanceFunction() {
        @Override
        public double evaluate(double x, double y) {
            return floor(x * x + y * y);
        }
    };


    double evaluate(double x, double y);
}
