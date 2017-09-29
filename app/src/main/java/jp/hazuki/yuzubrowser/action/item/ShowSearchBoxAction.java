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
import android.os.Parcelable;
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

public class ShowSearchBoxAction extends SingleAction implements Parcelable {
    private static final String FIELD_OPEN_NEW_TAB = "0";
    private static final String FIELD_REVERSE = "1";

    private boolean openNewTab;
    private boolean reverse;

    public ShowSearchBoxAction(int id, JsonParser parser) throws IOException {
        super(id);
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (FIELD_OPEN_NEW_TAB.equals(parser.getCurrentName())) {
                    if (parser.nextValue().isBoolean())
                        openNewTab = parser.getBooleanValue();
                }
                if (FIELD_REVERSE.equals(parser.getCurrentName())) {
                    if (parser.nextValue().isBoolean())
                        reverse = parser.getBooleanValue();
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
        generator.writeBooleanField(FIELD_REVERSE, reverse);
        generator.writeEndObject();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(openNewTab ? 1 : 0);
        dest.writeInt(reverse ? 1 : 0);
    }

    protected ShowSearchBoxAction(Parcel source) {
        super(source);
        openNewTab = source.readInt() != 0;
        reverse = source.readInt() != 0;
    }

    public static final Creator<ShowSearchBoxAction> CREATOR = new Creator<ShowSearchBoxAction>() {
        @Override
        public ShowSearchBoxAction createFromParcel(Parcel source) {
            return new ShowSearchBoxAction(source);
        }

        @Override
        public ShowSearchBoxAction[] newArray(int size) {
            return new ShowSearchBoxAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        View view = LayoutInflater.from(context).inflate(R.layout.action_show_search_box, null);
        final CheckBox checkBox = view.findViewById(R.id.checkBox);
        final CheckBox bottom = view.findViewById(R.id.checkBox2);
        checkBox.setChecked(openNewTab);
        bottom.setChecked(reverse);
        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openNewTab = checkBox.isChecked();
                        reverse = bottom.isChecked();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
        return null;
    }

    public boolean isOpenNewTab() {
        return openNewTab;
    }

    public boolean isReverse() {
        return reverse;
    }
}
