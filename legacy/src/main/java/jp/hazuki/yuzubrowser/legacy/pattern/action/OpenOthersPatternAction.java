package jp.hazuki.yuzubrowser.legacy.pattern.action;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.pattern.PatternAction;
import jp.hazuki.yuzubrowser.legacy.tab.manager.MainTabData;
import jp.hazuki.yuzubrowser.legacy.utils.PackageUtils;

import static jp.hazuki.yuzubrowser.legacy.Constants.intent.EXTRA_OPEN_FROM_YUZU;

public class OpenOthersPatternAction extends PatternAction {
    private static final String FIELD_TYPE = "0";
    private static final String FIELD_INTENT = "1";
    public static final int TYPE_NORMAL = 0;
    public static final int TYPE_APP_LIST = 1;
    public static final int TYPE_APP_CHOOSER = 2;
    private int mType;
    private String mUrl;

    public OpenOthersPatternAction(@NonNull Intent intent) {
        mType = TYPE_NORMAL;
        mUrl = intent.toUri(0);
    }

    public OpenOthersPatternAction(int type) {
        mType = type;
    }

    public OpenOthersPatternAction(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_OBJECT) return;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
            if (FIELD_TYPE.equals(parser.getCurrentName())) {
                if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
                mType = parser.getIntValue();
                continue;
            }
            if (FIELD_INTENT.equals(parser.getCurrentName())) {
                if (parser.nextToken() != JsonToken.VALUE_STRING) return;
                mUrl = parser.getText();
                continue;
            }
            parser.skipChildren();
        }
    }

    @Override
    public int getTypeId() {
        return OPEN_OTHERS;
    }

    @Override
    public boolean write(JsonGenerator generator) throws IOException {
        generator.writeNumber(OPEN_OTHERS);
        generator.writeStartObject();
        generator.writeNumberField(FIELD_TYPE, mType);
        if (mUrl != null)
            generator.writeStringField(FIELD_INTENT, mUrl);
        generator.writeEndObject();
        return true;
    }

    @Override
    public String getTitle(Context context) {
        switch (mType) {
            case TYPE_NORMAL: {
                String pre = context.getString(R.string.pattern_open_others);
                try {
                    PackageManager pm = context.getPackageManager();
                    return pre + " : " + pm.getActivityInfo(getIntent().getComponent(), 0).loadLabel(pm).toString();
                } catch (NameNotFoundException e) {
                    e.printStackTrace();
                }
                return pre;
            }
            case TYPE_APP_LIST:
                return context.getString(R.string.pattern_open_app_list);
            case TYPE_APP_CHOOSER:
                return context.getString(R.string.pattern_open_app_chooser);
            default:
                throw new IllegalStateException();
        }
    }

    public int getOpenType() {
        return mType;
    }

    public Intent getIntent() {
        try {
            return Intent.parseUri(mUrl, 0);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean run(Context context, MainTabData tab, String url) {
        Intent intent;
        switch (mType) {
            case TYPE_NORMAL:
                intent = getIntent();
                intent.setData(Uri.parse(url));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                break;
            case TYPE_APP_LIST:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                break;
            case TYPE_APP_CHOOSER:
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                intent = PackageUtils.createChooser(context, url, context.getText(R.string.open));
                break;
            default:
                throw new IllegalStateException();
        }
        try {
            intent.putExtra(EXTRA_OPEN_FROM_YUZU, true);
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.app_notfound, Toast.LENGTH_SHORT).show();
        }
        return false;
    }
}
