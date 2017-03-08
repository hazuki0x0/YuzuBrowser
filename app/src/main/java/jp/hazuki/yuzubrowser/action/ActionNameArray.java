package jp.hazuki.yuzubrowser.action;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

import jp.hazuki.yuzubrowser.R;

public class ActionNameArray implements Serializable, Parcelable {
    private static final long serialVersionUID = 442574116730542765L;
    public static final String INTENT_EXTRA = "action.extra.actionNameArray";

    public final String[] actionList;
    public final int[] actionValues;

    public ActionNameArray(Context context) {
        Resources res = context.getResources();
        actionList = res.getStringArray(R.array.action_list);
        actionValues = res.getIntArray(R.array.action_values);
    }

    public ActionNameArray(Context context, int addListId, int addValuesId) {
        Resources res = context.getResources();
        String[] baseList = res.getStringArray(R.array.action_list);
        int[] baseValues = res.getIntArray(R.array.action_values);
        String[] addList = res.getStringArray(addListId);
        int[] addValues = res.getIntArray(addValuesId);

        actionList = new String[baseList.length + addList.length];
        System.arraycopy(addList, 0, actionList, 0, addList.length);
        System.arraycopy(baseList, 0, actionList, addList.length, baseList.length);

        actionValues = new int[baseValues.length + addValues.length];
        System.arraycopy(addValues, 0, actionValues, 0, addValues.length);
        System.arraycopy(baseValues, 0, actionValues, addValues.length, baseValues.length);
    }

    public ActionNameArray(Parcel source) {
        actionList = source.createStringArray();
        actionValues = source.createIntArray();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(actionList);
        dest.writeIntArray(actionValues);
    }

    public int size() {
        return actionValues.length;
    }

    public static final Creator<ActionNameArray> CREATOR = new Creator<ActionNameArray>() {
        @Override
        public ActionNameArray createFromParcel(Parcel source) {
            return new ActionNameArray(source);
        }

        @Override
        public ActionNameArray[] newArray(int size) {
            return new ActionNameArray[size];
        }
    };
}
