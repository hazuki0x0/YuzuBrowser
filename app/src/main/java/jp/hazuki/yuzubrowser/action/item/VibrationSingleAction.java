package jp.hazuki.yuzubrowser.action.item;

import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;
import jp.hazuki.yuzubrowser.utils.view.SeekBarDialog;

public class VibrationSingleAction extends SingleAction implements Parcelable {
    private static final String FIELD_NAME_TIME = "0";
    private int mTime = 100;

    public VibrationSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (!FIELD_NAME_TIME.equals(parser.getCurrentName())) {
                    parser.skipChildren();
                    continue;
                }
                if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
                mTime = parser.getIntValue();
            }
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        generator.writeNumberField(FIELD_NAME_TIME, mTime);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mTime);
    }

    protected VibrationSingleAction(Parcel source) {
        super(source.readInt());
        mTime = source.readInt();
    }

    public static final Creator<VibrationSingleAction> CREATOR = new Creator<VibrationSingleAction>() {
        @Override
        public VibrationSingleAction createFromParcel(Parcel source) {
            return new VibrationSingleAction(source);
        }

        @Override
        public VibrationSingleAction[] newArray(int size) {
            return new VibrationSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        new SeekBarDialog(context)
                .setTitle(R.string.action_vibration_setting)
                .setPositiveButton(android.R.string.ok, new SeekBarDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, int value) {
                        mTime = value;
                    }
                })
                .setSeekMin(1)
                .setSeekMax(3000)
                .setValue(mTime)
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        return null;
    }

    public int getTime() {
        return mTime;
    }
}
