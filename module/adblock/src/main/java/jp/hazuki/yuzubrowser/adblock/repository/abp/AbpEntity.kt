/*
 * Copyright (C) 2017-2019 Hazuki
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

package jp.hazuki.yuzubrowser.adblock.repository.abp

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "abp")
@Parcelize
class AbpEntity(
        @PrimaryKey(autoGenerate = true)
        var entityId: Int = 0,

        var url: String = "",

        var title: String? = null,

        var lastUpdate: String? = null,

        var lastLocalUpdate: Long = 0,

        var expires: Int = -1,

        var version: String? = null,

        var homePage: String? = null,

        var lastModified: String? = null,

        var enabled: Boolean = true
) : Parcelable {

    constructor() : this(0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbpEntity) return false
        if (entityId != other.entityId) return false

        return true
    }

    override fun hashCode(): Int {
        return entityId
    }
}
