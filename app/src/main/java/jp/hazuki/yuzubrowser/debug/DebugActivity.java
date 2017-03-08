package jp.hazuki.yuzubrowser.debug;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.view.ActionStringActivity;

public class DebugActivity extends AppCompatActivity {
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
                    startActivity(new Intent(getActivity(), ActivityListActivity.class));
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
