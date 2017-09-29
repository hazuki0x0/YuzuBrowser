/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.action.item;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class AutoPageScrollAction extends SingleAction implements Parcelable {
    private static final String FIELD_NAME_SPEED = "0";
    private int mScrollSpeed = 40;

    public AutoPageScrollAction(int id, JsonParser parser) throws IOException {
        super(id);
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (FIELD_NAME_SPEED.equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
                    mScrollSpeed = parser.getIntValue();
                    continue;
                }
                parser.skipChildren();
            }
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        generator.writeNumberField(FIELD_NAME_SPEED, mScrollSpeed);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mScrollSpeed);
    }

    protected AutoPageScrollAction(Parcel source) {
        super(source.readInt());
        mScrollSpeed = source.readInt();
    }

    public static final Creator<AutoPageScrollAction> CREATOR = new Creator<AutoPageScrollAction>() {
        @Override
        public AutoPageScrollAction createFromParcel(Parcel source) {
            return new AutoPageScrollAction(source);
        }

        @Override
        public AutoPageScrollAction[] newArray(int size) {
            return new AutoPageScrollAction[size];
        }
    };

    @Override
    public StartActivityInfo showMainPreference(ActionActivity context) {
        return showSubPreference(context);
    }

    @Override
    public StartActivityInfo showSubPreference(final ActionActivity context) {
        View view = LayoutInflater.from(context).inflate(R.layout.action_auto_scroll, null);
        final EditText editText = view.findViewById(R.id.editText);
        editText.setText(String.valueOf(mScrollSpeed));
        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int y = 0;

                        try {
                            y = Integer.parseInt(editText.getText().toString());
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                        }

                        if (y == 0) {
                            Toast.makeText(context.getApplicationContext(), R.string.action_auto_scroll_speed_zero, Toast.LENGTH_SHORT).show();
                            showSubPreference(context);
                            return;
                        }

                        mScrollSpeed = y;
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        return null;
    }

    public int getScrollSpeed() {
        return mScrollSpeed;
    }
}
