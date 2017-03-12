package jp.hazuki.yuzubrowser.resblock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import jp.hazuki.yuzubrowser.BuildConfig;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.resblock.checker.NormalChecker;
import jp.hazuki.yuzubrowser.resblock.data.EmptyImageData;

public class ResourceBlockListActivity extends AppCompatActivity {
    public static final String ACTION_BLOCK_IMAGE = BuildConfig.APPLICATION_ID + ".action_block_image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        Fragment fragment = new ResourceBlockListFragment();
        Bundle bundle = new Bundle();

        if (ACTION_BLOCK_IMAGE.equals(getIntent().getAction())) {
            String url = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            NormalChecker checker = new NormalChecker(new EmptyImageData(), url, false);
            bundle.putSerializable(ResourceBlockListFragment.CHECKER, checker);
        }

        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }


}
