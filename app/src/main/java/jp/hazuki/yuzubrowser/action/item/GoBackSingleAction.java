package jp.hazuki.yuzubrowser.action.item;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.action.view.ActionActivity.OnActionActivityResultListener;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class GoBackSingleAction extends SingleAction implements Parcelable {
    private static final String FIELD_NAME_ACTION = "0";
    private Action mAction;

    public GoBackSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        mAction = new Action();
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (!FIELD_NAME_ACTION.equals(parser.getCurrentName())) {
                    parser.skipChildren();
                    continue;
                }
                mAction.loadAction(parser);
            }
        } else {
            mAction.add(SingleAction.makeInstance(CLOSE_AUTO_SELECT));
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        mAction.writeAction(FIELD_NAME_ACTION, generator);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(mAction, flags);
    }

    protected GoBackSingleAction(Parcel source) {
        super(source.readInt());
        mAction = source.readParcelable(Action.class.getClassLoader());
    }

    public static final Creator<GoBackSingleAction> CREATOR = new Creator<GoBackSingleAction>() {
        @Override
        public GoBackSingleAction createFromParcel(Parcel source) {
            return new GoBackSingleAction(source);
        }

        @Override
        public GoBackSingleAction[] newArray(int size) {
            return new GoBackSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        return new ActionActivity.Builder(context)
                .setDefaultAction(mAction)
                .setTitle(R.string.action_action_cant_back)
                .setActionNameArray(context.getActionNameArray())
                .setOnActionActivityResultListener(new OnActionActivityResultListener() {
                    @Override
                    public void onActionActivityResult(Context context, Action action) {
                        mAction = action;
                    }
                })
                .makeStartActivityInfo();
    }

    public Action getDefaultAction() {
        return mAction;
    }
}