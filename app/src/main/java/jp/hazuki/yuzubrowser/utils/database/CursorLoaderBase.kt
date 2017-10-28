/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * 2012 Cynthia Project
 * rewrited for abstract class
 */

package jp.hazuki.yuzubrowser.utils.database

import android.content.Context
import android.database.Cursor
import android.support.v4.content.AsyncTaskLoader

abstract class CursorLoaderBase(context: Context) : AsyncTaskLoader<Cursor>(context) {
    private var mCursor: Cursor? = null

    abstract override fun loadInBackground(): Cursor

    override fun deliverResult(cursor: Cursor?) {
        if (isReset) {
            cursor?.close()
            return
        }
        val oldCursor = mCursor
        mCursor = cursor

        if (isStarted) {
            super.deliverResult(cursor)
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed) {
            oldCursor.close()
        }
    }

    override fun onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor)
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad()
        }
    }

    override fun onStopLoading() {
        cancelLoad()
    }

    override fun onCanceled(cursor: Cursor?) {
        if (cursor != null && !cursor.isClosed) {
            cursor.close()
        }
    }

    override fun onReset() {
        super.onReset()

        onStopLoading()

        if (mCursor != null && !mCursor!!.isClosed) {
            mCursor!!.close()
        }
        mCursor = null
    }
}
