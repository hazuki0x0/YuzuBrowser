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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import jp.hazuki.yuzubrowser.utils.DisplayUtils;

public class DividerDrawable extends Drawable {

    private final int width;
    private final int padding;
    private Paint paint;

    public DividerDrawable(Context context) {
        width = DisplayUtils.convertDpToPx(context, 1);
        padding = DisplayUtils.convertDpToPx(context, 4);
        paint = new Paint();
        paint.setColor(Color.TRANSPARENT);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect rect = getBounds();
        canvas.drawRect(rect.left, rect.top + padding, rect.right, rect.bottom - padding, paint);
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return PixelFormat.UNKNOWN;
    }

    public void setColor(int color) {
        paint.setColor(color);
        invalidateSelf();
    }

    public void setWithTextColor(int color) {
        setColor(color & 0xffffff | 0x55000000);
    }

    @Override
    public int getIntrinsicWidth() {
        return width;
    }
}
