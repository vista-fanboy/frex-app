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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import android.provider.MediaStore;
import android.util.AttributeSet;
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
 * Frex' main activity.
 *
 * @author Norman Fomferra
 */
public class FrexActivity extends Activity {

    public static final boolean PRE_SDK14 = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH;

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
            /*
            // Later, use the NAVIGATION_MODE_LIST to open the Frex Gallery activity, etc.
            getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item,
                    new String[]{"A", "B", "C"});
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            getActionBar().setListNavigationCallbacks(arrayAdapter, new ActionBar.OnNavigationListener() {
                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                    return false;
                }
            });

            // Later, use the home button to go to the last saved position
            getActionBar().setHomeButtonEnabled(true);
            */
        }

        view = new FractalView(this);
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
            case R.id.about_frex:
                showDialog(R.id.about_frex);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK
                && requestCode == R.id.manage_fractals
                && data.getAction().equals(Intent.ACTION_VIEW)) {

            final Uri imageUri = data.getData();

            File imageFile = new File(imageUri.getPath());
            File paramFile = new File(imageFile.getParent(), FrexIO.getFilenameWithoutExt(imageFile) + FrexIO.PARAM_FILE_EXT);
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

            view.setConfigName(FrexIO.getFilenameWithoutExt(imageFile));
            view.restoreInstanceState(new DefaultPropertySet(properties));
            view.recomputeAll();
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

    Uri saveTemporaryImage() {
        final String url = MediaStore.Images.Media.insertImage(getContentResolver(),
                                                               view.getImage().createBitmap(),
                                                               "Frex Image",
                                                               "Fractal image generated by Frex");
        if (url != null) {
            // Toast.makeText(FrexActivity.this, getString(R.string.image_saved_msg, url), Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse(url);
            temporaryImageFiles.add(uri);
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

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        if (id == R.id.manage_fractals) {
            return createManageDialog();
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
        if (id == R.id.colors) {
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
        //colorTableSpinner.setAdapter(new ImageArrayAdapter(this, R.layout.spinner_image_view, COLOR_TABLE_ICONS));
        colorTableSpinner.setAdapter(new ImageArrayAdapter(this, 0, COLOR_TABLE_ICONS));
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
                view.setFractalId(newFractalId);
                view.setIterMax(iterMax);
                view.setDecoratedFractal(decoratedFractal.isChecked());
                view.setJuliaModeFractal(juliaModeCheckBox.isChecked());
                if (!oldFractalId.equals(newFractalId)) {
                    if (oldConfigName.contains(oldFractalId.toLowerCase())) {
                        view.setConfigName(newFractalId.toLowerCase());
                    }
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

    private Dialog createAboutDialog() {
        String versionName;
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "?";
            Log.w(TAG, e);
        }

        TextView textView = new TextView(this);
        textView.setSingleLine(false);
        textView.setPadding(5, 5, 5, 5);
        textView.setText(getString(R.string.about_frex_text, versionName));
        textView.setLinksClickable(true);

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.about_frex);
        b.setIcon(R.drawable.frex_logo);
        b.setView(textView);

        return b.create();
    }

    private Dialog createManageDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.manage_fractals);
        dialog.setTitle(getString(R.string.manage_fractals));
        registerForContextMenu(dialog.findViewById(R.id.fractal_gallery));

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

    private void saveFractal() {
        FrexIO frexIO = new FrexIO(this);
        File paramFile = frexIO.getUniqueParamFile(view.getFractalId().toLowerCase());
        File imageFile = new File(paramFile.getParent(), FrexIO.getFilenameWithoutExt(paramFile) + FrexIO.IMAGE_FILE_EXT);
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
            FileOutputStream out = new FileOutputStream(imageFile);
            try {
                view.createBitmap().compress(FrexIO.IMAGE_FILE_FORMAT, 100, out);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_msg, e.getLocalizedMessage()), Toast.LENGTH_SHORT).show();
        }

        MediaScannerConnection.scanFile(this,
                                        new String[]{imageFile.getPath()},
                                        new String[]{"image/*"},
                                        new MediaScannerConnection.OnScanCompletedListener() {
                                            @Override
                                            public void onScanCompleted(String path, Uri uri) {

                                            }
                                        });

        view.setConfigName(FrexIO.getFilenameWithoutExt(paramFile));
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

    public static void showYesNoDialog(Context context, int titleId, String message,
                                       DialogInterface.OnClickListener yesListener,
                                       DialogInterface.OnClickListener noListener,
                                       DialogInterface.OnCancelListener cancelListener) {

        TextView textView = new TextView(context);
        textView.setSingleLine(false);
        textView.setPadding(5, 5, 5, 5);
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
