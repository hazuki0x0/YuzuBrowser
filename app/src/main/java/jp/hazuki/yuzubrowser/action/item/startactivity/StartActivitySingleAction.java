package jp.hazuki.yuzubrowser.action.item.startactivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;

import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.tab.MainTabData;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.ImageUtils;
import jp.hazuki.yuzubrowser.utils.app.OnActivityResultListener;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class StartActivitySingleAction extends SingleAction implements Parcelable {
    private static final String TAG = "StartActivitySingleAction";
    private static final String FIELD_NAME_INTENT = "0";
    private static final String FIELD_NAME_ACTION_NAME = "1";
    private static final String FIELD_NAME_ACTION_ICON = "2";
    public static final String REPLACE_URI = "http://jp.hazuki.yuzubrowser/REPLACE_URI";
    public static final String REPLACE_TITLE = "http://jp.hazuki.yuzubrowser/REPLACE_TITLE";
    private Intent mIntent;
    private String mName;
    private Bitmap mIcon;
    private WeakReference<Drawable> mIconCache;

    public StartActivitySingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (FIELD_NAME_INTENT.equals(parser.getCurrentName())) {
                    switch (parser.nextToken()) {
                        case VALUE_STRING:
                            try {
                                mIntent = Intent.parseUri(parser.getText(), 0);
                            } catch (URISyntaxException e) {
                                ErrorReport.printAndWriteLog(e);
                            }
                            break;
                        case VALUE_NULL:
                            break;
                    }
                    continue;
                }
                if (FIELD_NAME_ACTION_NAME.equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_STRING) return;
                    mName = parser.getText();
                    continue;
                }
                if (FIELD_NAME_ACTION_ICON.equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_STRING) return;
                    mIcon = ImageUtils.convertToBitmap(parser.getBinaryValue());
                    continue;
                }
                parser.skipChildren();
            }
        } else {
            mIntent = null;
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        if (mIntent == null)
            generator.writeNullField(FIELD_NAME_INTENT);
        else {
            generator.writeStringField(FIELD_NAME_INTENT, mIntent.toUri(0));
            if (mName != null)
                generator.writeStringField(FIELD_NAME_ACTION_NAME, mName);
            if (mIcon != null)
                generator.writeBinaryField(FIELD_NAME_ACTION_ICON, ImageUtils.convertToByteArray(mIcon));
        }
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(mIntent, flags);
        dest.writeString(mName);
        dest.writeParcelable(mIcon, flags);
    }

    protected StartActivitySingleAction(Parcel source) {
        super(source.readInt());
        mIntent = source.readParcelable(Intent.class.getClassLoader());
        mName = source.readString();
        mIcon = source.readParcelable(Bitmap.class.getClassLoader());
    }

    public static final Creator<StartActivitySingleAction> CREATOR = new Creator<StartActivitySingleAction>() {
        @Override
        public StartActivitySingleAction createFromParcel(Parcel source) {
            return new StartActivitySingleAction(source);
        }

        @Override
        public StartActivitySingleAction[] newArray(int size) {
            return new StartActivitySingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showMainPreference(ActionActivity context) {
        return showSubPreference(context);
    }

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        Intent intent = new Intent(context.getApplicationContext(), StartActivityPreferenceActivity.class);
        intent.putExtra(Intent.EXTRA_INTENT, mIntent);
        OnActivityResultListener listener = new OnActivityResultListener() {
            @Override
            public void onActivityResult(Context context, int resultCode, Intent intent) {
                if (resultCode != Activity.RESULT_OK || intent == null)
                    return;
                String name = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
                Bitmap icon = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
                if (icon == null) {
                    ShortcutIconResource iconRes = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
                    if (iconRes != null) {
                        try {
                            Resources foreignResources = context.getPackageManager().getResourcesForApplication(iconRes.packageName);
                            int id = foreignResources.getIdentifier(iconRes.resourceName, null, null);
                            icon = BitmapFactory.decodeResource(foreignResources, id);
                        } catch (NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Intent sintent = intent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
                if (sintent == null)
                    return;
                mName = name;
                if (mIcon != null)
                    mIcon.recycle();
                mIcon = icon;
                mIntent = sintent;
                mIconCache = null;
            }
        };
        return new StartActivityInfo(intent, listener);
    }

    public Intent getIntent(MainTabData tab) {
        if (mIntent == null)
            return null;

        Intent intent = new Intent(mIntent);
        String data = intent.getDataString();

        if (data != null) {
            String type = intent.getType();
            data = replaceString(tab, data);
            intent.setDataAndType(Uri.parse(data), type);
        }

        Bundle extras = intent.getExtras();
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object obj = extras.get(key);
                if (obj instanceof CharSequence) {
                    intent.putExtra(key, replaceString(tab, obj.toString()));
                }
            }
        }
        return intent;
    }

    private static String replaceString(MainTabData tab, String data) {
        if (tab.mUrl != null)
            data = data.replace(REPLACE_URI, tab.mUrl);
        if (tab.mTitle != null)
            data = data.replace(REPLACE_TITLE, tab.mTitle);
        return data;
    }

    public Drawable getIconDrawable(Context context) {
        if (mIntent == null)
            return null;

        Drawable icon;
        if (mIconCache == null || (icon = mIconCache.get()) == null) {
            if (mIcon == null) {
                try {
                    icon = context.getPackageManager().getActivityIcon(mIntent.getComponent());//Does not return null.
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                icon = new BitmapDrawable(context.getResources(), mIcon);
            }
            mIconCache = new WeakReference<>(icon);
        }
        return icon;
    }
}
