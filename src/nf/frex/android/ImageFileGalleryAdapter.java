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
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
* @author Norman Fomferra
*/
public class ImageFileGalleryAdapter extends BaseAdapter {

    Bitmap[] bitmaps;
    private final Context context;
    private final File[] imageFiles;
    private final int galleryItemBackground;
    private final int thumbnailHeight;

    public ImageFileGalleryAdapter(Context context, File[] imageFiles) {
        this.context = context;
        this.imageFiles = imageFiles;
        bitmaps = new Bitmap[imageFiles.length];
        TypedArray attr = context.obtainStyledAttributes(R.styleable.fractal_gallery);
        galleryItemBackground = attr.getResourceId(R.styleable.fractal_gallery_android_galleryItemBackground, 0);
        attr.recycle();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        thumbnailHeight = display.getHeight() / 3;
    }

    @Override
    public int getCount() {
        return imageFiles.length;
    }

    @Override
    public Object getItem(int position) {
        return imageFiles[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;
        if (convertView instanceof ImageView) {
            imageView = (ImageView) convertView;
        } else {
            imageView = new ImageView(context);
            imageView.setBackgroundResource(galleryItemBackground);
        }
        if (bitmaps[position] == null) {
            // todo - load image in background thread
            try {
                FileInputStream stream = new FileInputStream(imageFiles[position]);
                try {
                    bitmaps[position] = BitmapFactory.decodeStream(stream);
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                // ?
            }
        }

        imageView.setId(position);
        if (bitmaps[position] != null) {
            int w = bitmaps[position].getWidth();
            int h = bitmaps[position].getHeight();
            int thumbnailWidth = (w * thumbnailHeight) / h;
            imageView.setLayoutParams(new Gallery.LayoutParams(thumbnailWidth, thumbnailHeight));
            //imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setImageBitmap(bitmaps[position]);
        }
        return imageView;
    }
}
