/*
 * Copyright (C) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.settings.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.settings.preference.common.CustomDialogPreference;
import jp.hazuki.yuzubrowser.utils.ColorFilterUtils;

public class NightModePreference extends CustomDialogPreference {

    public NightModePreference(Context context) {
        super(context);
    }

    public NightModePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_night_mode, null);
        final SeekBar temperature = (SeekBar) v.findViewById(R.id.temperatureSeekBar);
        final SeekBar brightness = (SeekBar) v.findViewById(R.id.brightnessSeekBar);
        final View preview = v.findViewById(R.id.previewView);

        temperature.setProgress(AppData.night_mode_color.get() - 3000);
        brightness.setProgress(AppData.night_mode_bright.get());


        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int[] rgb = ColorFilterUtils.colorTemperatureToRGB(temperature.getProgress() + 3000);
                float bright = brightness.getProgress() / 100f;
                preview.setBackgroundColor(Color.argb(
                        0xff,
                        (int) (rgb[0] * bright + 0.5f),
                        (int) (rgb[1] * bright + 0.5f),
                        (int) (rgb[2] * bright + 0.5f)));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        };
        temperature.setOnSeekBarChangeListener(listener);
        brightness.setOnSeekBarChangeListener(listener);

        int[] rgb = ColorFilterUtils.colorTemperatureToRGB(AppData.night_mode_color.get());
        float bright = AppData.night_mode_bright.get() / 100f;
        preview.setBackgroundColor(Color.argb(
                0xff,
                (int) (rgb[0] * bright + 0.5f),
                (int) (rgb[1] * bright + 0.5f),
                (int) (rgb[2] * bright + 0.5f)));

        builder.setView(v)
                .setTitle(R.string.pref_night_mode_settings)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AppData.night_mode_color.set(temperature.getProgress() + 3000);
                        AppData.night_mode_bright.set(brightness.getProgress());
                        AppData.commit(getContext(), AppData.night_mode_color, AppData.night_mode_bright);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
    }
}
