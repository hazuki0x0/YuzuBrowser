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

package jp.hazuki.yuzubrowser.legacy.backup;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BackupManager {

    private final File root;

    public BackupManager(Context context) {

        root = new File(context.getApplicationInfo().dataDir);
    }

    public List<File> getBackUpFiles() {
        Search search = new Search(root);
        return search.getBackupFiles();
    }

    public File getRoot() {
        return root;
    }

    public void cleanFiles() {
        clean(root);
    }

    private void clean(File file) {
        if (!file.exists()) return;
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (isBackUpFolder(child)) {
                    clean(child);
                }
            }
        } else {
            if (isBackupFile(file)) {
                file.delete();
            }
        }
    }

    private boolean isBackUpFolder(File folder) {
        switch (folder.getName()) {
            case "cache":
            case "app_webview":
            case "app_textures":
            case "app_public_html":
            case "app_appcache":
            case "app_tabs":
            case "app_favicon":
            case "instant-run":
                return false;
            default:
                return true;
        }
    }

    private boolean isBackupFile(File file) {
        String name = file.getName();
        if (name.endsWith(".db-journal") || name.endsWith(".db-shm") || name.endsWith(".db-wal")) {
            return false;
        }
        switch (file.getName()) {
            case "WebViewChromiumPrefs.xml":
            case "permission.xml":
            case "last_url_2.dat":
            case "webhistory1.db":
            case "searchsuggest.db":
            case "downloadinfolist1.db":
            case "speeddial1.db":
            case "download.db":
                return false;
            default:
                return true;
        }
    }

    private class Search {
        private final List<File> files;

        Search(File root) {
            files = new ArrayList<>();
            searchFile(root);
        }

        private List<File> getBackupFiles() {
            return files;
        }

        private void searchFile(File file) {
            if (!file.exists()) return;
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    if (isBackUpFolder(child))
                        searchFile(child);
                }
            } else {
                if (isBackupFile(file)) {
                    files.add(file);
                }
            }
        }

    }
}
