package jp.hazuki.yuzubrowser.pattern;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.pattern.action.BlockPatternAction;

public abstract class PatternActivity<T extends PatternChecker> extends AppCompatActivity implements View.OnClickListener, OnItemClickListener, OnItemLongClickListener {

    private PatternManager<T> mManager;
    private String mDefaultUrl;

    private LinearLayout rootLayout;
    private ListView listView;
    private Button openOthersButton;
    private Button webSettingButton;
    private Button blockButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pattern_list_activity);

        rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
        listView = (ListView) findViewById(R.id.listView);
        openOthersButton = (Button) findViewById(R.id.openOthersButton);
        webSettingButton = (Button) findViewById(R.id.webSettingButton);
        blockButton = (Button) findViewById(R.id.blockButton);

        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        openOthersButton.setOnClickListener(this);
        webSettingButton.setOnClickListener(this);
        blockButton.setOnClickListener(this);
    }

    protected void setPatternManager(PatternManager<T> manager) {
        mManager = manager;

        listView.setAdapter(new ArrayAdapter<T>(getApplicationContext(), 0, mManager.getList()) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(android.R.layout.simple_list_item_2, null);
                T item = getItem(position);
                ((TextView) convertView.findViewById(android.R.id.text1)).setText(item.getTitle(getApplicationContext()));
                ((TextView) convertView.findViewById(android.R.id.text2)).setText(item.getActionTitle(getApplicationContext()));
                return convertView;
            }
        });
    }

    protected void addHeaderView(View view) {
        rootLayout.addView(view, 0);
    }

    @Override
    public void onClick(View v) {
        if (v == openOthersButton)
            onClick(PatternAction.OPEN_OTHERS, null);
        else if (v == webSettingButton)
            onClick(PatternAction.WEB_SETTING, null);
        else if (v == blockButton)
            onClick(PatternAction.BLOCK, null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        T checker = mManager.get(position);
        onClick(checker.getAction().getTypeId(), checker);
    }

    protected void saveAndNotifyDataSetChanged() {
        mManager.save(getApplicationContext());
        ((ArrayAdapter<?>) listView.getAdapter()).notifyDataSetChanged();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.confirm_delete_action)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mManager.remove(position);
                        saveAndNotifyDataSetChanged();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        return true;
    }

    protected void setDefaultUrl(String url) {
        mDefaultUrl = url;
    }

    protected void onClick(int id, T checker) {
        switch (id) {
            case PatternAction.OPEN_OTHERS:
                getOpenOtherDialog(checker).show(getSupportFragmentManager(), "open");
                break;
            case PatternAction.WEB_SETTING:
                getWebSettingDialog(checker).show(getSupportFragmentManager(), "web");
                break;
            case PatternAction.BLOCK:
                settingBlockAction(checker, null);
                break;
        }
    }

    public abstract T makeActionChecker(PatternAction pattern_action, View header_view);

    protected int getPosition(T checker) {
        return mManager.getIndex(checker);
    }

    protected void add(int id, T newChecker) {
        if (id >= 0) {
            mManager.set(id, newChecker);
        } else {
            mManager.add(newChecker);
        }
        saveAndNotifyDataSetChanged();
    }

    protected abstract DialogFragment getWebSettingDialog(T checker);

    protected abstract DialogFragment getOpenOtherDialog(T checker);

    protected void settingBlockAction(final T checker, final View header_view) {
        if (checker != null)
            return;
        BlockPatternAction action = new BlockPatternAction();
        T new_checker = makeActionChecker(action, header_view);
        mManager.add(new_checker);
        saveAndNotifyDataSetChanged();
    }
}
