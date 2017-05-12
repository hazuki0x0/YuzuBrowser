/*
 * Copyright (C) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.gesture.multiFinger.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.action.Action;

public class MultiFingerGestureItem implements Parcelable {
    private List<Integer> traces;
    private int fingers = 1;
    private Action action;

    public MultiFingerGestureItem() {
        traces = new ArrayList<>();
        action = new Action();
    }

    public int getFingers() {
        return fingers;
    }

    public void setFingers(int fingers) {
        this.fingers = fingers;
    }

    public void addTrace(int action) {
        traces.add(action);
    }

    public void removeLastTrace() {
        if (traces.size() > 0)
            traces.remove(traces.size() - 1);
    }

    public boolean checkTrace(int trace) {
        return traces.size() <= 0 || !(traces.get(traces.size() - 1) == trace);
    }

    public List<Integer> getTraces() {
        return traces;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeList(traces);
        dest.writeInt(fingers);
        dest.writeParcelable(action, flags);
    }

    protected MultiFingerGestureItem(Parcel in) {
        traces = new ArrayList<>();
        in.readList(traces, null);
        fingers = in.readInt();
        action = in.readParcelable(Action.class.getClassLoader());
    }

    public static final Creator<MultiFingerGestureItem> CREATOR = new Creator<MultiFingerGestureItem>() {
        @Override
        public MultiFingerGestureItem createFromParcel(Parcel in) {
            return new MultiFingerGestureItem(in);
        }

        @Override
        public MultiFingerGestureItem[] newArray(int size) {
            return new MultiFingerGestureItem[size];
        }
    };
}
