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
import android.widget.CheckBox;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class PasteSearchBoxAction extends SingleAction {
    private static final String FIELD_OPEN_NEW_TAB = "0";

    private boolean openNewTab;

    public PasteSearchBoxAction(int id, JsonParser parser) throws IOException {
        super(id);
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (FIELD_OPEN_NEW_TAB.equals(parser.getCurrentName())) {
                    if (parser.nextValue().isBoolean())
                        openNewTab = parser.getBooleanValue();
                }

                if (parser.getCurrentToken() == JsonToken.START_OBJECT
                        || parser.getCurrentToken() == JsonToken.START_ARRAY) {
                    parser.skipChildren();
                }
            }
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        generator.writeBooleanField(FIELD_OPEN_NEW_TAB, openNewTab);
        generator.writeEndObject();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(openNewTab ? 1 : 0);
    }

    protected PasteSearchBoxAction(Parcel source) {
        super(source);
        openNewTab = source.readInt() != 0;
    }

    public static final Creator<PasteSearchBoxAction> CREATOR = new Creator<PasteSearchBoxAction>() {
        @Override
        public PasteSearchBoxAction createFromParcel(Parcel source) {
            return new PasteSearchBoxAction(source);
        }

        @Override
        public PasteSearchBoxAction[] newArray(int size) {
            return new PasteSearchBoxAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        View view = LayoutInflater.from(context).inflate(R.layout.action_show_search_box, null);
        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        checkBox.setChecked(openNewTab);
        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openNewTab = checkBox.isChecked();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        return null;
    }

    public boolean isOpenNewTab() {
        return openNewTab;
    }
}
