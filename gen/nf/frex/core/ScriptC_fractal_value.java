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
 * The source Renderscript file: C:\\Users\\Norman\\JavaProjects\\android\\frex-app\\src\\nf\\frex\\core\\fractal_value.rs
 */
package nf.frex.core;

import android.renderscript.*;
import android.content.res.Resources;

/**
 * @hide
 */
public class ScriptC_fractal_value extends ScriptC {
    private static final String __rs_resource_name = "fractal_value";
    // Constructor
    public  ScriptC_fractal_value(RenderScript rs) {
        this(rs,
             rs.getApplicationContext().getResources(),
             rs.getApplicationContext().getResources().getIdentifier(
                 __rs_resource_name, "raw",
                 rs.getApplicationContext().getPackageName()));
    }

    public  ScriptC_fractal_value(RenderScript rs, Resources resources, int id) {
        super(rs, resources, id);
        __F32 = Element.F32(rs);
    }

    private Element __F32;
    private FieldPacker __rs_fp_BOOLEAN;
    private FieldPacker __rs_fp_F32;
    private FieldPacker __rs_fp_F64;
    private FieldPacker __rs_fp_I32;
    private final static int mExportVarIdx_width = 0;
    private int mExportVar_width;
    public synchronized void set_width(int v) {
        setVar(mExportVarIdx_width, v);
        mExportVar_width = v;
    }

    public int get_width() {
        return mExportVar_width;
    }

    private final static int mExportVarIdx_ps = 1;
    private double mExportVar_ps;
    public synchronized void set_ps(double v) {
        setVar(mExportVarIdx_ps, v);
        mExportVar_ps = v;
    }

    public double get_ps() {
        return mExportVar_ps;
    }

    private final static int mExportVarIdx_z0x = 2;
    private double mExportVar_z0x;
    public synchronized void set_z0x(double v) {
        setVar(mExportVarIdx_z0x, v);
        mExportVar_z0x = v;
    }

    public double get_z0x() {
        return mExportVar_z0x;
    }

    private final static int mExportVarIdx_z0y = 3;
    private double mExportVar_z0y;
    public synchronized void set_z0y(double v) {
        setVar(mExportVarIdx_z0y, v);
        mExportVar_z0y = v;
    }

    public double get_z0y() {
        return mExportVar_z0y;
    }

    private final static int mExportVarIdx_iterMax = 4;
    private int mExportVar_iterMax;
    public synchronized void set_iterMax(int v) {
        setVar(mExportVarIdx_iterMax, v);
        mExportVar_iterMax = v;
    }

    public int get_iterMax() {
        return mExportVar_iterMax;
    }

    private final static int mExportVarIdx_bailOut = 5;
    private double mExportVar_bailOut;
    public synchronized void set_bailOut(double v) {
        setVar(mExportVarIdx_bailOut, v);
        mExportVar_bailOut = v;
    }

    public double get_bailOut() {
        return mExportVar_bailOut;
    }

    private final static int mExportVarIdx_decorated = 6;
    private boolean mExportVar_decorated;
    public synchronized void set_decorated(boolean v) {
        if (__rs_fp_BOOLEAN!= null) {
            __rs_fp_BOOLEAN.reset();
        } else {
            __rs_fp_BOOLEAN = new FieldPacker(1);
        }
        __rs_fp_BOOLEAN.addBoolean(v);
        setVar(mExportVarIdx_decorated, __rs_fp_BOOLEAN);
        mExportVar_decorated = v;
    }

    public boolean get_decorated() {
        return mExportVar_decorated;
    }

    private final static int mExportVarIdx_juliaMode = 7;
    private boolean mExportVar_juliaMode;
    public synchronized void set_juliaMode(boolean v) {
        if (__rs_fp_BOOLEAN!= null) {
            __rs_fp_BOOLEAN.reset();
        } else {
            __rs_fp_BOOLEAN = new FieldPacker(1);
        }
        __rs_fp_BOOLEAN.addBoolean(v);
        setVar(mExportVarIdx_juliaMode, __rs_fp_BOOLEAN);
        mExportVar_juliaMode = v;
    }

