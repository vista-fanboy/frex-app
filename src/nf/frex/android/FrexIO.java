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

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Norman Fomferra
 */
public class FrexIO {
    public static final String PARAM_FILE_EXT = ".frex";
    public static final String IMAGE_FILE_EXT = ".png";
    public static final Bitmap.CompressFormat IMAGE_FILE_FORMAT = Bitmap.CompressFormat.PNG;

    private final Context context;
    private final File appDir;

    public FrexIO(Context context) {
        this.context = context;
        File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File externalAppDir = null;
        if (picturesDirectory.exists()) {
            externalAppDir = new File(picturesDirectory, "Frex");
            if (!externalAppDir.exists()) {
                if (!externalAppDir.mkdir()) {
                    externalAppDir = null;
                }
            }
        }
        if (externalAppDir != null && externalAppDir.exists()) {
            appDir = externalAppDir;
        } else {
            appDir = context.getFilesDir();
        }
    }

    public File getFile(String fileName) {
        return new File(appDir, fileName);
    }

    public File getFile(String fileName, String ext) {
        return new File(appDir, fileName.endsWith(ext) ? fileName : fileName + ext);
    }

    public String[] getFileNames(String ext) {
        String[] names = appDir.list(new ExtFilenameFilter(ext));
        return names != null ? names : new String[0];
    }

    public File[] getFiles(String ext) {
        File[] files = appDir.listFiles(new ExtFilenameFilter(ext));
        return files != null ? files : new File[0];
    }

    public boolean hasFiles() {
        File[] files = appDir.listFiles(new ExtFilenameFilter(PARAM_FILE_EXT));
        return files != null && files.length > 0;
    }

    public static String getFilenameWithoutExt(File file) {
        String name = file.getName();
        if (name.endsWith(PARAM_FILE_EXT)) {
            return name.substring(0, name.length() - PARAM_FILE_EXT.length());
        }
        if (name.endsWith(IMAGE_FILE_EXT)) {
            return name.substring(0, name.length() - IMAGE_FILE_EXT.length());
        }
        return name;
    }

    private static class ExtFilenameFilter implements FilenameFilter {
        final String ext;

        private ExtFilenameFilter(String ext) {
            this.ext = ext;
        }

        @Override
        public boolean accept(File dir, String filename) {
            return filename.endsWith(ext);
        }
    }
}
