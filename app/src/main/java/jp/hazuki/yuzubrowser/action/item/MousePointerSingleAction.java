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

public class MousePointerSingleAction extends SingleAction implements Parcelable {
    private static final String TAG = "MousePointerSingleAction";
    private static final String FIELD_NAME_BACKFINISH = "0";
    private boolean mBackFinish = true;

    public MousePointerSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (!FIELD_NAME_BACKFINISH.equals(parser.getCurrentName())) {
                    parser.skipChildren();
                    continue;
                }
                switch (parser.nextToken()) {
                    case VALUE_TRUE:
                        mBackFinish = true;
                        break;
                    case VALUE_FALSE:
                        mBackFinish = false;
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
        generator.writeBooleanField(FIELD_NAME_BACKFINISH, mBackFinish);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(mBackFinish ? 1 : 0);
    }

    protected MousePointerSingleAction(Parcel source) {
        super(source.readInt());
        mBackFinish = source.readInt() == 1;
    }

    public static final Creator<MousePointerSingleAction> CREATOR = new Creator<MousePointerSingleAction>() {
        @Override
        public MousePointerSingleAction createFromParcel(Parcel source) {
            return new MousePointerSingleAction(source);
        }

        @Override
        public MousePointerSingleAction[] newArray(int size) {
            return new MousePointerSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        final CheckBox view = new CheckBox(context);
        view.setText(R.string.action_mousepointer_backfinish);
        view.setChecked(mBackFinish);

        new AlertDialog.Builder(context)
                .setTitle(R.string.action_settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBackFinish = view.isChecked();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();

        return null;
    }

    public boolean isBackFinish() {
        return mBackFinish;
    }

}
