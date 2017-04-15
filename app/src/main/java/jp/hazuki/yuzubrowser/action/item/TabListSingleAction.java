package jp.hazuki.yuzubrowser.action.item;

import android.app.AlertDialog;
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
import jp.hazuki.yuzubrowser.utils.Logger;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class TabListSingleAction extends SingleAction implements Parcelable {
    public static final int MODE_NORMAL = 0;
    public static final int MODE_REVERSE = 1;
    public static final int MODE_HORIZONTAL = 2;

    private static final String TAG = "TabListSingleAction";
    private static final String FIELD_NAME_REVERSE = "0";
    private static final String FIELD_NAME_MODE = "1";
    private int mode = MODE_NORMAL;

    public TabListSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (FIELD_NAME_REVERSE.equals(parser.getCurrentName())) {
                    switch (parser.nextToken()) {
                        case VALUE_TRUE:
                            mode = MODE_REVERSE;
                            break;
                        case VALUE_FALSE:
                            mode = MODE_NORMAL;
                            break;
                        default:
                            Logger.w(TAG, "current token is not boolean value : " + parser.getCurrentToken().toString());
                            break;
                    }
                } else if (FIELD_NAME_MODE.equals(parser.getCurrentName())) {
                    if (parser.nextValue().isNumeric())
                        mode = parser.getIntValue();
                } else {
                    parser.skipChildren();
                }

            }
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        generator.writeNumberField(FIELD_NAME_MODE, mode);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mode);
    }

    protected TabListSingleAction(Parcel source) {
        super(source.readInt());
        mode = source.readInt();
    }

    public static final Creator<TabListSingleAction> CREATOR = new Creator<TabListSingleAction>() {
        @Override
        public TabListSingleAction createFromParcel(Parcel source) {
            return new TabListSingleAction(source);
        }

        @Override
        public TabListSingleAction[] newArray(int size) {
            return new TabListSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setSingleChoiceItems(R.array.action_tab_list, mode, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mode = which;
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        return null;
    }

    public int getMode() {
        return mode;
    }
}
