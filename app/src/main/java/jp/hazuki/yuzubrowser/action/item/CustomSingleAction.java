package jp.hazuki.yuzubrowser.action.item;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.app.OnActivityResultListener;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class CustomSingleAction extends SingleAction {
    private static final String FIELD_NAME_ACTION = "0";
    private static final String FIELD_NAME_ACTION_NAME = "1";
    private Action mAction;
    private String mName;

    public CustomSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        mAction = new Action();
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (FIELD_NAME_ACTION.equals(parser.getCurrentName())) {
                    mAction.loadAction(parser);
                    continue;
                }
                if (FIELD_NAME_ACTION_NAME.equals(parser.getCurrentName())) {
                    if (parser.nextToken() != JsonToken.VALUE_STRING) return;
                    mName = parser.getText();
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
        mAction.writeAction(FIELD_NAME_ACTION, generator);
        generator.writeStringField(FIELD_NAME_ACTION_NAME, mName);
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
        dest.writeString(mName);
    }

    protected CustomSingleAction(Parcel source) {
        super(source.readInt());
        mAction = source.readParcelable(Action.class.getClassLoader());
        mName = source.readString();
    }

    public static final Parcelable.Creator<CustomSingleAction> CREATOR = new Parcelable.Creator<CustomSingleAction>() {
        @Override
        public CustomSingleAction createFromParcel(Parcel source) {
            return new CustomSingleAction(source);
        }

        @Override
        public CustomSingleAction[] newArray(int size) {
            return new CustomSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showMainPreference(ActionActivity context) {
        return showSubPreference(context);
    }

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        Intent intent = new Intent(context, CustomSingleActionActivity.class);
        intent.putExtra(CustomSingleActionActivity.EXTRA_ACTION, (Parcelable) mAction);
        intent.putExtra(CustomSingleActionActivity.EXTRA_NAME, mName);
        intent.putExtra(ActionNameArray.INTENT_EXTRA, (Parcelable) context.getActionNameArray());

        OnActivityResultListener l = new OnActivityResultListener() {
            @Override
            public void onActivityResult(Context context, int resultCode, Intent intent) {
                if (resultCode != Activity.RESULT_OK || intent == null)
                    return;
                mAction = intent.getParcelableExtra(CustomSingleActionActivity.EXTRA_ACTION);
                mName = intent.getStringExtra(CustomSingleActionActivity.EXTRA_NAME);
            }
        };

        return new StartActivityInfo(intent, l);
    }

    public Action getAction() {
        return mAction;
    }

    @Override
    public String toString(ActionNameArray nameArray) {
        if (mName != null)
            return mName;
        return super.toString(nameArray);
    }
}
