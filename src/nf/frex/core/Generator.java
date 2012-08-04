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
public class Generator {
    private final GeneratorConfig config;
    private final Task[] tasks;
    private final ProgressListener listener;

    public Generator(GeneratorConfig config, final ProgressListener listener) {
        this.config = config;
        this.listener = listener;
        int numThreads = 2 * Runtime.getRuntime().availableProcessors();
        this.tasks = new Task[numThreads];
    }

    public void start(final Image image, boolean colorsOnly) {
        cancel();

        ProgressListenerWrapper listenerWrapper = new ProgressListenerWrapper(listener);

        final int height = image.getHeight();
        final int n = height / tasks.length;

        listenerWrapper.onStarted(tasks.length);
        int y0 = 0;
        for (int i = 0; i < tasks.length; i++) {
            int y1 = y0 + n;
            if (y1 >= height) {
                y1 = height - 1;
            }
            Task task = new Task(config, image, i, y0, y1, colorsOnly, listenerWrapper);
            task.start();
            tasks[i] = task;
            y0 = y1 + 1;
        }
    }

    public void cancel() {
        for (Task task : tasks) {
            if (task != null) {
                task.cancel();
            }
        }
    }

    private static class Task extends Thread {
        private final GeneratorConfig config;
        private final Image image;
        private final int taskIndex;
        private final int startY;
        private final int endY;
        private final boolean regenColors;
        private final ProgressListenerWrapper listener;

        private boolean cancelled;

        private Task(GeneratorConfig config,  Image image, int taskIndex, int startY, int endY, boolean regenColors, ProgressListenerWrapper listener) {
            super("FrexTask(" + startY + "-" + endY + ")");
            this.config = config;
            this.image = image;
            this.taskIndex = taskIndex;
            this.startY = startY;
            this.endY = endY;
            this.regenColors = regenColors;
            this.listener = listener;
        }


        @Override
        public void run() {

            final int width = image.getWidth();
            final int height = image.getHeight();
            final Fractal fractal = Registries.fractals.getValue(config.getFractalId(), Fractal.MANDELBROT);
            final int[] colorPalette = config.getColorGradient();
            final int iterMax = config.getIterMax();
            final int[] colours = image.getColours();
            final float[] values = image.getValues();
            final boolean[] computed = image.getComputed();
            final double[] orbitX = new double[iterMax];
            final double[] orbitY = new double[iterMax];
            final OrbitFunction orbitFunction = new OrbitFunction(Registries.distanceFunctions.getValue(config.getDistanceFunctionId(), DistanceFunction.X),
                                                                  config.getDistanceDilation(),
                                                                  config.getDistanceTranslateX(),
                                                                  config.getDistanceTranslateY()
            );
            final boolean regenColors = this.regenColors;

            final Region region = config.getRegion();
            final double ps = region.getPixelSize(width, height);
            final double z0x = region.getUpperLeftX(width, ps);
            final double z0y = region.getUpperLeftY(height, ps);
            final int numColors = colorPalette.length;
            final int numColors2 = 2 * colorPalette.length;

            final float colorA = (float) (config.getColorGain() * numColors);
            final float colorB = (float) config.getColorOffset();
            final boolean repeatColors = config.isColorRepeat();

            final double bailOut = config.getBailOut();
            final boolean decorated = config.isDecoratedFractal();
            final boolean juliaMode = config.isJuliaModeFractal();
            final double jx = config.getJuliaX();
            final double jy = config.getJuliaY();

            int lastY = startY;
            float value;
            int iter;
            int colorIndex;
            int i, ix, iy;

            double zx, zy;
            for (iy = startY; iy <= endY && !cancelled; iy++) {
                for (ix = 0; ix < width; ix++) {
                    i = iy * width + ix;
                    if (!computed[i]) {
                        zx = z0x + ix * ps;
                        zy = z0y - iy * ps;
                        if (juliaMode) {
                            iter = fractal.computeOrbit(zx, zy, jx, jy, iterMax, bailOut, orbitX, orbitY);
                        } else {
                            iter = fractal.computeOrbit(0.0, 0.0, zx, zy, iterMax, bailOut, orbitX, orbitY);
                        }
                        value = decorated ? orbitFunction.processOrbit(iter, orbitX, orbitY) : (iter < iterMax ? iter : 0.0F);
                        values[i] = value;

                        //<<< Code duplication
                        colorIndex = (int) (colorA * value + colorB);
                        if (repeatColors) {
                            colorIndex = colorIndex % numColors2;
                            if (colorIndex >= numColors) {
                                colorIndex = numColors2 - colorIndex - 1;
                            }
                        } else {
                            if (colorIndex >= numColors) {
                                colorIndex = numColors - 1;
                            }
                        }
                        colours[i] = colorPalette[colorIndex];
                        //>>> Code duplication

                        computed[i] = true;
                    } else if (regenColors) {
                        value = values[i];

                        //<<< Code duplication
                        colorIndex = (int) (colorA * value + colorB);
                        if (repeatColors) {
                            colorIndex = colorIndex % numColors2;
                            if (colorIndex >= numColors) {
                                colorIndex = numColors2 - colorIndex - 1;
                            }
                        } else {
                            if (colorIndex >= numColors) {
                                colorIndex = numColors - 1;
                            }
                        }
                        colours[i] = colorPalette[colorIndex];
                        //>>> Code duplication
                    }
                }
                if (iy % 16 == 0) {
                    listener.onSomeLinesComputed(taskIndex, lastY, iy);
                    lastY = iy;
                }
            }

            listener.onTaskTerminated();
        }

        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public void interrupt() {
            cancel();
            super.interrupt();
        }
    }

    public interface ProgressListener {
        void onStarted(int numTasks);

        void onSomeLinesComputed(int taskId, int line1, int line2);

        void onStopped();
    }


    private class ProgressListenerWrapper implements ProgressListener {
        private final ProgressListener wrappedListener;
        int tasksDone;

        public ProgressListenerWrapper(ProgressListener wrappedListener) {
            this.wrappedListener = wrappedListener;
        }

        @Override
        public void onStarted(int numTasks) {
            tasksDone = 0;
            wrappedListener.onStarted(numTasks);
        }

        @Override
        public void onSomeLinesComputed(int taskId, int line1, int line2) {
            wrappedListener.onSomeLinesComputed(taskId, line1, line2);
        }

        @Override
        public void onStopped() {
        }

        public void onTaskTerminated() {
            tasksDone++;
            if (tasksDone == tasks.length) {
                wrappedListener.onStopped();
            }
        }

    }
}
