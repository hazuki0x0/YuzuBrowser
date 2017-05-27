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

package jp.hazuki.yuzubrowser.theme;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.hazuki.yuzubrowser.BrowserApplication;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.DisplayUtils;
import jp.hazuki.yuzubrowser.utils.ImageUtils;

public class ThemeData {
    public static final String THEME_LIGHT = "theme://internal/light";

    public Drawable tabBackgroundNormal, tabBackgroundSelect;
    public int tabTextColorNormal, tabTextColorLock, tabTextColorSelect, tabAccentColor;
    public int progressColor, progressIndeterminateColor;
    public int toolbarBackgroundColor;
    public int toolbarTextColor, toolbarImageColor;
    public ShapeDrawable toolbarButtonBackgroundPress;
    public int qcItemBackgroundColorNormal, qcItemBackgroundColorSelect, qcItemColor;
    public int statusBarColor;
    public boolean refreshUseDark;

    private ThemeData() {
    }

    private ThemeData(Context context, File folder) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(new File(folder, "theme.json")))) {
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(is);

            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                String field = parser.getText();
                parser.nextToken();
                if ("tabBackgroundNormal".equalsIgnoreCase(field)) {
                    tabBackgroundNormal = getColorOrBitmapDrawable(context, folder, parser);
                    continue;
                }
                if ("tabBackgroundSelect".equalsIgnoreCase(field)) {
                    tabBackgroundSelect = getColorOrBitmapDrawable(context, folder, parser);
                    continue;
                }
                if ("tabTextColorNormal".equalsIgnoreCase(field)) {
                    try {
                        tabTextColorNormal = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("tabTextColorLock".equalsIgnoreCase(field)) {
                    try {
                        tabTextColorLock = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("tabTextColorSelect".equalsIgnoreCase(field)) {
                    try {
                        tabTextColorSelect = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("tabAccentColor".equalsIgnoreCase(field)) {
                    try {
                        tabAccentColor = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("progressColor".equalsIgnoreCase(field)) {
                    try {
                        progressColor = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("progressIndeterminateColor".equalsIgnoreCase(field)) {
                    try {
                        progressIndeterminateColor = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("toolbarBackgroundColor".equalsIgnoreCase(field)) {
                    try {
                        toolbarBackgroundColor = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("toolbarTextColor".equalsIgnoreCase(field)) {
                    try {
                        toolbarTextColor = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("toolbarImageColor".equalsIgnoreCase(field)) {
                    try {
                        toolbarImageColor = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("toolbarButtonBackgroundPress".equalsIgnoreCase(field)) {
                    try {
                        int padding = context.getResources().getDimensionPixelOffset(R.dimen.swipebtn_bg_padding);
                        Rect paddingRect = new Rect(padding, padding, padding, padding);

                        toolbarButtonBackgroundPress = new ShapeDrawable(new RectShape());
                        toolbarButtonBackgroundPress.setPadding(paddingRect);
                        toolbarButtonBackgroundPress.getPaint().setColor(Long.decode(parser.getText().trim()).intValue());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("qcItemBackgroundColorNormal".equalsIgnoreCase(field)) {
                    try {
                        qcItemBackgroundColorNormal = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("qcItemBackgroundColorSelect".equalsIgnoreCase(field)) {
                    try {
                        qcItemBackgroundColorSelect = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("qcItemColor".equalsIgnoreCase(field)) {
                    try {
                        qcItemColor = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("statusBarColor".equalsIgnoreCase(field)) {
                    try {
                        statusBarColor = Long.decode(parser.getText().trim()).intValue();
                        refreshUseDark = isColorLight(statusBarColor);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if (parser.getCurrentToken() != JsonToken.START_OBJECT
                        && parser.getCurrentToken() != JsonToken.START_ARRAY) {
                    parser.nextValue();
                } else {
                    parser.skipChildren();
                }
            }

            toolbarImageColor = 0xFF000000 | toolbarImageColor;

            parser.close();
        }
    }


    private Drawable getColorOrBitmapDrawable(Context context, File folder, JsonParser parser) throws IOException {
        String value;
        Rect expandArea = null, paddingArea = null;
        boolean autoScale = false;
        boolean scaleFilter = false;
        if (parser.getCurrentToken() == JsonToken.START_OBJECT) {
            value = null;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return null;
                String field = parser.getText();
                if ("filename".equalsIgnoreCase(field)) {
                    value = parser.nextTextValue();
                    continue;
                }
                if ("expandArea".equalsIgnoreCase(field)) {
                    if (parser.nextToken() != JsonToken.START_ARRAY) return null;
                    expandArea = new Rect(parser.nextIntValue(0), parser.nextIntValue(0), parser.nextIntValue(0), parser.nextIntValue(0));
                    if (parser.nextToken() != JsonToken.END_ARRAY) return null;
                    continue;
                }
                if ("paddingArea".equalsIgnoreCase(field)) {
                    if (parser.nextToken() != JsonToken.START_ARRAY) return null;
                    paddingArea = new Rect(parser.nextIntValue(0), parser.nextIntValue(0), parser.nextIntValue(0), parser.nextIntValue(0));
                    if (parser.nextToken() != JsonToken.END_ARRAY) return null;
                    continue;
                }
                if ("autoScale".equalsIgnoreCase(field)) {
                    autoScale = parser.nextBooleanValue();
                    continue;
                }
                if ("scaleFilter".equalsIgnoreCase(field)) {
                    scaleFilter = parser.nextBooleanValue();
                    continue;
                }
                if (parser.getCurrentToken() != JsonToken.START_OBJECT
                        && parser.getCurrentToken() != JsonToken.START_ARRAY) {
                    parser.nextValue();
                } else {
                    parser.skipChildren();
                }
            }
        } else {
            value = parser.getText();
            try {
                return new ColorDrawable(Long.decode(value).intValue());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        if (value == null)
            return null;

        File file = new File(folder, value);
        if (file.exists()) {
            if (!value.contains(".9.")) {
                return Drawable.createFromPath(file.getAbsolutePath());
            } else {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (bitmap == null)
                    return null;
                if (autoScale) {
                    float density = DisplayUtils.getDensity(context);
                    Matrix matrix = new Matrix();
                    matrix.setScale(density, density);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, scaleFilter);
                    if (expandArea != null) {
                        scaleRect(expandArea, density);
                    }

                    if (paddingArea != null) {
                        scaleRect(paddingArea, density);
                    }
                }
                byte[] chunk = bitmap.getNinePatchChunk();
                if (!NinePatch.isNinePatchChunk(chunk)) {
                    if (expandArea == null || paddingArea == null)
                        return null;
                    else
                        chunk = ImageUtils.makeNinePatchChunk(expandArea, paddingArea);
                }
                if (paddingArea == null)
                    paddingArea = new Rect();
                return new NinePatchDrawable(context.getResources(), bitmap, chunk, paddingArea, null);
            }
        }
        return null;
    }

    public static boolean isEnabled() {
        return sInstance != null;
    }

    //maybe null (disable theme)
    public static ThemeData getInstance() {
        return sInstance;
    }

    public static ThemeData createInstance(Context context) {
        File file = new File(BrowserApplication.getExternalUserDirectory(), "theme");
        if (!file.exists() || !file.isDirectory())
            sInstance = null;
        else
            try {
                sInstance = new ThemeData(context, file);
            } catch (IOException e) {
                e.printStackTrace();
                sInstance = null;
            }
        return sInstance;
    }

    public static ThemeData createInstance(Context context, String folder) {

        if (TextUtils.isEmpty(folder)) {
            sInstance = null;
        } else if (THEME_LIGHT.equals(folder)) {
            sInstance = createLightTheme(context);
        } else {
            File file = new File(BrowserApplication.getExternalUserDirectory(), "theme" + File.separator + folder);
            if (!file.exists() || !file.isDirectory())
                sInstance = null;
            else
                try {
                    sInstance = new ThemeData(context, file);
                } catch (IOException e) {
                    e.printStackTrace();
                    return createInstance(context);
                }
        }
        return sInstance;
    }

    private static ThemeData sInstance;

    private boolean isColorLight(int color) {
        double lightness = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return lightness > 0.8;
    }

    private void scaleRect(Rect rect, float scale) {
        rect.left = (int) (rect.left * scale + 0.5f);
        rect.right = (int) (rect.right * scale + 0.5f);
        rect.top = (int) (rect.top * scale + 0.5f);
        rect.bottom = (int) (rect.bottom * scale + 0.5f);
    }

    private static ThemeData createLightTheme(Context context) {
        ThemeData data = new ThemeData();
        data.tabTextColorNormal = 0xFF444444;
        data.tabTextColorLock = 0xFF222222;
        data.tabTextColorSelect = 0xFF222222;

        data.toolbarBackgroundColor = 0xFFDDDDDD;
        data.toolbarTextColor = 0xFF222222;
        data.toolbarImageColor = 0xFF444444;

        data.statusBarColor = 0xFFAAAAAA;

        int padding = context.getResources().getDimensionPixelOffset(R.dimen.swipebtn_bg_padding);
        Rect paddingRect = new Rect(padding, padding, padding, padding);

        data.toolbarButtonBackgroundPress = new ShapeDrawable(new RectShape());
        data.toolbarButtonBackgroundPress.setPadding(paddingRect);
        data.toolbarButtonBackgroundPress.getPaint().setColor(0xFFB5B5B5);

        return data;
    }
}
