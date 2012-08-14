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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Norman Fomferra
 */
public class FrexIO {

    public static final String PARAM_FILE_EXT = ".frex";
    public static final String IMAGE_FILE_EXT = ".png";
    public static final Bitmap.CompressFormat IMAGE_FILE_FORMAT = Bitmap.CompressFormat.PNG;

    private final File externalAppDir;
    private final File internalAppDir;

    public FrexIO(Context context) {
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
            this.externalAppDir = externalAppDir;
        } else {
            this.externalAppDir = null;
        }
        this.internalAppDir = context.getFilesDir();
    }

    public File getFile(String fileName) {
        return externalAppDir != null ? new File(externalAppDir, fileName) : new File(internalAppDir, fileName);
    }

    public File[] getFiles(String ext) {
        List<File> fileList = getFileList(ext);
        return fileList.toArray(new File[fileList.size()]);
    }

    public File getUniqueParamFile(String baseName) {
        for (int i = 0; ; i++) {
            String fileName = baseName + "_" + i + FrexIO.PARAM_FILE_EXT;
            if (externalAppDir != null) {
                File file1 = new File(externalAppDir, fileName);
                if (!file1.exists()) {
                    File file2 = new File(internalAppDir, fileName);
                    if (!file2.exists()) {
                        return file1;
                    }
                }
            } else {
                File file2 = new File(internalAppDir, fileName);
                if (!file2.exists()) {
                    return file2;
                }
            }
        }
    }


    public List<File> getFileList(String ext) {
        ArrayList<File> files = new ArrayList<File>(32);
        collectFiles(internalAppDir, ext, files);
        if (externalAppDir != null) {
            collectFiles(externalAppDir, ext, files);
        }
        return files;
    }

    private void collectFiles(File appDir, String ext, ArrayList<File> fileNames) {
        File[] names = appDir.listFiles(new ExtFilenameFilter(ext));
        if (names != null) {
            fileNames.addAll(Arrays.asList(names));
        }
    }

    public boolean hasFiles() {
        List<File> fileList = getFileList(PARAM_FILE_EXT);
        return !fileList.isEmpty();
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
