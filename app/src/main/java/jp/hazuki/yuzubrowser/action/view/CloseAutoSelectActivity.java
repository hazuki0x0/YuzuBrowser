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

package jp.hazuki.yuzubrowser.action.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.utils.app.OnActivityResultListener;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class CloseAutoSelectActivity extends AppCompatActivity {

    private static final String DEFAULT = "0";
    private static final String INTENT = "1";
    private static final String WINDOW = "2";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        InnerFragment fragment = new InnerFragment();
        Bundle bundle = new Bundle();
        Intent intent = getIntent();
        if (intent != null) {
            bundle.putParcelable(DEFAULT, intent.getParcelableExtra(DEFAULT));
            bundle.putParcelable(INTENT, intent.getParcelableExtra(INTENT));
            bundle.putParcelable(WINDOW, intent.getParcelableExtra(WINDOW));
        }
        fragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.container);
        if (fragment instanceof InnerFragment) {
            Intent intent = ((InnerFragment) fragment).getReturnData();
            setResult(RESULT_OK, intent);
        } else {
            setResult(RESULT_CANCELED);
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class InnerFragment extends ListFragment {
        private static final int REQUEST_DEFAULT = 0;
        private static final int REQUEST_INTENT = 1;
        private static final int REQUEST_WINDOW = 2;

        private Action defaultAction;
        private Action intentAction;
        private Action windowAction;

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            defaultAction = getArguments().getParcelable(DEFAULT);
            intentAction = getArguments().getParcelable(INTENT);
            windowAction = getArguments().getParcelable(WINDOW);

            if (defaultAction == null)
                defaultAction = new Action();
            if (intentAction == null)
                intentAction = new Action();
            if (windowAction == null)
                windowAction = new Action();

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
            adapter.add(getString(R.string.pref_close_default));
            adapter.add(getString(R.string.pref_close_intent));
            adapter.add(getString(R.string.pref_close_window));
            setListAdapter(adapter);
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            ActionActivity.Builder builder = new ActionActivity.Builder(getActivity());
            switch (position) {
                case 0:
                    startActivityForResult(builder.setDefaultAction(defaultAction)
                                    .setTitle(R.string.pref_close_default)
                                    .create(),
                            REQUEST_DEFAULT);
                    break;
                case 1:
                    startActivityForResult(builder.setDefaultAction(intentAction)
                                    .setTitle(R.string.pref_close_intent)
                                    .create(),
                            REQUEST_INTENT);
                    break;
                case 2:
                    startActivityForResult(builder.setDefaultAction(windowAction)
                                    .setTitle(R.string.pref_close_window)
                                    .create(),
                            REQUEST_WINDOW);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown position:" + position);
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK) {
                switch (requestCode) {
                    case REQUEST_DEFAULT:
                        defaultAction = data.getParcelableExtra(ActionActivity.EXTRA_ACTION);
                        break;
                    case REQUEST_INTENT:
                        intentAction = data.getParcelableExtra(ActionActivity.EXTRA_ACTION);
                        break;
                    case REQUEST_WINDOW:
                        windowAction = data.getParcelableExtra(ActionActivity.EXTRA_ACTION);
                        break;
                }
            }
        }

        private Intent getReturnData() {
            Intent intent = new Intent();
            intent.putExtra(DEFAULT, (Parcelable) defaultAction);
            intent.putExtra(INTENT, (Parcelable) intentAction);
            intent.putExtra(WINDOW, (Parcelable) windowAction);
            return intent;
        }
    }

    public interface OnActionActivityResultListener {
        void onActionResult(Context context, Action defaultAction, Action intentAction, Action windowAction);
    }

    public static class Builder {
        private Context con;
        private OnActivityResultListener listener;

        public Builder(Context context) {
            con = context;
        }

        public Builder setListener(final OnActionActivityResultListener resultListener) {
            listener = new OnActivityResultListener() {
                @Override
                public void onActivityResult(Context context, int resultCode, Intent intent) {
                    if (resultCode == RESULT_OK) {
                        Action defaultAction = intent.getParcelableExtra(DEFAULT);
                        Action intentAction = intent.getParcelableExtra(INTENT);
                        Action windowAction = intent.getParcelableExtra(WINDOW);
                        resultListener.onActionResult(con, defaultAction, intentAction, windowAction);
                    }
                }
            };
            return this;
        }

        public StartActivityInfo getActivityInfo(Action defaultAction, Action intentAction, Action windowAction) {
            Intent intent = new Intent(con, CloseAutoSelectActivity.class);
            intent.putExtra(DEFAULT, (Parcelable) defaultAction);
            intent.putExtra(INTENT, (Parcelable) intentAction);
            intent.putExtra(WINDOW, (Parcelable) windowAction);

            return new StartActivityInfo(intent, listener);
        }
    }
}
