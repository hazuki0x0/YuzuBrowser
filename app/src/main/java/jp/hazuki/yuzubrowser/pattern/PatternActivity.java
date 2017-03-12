package jp.hazuki.yuzubrowser.pattern;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.pattern.action.BlockPatternAction;
import jp.hazuki.yuzubrowser.pattern.action.OpenOthersPatternAction;
import jp.hazuki.yuzubrowser.utils.WebUtils;

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
                settingOpenOthersAction(checker, null);
                break;
            case PatternAction.WEB_SETTING:
                //settingWebSettingAction(checker, null);
                getWebSettingDialog(checker).show(getSupportFragmentManager(), "web");
                break;
            case PatternAction.BLOCK:
                settingBlockAction(checker, null);
                break;
        }
    }

    protected abstract T makeActionChecker(PatternAction pattern_action, View header_view);

    protected void settingOpenOthersAction(final T checker, final View header_view) {
        ViewGroup view = (ViewGroup) getLayoutInflater().inflate(R.layout.pattern_add_open, null);
        if (header_view != null)
            view.addView(header_view, 0);
        ListView view_listView = (ListView) view.findViewById(R.id.listView);

        final PackageManager pm = getPackageManager();
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse((WebUtils.maybeContainsUrlScheme(mDefaultUrl)) ? mDefaultUrl : "http://" + mDefaultUrl));
        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flag = PackageManager.MATCH_ALL;
        } else {
            flag = PackageManager.MATCH_DEFAULT_ONLY;
        }
        final List<ResolveInfo> open_app_list = pm.queryIntentActivities(intent, flag);
        Collections.sort(open_app_list, new ResolveInfo.DisplayNameComparator(pm));

        view_listView.setAdapter(new ArrayAdapter<ResolveInfo>(getApplicationContext(), 0, open_app_list) {
            private final int app_icon_size = (int) getResources().getDimension(android.R.dimen.app_icon_size);

            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.image_text_list_item, null);
                    ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);

                    ViewGroup.LayoutParams params = imageView.getLayoutParams();
                    params.height = app_icon_size;
                    params.width = app_icon_size;
                    imageView.setLayoutParams(params);
                }

                ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
                TextView textView = (TextView) convertView.findViewById(R.id.textView);

                if (position == 0) {
                    imageView.setImageDrawable(null);
                    textView.setText(getString(R.string.pattern_open_app_list));
                } else {
                    ResolveInfo item = getItem(position);
                    imageView.setImageDrawable(item.loadIcon(pm));
                    textView.setText(item.loadLabel(pm));
                }

                return convertView;
            }

            @Override
            public ResolveInfo getItem(int position) {
                return super.getItem(position - 1);
            }

            @Override
            public int getCount() {
                return super.getCount() + 1;
            }
        });

        AlertDialog.Builder dialog_builder = new AlertDialog.Builder(this)
                .setTitle(R.string.pattern_open_others)
                .setView(view)
                .setNegativeButton(android.R.string.cancel, null);

        if (checker != null)
            dialog_builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    T new_checker = makeActionChecker(checker.getAction(), header_view);
                    if (new_checker != null) {
                        mManager.set(checker, new_checker);
                        saveAndNotifyDataSetChanged();
                    }
                }
            });

        final AlertDialog dialog = dialog_builder.show();

        view_listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                OpenOthersPatternAction pattern;
                if (position == 0) {
                    pattern = new OpenOthersPatternAction(OpenOthersPatternAction.TYPE_APP_LIST);
                } else {
                    ResolveInfo item = open_app_list.get(position - 1);
                    intent.setClassName(item.activityInfo.packageName, item.activityInfo.name);
                    pattern = new OpenOthersPatternAction(intent);
                }
                T new_checker = makeActionChecker(pattern, header_view);
                if (new_checker != null) {
                    if (checker == null)
                        mManager.add(new_checker);
                    else
                        mManager.set(checker, new_checker);
                    saveAndNotifyDataSetChanged();
                    dialog.dismiss();
                }
            }
        });

        view_listView.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    OpenOthersPatternAction new_pattern = new OpenOthersPatternAction(OpenOthersPatternAction.TYPE_APP_CHOOSER);
                    T new_checker = makeActionChecker(new_pattern, header_view);
                    if (new_checker != null) {
                        if (checker == null)
                            mManager.add(new_checker);
                        else
                            mManager.set(checker, new_checker);
                        saveAndNotifyDataSetChanged();
                        dialog.dismiss();
                    }
                }
                return true;
            }
        });
    }

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

    protected void settingWebSettingAction(final T checker, final View header_view) {

    }

    protected void settingBlockAction(final T checker, final View header_view) {
        if (checker != null)
            return;
        BlockPatternAction action = new BlockPatternAction();
        T new_checker = makeActionChecker(action, header_view);
        mManager.add(new_checker);
        saveAndNotifyDataSetChanged();
    }
}
