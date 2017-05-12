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

package jp.hazuki.yuzubrowser.settings.preference.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ImeUtils;

public class SeekBarPreferenceController {
    private final Context mContext;
    private int mSeekMin;
    private int mSeekMax;
    private int mValue, mTempValue;
    private String comment;

    public SeekBarPreferenceController(Context context) {
        mContext = context;
    }

    public void setSeekMin(int i) {
        mSeekMin = i;
    }

    public void setSeekMax(int i) {
        mSeekMax = i;
    }

    public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.seekbar_preference, null);
        final TextView textView = (TextView) view.findViewById(R.id.countTextView);
        final SeekBar seekbar = (SeekBar) view.findViewById(R.id.seekBar);

        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mTempValue = seekBar.getProgress();
                textView.setText(String.valueOf(mTempValue + mSeekMin));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(String.valueOf(progress + mSeekMin));
            }
        });
        seekbar.setMax(mSeekMax - mSeekMin);
        seekbar.setProgress(mTempValue = mValue - mSeekMin);

        view.findViewById(R.id.prevImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTempValue > 0)
                    seekbar.setProgress(--mTempValue);
            }
        });
        view.findViewById(R.id.nextImageButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTempValue < mSeekMax - mSeekMin)
                    seekbar.setProgress(++mTempValue);
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText edittext = new EditText(mContext);
                edittext.setInputType(InputType.TYPE_CLASS_NUMBER);
                edittext.setText(String.valueOf(mTempValue + mSeekMin));

                new AlertDialog.Builder(mContext)
                        .setView(edittext)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    int value = Integer.parseInt(edittext.getText().toString(), 10);
                                    if (value >= mSeekMin && value <= mSeekMax)
                                        seekbar.setProgress(mTempValue = value - mSeekMin);
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                }
                                ImeUtils.hideIme(mContext.getApplicationContext(), edittext);
                                SeekBarPreferenceController.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        });

        if (!TextUtils.isEmpty(comment)) {
            TextView commentText = (TextView) view.findViewById(R.id.commentTextView);
            commentText.setVisibility(View.VISIBLE);
            commentText.setText(comment);
        }

        builder.setView(view);
    }

    public int getCurrentValue() {
        return mTempValue + mSeekMin;
    }

    public void setValue(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void onClick(DialogInterface dialog, int which) {
    }
}
