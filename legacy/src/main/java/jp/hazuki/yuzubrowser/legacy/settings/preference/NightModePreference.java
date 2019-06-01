/*
 * Copyright (C) 2017-2019 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.settings.preference;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import jp.hazuki.yuzubrowser.core.utility.utils.ColorFilterUtils;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.ui.preference.YuzuPreferenceDialog;
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs;

public class NightModePreference extends DialogPreference {

    public NightModePreference(Context context) {
        this(context, null);
    }

    public NightModePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setTitle(R.string.pref_night_mode_settings);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    public static class SettingDialog extends YuzuPreferenceDialog {

        private SeekBar temperature;
        private SeekBar brightness;

        @Override
        protected View onCreateDialogView(Context context) {
            View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_night_mode, null);
            temperature = v.findViewById(R.id.temperatureSeekBar);
            brightness = v.findViewById(R.id.brightnessSeekBar);
            final View preview = v.findViewById(R.id.previewView);

            temperature.setProgress(AppPrefs.night_mode_color.get() - 3000);
            brightness.setProgress(AppPrefs.night_mode_bright.get());


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

            int[] rgb = ColorFilterUtils.colorTemperatureToRGB(AppPrefs.night_mode_color.get());
            float bright = AppPrefs.night_mode_bright.get() / 100f;
            preview.setBackgroundColor(Color.argb(
                    0xff,
                    (int) (rgb[0] * bright + 0.5f),
                    (int) (rgb[1] * bright + 0.5f),
                    (int) (rgb[2] * bright + 0.5f)));

            return v;
        }

        @Override
        public void onDialogClosed(boolean positiveResult) {
            if (positiveResult) {
                AppPrefs.night_mode_color.set(temperature.getProgress() + 3000);
                AppPrefs.night_mode_bright.set(brightness.getProgress());
                AppPrefs.commit(getContext(), AppPrefs.night_mode_color, AppPrefs.night_mode_bright);
            }
        }

        public static YuzuPreferenceDialog newInstance(Preference preference) {
            return newInstance(new SettingDialog(), preference);
        }
    }
}
