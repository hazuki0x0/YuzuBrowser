package jp.hazuki.yuzubrowser.action.item;

import android.app.AlertDialog;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class ShareScreenshotSingleAction extends SingleAction implements Parcelable {
    private static final String FIELD_NAME_SS_TYPE = "0";
    public static final int SS_TYPE_ALL = 0;
    public static final int SS_TYPE_PART = 1;
    private int mSsType;

    public ShareScreenshotSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (!FIELD_NAME_SS_TYPE.equals(parser.getCurrentName())) {
                    parser.skipChildren();
                    continue;
                }
                if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
                mSsType = parser.getIntValue();
            }
        } else {
            mSsType = SS_TYPE_PART;
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        generator.writeNumberField(FIELD_NAME_SS_TYPE, mSsType);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mSsType);
    }

    protected ShareScreenshotSingleAction(Parcel source) {
        super(source.readInt());
        mSsType = source.readInt();
    }

    public static final Creator<ShareScreenshotSingleAction> CREATOR = new Creator<ShareScreenshotSingleAction>() {
        @Override
        public ShareScreenshotSingleAction createFromParcel(Parcel source) {
            return new ShareScreenshotSingleAction(source);
        }

        @Override
        public ShareScreenshotSingleAction[] newArray(int size) {
            return new ShareScreenshotSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        View view = LayoutInflater.from(context).inflate(R.layout.action_screenshot_settings, null);
        CheckBox captureAllCheckBox = view.findViewById(R.id.captureAllCheckBox);
        view.findViewById(R.id.folderEditText).setVisibility(View.GONE);

        captureAllCheckBox.setChecked(mSsType == SS_TYPE_ALL);

        if (AppData.slow_rendering.get()) {
            captureAllCheckBox.setEnabled(true);
            view.findViewById(R.id.captureAllErrorTextView).setVisibility(View.GONE);
        }

        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (captureAllCheckBox.isChecked())
                        mSsType = SS_TYPE_ALL;
                    else
                        mSsType = SS_TYPE_PART;
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        return null;
    }

    public int getType() {
        return mSsType;
    }
}