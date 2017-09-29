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
import android.widget.Spinner;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.Logger;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class TabListSingleAction extends SingleAction implements Parcelable {
    public static final int MODE_NORMAL = 0;
    public static final int MODE_REVERSE = 1;
    public static final int MODE_HORIZONTAL = 2;

    private static final String TAG = "TabListSingleAction";
    private static final String FIELD_NAME_REVERSE = "0";
    private static final String FIELD_NAME_MODE = "1";
    private static final String FIELD_NAME_LEFT_BUTTON = "2";
    private int mode = MODE_HORIZONTAL;
    private boolean leftButton;

    public TabListSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (FIELD_NAME_REVERSE.equals(parser.getCurrentName())) {
                    switch (parser.nextToken()) {
                        case VALUE_TRUE:
                            mode = MODE_REVERSE;
                            break;
                        case VALUE_FALSE:
                            mode = MODE_NORMAL;
                            break;
                        default:
                            Logger.w(TAG, "current token is not boolean value : " + parser.getCurrentToken().toString());
                            break;
                    }
                } else if (FIELD_NAME_MODE.equals(parser.getCurrentName())) {
                    if (parser.nextValue().isNumeric())
                        mode = parser.getIntValue();
                } else if (FIELD_NAME_LEFT_BUTTON.equals(parser.getCurrentName())) {
                    if (parser.nextValue().isBoolean())
                        leftButton = parser.getBooleanValue();
                } else {
                    parser.skipChildren();
                }

            }
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        generator.writeNumberField(FIELD_NAME_MODE, mode);
        generator.writeBooleanField(FIELD_NAME_LEFT_BUTTON, leftButton);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mode);
    }

    protected TabListSingleAction(Parcel source) {
        super(source.readInt());
        mode = source.readInt();
    }

    public static final Creator<TabListSingleAction> CREATOR = new Creator<TabListSingleAction>() {
        @Override
        public TabListSingleAction createFromParcel(Parcel source) {
            return new TabListSingleAction(source);
        }

        @Override
        public TabListSingleAction[] newArray(int size) {
            return new TabListSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        View view = LayoutInflater.from(context).inflate(R.layout.action_tab_list, null);
        final Spinner modeSpinner = view.findViewById(R.id.modeSpinner);
        final Spinner buttonSpinner = view.findViewById(R.id.buttonSpinner);

        modeSpinner.setSelection(mode);
        buttonSpinner.setSelection(leftButton ? 1 : 0);
        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mode = modeSpinner.getSelectedItemPosition();
                        leftButton = buttonSpinner.getSelectedItemPosition() > 0;
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        return null;
    }

    public int getMode() {
        return mode;
    }

    public boolean isLeftButton() {
        return leftButton;
    }
}
