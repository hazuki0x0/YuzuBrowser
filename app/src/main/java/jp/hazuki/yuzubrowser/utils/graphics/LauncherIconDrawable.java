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

package jp.hazuki.yuzubrowser.utils.graphics;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class LauncherIconDrawable extends Drawable {

    private Drawable icon;

    public LauncherIconDrawable(Drawable drawable) {
        icon = drawable;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        icon.draw(canvas);
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        icon.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return icon.getOpacity();
    }

    @Override
    public int getIntrinsicWidth() {
        return icon.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return icon.getIntrinsicHeight();
    }

    @Override
    public int getAlpha() {
        return icon.getAlpha();
    }

    @Override
    public boolean setState(@NonNull int[] stateSet) {
        return icon.setState(stateSet);
    }

    @Nullable
    @Override
    public ConstantState getConstantState() {
        return icon.getConstantState();
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        icon.setBounds(left, top, right, bottom);
    }

    @Override
    public void applyTheme(@NonNull Resources.Theme t) {
        icon.applyTheme(t);
    }
}
