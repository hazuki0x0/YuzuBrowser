package jp.hazuki.yuzubrowser.debug;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import jp.hazuki.yuzubrowser.R;

public class ActivityListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);
        setTitle("Debug mode");

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new ActivityListFragment())
                .commit();
    }

    public static class ActivityListFragment extends ListFragment {
        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            try {
                ActivityInfo[] activities = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), PackageManager.GET_ACTIVITIES).activities;
                setListAdapter(new ArrayAdapter<ActivityInfo>(getActivity(), 0, activities) {
                    @NonNull
                    @Override
                    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                        if (convertView == null) {
                            convertView = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_1, parent, false);
                        }
                        ActivityInfo info = getItem(position);

                        if (info != null) {
                            String name = info.name;
                            ((TextView) convertView.findViewById(android.R.id.text1)).setText(name.substring(name.lastIndexOf('.') + 1));
                        }

                        return convertView;
                    }
                });
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            Intent intent = new Intent();
            intent.setClassName(getActivity(), ((ActivityInfo) l.getAdapter().getItem(position)).name);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(), "This activity can't open.", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
