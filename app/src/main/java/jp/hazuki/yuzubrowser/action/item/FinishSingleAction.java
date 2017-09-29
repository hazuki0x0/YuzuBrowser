package jp.hazuki.yuzubrowser.action.item;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import jp.hazuki.yuzubrowser.utils.Logger;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class FinishSingleAction extends SingleAction implements Parcelable {
    private static final String TAG = "FinishSingleAction";
    private static final String FIELD_NAME_ALERT = "0";
    private static final String FIELD_NAME_CLOSE_TAB = "1";
    private boolean mAlert = true;
    private boolean mCloseTab = false;

    public FinishSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (FIELD_NAME_ALERT.equals(parser.getCurrentName())) {
                    switch (parser.nextToken()) {
                        case VALUE_TRUE:
                            mAlert = true;
                            break;
                        case VALUE_FALSE:
                            mAlert = false;
                            break;
                        default:
                            Logger.w(TAG, "current token is not boolean value : " + parser.getCurrentToken().toString());
                            break;
                    }
                    continue;
                }
                if (FIELD_NAME_CLOSE_TAB.equals(parser.getCurrentName())) {
                    switch (parser.nextToken()) {
                        case VALUE_TRUE:
                            mCloseTab = true;
                            break;
                        case VALUE_FALSE:
                            mCloseTab = false;
                            break;
                        default:
                            Logger.w(TAG, "current token is not boolean value : " + parser.getCurrentToken().toString());
                            break;
                    }
                    continue;
                }
                parser.skipChildren();
            }
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        generator.writeBooleanField(FIELD_NAME_ALERT, mAlert);
        generator.writeBooleanField(FIELD_NAME_CLOSE_TAB, mCloseTab);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mAlert ? 1 : 0);
        dest.writeInt(mCloseTab ? 1 : 0);
    }

    protected FinishSingleAction(Parcel source) {
        super(source.readInt());
        mAlert = source.readInt() == 1;
        mCloseTab = source.readInt() == 1;
    }

    public static final Creator<FinishSingleAction> CREATOR = new Creator<FinishSingleAction>() {
        @Override
        public FinishSingleAction createFromParcel(Parcel source) {
            return new FinishSingleAction(source);
        }

        @Override
        public FinishSingleAction[] newArray(int size) {
            return new FinishSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        View view = LayoutInflater.from(context).inflate(R.layout.action_finish_setting, null);
        final CheckBox finishAlertCheckBox = view.findViewById(R.id.finishAlertCheckBox);
        final CheckBox closetabCheckBox = view.findViewById(R.id.closetabCheckBox);

        finishAlertCheckBox.setChecked(mAlert);
        closetabCheckBox.setChecked(mCloseTab);

        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAlert = finishAlertCheckBox.isChecked();
                        mCloseTab = closetabCheckBox.isChecked();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        return null;
    }

    public boolean isShowAlert() {
        return mAlert;
    }

    public boolean isCloseTab() {
        return mCloseTab;
    }

}
