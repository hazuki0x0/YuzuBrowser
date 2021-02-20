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

package jp.hazuki.yuzubrowser.download.repository

import android.net.Uri
import androidx.room.*
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo

@Dao
interface DownloadsDao {

    @Insert
    fun insert(info: DownloadFileInfo): Long

    @Insert
    suspend fun insertAsync(info: DownloadFileInfo): Long

    @Update
    fun update(info: DownloadFileInfo)

    fun updateWithEmptyRoot(info: DownloadFileInfo) {
        update(info.copy(root = Uri.parse("")))
    }

    @Query("select * from downloads where id = :id")
    operator fun get(id: Long): DownloadFileInfo

    @Query("select * from downloads order by startTime desc limit :count offset :offset")
    suspend fun getList(offset: Int, count: Int): List<DownloadFileInfo>

    @Delete
    suspend fun delete(info: DownloadFileInfo)

    @Delete
    suspend fun delete(list: List<DownloadFileInfo>)

    @Query("update downloads set state = ${(DownloadFileInfo.STATE_UNKNOWN_ERROR or DownloadFileInfo.STATE_PAUSED)} where state = ${DownloadFileInfo.STATE_DOWNLOADING}")
    fun cleanUp()
}
