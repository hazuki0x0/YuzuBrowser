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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.action.view.CloseAutoSelectActivity;
import jp.hazuki.yuzubrowser.action.view.CloseAutoSelectActivity.OnActionActivityResultListener;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class CloseAutoSelectAction extends SingleAction implements Parcelable {
    private static final String DEFAULT = "0";
    private static final String INTENT = "1";
    private static final String WINDOW = "2";

    private Action defaultAction;
    private Action intentAction;
    private Action windowAction;

    public CloseAutoSelectAction(int id, JsonParser parser) throws IOException {
        super(id);

        defaultAction = new Action();
        intentAction = new Action();
        windowAction = new Action();
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (DEFAULT.equals(parser.getCurrentName())) {
                    defaultAction.loadAction(parser);
                    continue;
                }
                if (INTENT.equals(parser.getCurrentName())) {
                    intentAction.loadAction(parser);
                    continue;
                }
                if (WINDOW.equals(parser.getCurrentName())) {
                    windowAction.loadAction(parser);
                    continue;
                }

                parser.skipChildren();
            }
        } else {
            defaultAction.add(SingleAction.makeInstance(SingleAction.CLOSE_TAB));
            intentAction.add(new CloseTabSingleAction());
            intentAction.add(SingleAction.makeInstance(SingleAction.MINIMIZE));
            windowAction.add(SingleAction.makeInstance(SingleAction.CLOSE_TAB));
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        defaultAction.writeAction(DEFAULT, generator);
        intentAction.writeAction(INTENT, generator);
        windowAction.writeAction(WINDOW, generator);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(defaultAction, flags);
        dest.writeParcelable(intentAction, flags);
        dest.writeParcelable(windowAction, flags);
    }

    protected CloseAutoSelectAction(Parcel source) {
        super(source.readInt());
        defaultAction = source.readParcelable(Action.class.getClassLoader());
        intentAction = source.readParcelable(Action.class.getClassLoader());
        windowAction = source.readParcelable(Action.class.getClassLoader());
    }

    public static final Creator<CloseAutoSelectAction> CREATOR = new Creator<CloseAutoSelectAction>() {
        @Override
        public CloseAutoSelectAction createFromParcel(Parcel source) {
            return new CloseAutoSelectAction(source);
        }

        @Override
        public CloseAutoSelectAction[] newArray(int size) {
            return new CloseAutoSelectAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        return new CloseAutoSelectActivity.Builder(context)
                .setListener(new OnActionActivityResultListener() {
                    @Override
                    public void onActionResult(Context context, Action def, Action intent, Action window) {
                        defaultAction = def;
                        intentAction = intent;
                        windowAction = window;
                    }
                })
                .getActivityInfo(defaultAction, intentAction, windowAction);
    }

    public Action getDefaultAction() {
        return defaultAction;
    }

    public Action getIntentAction() {
        return intentAction;
    }

    public Action getWindowAction() {
        return windowAction;
    }
}
