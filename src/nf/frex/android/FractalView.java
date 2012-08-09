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

package nf.frex.android;

import android.app.ActionBar;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import nf.frex.core.*;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Norman Fomferra
 */
public class FractalView extends View {
    public static final String TAG = "FrexActivity";
    public static final String PACKAGE_NAME = "nf.frex.android";

    private final GestureDetector gestureDetector;
    private final ScaleGestureDetector scaleGestureDetector;
    private final FrexActivity activity;
    private final ArrayList<Region> regionHistory;
    private final Generator screenGenerator;

    private Bitmap bitmap;
    private Image image;
    private Image imageCopy;
    private float scrollDistanceX;
    private float scrollDistanceY;
    private float focusX;
    private float focusY;
    private float zoomFactor;

    private final GeneratorConfig generatorConfig;

    public FractalView(final FrexActivity activity) {
        super(activity);

        this.activity = activity;

        scaleGestureDetector = new ScaleGestureDetector(activity, new ScaleGestureListener());
        gestureDetector = new GestureDetector(activity, new GestureListener());
        gestureDetector.setIsLongpressEnabled(true);
        regionHistory = new ArrayList<Region>();

        image = new Image(getWidth(), getHeight());
        imageCopy = new Image(getWidth(), getHeight());

        generatorConfig = new GeneratorConfig();
        generatorConfig.setConfigName(null); // --> default

        generatorConfig.setFractalId("MANDELBROT");
        Fractal fractal = Registries.fractals.getValue(generatorConfig.getFractalId());
        generatorConfig.setRegion(fractal.getDefaultRegion().clone());
        generatorConfig.setIterMax(fractal.getDefaultIterMax());
        generatorConfig.setBailOut(fractal.getDefaultBailOut());
        generatorConfig.setJuliaModeFractal(false);
        generatorConfig.setJuliaX(0.0);
        generatorConfig.setJuliaY(0.0);

        generatorConfig.setDecoratedFractal(false);
        generatorConfig.setDistanceFunctionId("STINGS");
        generatorConfig.setDistanceDilation(0.2);
        generatorConfig.setDistanceTranslateX(0.0);
        generatorConfig.setDistanceTranslateY(0.0);

        generatorConfig.setColorSchemeId("HEAT");
        generatorConfig.setColorScheme(Registries.colorSchemes.getValue(generatorConfig.getColorSchemeId()));
        generatorConfig.setColorGradient(generatorConfig.getColorScheme().createGradient(1024));
        generatorConfig.setColorGain(1.0 / fractal.getDefaultIterMax());
        generatorConfig.setColorOffset(0.0);
        generatorConfig.setColorRepeat(true);

        screenGenerator = new Generator(generatorConfig, new GeneratorProgressListener());
    }

    public GeneratorConfig getGeneratorConfig() {
        return generatorConfig;
    }

    public Image getImage() {
        return image;
    }

    public String getConfigName() {
        return generatorConfig.getConfigName();
    }

    public void setConfigName(String configName) {
        this.generatorConfig.setConfigName(configName);
    }

    public String getFractalId() {
        return generatorConfig.getFractalId();
    }

    public void setFractalId(String fractalId) {
        this.generatorConfig.setFractalId(fractalId);
    }

    public String getColorSchemeId() {
        return generatorConfig.getColorSchemeId();
    }

    public void setColorSchemeId(String colorSchemeId) {
        this.generatorConfig.setColorSchemeId(colorSchemeId);
    }

    public void setColorScheme(ColorScheme colorScheme) {
        this.generatorConfig.setColorScheme(colorScheme);
    }

    public int getIterMax() {
        return generatorConfig.getIterMax();
    }

    public void setIterMax(int iterMax) {
        this.generatorConfig.setIterMax(iterMax);
    }

    public void setBailOut(double bailOut) {
        this.generatorConfig.setBailOut(bailOut);
    }

    public double getDistanceDilation() {
        return generatorConfig.getDistanceDilation();
    }

    public void setDistanceDilation(double distanceDilation) {
        this.generatorConfig.setDistanceDilation(distanceDilation);
    }

