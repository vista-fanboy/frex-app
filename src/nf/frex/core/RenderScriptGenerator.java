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

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;

/**
 * @author Norman Fomferra
 */
public class RenderScriptGenerator extends Generator {
    private final RenderScript renderScript;
    private final ScriptC_fractal_value fractalValueScriptC;
    private final ScriptC_fractal_color fractalColorScriptC;
    private Allocation valueAllocation;
    private Allocation colorAllocation;
    private Allocation colorPaletteAllocation;

    public RenderScriptGenerator(GeneratorConfig config, RenderScript renderScript, ScriptC_fractal_value fractalValueScriptC, ScriptC_fractal_color fractalColorScriptC, ProgressListener listener) {
        super(config, listener);
        this.renderScript = renderScript;
        this.fractalValueScriptC = fractalValueScriptC;
        this.fractalColorScriptC = fractalColorScriptC;
    }

    @Override
    public void start(final Image image, boolean colorsOnly) {

        final int width = image.getWidth();
        final int height = image.getHeight();
        final int count = width * height;

        listener.onStarted(1);

        if (!colorsOnly) {

            final Region region = config.getRegion();
            final double ps = region.getPixelSize(width, height);

            fractalValueScriptC.set_width(width);

            fractalValueScriptC.set_z0x(region.getUpperLeftX(width, ps));
            fractalValueScriptC.set_z0y(region.getUpperLeftY(height, ps));
            fractalValueScriptC.set_ps(ps);

            fractalValueScriptC.set_iterMax(config.getIterMax());
            fractalValueScriptC.set_bailOut(config.getBailOut());
            fractalValueScriptC.set_juliaMode((short) (config.isJuliaModeFractal() ? 1 : 0));
            fractalValueScriptC.set_juliaX(config.getJuliaX());
            fractalValueScriptC.set_juliaY(config.getJuliaY());

            fractalValueScriptC.set_decorated((short) (config.isDecoratedFractal() ? 1 : 0));
            fractalValueScriptC.set_orbitDilation(config.getDistanceDilation());
            fractalValueScriptC.set_orbitTranslateX(config.getDistanceTranslateX());
            fractalValueScriptC.set_orbitTranslateY(config.getDistanceTranslateY());
            fractalValueScriptC.set_orbitTurbulence((short) (config.isTurbulenceEnabled() ? 1 : 0));
            fractalValueScriptC.set_orbitTurbulenceIntensity(config.getTurbulenceIntensity());
            fractalValueScriptC.set_orbitTurbulenceScale(config.getTurbulenceScale());

            if (valueAllocation == null) {
                valueAllocation = Allocation.createSized(renderScript, Element.F32(renderScript), count);
            } else if (valueAllocation.getType().getCount() != count) {
                valueAllocation.resize(count);
            }
            valueAllocation.copyFrom(image.getValues());
            fractalValueScriptC.forEach_root(valueAllocation);
            valueAllocation.copyTo(image.getValues());
        }


        final int[] colorPalette = config.getColorGradient();
        if (colorPaletteAllocation == null) {
            colorPaletteAllocation = Allocation.createSized(renderScript, Element.I32(renderScript), colorPalette.length);
            fractalColorScriptC.bind_colorPalette(colorPaletteAllocation);
        } else if (colorPaletteAllocation.getType().getCount() != colorPalette.length) {
            colorPaletteAllocation.resize(colorPalette.length);
        }
        colorPaletteAllocation.copyFrom(colorPalette);

        fractalColorScriptC.set_colorGain((float) config.getColorGain());
        fractalColorScriptC.set_colorOffset((float) config.getColorOffset());
        fractalColorScriptC.set_numColors(colorPalette.length);
        fractalColorScriptC.set_repeatColors((short) (config.isColorRepeat() ? 1 : 0));

        if (colorAllocation == null) {
            colorAllocation = Allocation.createSized(renderScript, Element.I32(renderScript), count);
        } else if (colorAllocation.getType().getCount() != count) {
            colorAllocation.resize(count);
        }
        colorAllocation.copyFrom(image.getColours());
        fractalColorScriptC.forEach_root(valueAllocation, colorAllocation);
        colorAllocation.copyTo(image.getColours());


        listener.onSomeLinesComputed(0, 0, height - 1);
        listener.onStopped(false);
    }

    @Override
    public void cancel() {
    }
}
