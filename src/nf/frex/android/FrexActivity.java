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

import android.app.*;
import android.content.*;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.PaintDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.*;
import nf.frex.core.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;
import java.util.*;

/**
 * Frex' main activity.
 *
 * @author Norman Fomferra
 */
public class FrexActivity extends Activity {

    public static final int SELECT_PICTURE_REQUEST_CODE = 4711;
    public static final boolean PRE_SDK14 = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    public static final String TAG = "Frex";
    public static final String EXAMPLES_URL = "https://code.google.com/p/frex-app/wiki/FrexExamples";

    private Registry<ColorScheme> colorSchemes;
    private FractalView view;

    @Override
    public void onBackPressed() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean navigateOnBack = preferences.getBoolean("navigate_on_back", false);
        if (!navigateOnBack) {
            super.onBackPressed();
            return;
        }

        LinkedList<Region> regionHistory = view.getRegionHistory();
        if (regionHistory.size() > 0) {
            Region region;
            if (regionHistory.size() == 1) {
                region = regionHistory.get(0);
                alert(getString(R.string.first_region_msg));
            } else {
                region = regionHistory.pollLast();
            }
            view.setRegionRecordingDisabled(true);
            view.regenerateRegion(region);
            view.setRegionRecordingDisabled(false);
        } else {
            alert(getString(R.string.empty_region_history_msg));
        }
    }

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

        if (PRE_SDK14) {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            // Fix: Frex to stop working on screen orientation changes (Android 2.3.x only)
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            getActionBar().setBackgroundDrawable(new PaintDrawable(Color.argb(128, 0, 0, 0)));
        }

        view = new FractalView(this);
        setContentView(view);

        if (!tryReadingFrexDocIntent(getIntent())) {
            if (savedInstanceState != null) {
                view.restoreInstanceState(new BundlePropertySet(savedInstanceState));
            } else {
                PropertySet propertySet = (PropertySet) getLastNonConfigurationInstance();
                if (propertySet != null) {
                    view.restoreInstanceState(propertySet);
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        tryReadingFrexDocIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        if (PRE_SDK14) {
            setMenuBackground(this);
        }
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
            case R.id.manage_fractals:
                if (new FrexIO(this).hasFiles()) {
                    startActivityForResult(new Intent(this, ManagerActivity.class), R.id.manage_fractals);
                } else {
                    Toast.makeText(FrexActivity.this, getString(R.string.no_fractals_found), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.save_fractal:
                saveFractal();
                return true;
            case R.id.set_wallpaper:
                setWallpaper();
                return true;
            case R.id.settings:
                startActivityForResult(new Intent(this, SettingsActivity.class), R.id.settings);
                return true;
            case R.id.examples:
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(EXAMPLES_URL));
                startActivity(intent);
                return true;
            case R.id.exit:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == R.id.manage_fractals && data.getAction().equals(Intent.ACTION_VIEW)) {
            final Uri imageUri = data.getData();
            if (imageUri != null) {
                File imageFile = new File(imageUri.getPath());
                String configName = FrexIO.getFilenameWithoutExt(imageFile);
                File paramFile = new File(imageFile.getParent(), configName + FrexIO.PARAM_FILE_EXT);
                try {
                    FileInputStream fis = new FileInputStream(paramFile);
                    try {
                        readFrexDoc(fis, configName);
                    } finally {
                        fis.close();
                    }
                } catch (IOException e) {
                    Toast.makeText(FrexActivity.this, getString(R.string.error_msg, e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == R.id.settings) {
            view.getGenerator().setNumTasks(SettingsActivity.getNumTasks(this));
        } else if (requestCode == SELECT_PICTURE_REQUEST_CODE) {
            final Uri imageUri = data.getData();
            final ColorQuantizer colorQuantizer = new ColorQuantizer();
            final ProgressDialog progressDialog = new ProgressDialog(FrexActivity.this);
            final DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    colorQuantizer.cancel();
                }
            };
            final ColorQuantizer.ProgressListener progressListener = new ColorQuantizer.ProgressListener() {
                @Override
                public void progress(final String msg, final int iter, final int maxIter) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.setMessage(msg);
                            progressDialog.setProgress(iter);
                        }
                    });
                }
            };

            progressDialog.setTitle(getString(R.string.get_pal_from_img_title));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(cancelListener);
            progressDialog.setMax(colorQuantizer.getMaxIterCount());
            progressDialog.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap;
                    try {
                        bitmap = FrexIO.readBitmap(getContentResolver(), imageUri, 256);
                    } catch (IOException e) {
                        alert("I/O error: " + e.getLocalizedMessage());
                        return;
                    }
                    ColorScheme colorScheme = colorQuantizer.quantize(bitmap, progressListener);
                    progressDialog.dismiss();
                    if (colorScheme != null) {
                        Log.d(TAG, "SELECT_PICTURE_REQUEST_CODE: Got colorScheme");
                        String colorSchemeId = "$" + imageUri.toString();
                        colorSchemes.add(colorSchemeId, colorScheme);
                        view.setColorSchemeId(colorSchemeId);
                        view.setColorScheme(colorScheme);
                        view.recomputeColors();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "SELECT_PICTURE_REQUEST_CODE: showDialog(R.id.colors)");
                                showDialog(R.id.colors);
                            }
                        });

                    }
                }
            });
            thread.start();
        }
    }

    private void setWallpaper() {
        final WallpaperManager wallpaperManager = WallpaperManager.getInstance(FrexActivity.this);
        final int minimumWidth = wallpaperManager.getDesiredMinimumWidth();
        final int minimumHeight = wallpaperManager.getDesiredMinimumHeight();

        showYesNoDialog(this, R.string.set_wallpaper,
                        getString(R.string.wallpaper_msg, minimumWidth, minimumHeight),
                        new DialogInterface.OnClickListener() {
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

                                    Generator.ProgressListener progressListener = new Generator.ProgressListener() {
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
                                    };
                                    final Generator wallpaperGenerator = new Generator(view.getGeneratorConfig(),
                                                                                       SettingsActivity.NUM_CORES,
                                                                                       progressListener);

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

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == R.id.colors) {
            return createColorsDialog();
        } else if (id == R.id.properties) {
            return createPropertiesDialog();
        } else if (id == R.id.decorations) {
            return createDecorationsDialog();
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        super.onPrepareDialog(id, dialog, args);
        if (id == R.id.colors) {
            prepareColorsDialog(dialog);
        } else if (id == R.id.properties) {
            preparePropertiesDialog(dialog);
        } else if (id == R.id.decorations) {
            prepareDecorationsDialog(dialog);
        }
    }

    private void prepareColorsDialog(final Dialog dialog) {

        Log.d(TAG, "prepareColorsDialog() entered");

        getColorSchemes();

        Log.d(TAG, "prepareColorsDialog: new bitmaps are being created");
        Bitmap[] colorSchemeIcons = new Bitmap[colorSchemes.getSize()];
        for (int i = 0; i < colorSchemeIcons.length; i++) {
            ColorScheme colorScheme = colorSchemes.getValue(i);
            colorSchemeIcons[i] = colorScheme.getGradientIcon();
        }

        int checkedIndex = Registries.colorSchemes.getIndex(view.getColorSchemeId());
        Log.d(TAG, "prepareColorsDialog: checkedIndex = " + checkedIndex);
        final Spinner colorTableSpinner = (Spinner) dialog.findViewById(R.id.color_table_spinner);
        colorTableSpinner.setAdapter(new ImageArrayAdapter(this, 0, colorSchemeIcons));
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

        Log.d(TAG, "prepareColorsDialog: WRAW!");

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
                colorTableSpinner.setSelection((int) (Math.random() * colorSchemes.getSize()));
                colorFactorSeekBarConfigurer.setRandomValue();
                colorBiasSeekBarConfigurer.setRandomValue();
                view.recomputeColors();
            }
        });

        Button getPalFromImgButton = (Button) dialog.findViewById(R.id.get_pal_from_img_button);
        getPalFromImgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                dialog.dismiss();
                startActivityForResult(photoPickerIntent, SELECT_PICTURE_REQUEST_CODE);
            }
        });

        Log.d(TAG, "prepareColorsDialog() exited");
    }

    private Registry<ColorScheme> getColorSchemes() {
        if (colorSchemes == null) {
            colorSchemes = Registries.colorSchemes;
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            Map<String, ?> all = preferences.getAll();
            for (String key : all.keySet()) {
                if (key.startsWith("$")) {
                    String value = preferences.getString(key, null);
                    if (value != null) {
                        try {
                            ColorScheme colorScheme = ColorScheme.parse(value);
                            colorSchemes.add(key, colorScheme);
                        } catch (IllegalArgumentException e) {
                            // What to do  here?
                        }
                    }
                }
            }
        }
        return colorSchemes;
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

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, fractals.getIds());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fractalTypeSpinner.setAdapter(arrayAdapter);
        fractalTypeSpinner.setSelection(fractalTypeIndex, true);
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
                String oldConfigName = view.getConfigName();
                String newFractalId = fractals.getId(fractalTypeSpinner.getSelectedItemPosition());
                Fractal fractal = fractals.getValue(fractalTypeSpinner.getSelectedItemPosition());
                String oldFractalId = view.getFractalId();
                boolean newJuliaModeFractal = juliaModeCheckBox.isChecked();
                boolean oldJuliaModeFractal = view.isJuliaModeFractal();
                view.setFractalId(newFractalId);
                view.setIterMax(iterMax);
                view.setDecoratedFractal(decoratedFractal.isChecked());
                view.setJuliaModeFractal(newJuliaModeFractal);
                boolean fractalTypeChanged = !oldFractalId.equals(newFractalId);
                if (fractalTypeChanged) {
                    if (oldConfigName.contains(oldFractalId.toLowerCase())) {
                        view.setConfigName(newFractalId.toLowerCase());
                    }
                    view.setRegion(fractal.getDefaultRegion());
                    view.setBailOut(fractal.getDefaultBailOut());
                }
                boolean juliaModeChanged = oldJuliaModeFractal != newJuliaModeFractal;
                if (fractalTypeChanged || juliaModeChanged) {
                    view.getRegionHistory().clear();
                    view.getRegionHistory().add(fractal.getDefaultRegion().clone());
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

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, distanceFunctions.getIds());
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner distanceFunctionSpinner = (Spinner) dialog.findViewById(R.id.distance_function_spinner);
        distanceFunctionSpinner.setAdapter(arrayAdapter);
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

    @Override
    protected void onPause() {
        super.onPause();
        view.cancelGenerators();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        view.saveInstanceState(new BundlePropertySet(outState));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            view.restoreInstanceState(new BundlePropertySet(savedInstanceState));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        view.cancelGenerators();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        PropertySet propertySet = new BundlePropertySet();
        view.saveInstanceState(propertySet);
        return propertySet;
    }

    private boolean tryReadingFrexDocIntent(Intent intent) {
        if (intent != null) {
            Uri data = intent.getData();
            if (data != null) {
                try {
                    readFrexDoc(data);
                    return true;
                } finally {
                    intent.setData(null);
                }
            }
        }
        return false;
    }

    private void readFrexDoc(final Uri frexDocUri) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InputStream stream;
                    try {
                        stream = openLocalContentStream(frexDocUri);
                    } catch (IOException e) {
                        try {
                            stream = openHttpContentStream(frexDocUri);
                        } catch (IOException e1) {
                            throw e;
                        }
                    }
                    if (stream != null) {
                        try {
                            readFrexDoc(stream, Long.toHexString(new Random().nextLong()));
                        } finally {
                            stream.close();
                        }
                    }
                } catch (IOException e) {
                    Toast.makeText(FrexActivity.this, getString(R.string.error_msg, e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
                } catch (Throwable t) {
                    Toast.makeText(FrexActivity.this, t.getClass().getSimpleName() + ": " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }).start();
    }

    private InputStream openLocalContentStream(Uri frexDocUri) throws FileNotFoundException {
        ContentResolver cr = getContentResolver();
        return cr.openInputStream(frexDocUri);
    }

    private InputStream openHttpContentStream(Uri frexDocUri) throws IOException {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(frexDocUri.toString());
        HttpResponse response = client.execute(request);
        response.getStatusLine().getStatusCode();
        return response.getEntity().getContent();
    }

    private void readFrexDoc(InputStream stream, String configName) throws IOException {
        Properties properties = new Properties();
        properties.load(stream);

        view.setConfigName(configName);
        view.restoreInstanceState(new DefaultPropertySet(properties));
        view.getRegionHistory().clear();
        view.getRegionHistory().add(view.getGeneratorConfig().getRegion().clone());
        view.recomputeAll();

        Registry<ColorScheme> colorSchemes = getColorSchemes();
        colorSchemes.add(view.getGeneratorConfig().getColorSchemeId(), view.getGeneratorConfig().getColorScheme());
    }

    private void saveFractal() {
        FrexIO frexIO = new FrexIO(this);

        File paramFile = frexIO.getUniqueParamFile(view.getFractalId().toLowerCase());
        if (!writeFrexDoc(paramFile)) {
            return;
        }

        File imageFile = new File(paramFile.getParent(), FrexIO.getFilenameWithoutExt(paramFile) + FrexIO.IMAGE_FILE_EXT);
        writeFrexImage(imageFile);

        updateMediaContentProvider(imageFile);

        view.setConfigName(FrexIO.getFilenameWithoutExt(paramFile));
        Toast.makeText(this, getString(R.string.fractal_saved), Toast.LENGTH_SHORT).show();
    }

    private boolean writeFrexDoc(File paramFile) {
        try {
            FileOutputStream stream = new FileOutputStream(paramFile);
            try {
                writeFrexDoc(stream);
            } finally {
                stream.close();
            }
            return true;
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_msg, e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void writeFrexDoc(OutputStream stream) {
        Properties properties = new Properties();
        view.saveInstanceState(new DefaultPropertySet(properties));
        properties.save(stream, "Generated by Frex on " + new Date());
    }

    private boolean writeFrexImage(File imageFile) {
        try {
            FileOutputStream out = new FileOutputStream(imageFile);
            try {
                view.createBitmap().compress(FrexIO.IMAGE_FILE_FORMAT, 100, out);
            } finally {
                out.close();
            }
            return true;
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_msg, e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void updateMediaContentProvider(File imageFile) {
        MediaScannerConnection.scanFile(this,
                                        new String[]{imageFile.getPath()},
                                        new String[]{"image/*"},
                                        new MediaScannerConnection.OnScanCompletedListener() {
                                            @Override
                                            public void onScanCompleted(String path, Uri uri) {

                                            }
                                        });
    }


    public static void showYesNoDialog(Context context, int titleId, String message,
                                       DialogInterface.OnClickListener yesListener,
                                       DialogInterface.OnClickListener noListener,
                                       DialogInterface.OnCancelListener cancelListener) {

        TextView textView = new TextView(context);
        textView.setSingleLine(false);
        textView.setPadding(10, 10, 10, 10);
        textView.setText(message);

        AlertDialog.Builder b = new AlertDialog.Builder(context);
        b.setTitle(titleId);
        b.setView(textView);
        b.setCancelable(true);
        if (yesListener != null) {
            b.setPositiveButton(android.R.string.yes, yesListener);
        }
        if (noListener != null) {
            b.setNegativeButton(android.R.string.no, noListener);
        }
        if (cancelListener != null) {
            b.setOnCancelListener(cancelListener);
        }
        b.show();
    }

    // On some Android 2.3 devices the options menu has a white background, but Frex uses white icons...
    //
    // This is a solution from http://stackoverflow.com/questions/6990524/white-background-on-optionsmenu-android
    //
    static void setMenuBackground(final Activity activity) {
        activity.getLayoutInflater().setFactory(new LayoutInflater.Factory() {
            @Override
            public View onCreateView(String name, Context context, AttributeSet attrs) {
                if (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")) {
                    try {
                        LayoutInflater inflater = activity.getLayoutInflater();
                        final View view = inflater.createView(name, null, attrs);
                        new Handler().post(new Runnable() {
                            public void run() {
                                view.setBackgroundColor(Color.argb(127, 0, 0, 0));
                            }
                        });
                        return view;
                    } catch (InflateException e) {
                        // :(
                    } catch (ClassNotFoundException e) {
                        // :(
                    }
                }
                return null;
            }
        });
    }
}
