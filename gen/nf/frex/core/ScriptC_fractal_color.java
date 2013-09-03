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

/*___Generated_by_IDEA___*/

/*
 * This file is auto-generated. DO NOT MODIFY!
 * The source Renderscript file: C:\\Users\\Norman\\JavaProjects\\android\\frex-app\\src\\nf\\frex\\core\\fractal_color.rs
 */
package nf.frex.core;

import android.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_fractal_color extends ScriptC {
    private static final String __rs_resource_name = "fractal_color";
    // Constructor
    public  ScriptC_fractal_color(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_fractal_color(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __F32 = Element.F32(rs);
        __I32 = Element.I32(rs);
    }

    private Element __F32;
    private Element __I32;
    private FieldPacker __rs_fp_F32;
    private FieldPacker __rs_fp_I32;
    private FieldPacker __rs_fp_U8;
    private final static int mExportVarIdx_colorPalette = 0;
    private Allocation mExportVar_colorPalette;
    public void bind_colorPalette(Allocation v) {
        mExportVar_colorPalette = v;
        if (v == null) bindAllocation(null, mExportVarIdx_colorPalette);
        else bindAllocation(v, mExportVarIdx_colorPalette);
    }

    public Allocation get_colorPalette() {
        return mExportVar_colorPalette;
    }

    private final static int mExportVarIdx_numColors = 1;
    private int mExportVar_numColors;
    public synchronized void set_numColors(int v) {
        setVar(mExportVarIdx_numColors, v);
        mExportVar_numColors = v;
    }

    public int get_numColors() {
        return mExportVar_numColors;
    }

    private final static int mExportVarIdx_colorGain = 2;
    private float mExportVar_colorGain;
    public synchronized void set_colorGain(float v) {
        setVar(mExportVarIdx_colorGain, v);
        mExportVar_colorGain = v;
    }

    public float get_colorGain() {
        return mExportVar_colorGain;
    }

    private final static int mExportVarIdx_colorOffset = 3;
    private float mExportVar_colorOffset;
    public synchronized void set_colorOffset(float v) {
        setVar(mExportVarIdx_colorOffset, v);
        mExportVar_colorOffset = v;
    }

    public float get_colorOffset() {
        return mExportVar_colorOffset;
    }

    private final static int mExportVarIdx_repeatColors = 4;
    private short mExportVar_repeatColors;
    public synchronized void set_repeatColors(short v) {
        if (__rs_fp_U8!= null) {
            __rs_fp_U8.reset();
        } else {
            __rs_fp_U8 = new FieldPacker(1);
        }
        __rs_fp_U8.addU8(v);
        setVar(mExportVarIdx_repeatColors, __rs_fp_U8);
        mExportVar_repeatColors = v;
    }

    public short get_repeatColors() {
        return mExportVar_repeatColors;
    }

    private final static int mExportForEachIdx_root = 0;
    public void forEach_root(Allocation ain, Allocation aout) {
        // check ain
        if (!ain.getType().getElement().isCompatible(__F32)) {
            throw new RSRuntimeException("Type mismatch with F32!");
        }
        // check aout
        if (!aout.getType().getElement().isCompatible(__I32)) {
            throw new RSRuntimeException("Type mismatch with I32!");
        }
        // Verify dimensions
        Type tIn = ain.getType();
        Type tOut = aout.getType();
        if ((tIn.getCount() != tOut.getCount()) ||
            (tIn.getX() != tOut.getX()) ||
            (tIn.getY() != tOut.getY()) ||
            (tIn.getZ() != tOut.getZ()) ||
            (tIn.hasFaces() != tOut.hasFaces()) ||
            (tIn.hasMipmaps() != tOut.hasMipmaps())) {
            throw new RSRuntimeException("Dimension mismatch between input and output parameters!");
        }
        forEach(mExportForEachIdx_root, ain, aout, null);
    }

}

