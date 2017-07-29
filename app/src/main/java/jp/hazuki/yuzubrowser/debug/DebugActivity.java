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

package jp.hazuki.yuzubrowser.debug;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import jp.hazuki.yuzubrowser.BuildConfig;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.view.ActionStringActivity;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;

public class DebugActivity extends ThemeActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);
        setTitle("Debug mode");

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new ItemFragment())
                .commit();
    }


    public static class ItemFragment extends ListFragment {
        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            String[] list = {"file list", "activity list", "action json string", "action list json string", "environment"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, list);
            setListAdapter(adapter);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            switch (position) {
                case 0:
                    startActivity(new Intent(getActivity(), DebugFileListActivity.class));
                    break;
                case 1:
                    if (BuildConfig.DEBUG)
                        startActivity(new Intent(getActivity(), ActivityListActivity.class));
                    else
                        Toast.makeText(getActivity(), "This feature is only valid for debug builds", Toast.LENGTH_SHORT).show();
                    break;
                case 2: {
                    Intent intent = new Intent(getActivity(), ActionStringActivity.class);
                    intent.putExtra(ActionStringActivity.EXTRA_ACTIVITY, ActionStringActivity.ACTION_ACTIVITY);
                    startActivity(intent);
                }
                break;
                case 3: {
                    Intent intent = new Intent(getActivity(), ActionStringActivity.class);
                    intent.putExtra(ActionStringActivity.EXTRA_ACTIVITY, ActionStringActivity.ACTION_LIST_ACTIVITY);
                    startActivity(intent);
                }
                break;
                case 4:
                    startActivity(new Intent(getActivity(), EnvironmentActivity.class));
                    break;
            }
        }
    }
}
