package jp.hazuki.yuzubrowser.action.item.startactivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.EditText;
import android.widget.Toast;

import java.net.URISyntaxException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.ImageUtils;
import jp.hazuki.yuzubrowser.utils.WebUtils;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;
import jp.hazuki.yuzubrowser.utils.appinfo.AppInfo;
import jp.hazuki.yuzubrowser.utils.appinfo.ApplicationListFragment;
import jp.hazuki.yuzubrowser.utils.appinfo.ShortCutListFragment;

public class StartActivityPreferenceActivity extends ThemeActivity implements StartActivityPreferenceFragment.OnActionListener,
        ApplicationListFragment.OnAppSelectListener, ShortCutListFragment.OnShortCutSelectListener {
    private static final int RESULT_REQUEST_APP = 0;
    private static final int RESULT_REQUEST_SHARE = 1;
    private static final int RESULT_REQUEST_OPEN_OTHER = 2;
    private Intent mCurrentIntent;
    private String mUrl = StartActivitySingleAction.REPLACE_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        Intent intent = getIntent();
        if (intent != null)
            mCurrentIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new StartActivityPreferenceFragment())
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.action_start_activity_edit_url).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final EditText edittext = new EditText(StartActivityPreferenceActivity.this);
                edittext.setSingleLine(true);
                edittext.setText(mUrl);

                new AlertDialog.Builder(StartActivityPreferenceActivity.this)
                        .setTitle(R.string.action_start_activity_edit_url)
                        .setView(edittext)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mUrl = edittext.getText().toString();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return false;
            }
        });
        menu.add(R.string.action_start_activity_edit_intent).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                final EditText edittext = new EditText(StartActivityPreferenceActivity.this);
                edittext.setSingleLine(true);
                if (mCurrentIntent != null)
                    edittext.setText(mCurrentIntent.toUri(0));

                new AlertDialog.Builder(StartActivityPreferenceActivity.this)
                        .setTitle(R.string.action_start_activity_edit_intent)
                        .setView(edittext)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    String str = edittext.getText().toString();
                                    Intent intent = Intent.parseUri(str, 0);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                } catch (URISyntaxException e) {
                                    ErrorReport.printAndWriteLog(e);
                                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void startAppListFragment(int type, Intent intent) {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("")
                .replace(R.id.container, ApplicationListFragment.newInstance(type, intent))
                .commit();
    }

    @Override
    public void openApplicationList() {
        Intent target = new Intent(Intent.ACTION_MAIN);
        target.addCategory(Intent.CATEGORY_LAUNCHER);
        startAppListFragment(RESULT_REQUEST_APP, target);
    }

    @Override
    public void openShortCutList() {
        getSupportFragmentManager().beginTransaction()
                .addToBackStack("")
                .replace(R.id.container, new ShortCutListFragment())
                .commit();
    }

    @Override
    public void openSharePage() {
        Intent query_intent = WebUtils.createShareWebIntent(mUrl, StartActivitySingleAction.REPLACE_TITLE);
        startAppListFragment(RESULT_REQUEST_SHARE, query_intent);
    }

    @Override
    public void openOther() {
        Intent query_intent = WebUtils.createOpenInOtherAppIntent(mUrl);
        startAppListFragment(RESULT_REQUEST_OPEN_OTHER, query_intent);
    }

    @Override
    public void onShortCutSelected(Intent intent) {
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onAppSelected(int type, AppInfo info) {
        Intent intent = new Intent();
        intent.setClassName(info.getPackageName(), info.getClassName());

        switch (type) {
            case RESULT_REQUEST_SHARE:
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, StartActivitySingleAction.REPLACE_URI);
                intent.putExtra(Intent.EXTRA_SUBJECT, StartActivitySingleAction.REPLACE_TITLE);
                break;
            case RESULT_REQUEST_OPEN_OTHER:
                WebUtils.createOpenInOtherAppIntent(intent, StartActivitySingleAction.REPLACE_URI);
                break;
        }

        Intent result = new Intent();
        result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
        result.putExtra(Intent.EXTRA_SHORTCUT_ICON, ImageUtils.getBitmap(info.getIcon()));
        setResult(RESULT_OK, result);
        finish();
    }
}
