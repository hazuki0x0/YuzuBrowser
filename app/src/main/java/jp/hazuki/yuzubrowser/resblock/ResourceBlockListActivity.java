package jp.hazuki.yuzubrowser.resblock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import jp.hazuki.yuzubrowser.R;

public class ResourceBlockListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new ResourceBlockListFragment())
                .commit();

    }


}
