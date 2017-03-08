package jp.hazuki.yuzubrowser.useragent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class UserAgentListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserAgentListDialog
                .newInstance(getIntent().getStringExtra(Intent.EXTRA_TEXT))
                .show(getSupportFragmentManager(), "ua");
    }
}
