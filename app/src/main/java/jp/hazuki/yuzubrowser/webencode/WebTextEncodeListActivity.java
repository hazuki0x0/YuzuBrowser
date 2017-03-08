package jp.hazuki.yuzubrowser.webencode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class WebTextEncodeListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebTextEncodeListDialog
                .newInstance(getIntent().getStringExtra(Intent.EXTRA_TEXT))
                .show(getSupportFragmentManager(), "list");
    }

}
