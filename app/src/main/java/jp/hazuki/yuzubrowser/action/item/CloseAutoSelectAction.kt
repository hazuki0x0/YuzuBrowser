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

package jp.hazuki.yuzubrowser.action.item

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.action.Action
import jp.hazuki.yuzubrowser.action.SingleAction
import jp.hazuki.yuzubrowser.action.view.ActionActivity
import jp.hazuki.yuzubrowser.action.view.CloseAutoSelectActivity
import jp.hazuki.yuzubrowser.utils.app.StartActivityInfo
import java.io.IOException

class CloseAutoSelectAction : SingleAction, Parcelable {

    var defaultAction: Action
        private set
    var intentAction: Action
        private set
    var windowAction: Action
        private set

    @Throws(IOException::class)
    constructor(id: Int, parser: JsonParser?) : super(id) {

        defaultAction = Action()
        intentAction = Action()
        windowAction = Action()
        if (parser != null) {
            if (parser.nextToken() != JsonToken.START_OBJECT) return
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                if (parser.currentToken != JsonToken.FIELD_NAME) return
                if (DEFAULT == parser.currentName) {
                    defaultAction.loadAction(parser)
                    continue
                }
                if (INTENT == parser.currentName) {
                    intentAction.loadAction(parser)
                    continue
                }
                if (WINDOW == parser.currentName) {
                    windowAction.loadAction(parser)
                    continue
                }

                parser.skipChildren()
            }
        } else {
            defaultAction.add(SingleAction.makeInstance(SingleAction.CLOSE_TAB))
            intentAction.add(CloseTabSingleAction())
            intentAction.add(SingleAction.makeInstance(SingleAction.MINIMIZE))
            windowAction.add(SingleAction.makeInstance(SingleAction.CLOSE_TAB))
        }
    }

    @Throws(IOException::class)
    override fun writeIdAndData(generator: JsonGenerator) {
        generator.writeNumber(id)
        generator.writeStartObject()
        defaultAction.writeAction(DEFAULT, generator)
        intentAction.writeAction(INTENT, generator)
        windowAction.writeAction(WINDOW, generator)
        generator.writeEndObject()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
        dest.writeParcelable(defaultAction, flags)
        dest.writeParcelable(intentAction, flags)
        dest.writeParcelable(windowAction, flags)
    }

    private constructor(source: Parcel) : super(source.readInt()) {
        defaultAction = source.readParcelable(Action::class.java.classLoader)!!
        intentAction = source.readParcelable(Action::class.java.classLoader)!!
        windowAction = source.readParcelable(Action::class.java.classLoader)!!
    }

    override fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        return CloseAutoSelectActivity.Builder(context)
                .setListener { def, intent, window ->
                    defaultAction = def
                    intentAction = intent
                    windowAction = window
                }
                .getActivityInfo(defaultAction, intentAction, windowAction)
    }

    companion object {
        private const val DEFAULT = "0"
        private const val INTENT = "1"
        private const val WINDOW = "2"

        @JvmField
        val CREATOR: Parcelable.Creator<CloseAutoSelectAction> = object : Parcelable.Creator<CloseAutoSelectAction> {
            override fun createFromParcel(source: Parcel): CloseAutoSelectAction {
                return CloseAutoSelectAction(source)
            }

            override fun newArray(size: Int): Array<CloseAutoSelectAction?> {
                return arrayOfNulls(size)
            }
        }
    }
}
