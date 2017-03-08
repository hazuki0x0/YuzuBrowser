package jp.hazuki.yuzubrowser.action.item;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionList;
import jp.hazuki.yuzubrowser.action.SingleAction;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.action.view.ActionListActivity;
import jp.hazuki.yuzubrowser.action.view.ActionListActivity.OnActionListActivityResultListener;
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo;

public class CustomMenuSingleAction extends SingleAction implements Parcelable {
    private static final String FIELD_NAME_ACTIONLIST = "0";
    private ActionList mActionList;

    public CustomMenuSingleAction(int id, JsonParser parser) throws IOException {
        super(id);

        mActionList = new ActionList();
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return;
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.getCurrentToken() != JsonToken.FIELD_NAME) return;
                if (!FIELD_NAME_ACTIONLIST.equals(parser.getCurrentName())) {
                    parser.skipChildren();
                    continue;
                }
                mActionList.loadAction(parser);
            }
        }
    }

    @Override
    public void writeIdAndData(JsonGenerator generator) throws IOException {
        generator.writeNumber(id);
        generator.writeStartObject();
        mActionList.writeAction(FIELD_NAME_ACTIONLIST, generator);
        generator.writeEndObject();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeParcelable(mActionList, flags);
    }

    protected CustomMenuSingleAction(Parcel source) {
        super(source.readInt());
        mActionList = source.readParcelable(ActionList.class.getClassLoader());
    }

    public static final Creator<CustomMenuSingleAction> CREATOR = new Creator<CustomMenuSingleAction>() {
        @Override
        public CustomMenuSingleAction createFromParcel(Parcel source) {
            return new CustomMenuSingleAction(source);
        }

        @Override
        public CustomMenuSingleAction[] newArray(int size) {
            return new CustomMenuSingleAction[size];
        }
    };

    @Override
    public StartActivityInfo showMainPreference(ActionActivity context) {
        return showSubPreference(context);
    }

    @Override
    public StartActivityInfo showSubPreference(ActionActivity context) {
        return new ActionListActivity.Builder(context)
                .setTitle(R.string.action_select_menu)
                .setActionNameArray(context.getActionNameArray())
                .setDefaultActionList(mActionList)
                .setOnActionListActivityResultListener(new OnActionListActivityResultListener() {
                    @Override
                    public void onActionListActivityResult(Context context, ActionList actionlist) {
                        mActionList = actionlist;
                    }
                })
                .makeStartActivityInfo();
    }

    public ActionList getActionList() {
        return mActionList;
    }
}
