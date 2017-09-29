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

package jp.hazuki.yuzubrowser.settings.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.preference.Preference;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;

import jp.hazuki.yuzubrowser.BuildConfig;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.AppUtils;
import jp.hazuki.yuzubrowser.utils.ClipboardUtils;
import jp.hazuki.yuzubrowser.utils.FileUtils;

public class AboutFragment extends YuzuPreferenceFragment {

    @Override
    public void onCreateYuzuPreferences(@Nullable Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_about);
        Preference version = findPreference("version");
        version.setOnPreferenceClickListener(preference -> {
            ClipboardUtils.setClipboardText(getActivity(), AppUtils.getVersionDeviceInfo(getActivity()));
            return true;
        });

        version.setSummary(BuildConfig.VERSION_NAME);
        findPreference("build").setSummary(BuildConfig.GIT_HASH);
        findPreference("build_time").setSummary(BuildConfig.BUILD_TIME);

        findPreference("osl").setOnPreferenceClickListener(preference -> {
            new OpenSourceLicenseDialog().show(getChildFragmentManager(), "osl");
            return true;
        });

        findPreference("delete_log").setOnPreferenceClickListener(preference -> {
            new DeleteLogDialog().show(getChildFragmentManager(), "delete");
            return true;
        });
    }

    public static class OpenSourceLicenseDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            WebView webView = new WebView(getActivity());
            webView.loadUrl("file:///android_asset/licenses.html");
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.open_source_license)
                    .setView(webView);
            return builder.create();
        }
    }

    public static class DeleteLogDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.pref_delete_all_logs)
                    .setMessage(R.string.pref_delete_log_mes)
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        File file = new File(getActivity().getExternalFilesDir(null), "./error_log/");
                        if (!file.exists()) {
                            Toast.makeText(getActivity(), R.string.succeed, Toast.LENGTH_SHORT).show();
                        } else if (FileUtils.deleteFile(file)) {
                            Toast.makeText(getActivity(), R.string.succeed, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), R.string.failed, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(android.R.string.no, null);
            return builder.create();
        }
    }
}
