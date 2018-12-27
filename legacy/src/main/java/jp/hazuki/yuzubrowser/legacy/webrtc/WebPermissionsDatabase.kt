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

package jp.hazuki.yuzubrowser.legacy.webrtc

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import jp.hazuki.yuzubrowser.legacy.webrtc.core.PermissionState
import jp.hazuki.yuzubrowser.legacy.webrtc.core.WebPermissions
import org.jetbrains.anko.db.*

class WebPermissionsDatabase private constructor(context: Context) : ManagedSQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    fun update(host: String, permissions: WebPermissions?) {
        if (permissions == null) return

        if (permissions.id >= 0) {
            writableDatabase.update(TABLE,
                    COL_CAMERA to permissions.camera.state,
                    COL_MICROPHONE to permissions.microphone.state,
                    COL_MIDI to permissions.midi.state,
                    COL_MEDIA_ID to permissions.mediaId.state)
                    .whereSimple("$COL_ID = ?", permissions.id.toString())
                    .exec()
        } else {
            permissions.id = writableDatabase.insert(TABLE,
                    COL_HOST to host,
                    COL_CAMERA to permissions.camera.state,
                    COL_MICROPHONE to permissions.microphone.state,
                    COL_MIDI to permissions.midi.state,
                    COL_MEDIA_ID to permissions.mediaId.state)
        }
    }

    fun getList(): List<Pair<String, WebPermissions>> {
        val db = readableDatabase
        return db.select(TABLE).orderBy(COL_HOST).parseList(WebPermissionParser())
    }

    operator fun get(host: String): WebPermissions? {
        val db = readableDatabase
        return db.select(TABLE).whereSimple("$COL_HOST = ?", host).parseOpt(PermissionParser())
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.createTable(TABLE, true,
                COL_ID to INTEGER + PRIMARY_KEY + UNIQUE,
                COL_HOST to TEXT + NOT_NULL,
                COL_CAMERA to INTEGER,
                COL_MICROPHONE to INTEGER,
                COL_MIDI to INTEGER,
                COL_MEDIA_ID to INTEGER)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.dropTable(TABLE, true)
    }

    private class WebPermissionParser : MapRowParser<Pair<String, WebPermissions>> {
        override fun parseRow(columns: Map<String, Any?>): Pair<String, WebPermissions> {
            return Pair(
                    columns[COL_HOST] as String,
                    WebPermissions(
                            columns[COL_ID] as Long,
                            PermissionState.from((columns[COL_CAMERA] as Long).toInt()),
                            PermissionState.from((columns[COL_MICROPHONE] as Long).toInt()),
                            PermissionState.from((columns[COL_MIDI] as Long).toInt()),
                            PermissionState.from((columns[COL_MEDIA_ID] as Long).toInt())
                    )
            )
        }

    }

    private class PermissionParser : MapRowParser<WebPermissions> {
        override fun parseRow(columns: Map<String, Any?>): WebPermissions {

            return WebPermissions(
                    columns[COL_ID] as Long,
                    PermissionState.from((columns[COL_CAMERA] as Long).toInt()),
                    PermissionState.from((columns[COL_MICROPHONE] as Long).toInt()),
                    PermissionState.from((columns[COL_MIDI] as Long).toInt()),
                    PermissionState.from((columns[COL_MEDIA_ID] as Long).toInt())
            )
        }
    }


    companion object {
        private const val DB_NAME = "WebPermissions.db"
        private const val DB_VERSION = 1

        private const val TABLE = "permissions"
        private const val COL_ID = "_id"
        private const val COL_HOST = "host"
        private const val COL_CAMERA = "camera"
        private const val COL_MICROPHONE = "mic"
        private const val COL_MIDI = "midi"
        private const val COL_MEDIA_ID = "media_id"

        private var instance: WebPermissionsDatabase? = null

        @Synchronized
        fun getInstance(context: Context): WebPermissionsDatabase {
            if (instance == null) {
                instance = WebPermissionsDatabase(context)
            }
            return instance!!
        }
    }
}