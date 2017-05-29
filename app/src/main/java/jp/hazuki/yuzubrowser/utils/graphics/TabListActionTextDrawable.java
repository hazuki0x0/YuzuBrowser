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

public class TabListActionTextDrawable extends Drawable {

    private final String text;
    private final Paint paint;
    private final int padding_y;

    public TabListActionTextDrawable(Context context, int tabs) {
        if (tabs > 99) {
            text = ":D";
        } else {
            text = Integer.toString(tabs);
        }


        paint = new Paint();
        paint.setTextSize(DisplayUtils.convertDpToPx(context, 10));
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setFakeBoldText(true);

        padding_y = DisplayUtils.convertDpToPx(context, 2);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect r = new Rect();
        paint.getTextBounds(text, 0, text.length(), r);
        Rect bounds = getBounds();

        int xPos = bounds.left + bounds.width() / 2;
        int yPos = bounds.top + bounds.height() / 2 + r.height() / 2 + padding_y;
        canvas.drawText(text, xPos, yPos, paint);
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSPARENT;
    }
}
