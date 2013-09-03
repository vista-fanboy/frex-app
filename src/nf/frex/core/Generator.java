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
public abstract class Generator {
    final GeneratorConfig config;
    final ProgressListener listener;

    protected Generator(GeneratorConfig config, final ProgressListener listener) {
        this.config = config;
        this.listener = listener;
    }


    public abstract void start(final Image image, boolean colorsOnly);


    public abstract void cancel();


    public interface ProgressListener {
        void onStarted(int numTasks);

        void onSomeLinesComputed(int taskId, int line1, int line2);

        void onStopped(boolean cancelled);
    }
}
