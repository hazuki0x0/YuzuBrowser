/*
 * Copyright (C) 2017-2018 Hazuki
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
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.hazuki.yuzubrowser.BrowserApplication;
import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ImageUtils;
import jp.hazuki.yuzubrowser.utils.JsonUtils;
import jp.hazuki.yuzubrowser.utils.extensions.ContextExtensionsKt;

public class ThemeData {
    public static final String THEME_LIGHT = "theme://internal/light";

    public Drawable tabBackgroundNormal, tabBackgroundSelect;
    public int tabTextColorNormal, tabTextColorLock, tabTextColorPin, tabTextColorSelect, tabAccentColor, tabDividerColor;
    public int scrollbarAccentColor;
    public boolean showTabDivider;
    public int progressColor, progressIndeterminateColor;
    public int toolbarBackgroundColor;
    public int toolbarTextColor, toolbarImageColor;
    public ShapeDrawable toolbarButtonBackgroundPress, toolbarTextButtonBackgroundPress;
    public int qcItemBackgroundColorNormal, qcItemBackgroundColorSelect, qcItemColor;
    public int statusBarColor;
    private boolean statusBarDarkIcon;
    public boolean refreshUseDark;
    public boolean lightTheme;

    private ThemeData() {
    }

    private ThemeData(Context context, File folder) throws IOException {
        try (InputStream is = new BufferedInputStream(new FileInputStream(new File(folder, "theme.json")))) {
            JsonParser parser = JsonUtils.getFactory().createParser(is);

            if (parser.nextToken() != JsonToken.START_OBJECT) return;

            boolean refreshColorDef = false;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                String field = parser.getText();
                parser.nextToken();
                if ("lightTheme".equals(field)) {
                    lightTheme = getBoolean(parser);
                    continue;
                }
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
                if ("tabTextColorPin".equalsIgnoreCase(field)) {
                    try {
                        tabTextColorPin = Long.decode(parser.getText().trim()).intValue();
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
                if ("tabDividerColor".equalsIgnoreCase(field)) {
                    try {
                        tabDividerColor = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("scrollbarAccentColor".equalsIgnoreCase(field)) {
                    try {
                        scrollbarAccentColor = Long.decode(parser.getText().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                if ("showTabDivider".equalsIgnoreCase(field)) {
                    showTabDivider = getBoolean(parser);
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
                        Rect textPaddingRect = new Rect(padding, 0, padding, 0);

                        toolbarButtonBackgroundPress = new ShapeDrawable(new RectShape());
                        toolbarButtonBackgroundPress.setPadding(paddingRect);
                        toolbarButtonBackgroundPress.getPaint().setColor(Long.decode(parser.getText().trim()).intValue());

                        toolbarTextButtonBackgroundPress = new ShapeDrawable(new RectShape());
                        toolbarTextButtonBackgroundPress.setPadding(textPaddingRect);
                        toolbarTextButtonBackgroundPress.getPaint().setColor(Long.decode(parser.getText().trim()).intValue());
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
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("pullToRefreshDark".equalsIgnoreCase(field)) {
                    refreshUseDark = getBoolean(parser);
                    refreshColorDef = true;
                    continue;
                }
                if ("statusBarDarkIcon".equalsIgnoreCase(field)) {
                    statusBarDarkIcon = getBoolean(parser);
                    continue;
                }
                if (parser.getCurrentToken() == JsonToken.START_OBJECT
                        || parser.getCurrentToken() == JsonToken.START_ARRAY) {
                    parser.skipChildren();
                }
            }

            toolbarImageColor = 0xFF000000 | toolbarImageColor;
            if (!refreshColorDef && !refreshUseDark) {
                refreshUseDark = isColorLight(statusBarColor) && !lightTheme;
            }


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
                    parser.nextToken();
                    autoScale = getBoolean(parser);
                    continue;
                }
                if ("scaleFilter".equalsIgnoreCase(field)) {
                    parser.nextToken();
                    autoScale = getBoolean(parser);
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
                return new LayerDrawable(new Drawable[]{new ColorDrawable(Long.decode(value).intValue()), context.getDrawable(R.drawable.tab_background_normal)});
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
                    float density = ContextExtensionsKt.getDensity(context);
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
        File file = new File(BrowserApplication.Companion.getExternalUserDirectory(), "theme");
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
            File file = new File(BrowserApplication.Companion.getExternalUserDirectory(), "theme" + File.separator + folder);
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

    private static boolean getBoolean(JsonParser parser) throws IOException {
        switch (parser.getCurrentToken()) {
            case VALUE_TRUE:
                return true;
            case VALUE_FALSE:
                return false;
            default:
                return Boolean.valueOf(parser.getText().trim());
        }
    }

    public static int getSystemUiVisibilityFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && sInstance != null && (sInstance.statusBarDarkIcon || sInstance.isLightStatusBar())) {
            return View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        } else {
            return 0;
        }
    }

    public boolean isLightStatusBar() {
        return isColorLight(statusBarColor);
    }

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
        data.lightTheme = true;

        data.tabTextColorNormal = 0xFF444444;
        data.tabTextColorLock = 0xFF00B53C;
        data.tabTextColorPin = 0xFF3B4EF9;
        data.tabTextColorSelect = 0xFF222222;
        data.showTabDivider = true;

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
