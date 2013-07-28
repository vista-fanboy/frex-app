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
public class Region implements Cloneable {
    private double centerX;
    private double centerY;
    private double radius;

    public Region(double centerX, double centerY, double radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    public void set(double centerX, double centerY, double radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
    }

    public void set(Region region) {
        set(region.getCenterX(), region.getCenterY(), region.getRadius());
    }

    public double getCenterX() {
        return centerX;
    }

    public double getCenterY() {
        return centerY;
    }

    public double getRadius() {
        return radius;
    }

    public double getPixelSize(int width, int height) {
        return getPixelSize(radius, width, height);
    }


    public static double getPixelSize(double radius, int width, int height) {
        return 2.0 * radius / Math.min(width, height);
    }

    public double getUpperLeftX(int width, double pixelSize) {
        return centerX - 0.5 * pixelSize * width;
    }

    public double getUpperLeftY(int height, double pixelSize) {
        return centerY + 0.5 * pixelSize * height;
    }

    @Override
    public Region clone() {
        try {
            return (Region) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }
}
