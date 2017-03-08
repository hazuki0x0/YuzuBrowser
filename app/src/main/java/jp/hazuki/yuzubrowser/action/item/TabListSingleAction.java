package jp.hazuki.yuzubrowser.action.item;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
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

public class TabListSingleAction extends SingleAction implements Parcelable {
    private static final String TAG = "TabListSingleAction";
    private static final String FIELD_NAME_REVERSE = "0";
    private boolean mReverse = false;

    public TabListSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (!FIELD_NAME_REVERSE.equals(parser.getCurrentName())) {
                    parser.skipChildren();
                    continue;
                }
                switch (parser.nextToken()) {
                    case VALUE_TRUE:
                        mReverse = true;
                        break;
                    case VALUE_FALSE:
                        mReverse = false;
                        break;
                    default:
                        Logger.w(TAG, "current token is not boolean value : " + parser.getCurrentToken().toString());
                        break;
                }
            }
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        generator.writeBooleanField(FIELD_NAME_REVERSE, mReverse);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mReverse ? 1 : 0);
    }

    protected TabListSingleAction(Parcel source) {
        super(source.readInt());
        mReverse = source.readInt() == 1;
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
        final CheckBox view = new CheckBox(context);
        view.setText(R.string.action_tablist_reverse);
        view.setChecked(mReverse);

        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mReverse = view.isChecked();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        return null;
    }

    public boolean isReverse() {
        return mReverse;
    }
}
