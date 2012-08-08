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

import android.app.*;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.PaintDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.*;
import android.widget.*;
import nf.frex.core.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * To-do List
 * <pre>
 *     todo - add welcome screen with a few hints
 *     todo - I18N of fractals, functions, color scheme names
 *     todo - Define ready-to-use orbit processors
 *     todo - Refine distance functions and give better names (by what it looks like)
 *     todo - check I18N, generate uk-english, italian, french and spanish versions
 *     todo - fix problem of too small dialogs on tablets
 *     todo - introduce function that auto-creates images (e.g. shake mobile using varying intensities)
 *     todo - add better/nicer color bars
 *     todo - manage saved fractals: rename and delete
 *     todo - navigate back to last region (use 'regionHistory')
 *     todo - override back-button action, add preference to don't do it
 *     todo - undo/redo
 * </pre>
 *
 * @author Norman Fomferra
 */
public class FrexActivity extends Activity {

    public static final String TAG = "Frex";

    private static Bitmap[] COLOR_TABLE_ICONS;

    private FractalView view;
    private final List<Uri> temporaryImageFiles = new ArrayList<Uri>();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "onCreate(savedInstanceState=" + savedInstanceState + ")");
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_ACTION_BAR);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        requestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            getActionBar().setBackgroundDrawable(new PaintDrawable(Color.argb(128, 0, 0, 0)));
        }

        view = new FractalView(this);
        registerForContextMenu(view);
        setContentView(view);
        if (savedInstanceState != null) {
            view.restoreInstanceState(new BundlePropertySet(savedInstanceState));
        } else {
            PropertySet propertySet = (PropertySet) getLastNonConfigurationInstance();
            if (propertySet != null) {
                view.restoreInstanceState(propertySet);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.zoom_all:
                view.zoomAll();
                return true;
            case R.id.zoom_in:
                view.zoomRegion(2.0);
                return true;
            case R.id.zoom_out:
                view.zoomRegion(0.5);
                return true;
            case R.id.colors:
                showDialog(R.id.colors);
                return true;
            case R.id.decorations:
                showDialog(R.id.decorations);
                return true;
            case R.id.properties:
                showDialog(R.id.properties);
                return true;
            case R.id.open_fractal:
                if (new FrexIO(this).hasFiles()) {
                    showDialog(R.id.open_fractal);
                } else {
                    Toast.makeText(FrexActivity.this, getString(R.string.no_fractals_found), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.save_fractal:
                showDialog(R.id.save_fractal);
                return true;
            case R.id.save_image:
                saveImage(false);
                return true;
            case R.id.share_image:
                shareImage();
                return true;
            case R.id.set_wallpaper:
                setWallpaper();
                return true;
            case R.id.delete_all_fractals:
                if (new FrexIO(this).hasFiles()) {
                    deleteAllFractals();
                } else {
                    Toast.makeText(FrexActivity.this, getString(R.string.no_fractals_found), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.about_frex:
                showDialog(R.id.about_frex);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareImage() {
        Intent shareIntent = createShareIntent();
        if (shareIntent != null) {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)));
        }
    }

    private Intent createShareIntent() {
        Uri uri = saveImage(true);
        if (uri != null) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
            shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
            return shareIntent;
        }
        return null;
    }

    private void setWallpaper() {
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(FrexActivity.this);
        final int minimumWidth = wallpaperManager.getDesiredMinimumWidth();
        final int minimumHeight = wallpaperManager.getDesiredMinimumHeight();

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.set_wallpaper);
        TextView view1 = new TextView(this);
        view1.setSingleLine(false);
        view1.setPadding(5, 5, 5, 5);
        view1.setText(getString(R.string.wallpaper_msg, minimumWidth, minimumHeight));
        b.setView(view1);
        //b.setMessage(getString(R.string.wallpaper_msg, minimumWidth, minimumHeight));
        b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // ok
            }
        });
        b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final Image image = view.getImage();
                final int imageWidth = image.getWidth();
                final int imageHeight = image.getHeight();

                if (imageWidth != minimumWidth || imageHeight != minimumHeight) {

                    final Image wallpaperImage;
                    try {
                        wallpaperImage = new Image(minimumWidth, minimumHeight);
                    } catch (OutOfMemoryError e) {
                        Toast.makeText(FrexActivity.this, getString(R.string.out_of_memory), Toast.LENGTH_LONG).show();
                        return;
                    }

                    final ProgressDialog progressDialog = new ProgressDialog(FrexActivity.this);
                    final Generator wallpaperGenerator = new Generator(view.getGeneratorConfig(), new Generator.ProgressListener() {
                        int numLines;

                        @Override
                        public void onStarted(int numTasks) {
                        }

                        @Override
                        public void onSomeLinesComputed(int taskId, int line1, int line2) {
                            numLines += 1 + line2 - line1;
                            progressDialog.setProgress(numLines);
                        }

                        @Override
                        public void onStopped(boolean cancelled) {
                            progressDialog.dismiss();
                            if (!cancelled) {
                                setWallpaper(wallpaperManager, wallpaperImage);
                            }
                        }
                    });

                    DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            wallpaperGenerator.cancel();
                        }
                    };
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setCancelable(true);
                    progressDialog.setMax(minimumHeight);
                    progressDialog.setOnCancelListener(cancelListener);
                    progressDialog.show();

                    wallpaperGenerator.start(wallpaperImage, false);
                } else {
                    setWallpaper(wallpaperManager, image);
                }
            }
        });
        b.show();
    }

    static int imgId = 0;

    Uri saveImage(boolean temp) {
        final String url = MediaStore.Images.Media.insertImage(getContentResolver(),
                view.getImage().createBitmap(),
                view.getConfigName() != null ? view.getConfigName() : "frex-" + (++imgId),
                "Fractal generated by Frex");
        if (url != null) {
            Toast.makeText(FrexActivity.this, getString(R.string.image_saved_msg, url), Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse(url);
            if (temp) {
                temporaryImageFiles.add(uri);
            }
            return uri;
        } else {
            Toast.makeText(this, getString(R.string.image_save_failed_msg), Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    private void setWallpaper(WallpaperManager wallpaperManager, Image image) {
        try {
            wallpaperManager.setBitmap(image.createBitmap());
            alert(R.string.wallpaper_set_msg);
        } catch (IOException e) {
            alert(e.getLocalizedMessage());
        }
    }

    public void alert(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FrexActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void alert(final int messageId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(FrexActivity.this, messageId, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteAllFractals() {
        final FrexIO frexIO = new FrexIO(this);
        final File[] files = frexIO.getFiles(FrexIO.PARAM_FILE_EXT);

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(getString(R.string.delete_all_fractals));
        b.setMessage(getString(R.string.really_delete_all_fractals_msg, files.length));
        b.setCancelable(true);
        b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int n = 0;
                for (File paramFile : files) {
                    if (paramFile.delete()) {
                        n++;
                    }
                    File imageFile = new File(paramFile.getParentFile(), FrexIO.getFilenameWithoutExt(paramFile) + FrexIO.IMAGE_FILE_EXT);
                    if (imageFile.delete()) {
                        n++;
                    }
                }
                Toast.makeText(FrexActivity.this, getString(R.string.files_deleted_msg, n, files.length), Toast.LENGTH_SHORT).show();
            }
        });
        b.setNeutralButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        b.show();
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == R.id.save_fractal) {
            return createSaveDialog();
        } else if (id == R.id.open_fractal) {
            return createOpenDialog();
        } else if (id == R.id.colors) {
            return createColorsDialog();
        } else if (id == R.id.properties) {
            return createPropertiesDialog();
        } else if (id == R.id.decorations) {
            return createDecorationsDialog();
        } else if (id == R.id.about_frex) {
            return createAboutDialog();
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        super.onPrepareDialog(id, dialog, args);
        if (id == R.id.save_fractal) {
            prepareSaveDialog(dialog);
        } else if (id == R.id.open_fractal) {
            prepareOpenDialog(dialog);
        } else if (id == R.id.colors) {
            prepareColorsDialog(dialog);
        } else if (id == R.id.properties) {
            preparePropertiesDialog(dialog);
        } else if (id == R.id.decorations) {
            prepareDecorationsDialog(dialog);
        }
    }

    private void prepareColorsDialog(Dialog dialog) {
        if (COLOR_TABLE_ICONS == null) {
            Registry<ColorScheme> colorTables = Registries.colorSchemes;
            COLOR_TABLE_ICONS = new Bitmap[colorTables.getSize()];
            List<String> ids = colorTables.getIdList();
            for (int i = 0; i < ids.size(); i++) {
                COLOR_TABLE_ICONS[i] = colorTables.getValue(i).createGradientIcon();
            }
        }
        int checkedIndex = Registries.colorSchemes.getIndex(view.getColorSchemeId());

        final Spinner colorTableSpinner = (Spinner) dialog.findViewById(R.id.color_table_spinner);
        colorTableSpinner.setAdapter(new ImageListAdapter(this, COLOR_TABLE_ICONS));
        colorTableSpinner.setSelection(checkedIndex, false);
        colorTableSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View spinnerView, int position, long id) {
                view.setColorSchemeId(Registries.colorSchemes.getId(position));
                view.setColorScheme(Registries.colorSchemes.getValue(position));
                view.recomputeColors();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final SeekBar colorFactorSeekBar = (SeekBar) dialog.findViewById(R.id.color_gain_seek_bar);
        final double colorFactorMin = -3.0;
        final double colorFactorMax = 2.0;
        final SeekBarConfigurer colorFactorSeekBarConfigurer = new SeekBarConfigurer(colorFactorSeekBar, colorFactorMin, colorFactorMax, true);
        colorFactorSeekBarConfigurer.setValue(view.getColorGain());
        colorFactorSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                view.setColorGain((float) colorFactorSeekBarConfigurer.getValue());
                view.recomputeColors();
            }
        });

        final SeekBar colorBiasSeekBar = (SeekBar) dialog.findViewById(R.id.color_offset_seek_bar);
        final double colorBiasMin = 0;
        final double colorBiasMax = 1024;
        final SeekBarConfigurer colorBiasSeekBarConfigurer = new SeekBarConfigurer(colorBiasSeekBar, colorBiasMin, colorBiasMax, false);
        colorBiasSeekBarConfigurer.setValue(view.getColorOffset());
        colorBiasSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                view.setColorOffset((float) colorBiasSeekBarConfigurer.getValue());
                view.recomputeColors();
            }
        });

        final CheckBox colorRepeatCheckBox = (CheckBox) dialog.findViewById(R.id.color_repeat);
        colorRepeatCheckBox.setChecked(view.isColorRepeat());
        colorRepeatCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                view.setColorRepeat(isChecked);
                view.recomputeColors();
            }
        });

        Button randomButton = (Button) dialog.findViewById(R.id.random_button);
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorTableSpinner.setSelection((int) (Math.random() * COLOR_TABLE_ICONS.length));
                colorFactorSeekBarConfigurer.setRandomValue();
                colorBiasSeekBarConfigurer.setRandomValue();
                view.recomputeColors();
            }
        });

    }

    private void preparePropertiesDialog(final Dialog dialog) {

        final Registry<Fractal> fractals = Registries.fractals;
        int fractalTypeIndex = fractals.getIndex(view.getFractalId());

        final Spinner fractalTypeSpinner = (Spinner) dialog.findViewById(R.id.fractal_type_spinner);
        final SeekBar iterationsSeekBar = (SeekBar) dialog.findViewById(R.id.num_iterations_seek_bar);
        final EditText iterationsEditText = (EditText) dialog.findViewById(R.id.num_iterations_edit_text);
        final CheckBox juliaModeCheckBox = (CheckBox) dialog.findViewById(R.id.julia_mode_fractal_check_box);
        final CheckBox decoratedFractal = (CheckBox) dialog.findViewById(R.id.decorated_fractal_check_box);
        final Button okButton = (Button) dialog.findViewById(R.id.ok_button);
        final Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);

        juliaModeCheckBox.setEnabled(true);

        fractalTypeSpinner.setAdapter(new TextListAdapter(this, fractals.getIds()));
        fractalTypeSpinner.setSelection(fractalTypeIndex, false);
        fractalTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View spinnerView, int position, long id) {
                Fractal fractal = fractals.getValue(position);
                iterationsEditText.setText(fractal.getDefaultIterMax() + "");
                boolean sameFractal = view.getFractalId().equals(fractals.getId(position));
                if (!sameFractal) {
                    juliaModeCheckBox.setChecked(false);
                }
                juliaModeCheckBox.setEnabled(sameFractal);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        iterationsEditText.setText(view.getIterMax() + "", TextView.BufferType.NORMAL);

        final double iterationsMin = 1;
        final double iterationsMax = 3;
        final SeekBarConfigurer iterationsSeekBarConfigurer = new SeekBarConfigurer(iterationsSeekBar, iterationsMin, iterationsMax, true);
        iterationsSeekBarConfigurer.setValue(view.getIterMax());
        iterationsSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int iterMax = iterationsSeekBarConfigurer.getValueInt();
                    iterationsEditText.setText(iterMax + "", TextView.BufferType.NORMAL);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        iterationsEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                try {
                    int iterMax = Integer.parseInt(v.getText().toString());
                    iterationsSeekBarConfigurer.setValue(iterMax);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        });

        decoratedFractal.setChecked(view.isDecoratedFractal());

        juliaModeCheckBox.setChecked(view.isJuliaModeFractal());

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int iterMax;
                try {
                    iterMax = Integer.parseInt(iterationsEditText.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(FrexActivity.this, getString(R.string.error_msg, e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                String newFractalId = fractals.getId(fractalTypeSpinner.getSelectedItemPosition());
                Fractal fractal = fractals.getValue(fractalTypeSpinner.getSelectedItemPosition());
                String oldFractalId = view.getFractalId();
                view.setFractalId(newFractalId);
                view.setIterMax(iterMax);
                view.setDecoratedFractal(decoratedFractal.isChecked());
                view.setJuliaModeFractal(juliaModeCheckBox.isChecked());
                if (!oldFractalId.equals(newFractalId)) {
                    view.setRegion(fractal.getDefaultRegion());
                    view.setBailOut(fractal.getDefaultBailOut());
                }
                view.recomputeAll();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void prepareDecorationsDialog(final Dialog dialog) {
        final Registry<DistanceFunction> distanceFunctions = Registries.distanceFunctions;
        int checkedIndex = distanceFunctions.getIndex(view.getDistanceFunctionId());

        final Spinner distanceFunctionSpinner = (Spinner) dialog.findViewById(R.id.distance_function_spinner);
        distanceFunctionSpinner.setAdapter(new TextListAdapter(this, distanceFunctions.getIds()));
        distanceFunctionSpinner.setSelection(checkedIndex, false);

        final SeekBar dilationSeekBar = (SeekBar) dialog.findViewById(R.id.dilation_seek_bar);
        final double dilationMin = -3.0;
        final double dilationMax = 1.0;
        final SeekBarConfigurer dilationSeekBarConfigurer = new SeekBarConfigurer(dilationSeekBar, dilationMin, dilationMax, true);
        dilationSeekBarConfigurer.setValue(view.getDistanceDilation());

        final SeekBar translXSeekBar = (SeekBar) dialog.findViewById(R.id.transl_x_seek_bar);
        final double translXMin = -2.5;
        final double translXMax = 2.5;
        final SeekBarConfigurer translateXSeekBarConfigurer = new SeekBarConfigurer(translXSeekBar, translXMin, translXMax, false);
        translateXSeekBarConfigurer.setValue(view.getDistanceTranslateX());

        final SeekBar translYSeekBar = (SeekBar) dialog.findViewById(R.id.transl_y_seek_bar);
        final double translYMin = -2.5;
        final double translYMax = 2.5;
        final SeekBarConfigurer translateYSeekBarConfigurer = new SeekBarConfigurer(translYSeekBar, translYMin, translYMax, false);
        translateYSeekBarConfigurer.setValue(view.getDistanceTranslateY());

        Button randomButton = (Button) dialog.findViewById(R.id.random_button);
        randomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dilationSeekBarConfigurer.setRandomValue();
                translateXSeekBarConfigurer.setRandomValue();
                translateYSeekBarConfigurer.setRandomValue();
                distanceFunctionSpinner.setSelection((int) (Math.random() * distanceFunctions.getSize()));
            }
        });

        Button okButton = (Button) dialog.findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = distanceFunctionSpinner.getSelectedItemPosition();
                dialog.dismiss();
                view.setDecoratedFractal(true);
                view.setDistanceDilation(dilationSeekBarConfigurer.getValue());
                view.setDistanceTranslateX(translateXSeekBarConfigurer.getValue());
                view.setDistanceTranslateY(translateYSeekBarConfigurer.getValue());
                view.setDistanceFunctionId(distanceFunctions.getId(index));
                view.recomputeAll();
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void prepareOpenDialog(final Dialog dialog) {
        final FrexIO frexIO = new FrexIO(FrexActivity.this);
        final File[] imageFiles = frexIO.getFiles(FrexIO.IMAGE_FILE_EXT);

        final Gallery gallery = (Gallery) dialog.findViewById(R.id.fractal_gallery);
        final EditText editText = (EditText) dialog.findViewById(R.id.fractal_name_edit_text);
        gallery.setAdapter(new ImageFileGalleryAdapter(this, imageFiles));
        gallery.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editText.getEditableText().clear();
                editText.getEditableText().append(FrexIO.getFilenameWithoutExt(imageFiles[position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editText.getEditableText().clear();
            }
        });

        final Button okButton = (Button) dialog.findViewById(R.id.ok_button);
        final Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String fractalName = editText.getText().toString().trim();
                if (fractalName.length() == 0) {
                    Toast.makeText(FrexActivity.this, R.string.enter_name_msg, Toast.LENGTH_SHORT).show();
                    return;
                }

                FrexIO frexIO = new FrexIO(FrexActivity.this);
                File paramFile = frexIO.getFile(fractalName, FrexIO.PARAM_FILE_EXT);
                if (!paramFile.exists()) {
                    Toast.makeText(FrexActivity.this, getString(R.string.parameters_not_found_msg), Toast.LENGTH_SHORT).show();
                    return;
                }

                Properties properties = new Properties();
                try {
                    FileInputStream fis = new FileInputStream(paramFile);
                    try {
                        properties = new Properties();
                        properties.load(fis);
                    } finally {
                        fis.close();
                    }
                } catch (IOException e) {
                    Toast.makeText(FrexActivity.this, getString(R.string.error_msg, e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();
                view.setConfigName(fractalName);
                view.restoreInstanceState(new DefaultPropertySet(properties));
                view.recomputeAll();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void prepareSaveDialog(final Dialog dialog) {
        final EditText editText = (EditText) dialog.findViewById(R.id.fractal_name_edit_text);
        if (view.getConfigName() != null) {
            editText.getEditableText().clear();
            editText.getEditableText().append(view.getConfigName());
        }

        final Button okButton = (Button) dialog.findViewById(R.id.ok_button);
        final Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fractalName = editText.getText().toString().trim();
                if (fractalName.length() == 0) {
                    Toast.makeText(FrexActivity.this, getString(R.string.enter_name_msg), Toast.LENGTH_SHORT).show();
                    return;
                }

                final FrexIO frexIO = new FrexIO(FrexActivity.this);
                final File paramFile = frexIO.getFile(fractalName, FrexIO.PARAM_FILE_EXT);
                if (paramFile.exists()) {
                    AlertDialog.Builder b = new AlertDialog.Builder(FrexActivity.this);
                    b.setTitle(R.string.safe_fractal);
                    b.setMessage(String.format(getString(R.string.name_exists_msg), fractalName));
                    b.setCancelable(true);
                    b.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogIfc, int which) {
                            // "Yes" selected --> save / overwrite
                            saveConfig(fractalName, frexIO, paramFile, dialog);
                        }
                    });
                    b.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // "No" selected --> do nothing, leave Save dialog open
                        }
                    });
                    b.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // cancel Save action --> do nothing, close Save dialog and return
                            dialog.dismiss();
                        }
                    });
                    b.show();

                } else {
                    saveConfig(fractalName, frexIO, paramFile, dialog);
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

    }

    private Dialog createAboutDialog() {
        String versionName;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?";
            Log.w(TAG, e);
        }
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.about_frex);
        b.setIcon(R.drawable.frex_logo);
        b.setMessage(getString(R.string.about_frex_text, versionName));
        return b.create();
    }

    private Dialog createSaveDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.save_fractal_dialog);
        dialog.setTitle(getString(R.string.safe_fractal));
        return dialog;
    }

    private Dialog createOpenDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.open_fractal_dialog);
        dialog.setTitle(getString(R.string.open_fractal));
        return dialog;
    }

    private Dialog createDecorationsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.decorations_dialog);
        dialog.setTitle(R.string.decorations);
        return dialog;
    }

    private Dialog createPropertiesDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.properties_dialog);
        dialog.setTitle(getString(R.string.properties));
        return dialog;
    }

    private Dialog createColorsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        dialog.setContentView(R.layout.colors_dialog);
        dialog.setTitle(getString(R.string.colors));
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private void saveConfig(String configName, FrexIO frexIO, File paramFile, Dialog dialog) {
        try {
            FileOutputStream fos = new FileOutputStream(paramFile);
            try {
                Properties properties = new Properties();
                view.saveInstanceState(new DefaultPropertySet(properties));
                properties.save(fos, "Generated by Frex on " + new Date());
            } finally {
                fos.close();
            }
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_msg, e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File imageFile = frexIO.getFile(configName, FrexIO.IMAGE_FILE_EXT);
            FileOutputStream out = new FileOutputStream(imageFile);
            try {
                view.createBitmap().compress(FrexIO.IMAGE_FILE_FORMAT, 100, out);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_msg, e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
        }

        dialog.dismiss();
        view.setConfigName(configName);
        Toast.makeText(this, getString(R.string.fractal_saved), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        // Log.d(TAG, "onStart()");
        super.onStart();
    }

    @Override
    protected void onRestart() {
        // Log.d(TAG, "onRestart()");
        super.onRestart();
    }

    @Override
    protected void onResume() {
        // Log.d(TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onPostResume() {
        // Log.d(TAG, "onPostResume()");
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        // Log.d(TAG, "Cancelling generators");
        view.cancelGenerators();

        super.onPause();
    }

    @Override
    protected void onUserLeaveHint() {
        // Log.d(TAG, "onUserLeaveHint()");
        super.onUserLeaveHint();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Log.d(TAG, "onSaveInstanceState(outState=" + outState + ")");
        super.onSaveInstanceState(outState);
        view.saveInstanceState(new BundlePropertySet(outState));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Log.d(TAG, "onRestoreInstanceState(outState=" + savedInstanceState + ")");
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            view.restoreInstanceState(new BundlePropertySet(savedInstanceState));
        }
    }

    @Override
    protected void onStop() {
        view.getScreenGenerator().cancel();
        // Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // Log.d(TAG, "onDestroy: deleting temporary image files");
        deleteTemporaryImageFiles();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // Log.d(TAG, "onConfigurationChanged(newConfig=" + newConfig + ")");
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        PropertySet propertySet = new BundlePropertySet();
        view.saveInstanceState(propertySet);
        return propertySet;
    }

    @Override
    public void onLowMemory() {
        // Log.d(TAG, "onLowMemory()");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        // Log.d(TAG, "onTrimMemory(level=" + level + ")");
        super.onTrimMemory(level);
    }

    private void deleteTemporaryImageFiles() {
        for (Uri uri : temporaryImageFiles) {
            if (getContentResolver().delete(uri, null, null) > 0) {
                Log.v(TAG, "Deleted " + uri);
            } else {
                Log.v(TAG, "Failed to delete " + uri);
            }
        }
        temporaryImageFiles.clear();
    }

}
