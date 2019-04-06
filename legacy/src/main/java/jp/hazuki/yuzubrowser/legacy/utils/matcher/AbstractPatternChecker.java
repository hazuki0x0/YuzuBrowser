/*
 * Copyright (C) 2017-2019 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.utils.matcher;

import android.content.Context;

import com.squareup.moshi.JsonWriter;

import java.io.IOException;
import java.io.Serializable;

public abstract class AbstractPatternChecker<T extends AbstractPatternAction> implements Serializable {
    private final T mPatternAction;

    protected AbstractPatternChecker(T pattern_action) {
        mPatternAction = pattern_action;
    }

    public final T getAction() {
        return mPatternAction;
    }

    public abstract String getTitle(Context context);

    public abstract boolean isEnable();

    public abstract void setEnable(boolean enable);

    public String getActionTitle(Context context) {
        return mPatternAction.getTitle(context);
    }

    public abstract boolean write(JsonWriter writer) throws IOException;
}
