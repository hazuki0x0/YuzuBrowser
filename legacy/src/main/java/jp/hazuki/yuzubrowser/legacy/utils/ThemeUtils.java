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

package jp.hazuki.yuzubrowser.legacy.utils;

import android.content.Context;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.core.content.res.ResourcesCompat;

public final class ThemeUtils {

    public static int getIdFromThemeRes(Context context, @AttrRes int id) {
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(id, outValue, true);
        return outValue.resourceId;
    }

    @ColorInt
    public static int getColorFromThemeRes(Context context, @AttrRes int id) {
        return ResourcesCompat.getColor(context.getResources(), getIdFromThemeRes(context, id), context.getTheme());
    }
}
