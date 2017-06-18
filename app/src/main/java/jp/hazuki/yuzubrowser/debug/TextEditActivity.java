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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import jp.hazuki.yuzubrowser.R;

public class TextEditActivity extends AppCompatActivity {
    private File mFile;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scroll_edittext);
        editText = (EditText) findViewById(R.id.editText);

        File file = (File) getIntent().getSerializableExtra(Intent.EXTRA_STREAM);
        if (file == null) {
            finish();
            return;
        }
        mFile = file;

        setTitle(file.getName());

        StringBuilder str_builder = new StringBuilder();
        try (Reader in = new BufferedReader(new FileReader(file))) {

            char buf[] = new char[1024];
            int n;
            while ((n = in.read(buf)) >= 0) {
                str_builder.append(buf, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        editText.setText(str_builder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Save").setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem arg0) {
                (new AlertDialog.Builder(TextEditActivity.this))
                        .setTitle("Confirm")
                        .setMessage("Save?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                try (Writer os = new BufferedWriter(new FileWriter(mFile))) {
                                    os.write(editText.getText().toString());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
