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

package jp.hazuki.yuzubrowser.legacy.resblock;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import jp.hazuki.yuzubrowser.legacy.Constants;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.resblock.checker.NormalChecker;
import jp.hazuki.yuzubrowser.legacy.resblock.data.EmptyImageData;
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity;

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
