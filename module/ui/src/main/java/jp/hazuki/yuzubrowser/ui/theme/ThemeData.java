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

package jp.hazuki.yuzubrowser.ui.theme;

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
import android.widget.Toast;

import com.squareup.moshi.JsonDataException;
import com.squareup.moshi.JsonReader;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import jp.hazuki.yuzubrowser.core.utility.extensions.ContextExtensionsKt;
import jp.hazuki.yuzubrowser.core.utility.utils.ImageUtils;
import jp.hazuki.yuzubrowser.ui.R;
import okio.Okio;

import static jp.hazuki.yuzubrowser.core.utility.utils.FileUtilsKt.getExternalUserDirectory;

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
        try (JsonReader reader = JsonReader.of(Okio.buffer(Okio.source(new File(folder, "theme.json"))))) {
            if (reader.peek() != JsonReader.Token.BEGIN_OBJECT) return;
            reader.beginObject();

            boolean refreshColorDef = false;

            while (reader.hasNext()) {
                String field = reader.nextName();
                if ("lightTheme".equals(field)) {
                    lightTheme = getBoolean(reader);
                    continue;
                }
                if ("tabBackgroundNormal".equalsIgnoreCase(field)) {
                    tabBackgroundNormal = getColorOrBitmapDrawable(context, folder, reader);
                    continue;
                }
                if ("tabBackgroundSelect".equalsIgnoreCase(field)) {
                    tabBackgroundSelect = getColorOrBitmapDrawable(context, folder, reader);
                    continue;
                }
                if ("tabTextColorNormal".equalsIgnoreCase(field)) {
                    try {
                        tabTextColorNormal = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("tabTextColorLock".equalsIgnoreCase(field)) {
                    try {
                        tabTextColorLock = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("tabTextColorPin".equalsIgnoreCase(field)) {
                    try {
                        tabTextColorPin = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("tabTextColorSelect".equalsIgnoreCase(field)) {
                    try {
                        tabTextColorSelect = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("tabAccentColor".equalsIgnoreCase(field)) {
                    try {
                        tabAccentColor = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("tabDividerColor".equalsIgnoreCase(field)) {
                    try {
                        tabDividerColor = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("scrollbarAccentColor".equalsIgnoreCase(field)) {
                    try {
                        scrollbarAccentColor = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                if ("showTabDivider".equalsIgnoreCase(field)) {
                    showTabDivider = getBoolean(reader);
                    continue;
                }
                if ("progressColor".equalsIgnoreCase(field)) {
                    try {
                        progressColor = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("progressIndeterminateColor".equalsIgnoreCase(field)) {
                    try {
                        progressIndeterminateColor = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("toolbarBackgroundColor".equalsIgnoreCase(field)) {
                    try {
                        toolbarBackgroundColor = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("toolbarTextColor".equalsIgnoreCase(field)) {
                    try {
                        toolbarTextColor = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("toolbarImageColor".equalsIgnoreCase(field)) {
                    try {
                        toolbarImageColor = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("toolbarButtonBackgroundPress".equalsIgnoreCase(field)) {
                    try {
                        int padding = context.getResources().getDimensionPixelOffset(R.dimen.dimen_theme_padding);
                        Rect paddingRect = new Rect(padding, padding, padding, padding);
                        Rect textPaddingRect = new Rect(padding, 0, padding, 0);
                        int color = Long.decode(reader.nextString().trim()).intValue();

                        toolbarButtonBackgroundPress = new ShapeDrawable(new RectShape());
                        toolbarButtonBackgroundPress.setPadding(paddingRect);
                        toolbarButtonBackgroundPress.getPaint().setColor(color);

                        toolbarTextButtonBackgroundPress = new ShapeDrawable(new RectShape());
                        toolbarTextButtonBackgroundPress.setPadding(textPaddingRect);
                        toolbarTextButtonBackgroundPress.getPaint().setColor(color);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("qcItemBackgroundColorNormal".equalsIgnoreCase(field)) {
                    try {
                        qcItemBackgroundColorNormal = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("qcItemBackgroundColorSelect".equalsIgnoreCase(field)) {
                    try {
                        qcItemBackgroundColorSelect = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("qcItemColor".equalsIgnoreCase(field)) {
                    try {
                        qcItemColor = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("statusBarColor".equalsIgnoreCase(field)) {
                    try {
                        statusBarColor = Long.decode(reader.nextString().trim()).intValue();
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                if ("pullToRefreshDark".equalsIgnoreCase(field)) {
                    refreshUseDark = getBoolean(reader);
                    refreshColorDef = true;
                    continue;
                }
                if ("statusBarDarkIcon".equalsIgnoreCase(field)) {
                    statusBarDarkIcon = getBoolean(reader);
                    continue;
                }
                reader.skipValue();
            }
            reader.endObject();

            toolbarImageColor = 0xFF000000 | toolbarImageColor;
            if (!refreshColorDef && !refreshUseDark) {
                refreshUseDark = isColorLight(statusBarColor) && !lightTheme;
            }
        } catch (JsonDataException e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stackTrace = sw.toString(); // stack trace as a string
            Toast.makeText(context, "Theme error:\n" + stackTrace, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    private Drawable getColorOrBitmapDrawable(Context context, File folder, JsonReader reader) throws IOException {
        String value = null;
        Rect expandArea = null, paddingArea = null;
        boolean autoScale = false;
        boolean scaleFilter = false;
        if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                String field = reader.nextName();
                if ("filename".equalsIgnoreCase(field)) {
                    value = reader.nextString();
                    continue;
                }
                if ("expandArea".equalsIgnoreCase(field)) {
                    if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) return null;
                    reader.beginArray();
                    try {
                        expandArea = new Rect(reader.nextInt(), reader.nextInt(), reader.nextInt(), reader.nextInt());
                    } catch (JsonDataException e) {
                        return null;
                    }
                    if (reader.peek() != JsonReader.Token.END_ARRAY) return null;
                    reader.endArray();
                    continue;
                }
                if ("paddingArea".equalsIgnoreCase(field)) {
                    if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) return null;
                    reader.beginArray();
                    try {
                        paddingArea = new Rect(reader.nextInt(), reader.nextInt(), reader.nextInt(), reader.nextInt());
                    } catch (JsonDataException e) {
                        return null;
                    }
                    if (reader.peek() != JsonReader.Token.END_ARRAY) return null;
                    reader.endArray();
                    continue;
                }
                if ("autoScale".equalsIgnoreCase(field)) {
                    autoScale = getBoolean(reader);
                    continue;
                }
                if ("scaleFilter".equalsIgnoreCase(field)) {
                    autoScale = getBoolean(reader);
                    continue;
                }
                reader.skipValue();
            }
            reader.endObject();
        } else {
            value = reader.nextString();
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
    @Nullable
    public static ThemeData getInstance() {
        return sInstance;
    }

    @Nullable
    public static ThemeData createInstanceIfNeed(@NonNull Context context, @Nullable String folder) {
        if (!isLoaded || !Objects.equals(folder, loadedTheme)) {
            return createInstance(context, folder);
        } else {
            return sInstance;
        }
    }

    @Nullable
    public static ThemeData createInstance(@NonNull Context context, @Nullable String folder) {
        isLoaded = true;
        if (TextUtils.isEmpty(folder)) {
            sInstance = null;
            loadedTheme = null;
        } else if (THEME_LIGHT.equals(folder)) {
            sInstance = createLightTheme(context);
            loadedTheme = folder;
        } else {
            File file = new File(getExternalUserDirectory(), "theme" + File.separator + folder);
            if (!file.exists() || !file.isDirectory()) {
                sInstance = null;
                loadedTheme = null;
            } else {
                try {
                    sInstance = new ThemeData(context, file);
                    loadedTheme = folder;
                } catch (IOException e) {
                    e.printStackTrace();
                    sInstance = null;
                    loadedTheme = null;
                }
            }
        }
        return sInstance;
    }

    private static ThemeData sInstance;

    private static boolean isLoaded = false;

    public static boolean isLoaded() {
        return isLoaded;
    }

    private static String loadedTheme = null;

    @Nullable
    public static String getLoadedTheme() {
        return loadedTheme;
    }

    private static boolean getBoolean(JsonReader reader) throws IOException {
        if (reader.peek() == JsonReader.Token.BOOLEAN) {
            return reader.nextBoolean();
        } else {
            return Boolean.valueOf(reader.nextString().trim());
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

        int padding = context.getResources().getDimensionPixelOffset(R.dimen.dimen_theme_padding);
        Rect paddingRect = new Rect(padding, padding, padding, padding);

        data.toolbarButtonBackgroundPress = new ShapeDrawable(new RectShape());
        data.toolbarButtonBackgroundPress.setPadding(paddingRect);
        data.toolbarButtonBackgroundPress.getPaint().setColor(0xFFB5B5B5);

        return data;
    }
}
