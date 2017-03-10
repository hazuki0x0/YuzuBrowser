package jp.hazuki.yuzubrowser.backup;

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hazuki on 17/01/25.
 */

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
            case "instant-run":
                return false;
            default:
                return true;
        }
    }

    private boolean isBackupFile(File file) {
        switch (file.getName()) {
            case "WebViewChromiumPrefs.xml":
            case "permission.xml":
            case "last_url_2.dat":
            case "webhistory1.db":
            case "webhistory1.db-journal":
            case "searchsuggest.db":
            case "searchsuggest.db-journal":
            case "downloadinfolist1.db":
            case "downloadinfolist1.db-journal":
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
