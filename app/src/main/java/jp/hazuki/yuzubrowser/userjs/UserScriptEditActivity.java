package jp.hazuki.yuzubrowser.userjs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.EditText;

import jp.hazuki.yuzubrowser.R;

public class UserScriptEditActivity extends AppCompatActivity {
    public static final String EXTRA_USERSCRIPT = "UserScriptEditActivity.extra.userscript";

    private UserScript mUserScript;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userjs_edit_activity);
        editText = (EditText) findViewById(R.id.editText);

        Intent intent = getIntent();
        if (intent == null)
            throw new NullPointerException("intent is null");
        mUserScript = intent.getParcelableExtra(EXTRA_USERSCRIPT);
        if (mUserScript == null) {
            mUserScript = new UserScript();
        } else {
            setTitle(mUserScript.getName());
            editText.setText(mUserScript.getData());
        }
    }

    @Override
    public void onBackPressed() {
        showSaveDialog(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(R.string.userjs_save).setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                showSaveDialog(false);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void showSaveDialog(final boolean orFinish) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.confirm)
                .setMessage(R.string.userjs_save_confirm)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        mUserScript.setData(editText.getText().toString());
                        Intent intent = new Intent();
                        intent.putExtra(EXTRA_USERSCRIPT, mUserScript);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                })
                .setNeutralButton(android.R.string.cancel, null);
        if (orFinish)
            builder.setNegativeButton(R.string.not_save, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        builder.show();
    }
}
