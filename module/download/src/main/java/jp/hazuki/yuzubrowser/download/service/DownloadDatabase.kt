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

package jp.hazuki.yuzubrowser.download.service

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import jp.hazuki.yuzubrowser.download.compatible.ConvertDownloadInfo
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.utils.toDocumentFile
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import org.jetbrains.anko.db.*

class DownloadDatabase private constructor(context: Context) : ManagedSQLiteOpenHelper(context, NAME, null, VERSION) {
    private val parser = InfoParser(context)

    fun insert(info: DownloadFileInfo) = use {
        info.id = insert(TABLE,
                COL_URL to info.url,
                COL_MIME_TYPE to info.mimeType,
                COL_ROOT to info.root.uri.toString(),
                COL_NAME to info.name,
                COL_SIZE to info.size,
                COL_RESUMABLE to info.resumable,
                COL_START_TIME to info.startTime,
                COL_STATE to info.state)
    }

    fun update(info: DownloadFileInfo) = use {
        update(TABLE,
                COL_URL to info.url,
                COL_MIME_TYPE to info.mimeType,
                COL_ROOT to info.root.uri.toString(),
                COL_NAME to info.name,
                COL_SIZE to info.size,
                COL_RESUMABLE to info.resumable,
                COL_START_TIME to info.startTime,
                COL_STATE to info.state)
                .whereArgs("$COL_ID = ${info.id}")
                .exec()
    }

    fun updateWithEmptyRoot(info: DownloadFileInfo) = use {
        update(TABLE,
                COL_URL to info.url,
                COL_MIME_TYPE to info.mimeType,
                COL_ROOT to "",
                COL_NAME to info.name,
                COL_SIZE to info.size,
                COL_RESUMABLE to info.resumable,
                COL_START_TIME to info.startTime,
                COL_STATE to info.state)
                .whereArgs("$COL_ID = ${info.id}")
                .exec()
    }

    operator fun get(id: Long): DownloadFileInfo? = use {
        select(TABLE).whereArgs("$COL_ID = $id").parseOpt(parser)
    }

    fun getList(offset: Int, count: Int): List<DownloadFileInfo> = use {
        select(TABLE).orderBy(COL_START_TIME, SqlOrderDirection.DESC).limit(offset, count).parseList(parser)
    }

    fun delete(id: Long) = use {
        if (id < 0) throw IllegalArgumentException("id must be greater than or equal to 0")
        return@use delete(TABLE, "$COL_ID = $id") > 0
    }

    fun delete(ids: List<DownloadFileInfo>) = use {
        transaction {
            ids.forEach { delete(TABLE, "$COL_ID = ${it.id}") }
        }
    }

    fun deleteAllHistories() = use {
        delete(TABLE, "$COL_STATE <> ${DownloadFileInfo.STATE_DOWNLOADING}")
    }

    fun cleanUp() = use {
        update(TABLE, COL_STATE to (DownloadFileInfo.STATE_UNKNOWN_ERROR or DownloadFileInfo.STATE_PAUSED))
                .whereArgs("$COL_STATE = ${DownloadFileInfo.STATE_DOWNLOADING}")
                .exec()
    }

    fun convert(list: List<ConvertDownloadInfo>) = use {
        transaction {
            list.forEach { info ->
                insert(TABLE,
                        COL_URL to info.url,
                        COL_MIME_TYPE to "",
                        COL_ROOT to info.root,
                        COL_NAME to info.name,
                        COL_SIZE to -1,
                        COL_RESUMABLE to false,
                        COL_START_TIME to info.time,
                        COL_STATE to info.state)
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(TABLE, true,
                COL_ID to INTEGER + PRIMARY_KEY + UNIQUE,
                COL_URL to TEXT + NOT_NULL,
                COL_MIME_TYPE to TEXT,
                COL_ROOT to TEXT + NOT_NULL,
                COL_NAME to TEXT,
                COL_SIZE to INTEGER,
                COL_RESUMABLE to INTEGER,
                COL_START_TIME to INTEGER,
                COL_STATE to INTEGER)
    }

    private class InfoParser(val context: Context) : MapRowParser<DownloadFileInfo> {
        override fun parseRow(columns: Map<String, Any?>): DownloadFileInfo {
            return DownloadFileInfo(
                    columns[COL_ID] as Long,
                    columns[COL_URL] as String,
                    columns[COL_MIME_TYPE] as String,
                    Uri.parse(
                            (columns[COL_ROOT] as String?).let {
                                if (it.isNullOrEmpty()) AppPrefs.download_folder.get() else it
                            }
                    ).toDocumentFile(context),
                    columns[COL_NAME] as String,
                    columns[COL_SIZE] as Long,
                    (columns[COL_RESUMABLE] as Long) != 0L,
                    columns[COL_START_TIME] as Long,
                    (columns[COL_STATE] as Long).toInt())
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable(TABLE, true)
    }

    companion object {
        private const val NAME = "download.db"
        private const val VERSION = 1

        private const val TABLE = "downloads"

        private const val COL_ID = "_id"
        private const val COL_URL = "url"
        private const val COL_MIME_TYPE = "mimeType"
        private const val COL_ROOT = "root"
        private const val COL_NAME = "name"
        private const val COL_SIZE = "size"
        private const val COL_RESUMABLE = "resumable"
        private const val COL_START_TIME = "start_time"
        private const val COL_STATE = "state"

        @SuppressLint("StaticFieldLeak")
        private var instance: DownloadDatabase? = null

        @Synchronized
        fun getInstance(context: Context): DownloadDatabase {
            if (instance == null) {
                instance = DownloadDatabase(context.applicationContext)
            }
            return instance!!
        }
    }
}
