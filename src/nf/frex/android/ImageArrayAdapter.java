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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * @author Norman Fomferra
 */
public class ImageArrayAdapter extends BaseAdapter {

    private final Context context;
    private final Bitmap[] bitmaps;
    private final LayoutInflater inflater;
    private final int imageViewResourceId;

    public ImageArrayAdapter(Context context, int imageViewResourceId, Bitmap[] bitmaps) {
        this.context = context;
        this.bitmaps = bitmaps;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.imageViewResourceId = imageViewResourceId;
    }

    @Override
    public int getCount() {
        return bitmaps.length;
    }

    @Override
    public Bitmap getItem(int position) {
        return bitmaps[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        try {
            if (convertView == null) {
                if (imageViewResourceId != 0) {
                    imageView = (ImageView) inflater.inflate(imageViewResourceId, parent, false);
                } else {
                    imageView = new ImageView(context);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    // padding is IMPORTANT, otherwise selection is invisible
                    imageView.setPadding(16, 8, 16, 8);
                }
            } else {
                imageView = (ImageView) convertView;
            }
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for an ImageView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be an ImageView", e);
        }

        Bitmap image = getItem(position);
        imageView.setImageBitmap(image);

        return imageView;
    }


}
