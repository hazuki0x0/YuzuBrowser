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

package jp.hazuki.yuzubrowser.legacy.userjs

import android.os.Parcel
import android.os.Parcelable

class UserScriptInfo : Parcelable {

    var id: Long = -1
    var data = ""
    var isEnabled = true

    internal constructor(id: Long, data: String, enabled: Boolean) {
        this.id = id
        this.data = data
        this.isEnabled = enabled
    }

    internal constructor(data: String) {
        this.data = data
    }

    internal constructor()

    private constructor(parcel: Parcel) {
        id = parcel.readLong()
        data = parcel.readString()!!
        isEnabled = parcel.readByte().toInt() != 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(data)
        dest.writeByte((if (isEnabled) 1 else 0).toByte())
    }

    override fun describeContents(): Int = 0

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<UserScriptInfo> = object : Parcelable.Creator<UserScriptInfo> {
            override fun createFromParcel(parcel: Parcel): UserScriptInfo = UserScriptInfo(parcel)

            override fun newArray(size: Int): Array<UserScriptInfo?> = arrayOfNulls(size)
        }
    }
}