    public double getDistanceTranslateX() {
        return generatorConfig.getDistanceTranslateX();
    }

    public void setDistanceTranslateX(double distanceTranslateX) {
        this.generatorConfig.setDistanceTranslateX(distanceTranslateX);
    }

    public double getDistanceTranslateY() {
        return generatorConfig.getDistanceTranslateY();
    }

    public void setDistanceTranslateY(double distanceTranslateY) {
        this.generatorConfig.setDistanceTranslateY(distanceTranslateY);
    }

    public String getDistanceFunctionId() {
        return generatorConfig.getDistanceFunctionId();
    }

    public void setDistanceFunctionId(String distanceFunctionId) {
        this.generatorConfig.setDistanceFunctionId(distanceFunctionId);
    }

    public void setRegion(Region region) {
        this.generatorConfig.getRegion().set(region);
    }

    public double getColorGain() {
        return generatorConfig.getColorGain();
    }

    public void setColorGain(double colorGain) {
        this.generatorConfig.setColorGain(colorGain);
    }

    public double getColorOffset() {
        return generatorConfig.getColorOffset();
    }

    public void setColorOffset(double colorOffset) {
        this.generatorConfig.setColorOffset(colorOffset);
    }

    public boolean isColorRepeat() {
        return generatorConfig.isColorRepeat();
    }

    public void setColorRepeat(boolean colorRepeat) {
        this.generatorConfig.setColorRepeat(colorRepeat);
    }

    public boolean isDecoratedFractal() {
        return generatorConfig.isDecoratedFractal();
    }

    public void setDecoratedFractal(boolean decoratedFractal) {
        setDecoratedFractal(decoratedFractal, true);
    }

    public void setDecoratedFractal(boolean decoratedFractal, boolean adjustColor) {
        if (this.generatorConfig.isDecoratedFractal() != decoratedFractal) {
            this.generatorConfig.setDecoratedFractal(decoratedFractal);
            if (adjustColor && !this.generatorConfig.isDecoratedFractal()) {
                setColorGain(1.0);
                setColorOffset(0.0);
            }
        }
    }

    public boolean isJuliaModeFractal() {
        return generatorConfig.isJuliaModeFractal();
    }

    public void setJuliaModeFractal(boolean juliaModeFractal) {
        setJuliaModeFractal(juliaModeFractal, true);
    }

    public void setJuliaModeFractal(boolean juliaModeFractal, boolean adjustCoords) {
        if (this.generatorConfig.isJuliaModeFractal() != juliaModeFractal) {
            this.generatorConfig.setJuliaModeFractal(juliaModeFractal);
            if (adjustCoords) {
                if (this.generatorConfig.isJuliaModeFractal()) {
                    setJuliaX(generatorConfig.getRegion().getCenterX());
                    setJuliaY(generatorConfig.getRegion().getCenterY());
                } else {
                    setJuliaX(generatorConfig.getRegion().getCenterX());
                    setJuliaY(generatorConfig.getRegion().getCenterY());
                    generatorConfig.getRegion().set(generatorConfig.getJuliaX(), generatorConfig.getJuliaY(), generatorConfig.getRegion().getRadius());
                }
            }
        }
    }

    public void setJuliaX(double juliaX) {
        this.generatorConfig.setJuliaX(juliaX);
    }

    public void setJuliaY(double juliaY) {
        this.generatorConfig.setJuliaY(juliaY);
    }

