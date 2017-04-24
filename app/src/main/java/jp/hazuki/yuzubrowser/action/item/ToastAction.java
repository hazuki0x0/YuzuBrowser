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

package jp.hazuki.yuzubrowser.action.item;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Parcel;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class ToastAction extends SingleAction {
    private static final String FIELD_TEXT = "0";

    private String text;

    public ToastAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (FIELD_TEXT.equals(parser.getCurrentName())) {
                    if (parser.nextToken() == JsonToken.VALUE_STRING)
                        text = parser.getText();
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
        generator.writeStringField(FIELD_TEXT, text);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(text);
    }

    protected ToastAction(Parcel source) {
        super(source.readInt());
        text = source.readString();
    }

    public static final Creator<ToastAction> CREATOR = new Creator<ToastAction>() {
        @Override
        public ToastAction createFromParcel(Parcel source) {
            return new ToastAction(source);
        }

        @Override
        public ToastAction[] newArray(int size) {
            return new ToastAction[size];
        }
    };

    @Override
    public StartActivityInfo showMainPreference(ActionActivity context) {
        return showSubPreference(context);
    }

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        View v = LayoutInflater.from(context).inflate(R.layout.action_toast_dialog, null);
        final EditText editText = (EditText) v.findViewById(R.id.editText);

        editText.setText(text);

        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        text = editText.getText().toString();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        return null;
    }

    public String getText() {
        return text;
    }
}
