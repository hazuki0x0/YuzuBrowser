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
import jp.hazuki.yuzubrowser.browser.BrowserManager;
import jp.hazuki.yuzubrowser.utils.ArrayUtils;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class PasteGoSingleAction extends SingleAction implements Parcelable {
    //private static final String TAG = "PasteGoSingleAction";
    private static final String FIELD_TARGET_TAB = "0";
    private int mTargetTab;

    public PasteGoSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (!FIELD_TARGET_TAB.equals(parser.getCurrentName())) {
                    parser.skipChildren();
                    continue;
                }
                if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return;
                mTargetTab = parser.getIntValue();
            }
        } else {
            mTargetTab = BrowserManager.LOAD_URL_TAB_CURRENT;
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        generator.writeNumberField(FIELD_TARGET_TAB, mTargetTab);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mTargetTab);
    }

    protected PasteGoSingleAction(Parcel source) {
        super(source.readInt());
        mTargetTab = source.readInt();
    }

    public static final Creator<PasteGoSingleAction> CREATOR = new Creator<PasteGoSingleAction>() {
        @Override
        public PasteGoSingleAction createFromParcel(Parcel source) {
            return new PasteGoSingleAction(source);
        }

        @Override
        public PasteGoSingleAction[] newArray(int size) {
            return new PasteGoSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        final int value_array[] = context.getResources().getIntArray(R.array.pref_newtab_values);
        int def_value = ArrayUtils.findIndexOfValue(mTargetTab, value_array);
        new AlertDialog.Builder(context)
                .setTitle(R.string.action_target_tab)
                .setSingleChoiceItems(R.array.pref_newtab_list, def_value, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTargetTab = value_array[which];
                        dialog.dismiss();
                    }
                })
                .show();

        return null;
    }

    public int getTargetTab() {
        return mTargetTab;
    }
}