    public Generator getScreenGenerator() {
        return screenGenerator;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap != null) {
            Matrix matrix = new Matrix();
            if (hasScrollDistance()) {
                matrix.setTranslate(-scrollDistanceX, -scrollDistanceY);
                //Log.d(TAG, "Scrolling");
            } else {
                matrix.setScale(zoomFactor, zoomFactor, focusX, focusY);
                //Log.d(TAG, "Zooming!");
            }
            canvas.drawBitmap(bitmap, matrix, null);
        } else {
            canvas.drawBitmap(image.getColours(), 0, image.getWidth(), 0F, 0F, image.getWidth(), image.getHeight(), false, null);
            //Log.d(TAG, "No interaction!");
        }
    }

    public Bitmap createBitmap() {
        return image.createBitmap();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        // Log.d(TAG, "onSizeChanged: w = " + w + ", h = " + h);
        super.onSizeChanged(w, h, oldw, oldh);
        screenGenerator.cancel();
        image.resize(w, h);
        imageCopy.resize(w, h);
        recomputeAll();
    }

    public void recomputeAll() {
        Arrays.fill(image.getComputed(), false);
        Arrays.fill(imageCopy.getComputed(), false);
        screenGenerator.cancel();
        screenGenerator.start(image, false);
    }

    public void recomputeColors() {
        screenGenerator.cancel();
        screenGenerator.start(image, true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        boolean handled = gestureDetector.onTouchEvent(event);
        // up to SDK 4.0.3,  ACTION_UP after scrolling is not handled by GestureDetector
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (hasScrollDistance()) {
                moveRegion(scrollDistanceX, scrollDistanceY);
                clearScrollDistance();
                return true;
            }
        }
        return handled;
    }

    private boolean hasScrollDistance() {
        return scrollDistanceX != 0 || scrollDistanceY != 0;
    }

    private void clearScrollDistance() {
        scrollDistanceX = scrollDistanceY = 0;
        zoomFactor = 1;
        bitmap = null;
    }

    private void incrementScrollDistance(float distanceX, float distanceY) {
        this.scrollDistanceX += distanceX;
        this.scrollDistanceY += distanceY;
        zoomFactor = 1;
    }

    public void restoreInstanceState(PropertySet propertySet) {
        generatorConfig.restoreInstanceState(propertySet);
    }

    public void saveInstanceState(PropertySet propertySet) {
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            propertySet.setString("frexVersionName", packageInfo.versionName);
            propertySet.setInt("frexVersionCode", packageInfo.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, e);
        }
        generatorConfig.saveInstanceState(propertySet);
    }

    public void zoomRegion(double zoomFactor) {
        zoomRegion(image.getWidth() / 2, image.getHeight() / 2, zoomFactor);
    }

    private void zoomRegion(MotionEvent event, double zoomFactor) {
        zoomRegion(event.getX(), event.getY(), zoomFactor);
    }

    /**
     * Zooms the view.
     *
     * @param viewX      The view X coordinate which determines the new center Z-plane X coordinate
     * @param viewY      The view Y coordinate which determines the new center Z-plane Y coordinate
     * @param zoomFactor The zoom factor.
     */
    public void zoomRegion(float viewX, float viewY, double zoomFactor) {
        // Log.d(TAG, "zoom: viewX = " + viewX + ", viewY = " + viewY + ", zoomFactor = " + zoomFactor);

        int w = image.getWidth();
        int h = image.getHeight();
        double s = generatorConfig.getRegion().getPixelSize(w, h);
        double cx0 = generatorConfig.getRegion().getUpperLeftX(w, s);
        double cy0 = generatorConfig.getRegion().getUpperLeftY(h, s);
        double cx = cx0 + s * viewX;
        double cy = cy0 - s * viewY;
        double r = (1.0 / zoomFactor) * generatorConfig.getRegion().getRadius();

        regenerateRegion(cx, cy, r);
    }

    /**
     * Zooms the view.
     *
     * @param invariantViewX The view X coordinate at which the Z-plane X coordinate is invariant
     * @param invariantViewY The view Y coordinate at which the Z-plane Y coordinate is invariant
     * @param zoomFactor     The zoom factor.
     */
    private void zoomRegionWithFocus(float invariantViewX, float invariantViewY, float zoomFactor) {
        // Log.d(TAG, "zoom: viewX = " + viewX + ", viewY = " + viewY + ", zoomFactor = " + zoomFactor);

        int w = image.getWidth();
        int h = image.getHeight();
        double s1 = generatorConfig.getRegion().getPixelSize(w, h);
        double r = (1.0 / zoomFactor) * generatorConfig.getRegion().getRadius();
        double s2 = Region.getPixelSize(r, w, h);
        double cx = generatorConfig.getRegion().getCenterX() + (s1 - s2) * (invariantViewX - w / 2);
        double cy = generatorConfig.getRegion().getCenterY() - (s1 - s2) * (invariantViewY - h / 2);

        regenerateRegion(cx, cy, r);
    }


    public void regenerateRegion(Region region) {
        regenerateRegion(region.getCenterX(), region.getCenterY(), region.getRadius());
    }

    private void regenerateRegion(double cx, double cy, double r) {
        Region region1 = generatorConfig.getRegion().clone();
        generatorConfig.getRegion().set(cx, cy, r);

        recycle(image, imageCopy, region1, generatorConfig.getRegion());

        Image imageTemp = image;
        this.image = imageCopy;
        this.imageCopy = imageTemp;

        screenGenerator.start(image, false);
    }

    private void moveRegion(float viewDistanceX, float viewDistanceY) {
        // Log.d(TAG, "moveRegion: viewDistanceX = " + viewDistanceX + ", viewDistanceY = " + viewDistanceY);

        double s = generatorConfig.getRegion().getPixelSize(image.getWidth(), image.getHeight());
        double cx = generatorConfig.getRegion().getCenterX() + s * (int) viewDistanceX;
        double cy = generatorConfig.getRegion().getCenterY() - s * (int) viewDistanceY;
        double r = generatorConfig.getRegion().getRadius();

        generatorConfig.getRegion().set(cx, cy, r);

        recycle(image, imageCopy, (int) viewDistanceX, (int) viewDistanceY);

        Image imageTemp = image;
        this.image = imageCopy;
        this.imageCopy = imageTemp;

        screenGenerator.start(image, false);
    }


    public static void recycle(final Image image1,
                               final Image image2,
                               final Region reg1, Region reg2) {

        final int w = image1.getWidth();
        final int h = image1.getHeight();

        final int[] colors1 = image1.getColours();
        final boolean[] computed1 = image1.getComputed();
        final float[] values1 = image1.getValues();

        final int[] colors2 = image2.getColours();
        final boolean[] computed2 = image2.getComputed();
        final float[] values2 = image2.getValues();

        Arrays.fill(computed2, false);
        Arrays.fill(colors2, 0);
        Arrays.fill(values2, 0.0F);

        final double s1x = reg1.getPixelSize(w, h);
        final double s1y = -s1x;
        final double s2x = reg2.getPixelSize(w, h);
        final double s2y = -s2x;
        final double pcx = (w / 2);
        final double pcy = (h / 2);

        if (s1x > s2x) {
            final double p0x = pcx + (reg1.getCenterX() - reg2.getCenterX()) / s2x;
            final double p0y = pcy + (reg1.getCenterY() - reg2.getCenterY()) / s2y;
            final double sx = s1x / s2x;
            final double sy = s1y / s2y;
            for (int p1y = 0; p1y < h; p1y++) {
                for (int p1x = 0; p1x < w; p1x++) {
                    final int i1 = p1y * w + p1x;
                    if (computed1[i1]) {
                        final int p2x = round(p0x + sx * (p1x - pcx));
                        final int p2y = round(p0y + sy * (p1y - pcy));
                        if (p2x >= 0 && p2x < w && p2y >= 0 && p2y < h) {
                            final int i2 = p2y * w + p2x;
                            computed2[i2] = true;
                            colors2[i2] = colors1[i1];
                            values2[i2] = values1[i1];
                        }
                    }
                }
            }
        } else {
            final double p0x = pcx + (reg2.getCenterX() - reg1.getCenterX()) / s1x;
            final double p0y = pcy + (reg2.getCenterY() - reg1.getCenterY()) / s1y;
            final double sx = s2x / s1x;
            final double sy = s2y / s1y;
            for (int p2y = 0; p2y < h; p2y++) {
                for (int p2x = 0; p2x < w; p2x++) {
                    final int p1x = round(p0x + sx * (p2x - pcx));
                    final int p1y = round(p0y + sy * (p2y - pcy));
                    if (p1x >= 0 && p1x < w && p1y >= 0 && p1y < h) {
                        final int i1 = p1y * w + p1x;
                        final int i2 = p2y * w + p2x;
                        if (computed1[i1]) {
                            computed2[i2] = true;
                            values2[i2] = values1[i1];
                        }
                        colors2[i2] = colors1[i1];
                    }
                }
            }
        }
    }

    public static void recycle(final Image image1,
                               final Image image2,
                               final int dx, final int dy) {

        final int w = image1.getWidth();
        final int h = image1.getHeight();

        final int[] colors1 = image1.getColours();
        final boolean[] computed1 = image1.getComputed();
        final float[] values1 = image1.getValues();

        final int[] colors2 = image2.getColours();
        final boolean[] computed2 = image2.getComputed();
        final float[] values2 = image2.getValues();

        Arrays.fill(computed2, false);
        Arrays.fill(colors2, 0);
        Arrays.fill(values2, 0.0F);

        for (int p2y = 0; p2y < h; p2y++) {
            for (int p2x = 0; p2x < w; p2x++) {
                final int p1x = p2x + dx;
                final int p1y = p2y + dy;
                if (p1x >= 0 && p1x < w && p1y >= 0 && p1y < h) {
                    final int i1 = p1y * w + p1x;
                    final int i2 = p2y * w + p2x;
                    if (computed1[i1]) {
                        computed2[i2] = true;
                        values2[i2] = values1[i1];
                    }
                    colors2[i2] = colors1[i1];
                }
            }
        }
    }


    private static int round(double v) {
        return (int) Math.floor(v + 0.5);
    }

    public void zoomAll() {
        Region defaultRegion = Registries.fractals.getValue(getFractalId()).getDefaultRegion();
        regenerateRegion(defaultRegion);
    }

    public void cancelGenerators() {
        getScreenGenerator().cancel();
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            bitmap = image.createBitmap();
            focusX = detector.getFocusX();
            focusY = detector.getFocusY();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            zoomFactor = getZoomFactor(detector);
            postInvalidate();
            bitmap = null;
//            Log.d(TAG, "onScaleEnd: " + ", scaleFactor=" + detector.getScaleFactor() + ", zoomFactor=" + zoomFactor);
            zoomRegionWithFocus(focusX, focusY, zoomFactor);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            zoomFactor = getZoomFactor(detector);
            postInvalidate();
            return super.onScale(detector);
        }

        private float getZoomFactor(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            final float stepSize = 0.25F;
            float zoomFactor = stepSize * Math.round((scaleFactor < 1 ? 1 / scaleFactor : scaleFactor) / stepSize);
            if (zoomFactor > 8) {
                zoomFactor = 8;
            }
            if (zoomFactor <= 1) {
                zoomFactor = 1 + stepSize;
            }
            if (scaleFactor < 1) {
                zoomFactor = 1 / zoomFactor;
            }
            return zoomFactor;
        }

    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            clearScrollDistance();
            //Log.d(TAG, "onDown");
            return true;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return super.onDoubleTapEvent(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            activity.showDialog(R.id.colors);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (bitmap == null) {
                bitmap = image.createBitmap();
            }
            incrementScrollDistance(distanceX, distanceY);
            postInvalidate();
            //Log.d(TAG, "onScroll: " + distanceX + "," + distanceY);
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //Log.d(TAG, "onFling: " + velocityX + "," + velocityY);
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTap(MotionEvent event) {
            zoomRegion(event, 2.0);
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                ActionBar actionBar = activity.getActionBar();
                if (actionBar.isShowing()) {
                    actionBar.hide();
                } else {
                    actionBar.show();
                }
                return true;
            } else {
                return super.onSingleTapConfirmed(event);
            }
        }
    }

    private class GeneratorProgressListener implements Generator.ProgressListener {
        @Override
        public void onStarted(int numTasks) {
        }

        @Override
        public void onSomeLinesComputed(int taskId, int line1, int line2) {
            postInvalidate(0, line1, image.getWidth() - 1, line2);
        }

        @Override
        public void onStopped(boolean cancelled) {
            postInvalidate();
        }
    }
}
