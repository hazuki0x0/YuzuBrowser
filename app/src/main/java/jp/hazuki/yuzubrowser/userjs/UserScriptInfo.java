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

package jp.hazuki.yuzubrowser.userjs;

import android.os.Parcel;
import android.os.Parcelable;

class UserScriptInfo implements Parcelable {

    private long id = -1;
    private String data = "";
    private boolean enabled = true;

    UserScriptInfo(long id, String data, boolean enabled) {
        this.id = id;
        this.data = data;
        this.enabled = enabled;
    }

    UserScriptInfo(String data) {
        this.data = data;
    }

    UserScriptInfo() {
    }

    private UserScriptInfo(Parcel in) {
        id = in.readLong();
        data = in.readString();
        enabled = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(data);
        dest.writeByte((byte) (enabled ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserScriptInfo> CREATOR = new Creator<UserScriptInfo>() {
        @Override
        public UserScriptInfo createFromParcel(Parcel in) {
            return new UserScriptInfo(in);
        }

        @Override
        public UserScriptInfo[] newArray(int size) {
            return new UserScriptInfo[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


}
