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

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static nf.frex.android.FrexActivity.showYesNoDialog;

/**
 * @author Norman Fomferra
 */
public class ManagerActivity extends Activity {

    private Gallery gallery;
    private File[] imageFiles;
    private ImageFileGalleryAdapter galleryAdapter;
    private int thumbnailHeight;
    private int thumbnailWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        thumbnailWidth = (2 * display.getWidth()) / 3;
        thumbnailHeight = (2 * display.getHeight()) / 3;

        final FrexIO frexIO = new FrexIO(this);
        imageFiles = frexIO.getFiles(FrexIO.IMAGE_FILE_EXT);
        final Bitmap[] thumbnails = new Bitmap[imageFiles.length];
        final Bitmap proxyBitmap = Bitmap.createBitmap(new int[thumbnailWidth * thumbnailHeight], thumbnailWidth, thumbnailHeight, Bitmap.Config.ARGB_8888);
        Arrays.fill(thumbnails, proxyBitmap);

        final View view = getLayoutInflater().inflate(R.layout.manage_fractals, null);
        gallery = (Gallery) view.findViewById(R.id.fractal_gallery);
        galleryAdapter = new ImageFileGalleryAdapter(this, imageFiles, thumbnails);
        gallery.setAdapter(galleryAdapter);
        gallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setResult(RESULT_OK, new Intent(Intent.ACTION_VIEW, Uri.fromFile(imageFiles[position])));
                finish();
            }
        });
        setContentView(view);

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadAllImages();
            }
        }).start();
    }

    private void loadAllImages() {
        for (int i = 0; i < imageFiles.length; i++) {
            File imageFile = imageFiles[i];
            try {
                FileInputStream stream = new FileInputStream(imageFile);
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(stream);
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();
                    int thumbnailWidth = (w * thumbnailHeight) / h;
                    Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, thumbnailWidth, thumbnailHeight, true);
                    galleryAdapter.setThumbnail(i, thumbnail);
                } finally {
                    stream.close();
                }
            } catch (IOException e) {
                // ?
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    galleryAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gallery = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.manager, menu);
        if (FrexActivity.PRE_SDK14) {
            FrexActivity.setMenuBackground(this);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                deleteFractal();
                return true;
            case R.id.delete_all:
                deleteAllFractals();
                return true;
            case R.id.share_image:
                shareImage();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteFractal() {
        final int position = gallery.getSelectedItemPosition();
        if (position < 0) {
            return;
        }
        showYesNoDialog(ManagerActivity.this,
                        R.string.delete_fractal,
                        getString(R.string.really_delete_fractal),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteFractal(imageFiles[position]);
                                Toast.makeText(ManagerActivity.this, getString(R.string.fractal_deleted), Toast.LENGTH_SHORT).show();
                                galleryAdapter.removeFractal(position);
                                if (galleryAdapter.isEmpty()) {
                                    setResult(RESULT_CANCELED);
                                    finish();
                                }
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // ok
                            }
                        },
                        null
        );
    }

    private boolean deleteFractal(File imageFile) {
        File paramFile = new File(imageFile.getParent(), FrexIO.getFilenameWithoutExt(imageFile) + FrexIO.PARAM_FILE_EXT);
        int n = 0;
        n += imageFile.delete() ? 1 : 0;
        n += paramFile.delete() ? 1 : 0;
        return n == 2;
    }

    private void deleteAllFractals() {
        final FrexIO frexIO = new FrexIO(this);
        final File[] files = frexIO.getFiles(FrexIO.IMAGE_FILE_EXT);

        showYesNoDialog(this, R.string.delete_all,
                        getString(R.string.delete_all_warning,
                                  files.length),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (File imageFile : files) {
                                    deleteFractal(imageFile);
                                }
                                galleryAdapter.removeAllFractals();
                                Toast.makeText(ManagerActivity.this,
                                               getString(R.string.fractals_deleted_msg, files.length), Toast.LENGTH_SHORT).show();
                                setResult(RESULT_CANCELED);
                                finish();
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        },
                        null
        );
    }


    private void shareImage() {
        final int position = gallery.getSelectedItemPosition();
        if (position < 0) {
            return;
        }
        File imageFile = imageFiles[position];
        File paramFile = new File(imageFile.getParent(), FrexIO.getFilenameWithoutExt(imageFile) + FrexIO.PARAM_FILE_EXT);
        Intent shareIntent = createShareIntent(Uri.fromFile(imageFile), Uri.fromFile(paramFile));
        if (shareIntent != null) {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)));
        }
    }

    // todo - check if sending 2 files works for most recipients
    private Intent createShareIntent(Uri imageUri, Uri paramUri) {
        //Intent shareIntent = new Intent(Intent.ACTION_SEND);
        Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        shareIntent.setType("image/*");
        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, new ArrayList<Uri>(Arrays.asList(imageUri, paramUri)));
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
        return shareIntent;
    }


}
