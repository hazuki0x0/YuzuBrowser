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

package jp.hazuki.yuzubrowser.ui.settings.container;

import android.content.SharedPreferences;

public abstract class BaseContainer<T> implements Containable {
    protected BaseContainer(String name, T def_value) {
        mName = name;
        mDefValue = def_value;
        mValue = def_value;
    }

    @Override
    public abstract void read(SharedPreferences shared_preference);

    @Override
    public abstract void write(SharedPreferences.Editor editor);

    public T get() {
        return mValue;
    }

    public void set(T value) {
        mValue = value;
    }

    public void resetToDefaultValue() {
        mValue = mDefValue;
    }

    protected final String mName;
    protected final T mDefValue;
    protected T mValue;
}
