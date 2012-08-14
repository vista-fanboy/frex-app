package nf.frex.android;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Norman Fomferra
 */
public class ManagerActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final View view = getLayoutInflater().inflate(R.layout.manage_fractals_dialog, null);

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final int thumbnailWidth = display.getWidth() / 2;
        final int thumbnailHeight = display.getHeight() / 2;

        final FrexIO frexIO = new FrexIO(this);
        final File[] imageFiles = frexIO.getFiles(FrexIO.IMAGE_FILE_EXT);
        final Bitmap[] thumbnails = new Bitmap[imageFiles.length];
        final Bitmap proxyBitmap = Bitmap.createBitmap(new int[thumbnailWidth * thumbnailHeight], thumbnailWidth, thumbnailHeight, Bitmap.Config.ARGB_8888);
        Arrays.fill(thumbnails, proxyBitmap);

        final Gallery gallery = (Gallery) view.findViewById(R.id.fractal_gallery);
        final ImageFileGalleryAdapter galleryAdapter = new ImageFileGalleryAdapter(this, imageFiles, thumbnails);
        gallery.setAdapter(galleryAdapter);
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        gallery.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                gallery.showContextMenu();
                return false;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < imageFiles.length; i++) {
                    try {
                        FileInputStream stream = new FileInputStream(imageFiles[i]);
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
        }).start();

        final Button openButton = (Button) view.findViewById(R.id.ok_button);
        final Button deleteButton = (Button) view.findViewById(R.id.delete_button);
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = gallery.getSelectedItemPosition();
                if (position >= 0) {
                    ManagerActivity.this.setResult(RESULT_OK, new Intent(Intent.ACTION_VIEW, Uri.fromFile(imageFiles[position])));
                    ManagerActivity.this.finish();
                }
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = gallery.getSelectedItemPosition();
                if (position < 0) {
                    return;
                }
                FrexActivity.showYesNoDialog(ManagerActivity.this, R.string.delete_fractal, getString(R.string.really_delete_fractal), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File imageFile = imageFiles[position];
                        File paramFile = new File(imageFile.getParent(), FrexIO.getFilenameWithoutExt(imageFile) + FrexIO.PARAM_FILE_EXT);
                        int n = 0;
                        n += imageFile.delete() ? 1 : 0;
                        n += paramFile.delete() ? 1 : 0;
                        Toast.makeText(ManagerActivity.this, getString(R.string.fractal_deleted), Toast.LENGTH_SHORT).show();
                        galleryAdapter.removeFractal(gallery.getSelectedItemPosition());
                    }
                },
                                             null,
                                             null);

            }
        });


        setContentView(view);
    }
}
