package jp.hazuki.yuzubrowser.legacy.gesture;

import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray;

public class GestureItem implements Parcelable {
    private final long mId;
    private final Gesture mGesture;
    private final Action mAction;

    public GestureItem(long id, Gesture gesture, Action action) {
        mId = id;
        mGesture = gesture;
        mAction = action;
    }

    public GestureItem(Parcel source) {
        mId = source.readLong();
        mGesture = source.readParcelable(Gesture.class.getClassLoader());
        mAction = source.readParcelable(Action.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mId);
        dest.writeParcelable(mGesture, flags);
        dest.writeParcelable(mAction, flags);
    }

    public static final Creator<GestureItem> CREATOR = new Creator<GestureItem>() {
        @Override
        public GestureItem createFromParcel(Parcel source) {
            return new GestureItem(source);
        }

        @Override
        public GestureItem[] newArray(int size) {
            return new GestureItem[size];
        }
    };

    public long getId() {
        return mId;
    }

    public Gesture getGesture() {
        return mGesture;
    }

    public Action getAction() {
        return mAction;
    }

    public Bitmap getBitmap(int width, int height, int color) {
        return mGesture.toBitmap(width, height, 1, color);
    }

	/*public String getActionName(String[] list, int[] values) {
        return mAction.toString(list, values);
	}*/

    public String getActionName(ActionNameArray array) {
        return mAction.toString(array);
    }
}
