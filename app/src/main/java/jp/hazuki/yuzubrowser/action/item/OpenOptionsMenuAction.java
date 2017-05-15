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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Gravity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class OpenOptionsMenuAction extends SingleAction {
    private static final String FIELD_NAME_SHOW_MODE = "0";
    private int showMode = 1;

    public OpenOptionsMenuAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (FIELD_NAME_SHOW_MODE.equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
                    showMode = parser.getIntValue();
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
        generator.writeNumberField(FIELD_NAME_SHOW_MODE, showMode);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(showMode);
    }

    protected OpenOptionsMenuAction(Parcel source) {
        super(source.readInt());
        showMode = source.readInt();
    }

    public static final Parcelable.Creator<OpenOptionsMenuAction> CREATOR = new Parcelable.Creator<OpenOptionsMenuAction>() {
        @Override
        public OpenOptionsMenuAction createFromParcel(Parcel source) {
            return new OpenOptionsMenuAction(source);
        }

        @Override
        public OpenOptionsMenuAction[] newArray(int size) {
            return new OpenOptionsMenuAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.action_open_menu)
                .setSingleChoiceItems(R.array.action_open_menu_list, showMode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showMode = which;
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        return null;
    }

    @SuppressLint("RtlHardcoded")
    public int getGravity() {
        return getGravity(showMode);
    }

    public static int getGravity(int mode) {
        switch (mode) {
            case 0:
                return Gravity.BOTTOM | Gravity.LEFT;
            case 1:
                return Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            case 2:
                return Gravity.BOTTOM | Gravity.RIGHT;
            default:
                throw new RuntimeException("Unknown mode : " + mode);
        }
    }
}
