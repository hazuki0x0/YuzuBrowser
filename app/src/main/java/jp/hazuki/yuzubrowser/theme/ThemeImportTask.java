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

package jp.hazuki.yuzubrowser.theme;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import jp.hazuki.yuzubrowser.BrowserApplication;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.FileUtils;

public class ThemeImportTask extends AsyncTaskLoader<ThemeImportTask.Result> {

    private boolean isLoading;
    private Uri fileUri;

    public ThemeImportTask(Context context, Uri uri) {
        super(context);
        fileUri = uri;
    }

    @Override
    public Result loadInBackground() {
        isLoading = true;
        OutputStream os;
        File root = new File(BrowserApplication.getExternalUserDirectory(), "theme");
        File tmpFolder = new File(root, Long.toString(System.currentTimeMillis()));

        try (InputStream is = getContext().getContentResolver().openInputStream(fileUri);
             ZipInputStream zis = new ZipInputStream(is)) {

            byte[] buffer = new byte[8192];
            int len;
            File file;
            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                file = new File(tmpFolder, entry.getName());
                if (entry.isDirectory()) {
                    if (file.exists()) {
                        FileUtils.deleteFile(file);
                    }
                    if (!file.mkdirs()) {
                        FileUtils.deleteFile(tmpFolder);
                        return new Result(false, getContext().getString(R.string.cant_create_folder));
                    }
                } else {
                    if (file.exists()) {
                        FileUtils.deleteFile(file);
                    } else if (!file.getParentFile().exists()) {
                        if (!file.getParentFile().mkdirs()) {
                            FileUtils.deleteFile(tmpFolder);
                            return new Result(false, getContext().getString(R.string.cant_create_folder));
                        }
                    }
                    os = new FileOutputStream(file);

                    while ((len = zis.read(buffer)) > 0) {
                        os.write(buffer, 0, len);
                    }
                    os.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            FileUtils.deleteFile(tmpFolder);
            return new Result(false, getContext().getString(R.string.theme_unknown_error));
        }

        File manifestFile = new File(tmpFolder, ThemeManifest.MANIFEST);
        ThemeManifest manifest;

        try {
            manifest = ThemeManifest.decodeManifest(manifestFile);
            if (manifest == null) {
                FileUtils.deleteFile(tmpFolder);
                return new Result(false, getContext().getString(R.string.theme_manifest_not_found));
            }
        } catch (ThemeManifest.IllegalManifestException e) {
            FileUtils.deleteFile(tmpFolder);
            int text;
            switch (e.getErrorType()) {
                default:
                case 0:
                    text = R.string.theme_unknown_error;
                    break;
                case 1:
                    text = R.string.theme_broken_manifest;
                    break;
                case 2:
                    text = R.string.theme_unknown_version;
                    break;
            }
            return new Result(false, getContext().getString(text));
        }

        String name = removeFileProhibitionWord(manifest.getName());
        if (TextUtils.isEmpty(name)) {
            FileUtils.deleteFile(tmpFolder);
            return new Result(false, getContext().getString(R.string.theme_broken_manifest));
        }

        File theme = new File(root, name);

        if (theme.exists()) {
            if (theme.isDirectory()) {
                File dest_manifest = new File(theme, ThemeManifest.MANIFEST);
                if (dest_manifest.exists()) {
                    try {
                        ThemeManifest dest = ThemeManifest.decodeManifest(dest_manifest);
                        if (dest != null) {
                            if (dest.getId().equals(manifest.getId())) {
                                if (dest.getVersion().equals(manifest.getVersion())) {
                                    FileUtils.deleteFile(tmpFolder);
                                    return new Result(false, getContext().getString(R.string.theme_installed_version));
                                }
                            } else {
                                FileUtils.deleteFile(tmpFolder);
                                return new Result(false, getContext().getString(R.string.theme_same_name, manifest.getName()));
                            }
                        }
                    } catch (ThemeManifest.IllegalManifestException e) {
                        e.printStackTrace();
                    }
                }
            }
            FileUtils.deleteFile(theme);
        }

        if (tmpFolder.renameTo(theme)) {
            return new Result(true, manifest.getName());
        }

        FileUtils.deleteFile(tmpFolder);
        return new Result(false, getContext().getString(R.string.theme_unknown_error));
    }

    private String removeFileProhibitionWord(String name) {
        return name.replace("\\", "")
                .replace("/", "")
                .replace(":", "")
                .replace("*", "")
                .replace("?", "")
                .replace("\"", "")
                .replace("<", "")
                .replace(">", "")
                .replace("|", "")
                .trim();
    }

    @Override
    protected void onStartLoading() {
        if (!isLoading)
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    public static class Result {
        private final boolean success;
        private final String message;

        private Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}
