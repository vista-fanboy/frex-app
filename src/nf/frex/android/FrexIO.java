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

package nf.frex.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.*;
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

    private final Context context;
    private static File appStorageDir;
    private static boolean internal;

    public FrexIO(Context context) {
        this.context = context;
    }

    public File getAppStorageDir() {
        checkAppStorageDir(context);
        return appStorageDir;
    }

    public File getUniqueParamFile(String baseName) {
        File appStorageDir = getAppStorageDir();
        for (int i = 0; ; i++) {
            String fileName = baseName + "_" + i + FrexIO.PARAM_FILE_EXT;
            File file = new File(appStorageDir, fileName);
            if (!file.exists()) {
                return file;
            }
        }
    }

    public File[] getFiles(String ext) {
        List<File> fileList = getFileList(ext);
        return fileList.toArray(new File[fileList.size()]);
    }

    public List<File> getFileList(String ext) {
        return getFileList(new ExtFilenameFilter(ext));
    }

    public List<File> getFileList() {
        return getFileList(new AllFilenameFilter());
    }

    public List<File> getFileList(FilenameFilter filter) {
        ArrayList<File> files = new ArrayList<File>(32);
        collectFiles(filter, files);
        return files;
    }

    private void collectFiles(FilenameFilter filter, ArrayList<File> fileNames) {
        File appStorageDir = getAppStorageDir();
        File[] names = appStorageDir.listFiles(filter);
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
        int i = name.lastIndexOf('.');
        if (i > 0) {
            return name.substring(0, i);
        }
        return name;
    }

    public static File rename(File file, String suffix) {
        String name = file.getName();
        int i = name.lastIndexOf('.');
        if (i > 0) {
            String ext = name.substring(i);
            String base = name.substring(0, i);
            File dir = file.getParentFile();
            return new File(dir, base + "i" + ext);
        }
        return file;
    }

    private void checkAppStorageDir(Context context) {
        if (internal || appStorageDir == null || !appStorageDir.exists()) {
            synchronized (this) {
                setAppStorageDir(context);
            }
        }
    }

    private static void setAppStorageDir(Context context) {
        File internalAppDir = context.getFilesDir();
        File externalAppDir = findOrMountExternalAppDir();
        if (externalAppDir.exists()) {
            // Move all files from internal storage to external storage.
            moveDirContent(internalAppDir, externalAppDir);
            // Storage dir is external.
            appStorageDir = externalAppDir;
            internal = false;
        } else {
            // Storage dir is internal.
            appStorageDir = internalAppDir;
            internal = true;
        }
    }

    private static void moveDirContent(File srcDir, File dstDir) {
        File[] files = srcDir.listFiles();
        if (files != null) {
            for (File srcFile : files) {

                File dstFile = new File(dstDir, srcFile.getName());
                try {
                    copyFile(srcFile, rename(dstFile, "_restored"));
                    if (srcFile.delete()) {
                        Log.w("FrexIO", "Moved file to external storage: " + srcFile);
                    } else {
                        Log.w("FrexIO", "Failed to delete file after copying to external storage: " + srcFile);
                    }
                } catch (IOException e) {
                    Log.w("FrexIO", "Failed to copy file to external storage: " + srcFile);
                }
            }
        }
    }

    private static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private static File findOrMountExternalAppDir() {
        File picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File externalAppDir = new File(picturesDirectory, "Frex");
        if (!externalAppDir.exists()) {
            if (!externalAppDir.mkdirs()) {
                Log.w("FrexIO", "Failed to create external storage dir: " + externalAppDir);
            }
        }
        return externalAppDir;
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

    private static class AllFilenameFilter implements FilenameFilter {
        @Override
        public boolean accept(File dir, String filename) {
            return filename.endsWith(IMAGE_FILE_EXT) || filename.endsWith(PARAM_FILE_EXT);
        }
    }


}
