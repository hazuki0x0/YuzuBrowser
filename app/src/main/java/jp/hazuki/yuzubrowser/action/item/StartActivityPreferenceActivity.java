package jp.hazuki.yuzubrowser.action.item;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.net.URISyntaxException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.WebUtils;
import jp.hazuki.yuzubrowser.utils.view.applist.ApplicationListActivity;

public class StartActivityPreferenceActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final int RESULT_REQUEST_APP = 0;
    private static final int RESULT_REQUEST_PICK_SHORTCUT = 1;
    private static final int RESULT_REQUEST_CREATE_SHORTCUT = 2;
    private static final int RESULT_REQUEST_SHARE = 3;
    private static final int RESULT_REQUEST_OPEN_OTHER = 4;
    private Intent mCurrentIntent;
    private String mUrl = StartActivitySingleAction.REPLACE_URI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(this);

        Intent intent = getIntent();
        if (intent != null)
            mCurrentIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);

        ListAdapter adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.action_start_activity_template));
        listView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0://Application
            {
                Intent query_intent = new Intent(Intent.ACTION_MAIN);
                query_intent.addCategory(Intent.CATEGORY_LAUNCHER);
                Intent intent = new Intent(getApplicationContext(), ApplicationListActivity.class);
                intent.putExtra(Intent.EXTRA_INTENT, query_intent);
                intent.putExtra(Intent.EXTRA_TITLE, getResources().getStringArray(R.array.action_start_activity_template)[0]);
                startActivityForResult(intent, RESULT_REQUEST_APP);
            }
            break;
            case 1://Shortcut
            {
                Intent intent = new Intent(Intent.ACTION_PICK_ACTIVITY);
                intent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
                intent.putExtra(Intent.EXTRA_TITLE, getResources().getStringArray(R.array.action_start_activity_template)[1]);
                startActivityForResult(intent, RESULT_REQUEST_PICK_SHORTCUT);
            }
            break;
            case 2://Share page
            {
                Intent query_intent = WebUtils.createShareWebIntent(mUrl, StartActivitySingleAction.REPLACE_TITLE);
                Intent intent = new Intent(getApplicationContext(), ApplicationListActivity.class);
                intent.putExtra(Intent.EXTRA_INTENT, query_intent);
                intent.putExtra(Intent.EXTRA_TITLE, getResources().getStringArray(R.array.action_start_activity_template)[2]);
                startActivityForResult(intent, RESULT_REQUEST_SHARE);
            }
            break;
            case 3://Open in other app
            {
                Intent query_intent = WebUtils.createOpenInOtherAppIntent(mUrl);
                Intent intent = new Intent(getApplicationContext(), ApplicationListActivity.class);
                intent.putExtra(Intent.EXTRA_INTENT, query_intent);
                intent.putExtra(Intent.EXTRA_TITLE, getResources().getStringArray(R.array.action_start_activity_template)[3]);
                startActivityForResult(intent, RESULT_REQUEST_OPEN_OTHER);
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null)
            return;

        switch (requestCode) {
            case RESULT_REQUEST_APP: {
                Intent result = new Intent();
                result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, data.getParcelableExtra(Intent.EXTRA_INTENT));
                setResult(RESULT_OK, result);
                finish();
            }
            break;
            case RESULT_REQUEST_PICK_SHORTCUT:
                startActivityForResult(data, RESULT_REQUEST_CREATE_SHORTCUT);
                break;
            case RESULT_REQUEST_CREATE_SHORTCUT:
                setResult(RESULT_OK, data);
                finish();
                break;
            case RESULT_REQUEST_SHARE: {
                Intent result = new Intent();
                Intent intent = data.getParcelableExtra(Intent.EXTRA_INTENT);
                WebUtils.createShareWebIntent(intent, StartActivitySingleAction.REPLACE_URI);
                result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
                setResult(RESULT_OK, result);
                finish();
            }
            break;
            case RESULT_REQUEST_OPEN_OTHER: {
                Intent result = new Intent();
                Intent intent = data.getParcelableExtra(Intent.EXTRA_INTENT);
                WebUtils.createOpenInOtherAppIntent(intent, StartActivitySingleAction.REPLACE_URI);
                result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
                setResult(RESULT_OK, result);
                finish();
            }
            break;
        }
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
}
