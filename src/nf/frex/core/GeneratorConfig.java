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
public class GeneratorConfig {

    public static final int GRADIENT_COLOR_COUNT = 1024;
    private String configName;
    private String fractalId;
    private Region region;
    private int iterMax;
    private double bailOut;
    private boolean juliaModeFractal;
    private double juliaX;
    private double juliaY;

    private boolean decoratedFractal;
    private String distanceFunctionId;
    private double distanceDilation;
    private double distanceTranslateX;
    private double distanceTranslateY;

    private String colorSchemeId;
    private ColorScheme colorScheme;
    private int[] colorGradient;
    private double colorGain;
    private double colorOffset;
    private boolean colorRepeat;

    public void restoreInstanceState(PropertySet propertySet) {
        setFractalId(propertySet.getString("fractalId", getFractalId()));
        setIterMax(propertySet.getInt("iterMax", getIterMax()));
        setBailOut(propertySet.getDouble("bailOut", getBailOut()));
        setJuliaModeFractal(propertySet.getBoolean("juliaModeFractal", isJuliaModeFractal()));
        setJuliaX(propertySet.getDouble("juliaX", getJuliaX()));
        setJuliaY(propertySet.getDouble("juliaY", getJuliaY()));
        setRegion(new Region(propertySet.getDouble("regionCenterX", getRegion().getCenterX()),
                             propertySet.getDouble("regionCenterY", getRegion().getCenterY()),
                             propertySet.getDouble("regionRadius", getRegion().getRadius())));

        setDecoratedFractal(propertySet.getBoolean("decoratedFractal", isDecoratedFractal()));
        setDistanceFunctionId(propertySet.getString("distanceFunctionId", getDistanceFunctionId()));
        setDistanceDilation(propertySet.getDouble("distanceDilation", getDistanceDilation()));
        setDistanceTranslateX(propertySet.getDouble("distanceTranslateX", getDistanceTranslateX()));
        setDistanceTranslateY(propertySet.getDouble("distanceTranslateY", getDistanceTranslateY()));

        setColorSchemeId(propertySet.getString("colorSchemeId", getColorSchemeId()));
        ColorScheme colorScheme = Registries.colorSchemes.getValue(getColorSchemeId());
        if (colorScheme != null) {
            setColorScheme(colorScheme);
        } else {
            String colorSchemeValue = propertySet.getString("colorScheme", null);
            if (colorSchemeValue != null) {
                try {
                    setColorScheme(ColorScheme.parse(colorSchemeValue));
                } catch (IllegalArgumentException e) {
                    setColorSchemeId(Registries.colorSchemes.getId(0));
                    setColorScheme(Registries.colorSchemes.getValue(0));
                }
            }
        }
        setColorGain(propertySet.getDouble("colorGain", getColorGain()));
        setColorOffset(propertySet.getDouble("colorOffset", getColorOffset()));
        setColorRepeat(propertySet.getBoolean("colorRepeat", isColorRepeat()));
    }

    public void saveInstanceState(PropertySet propertySet) {
        propertySet.setString("fractalId", getFractalId());
        propertySet.setInt("iterMax", getIterMax());
        propertySet.setDouble("bailOut", getBailOut());
        propertySet.setBoolean("juliaModeFractal", isJuliaModeFractal());
        propertySet.setDouble("juliaX", getJuliaX());
        propertySet.setDouble("juliaY", getJuliaY());
        propertySet.setDouble("regionCenterX", getRegion().getCenterX());
        propertySet.setDouble("regionCenterY", getRegion().getCenterY());
        propertySet.setDouble("regionRadius", getRegion().getRadius());

        propertySet.setBoolean("decoratedFractal", isDecoratedFractal());
        propertySet.setString("distanceFunctionId", getDistanceFunctionId());
        propertySet.setDouble("distanceDilation", getDistanceDilation());
        propertySet.setDouble("distanceTranslateX", getDistanceTranslateX());
        propertySet.setDouble("distanceTranslateY", getDistanceTranslateY());

        propertySet.setString("colorSchemeId", getColorSchemeId());
        propertySet.setString("colorScheme", getColorScheme().asText());
        propertySet.setDouble("colorGain", getColorGain());
        propertySet.setDouble("colorOffset", getColorOffset());
        propertySet.setBoolean("colorRepeat", isColorRepeat());
    }

    public double getBailOut() {
        return bailOut;
    }

    public void setBailOut(double bailOut) {
        this.bailOut = bailOut;
    }

    public double getColorGain() {
        return colorGain;
    }

    public void setColorGain(double colorGain) {
        this.colorGain = colorGain;
    }

    public int[] getColorGradient() {
        return colorGradient;
    }

    public void setColorGradient(int[] colorGradient) {
        this.colorGradient = colorGradient;
    }

    public double getColorOffset() {
        return colorOffset;
    }

    public void setColorOffset(double colorOffset) {
        this.colorOffset = colorOffset;
    }

    public boolean isColorRepeat() {
        return colorRepeat;
    }

    public void setColorRepeat(boolean colorRepeat) {
        this.colorRepeat = colorRepeat;
    }

    public ColorScheme getColorScheme() {
        return colorScheme;
    }

    public void setColorScheme(ColorScheme colorScheme) {
        this.colorScheme = colorScheme;
        this.colorGradient = colorScheme.getGradient(GRADIENT_COLOR_COUNT);
    }

    public String getColorSchemeId() {
        return colorSchemeId;
    }

    public void setColorSchemeId(String colorSchemeId) {
        this.colorSchemeId = colorSchemeId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public boolean isDecoratedFractal() {
        return decoratedFractal;
    }

    public void setDecoratedFractal(boolean decoratedFractal) {
        this.decoratedFractal = decoratedFractal;
    }

    public double getDistanceDilation() {
        return distanceDilation;
    }

    public void setDistanceDilation(double distanceDilation) {
        this.distanceDilation = distanceDilation;
    }

    public String getDistanceFunctionId() {
        return distanceFunctionId;
    }

    public void setDistanceFunctionId(String distanceFunctionId) {
        this.distanceFunctionId = distanceFunctionId;
    }

    public double getDistanceTranslateX() {
        return distanceTranslateX;
    }

    public void setDistanceTranslateX(double distanceTranslateX) {
        this.distanceTranslateX = distanceTranslateX;
    }

    public double getDistanceTranslateY() {
        return distanceTranslateY;
    }

    public void setDistanceTranslateY(double distanceTranslateY) {
        this.distanceTranslateY = distanceTranslateY;
    }

    public String getFractalId() {
        return fractalId;
    }

    public void setFractalId(String fractalId) {
        this.fractalId = fractalId;
    }

    public int getIterMax() {
        return iterMax;
    }

    public void setIterMax(int iterMax) {
        this.iterMax = iterMax;
    }

    public boolean isJuliaModeFractal() {
        return juliaModeFractal;
    }

    public void setJuliaModeFractal(boolean juliaModeFractal) {
        this.juliaModeFractal = juliaModeFractal;
    }

    public double getJuliaX() {
        return juliaX;
    }

    public void setJuliaX(double juliaX) {
        this.juliaX = juliaX;
    }

    public double getJuliaY() {
        return juliaY;
    }

    public void setJuliaY(double juliaY) {
        this.juliaY = juliaY;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }
}
