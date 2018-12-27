package jp.hazuki.yuzubrowser.legacy.gesture;

import android.os.Parcel;
import android.os.Parcelable;

import jp.hazuki.yuzubrowser.legacy.action.Action;

public class GestureScore implements Parcelable {
    public final double score;
    public final Action action;

    public GestureScore(double score, Action action) {
        this.score = score;
        this.action = action;
    }

    public GestureScore(Parcel source) {
        score = source.readDouble();
        action = source.readParcelable(Action.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(score);
        dest.writeParcelable(action, flags);
    }

    public static final Creator<GestureScore> CREATOR = new Creator<GestureScore>() {
        @Override
        public GestureScore createFromParcel(Parcel source) {
            return new GestureScore(source);
        }

        @Override
        public GestureScore[] newArray(int size) {
            return new GestureScore[size];
        }
    };
}
