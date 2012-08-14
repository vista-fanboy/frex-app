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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Norman Fomferra
 */
public class ImageFileGalleryAdapter extends BaseAdapter {

    private final Context context;
    private final ArrayList<File> imageFiles;
    private final ArrayList<Bitmap> thumbnails;
    private final int galleryItemBackground;

    public ImageFileGalleryAdapter(Context context, File[] imageFiles, Bitmap[] thumbnails) {
        this.context = context;
        this.imageFiles = new ArrayList<File>(Arrays.asList(imageFiles));
        this.thumbnails = new ArrayList<Bitmap>(Arrays.asList(thumbnails));
        TypedArray attr = context.obtainStyledAttributes(R.styleable.fractal_gallery);
        galleryItemBackground = attr.getResourceId(R.styleable.fractal_gallery_android_galleryItemBackground, 0);
        attr.recycle();
    }

    @Override
    public int getCount() {
        return imageFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return imageFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ImageView imageView;
        if (convertView instanceof ImageView) {
            imageView = (ImageView) convertView;
        } else {
            imageView = new ImageView(context);
            imageView.setBackgroundResource(galleryItemBackground);
        }

        imageView.setId(position);

        Bitmap bitmap = thumbnails.get(position);
        imageView.setLayoutParams(new Gallery.LayoutParams(bitmap.getWidth(), bitmap.getHeight()));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageBitmap(bitmap);

        return imageView;
    }

    public void setThumbnail(int position, Bitmap thumbnail) {
        thumbnails.set(position, thumbnail);
        // This will cause a crash:
        // notifyDataSetChanged();
    }

    public void removeFractal(int position) {
        imageFiles.remove(position);
        thumbnails.remove(position);
        notifyDataSetChanged();
    }

    public void removeAllFractals() {
        imageFiles.clear();
        thumbnails.clear();
        notifyDataSetChanged();
    }

}