    public boolean get_juliaMode() {
        return mExportVar_juliaMode;
    }

    private final static int mExportVarIdx_juliaX = 8;
    private double mExportVar_juliaX;
    public synchronized void set_juliaX(double v) {
        setVar(mExportVarIdx_juliaX, v);
        mExportVar_juliaX = v;
    }

    public double get_juliaX() {
        return mExportVar_juliaX;
    }

    private final static int mExportVarIdx_juliaY = 9;
    private double mExportVar_juliaY;
    public synchronized void set_juliaY(double v) {
        setVar(mExportVarIdx_juliaY, v);
        mExportVar_juliaY = v;
    }

    public double get_juliaY() {
        return mExportVar_juliaY;
    }

    private final static int mExportVarIdx_orbitDilation = 10;
    private float mExportVar_orbitDilation;
    public synchronized void set_orbitDilation(float v) {
        setVar(mExportVarIdx_orbitDilation, v);
        mExportVar_orbitDilation = v;
    }

    public float get_orbitDilation() {
        return mExportVar_orbitDilation;
    }

    private final static int mExportVarIdx_orbitTranslateX = 11;
    private float mExportVar_orbitTranslateX;
    public synchronized void set_orbitTranslateX(float v) {
        setVar(mExportVarIdx_orbitTranslateX, v);
        mExportVar_orbitTranslateX = v;
    }

    public float get_orbitTranslateX() {
        return mExportVar_orbitTranslateX;
    }

    private final static int mExportVarIdx_orbitTranslateY = 12;
    private float mExportVar_orbitTranslateY;
    public synchronized void set_orbitTranslateY(float v) {
        setVar(mExportVarIdx_orbitTranslateY, v);
        mExportVar_orbitTranslateY = v;
    }

    public float get_orbitTranslateY() {
        return mExportVar_orbitTranslateY;
    }

    private final static int mExportVarIdx_orbitTurbulence = 13;
    private boolean mExportVar_orbitTurbulence;
    public synchronized void set_orbitTurbulence(boolean v) {
        if (__rs_fp_BOOLEAN!= null) {
            __rs_fp_BOOLEAN.reset();
        } else {
            __rs_fp_BOOLEAN = new FieldPacker(1);
        }
        __rs_fp_BOOLEAN.addBoolean(v);
        setVar(mExportVarIdx_orbitTurbulence, __rs_fp_BOOLEAN);
        mExportVar_orbitTurbulence = v;
    }

    public boolean get_orbitTurbulence() {
        return mExportVar_orbitTurbulence;
    }

    private final static int mExportVarIdx_orbitTurbulenceIntensity = 14;
    private float mExportVar_orbitTurbulenceIntensity;
    public synchronized void set_orbitTurbulenceIntensity(float v) {
        setVar(mExportVarIdx_orbitTurbulenceIntensity, v);
        mExportVar_orbitTurbulenceIntensity = v;
    }

    public float get_orbitTurbulenceIntensity() {
        return mExportVar_orbitTurbulenceIntensity;
    }

    private final static int mExportVarIdx_orbitTurbulenceScale = 15;
    private float mExportVar_orbitTurbulenceScale;
    public synchronized void set_orbitTurbulenceScale(float v) {
        setVar(mExportVarIdx_orbitTurbulenceScale, v);
        mExportVar_orbitTurbulenceScale = v;
    }

    public float get_orbitTurbulenceScale() {
        return mExportVar_orbitTurbulenceScale;
    }

    private final static int mExportForEachIdx_root = 0;
    public void forEach_root(Allocation aout) {
        // check aout
        if (!aout.getType().getElement().isCompatible(__F32)) {
            throw new RSRuntimeException("Type mismatch with F32!");
        }
        forEach(mExportForEachIdx_root, null, aout, null);
    }

}

