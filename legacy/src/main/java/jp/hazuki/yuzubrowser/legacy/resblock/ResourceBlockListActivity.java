package jp.hazuki.yuzubrowser.legacy.resblock;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import jp.hazuki.yuzubrowser.legacy.Constants;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.resblock.checker.NormalChecker;
import jp.hazuki.yuzubrowser.legacy.resblock.data.EmptyImageData;
import jp.hazuki.yuzubrowser.legacy.utils.app.ThemeActivity;

public class ResourceBlockListActivity extends ThemeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        Fragment fragment = new ResourceBlockListFragment();
        Bundle bundle = new Bundle();

        if (Constants.intent.ACTION_BLOCK_IMAGE.equals(getIntent().getAction())) {
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
