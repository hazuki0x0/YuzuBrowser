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
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class SimpleLayerDrawable extends Drawable {

    private final Drawable[] drawables;

    public SimpleLayerDrawable(Drawable... drawables) {
        this.drawables = drawables;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        for (Drawable d : drawables)
            d.draw(canvas);
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        for (Drawable d : drawables)
            d.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        for (Drawable d : drawables)
            d.setColorFilter(colorFilter);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        for (Drawable d : drawables)
            d.setBounds(left, top, right, bottom);
    }

    @Override
    public void applyTheme(@NonNull Resources.Theme t) {
        super.applyTheme(t);
        for (Drawable d : drawables)
            d.applyTheme(t);
    }

    @Override
    public int getIntrinsicWidth() {
        int width = -1;
        for (Drawable d : drawables) {
            int d_width = d.getIntrinsicWidth();
            if (width < d_width)
                width = d_width;
        }
        return width;
    }

    @Override
    public int getIntrinsicHeight() {
        int height = -1;
        for (Drawable d : drawables) {
            int d_height = d.getIntrinsicHeight();
            if (height < d_height)
                height = d_height;
        }
        return height;
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
