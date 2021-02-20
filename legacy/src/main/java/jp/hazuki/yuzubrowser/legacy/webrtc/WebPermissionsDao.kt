/*
 * Copyright (C) 2017-2021 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.webrtc

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import jp.hazuki.yuzubrowser.legacy.webrtc.core.WebPermissions

@Dao
interface WebPermissionsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(permission: WebPermissions): Long

    @Query("select * from permissions order by host")
    suspend fun getList(): List<WebPermissions>

    @Query("select * from permissions where host = :host")
    suspend fun get(host: String): WebPermissions?
}
