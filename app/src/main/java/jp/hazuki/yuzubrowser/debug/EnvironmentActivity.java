package jp.hazuki.yuzubrowser.debug;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.EnvironmentUtils;
import jp.hazuki.yuzubrowser.utils.ErrorReport;

public class EnvironmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.environment_activity);
        setTitle("Environment");

        TextView externalStorageDirTextView = (TextView) findViewById(R.id.externalStorageDirTextView);
        TextView externalStorageStateTextView = (TextView) findViewById(R.id.externalStorageStateTextView);
        TextView externalFilesDirTextView = (TextView) findViewById(R.id.externalFilesDirTextView);
        TextView estimatedExternalFilesDirTextView = (TextView) findViewById(R.id.estimatedExternalFilesDirTextView);

        try {
            File externalStorageDir = Environment.getExternalStorageDirectory();
            externalStorageDirTextView.setText((externalStorageDir == null) ? "null" : externalStorageDir.getCanonicalPath());
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }

        String externalStorageState = Environment.getExternalStorageState();
        externalStorageStateTextView.setText((externalStorageState == null) ? "null" : externalStorageState);

        try {
            File externalFilesDir = getExternalFilesDir(null);
            externalFilesDirTextView.setText((externalFilesDir == null) ? "null" : externalFilesDir.getCanonicalPath());
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }

        for (String str : EnvironmentUtils.getExternalStoragesFromSystemFile()) {
            estimatedExternalFilesDirTextView.append(str);
            estimatedExternalFilesDirTextView.append("\n");
        }
    }

}
