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

package jp.hazuki.yuzubrowser.legacy.action

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.legacy.action.item.*
import jp.hazuki.yuzubrowser.legacy.action.item.startactivity.StartActivitySingleAction
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import java.io.IOException

open class SingleAction : Parcelable {
    val id: Int

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(id)
    }

    protected constructor(source: Parcel) {
        this.id = source.readInt()
    }

    //for extended class
    protected constructor(id: Int) {
        this.id = id
        //if(id < 0) throw new IllegalArgumentException();
    }

    //node can be null
    @Throws(IOException::class)
    private constructor(id: Int, reader: JsonReader?) : this(id) {
        reader?.skipValue()
    }

    @Throws(IOException::class)
    open fun writeIdAndData(writer: JsonWriter) {
        writer.value(id)
        writer.nullValue()
    }

    open fun showMainPreference(context: ActionActivity): StartActivityInfo? {
        return null
    }

    open fun showSubPreference(context: ActionActivity): StartActivityInfo? {
        return null
    }

    open fun toString(nameArray: ActionNameArray): String? {
        for ((i, value) in nameArray.actionValues.withIndex()) {
            if (value == id)
                return nameArray.actionList[i]
        }
        return null
    }

    companion object {
        const val GO_BACK = 1000
        const val GO_FORWARD = 1001
        const val WEB_RELOAD_STOP = 1005
        const val WEB_RELOAD = 1006
        const val WEB_STOP = 1007
        const val GO_HOME = 1020
        const val ZOOM_IN = 1205
        const val ZOOM_OUT = 1206
        const val PAGE_UP = 1207
        const val PAGE_DOWN = 1208
        const val PAGE_TOP = 1209
        const val PAGE_BOTTOM = 1210
        const val PAGE_SCROLL = 1215
        const val PAGE_FAST_SCROLL = 1216
        const val PAGE_AUTO_SCROLL = 1217
        const val FOCUS_UP = 1220
        const val FOCUS_DOWN = 1221
        const val FOCUS_LEFT = 1222
        const val FOCUS_RIGHT = 1223
        const val FOCUS_CLICK = 1224
        const val TOGGLE_JS = 2000
        const val TOGGLE_IMAGE = 2001
        const val TOGGLE_COOKIE = 2002
        const val TOGGLE_USERJS = 2200
        const val TOGGLE_NAV_LOCK = 2300
        const val PAGE_INFO = 5000
        const val COPY_URL = 5001
        const val COPY_TITLE = 5002
        const val COPY_TITLE_URL = 5003
        const val TAB_HISTORY = 5010
        const val MOUSE_POINTER = 5015
        const val FIND_ON_PAGE = 5020
        const val SAVE_SCREENSHOT = 5030
        const val SHARE_SCREENSHOT = 5031
        const val SAVE_PAGE = 5035
        const val OPEN_URL = 5200
        const val TRANSLATE_PAGE = 5300
        const val NEW_TAB = 10000
        const val CLOSE_TAB = 10001
        const val CLOSE_ALL = 10002
        const val CLOSE_OTHERS = 10003
        const val CLOSE_AUTO_SELECT = 10100
        const val LEFT_TAB = 10005
        const val RIGHT_TAB = 10006
        const val SWAP_LEFT_TAB = 10007
        const val SWAP_RIGHT_TAB = 10008
        const val TAB_LIST = 10010
        const val CLOSE_ALL_LEFT = 10015
        const val CLOSE_ALL_RIGHT = 10016
        const val RESTORE_TAB = 10020
        const val REPLICATE_TAB = 10021
        const val SHOW_SEARCHBOX = 35000
        const val PASTE_SEARCHBOX = 35001
        const val PASTE_GO = 35002
        const val SHOW_BOOKMARK = 35010
        const val SHOW_HISTORY = 35011
        const val SHOW_DOWNLOADS = 35012
        const val SHOW_SETTINGS = 35013
        const val OPEN_SPEED_DIAL = 35014
        const val ADD_BOOKMARK = 35020
        const val ADD_SPEED_DIAL = 35021
        const val ADD_PATTERN = 35022
        const val ADD_TO_HOME = 35023
        const val SUB_GESTURE = 35031
        const val CLEAR_DATA = 35300
        const val SHOW_PROXY_SETTING = 35301
        const val ORIENTATION_SETTING = 35302
        const val OPEN_LINK_SETTING = 35304
        const val USERAGENT_SETTING = 35305
        const val TEXTSIZE_SETTING = 35306
        const val USERJS_SETTING = 35307
        const val WEB_ENCODE_SETTING = 35308
        const val DEFALUT_USERAGENT_SETTING = 35309
        const val RENDER_ALL_SETTING = 35400
        const val RENDER_SETTING = 35401
        const val TOGGLE_VISIBLE_TAB = 38000
        const val TOGGLE_VISIBLE_URL = 38001
        const val TOGGLE_VISIBLE_PROGRESS = 38002
        const val TOGGLE_VISIBLE_CUSTOM = 38003
        const val TOGGLE_WEB_TITLEBAR = 38010
        const val TOGGLE_WEB_GESTURE = 38100
        const val TOGGLE_FLICK = 38101
        const val TOGGLE_QUICK_CONTROL = 38102
        const val TOGGLE_MULTI_FINGER_GESTURE = 38103
        const val TOGGLE_AD_BLOCK = 38200
        const val OPEN_BLACK_LIST = 38210
        const val OPEN_WHITE_LIST = 38211
        const val OPEN_WHITE_PATE_LIST = 38212
        const val ADD_WHITE_LIST_PAGE = 38220
        const val SHARE_WEB = 50000
        const val OPEN_OTHER = 50001
        const val START_ACTIVITY = 50005
        const val TOGGLE_FULL_SCREEN = 50100
        const val OPEN_OPTIONS_MENU = 50120
        const val CUSTOM_MENU = 80000
        const val FINISH = 90001
        const val MINIMIZE = 90005
        const val CUSTOM_ACTION = 100000
        const val VIBRATION = 100100
        const val TOAST = 100101
        const val PRIVATE = 100110
        const val VIEW_SOURCE = 101000
        const val PRINT = 101010
        const val TAB_PINNING = 101020
        const val ALL_ACTION = 101030
        const val READER_MODE = 101040
        const val READ_IT_LATER = 101050
        const val READ_IT_LATER_LIST = 101051
        const val LPRESS_OPEN = -10
        const val LPRESS_OPEN_NEW = -11
        const val LPRESS_OPEN_BG = -12
        const val LPRESS_OPEN_NEW_RIGHT = -13
        const val LPRESS_OPEN_BG_RIGHT = -14
        const val LPRESS_SHARE = -50
        const val LPRESS_OPEN_OTHERS = -51
        const val LPRESS_COPY_URL = -52
        const val LPRESS_SAVE_PAGE_AS = -53
        const val LPRESS_SAVE_PAGE = -54
        const val LPRESS_OPEN_IMAGE = -110
        const val LPRESS_OPEN_IMAGE_NEW = -111
        const val LPRESS_OPEN_IMAGE_BG = -112
        const val LPRESS_OPEN_IMAGE_NEW_RIGHT = -113
        const val LPRESS_OPEN_IMAGE_BG_RIGHT = -114
        const val LPRESS_SHARE_IMAGE_URL = -150
        const val LPRESS_OPEN_IMAGE_OTHERS = -151
        const val LPRESS_COPY_IMAGE_URL = -152
        const val LPRESS_SAVE_IMAGE_AS = -153
        const val LPRESS_GOOGLE_IMAGE_SEARCH = -154
        const val LPRESS_IMAGE_RES_BLOCK = -155
        const val LPRESS_PATTERN_MATCH = -156
        const val LPRESS_COPY_LINK_TEXT = -157
        const val LPRESS_SHARE_IMAGE = -158
        const val LPRESS_SAVE_IMAGE = -159
        const val LPRESS_ADD_BLACK_LIST = -160
        const val LPRESS_ADD_IMAGE_BLACK_LIST = -161
        const val LPRESS_ADD_WHITE_LIST = -162
        const val LPRESS_ADD_IMAGE_WHITE_LIST = -163

        @JvmStatic
        fun makeInstance(id: Int): SingleAction {
            try {
                return makeInstance(id, null)
            } catch (e: IOException) {
                ErrorReport.printAndWriteLog(e)
            }

            throw IllegalStateException()
        }

        @JvmField
        val CREATOR: Parcelable.Creator<SingleAction> = object : Parcelable.Creator<SingleAction> {
            override fun createFromParcel(source: Parcel): SingleAction {
                return SingleAction(source)
            }

            override fun newArray(size: Int): Array<SingleAction?> {
                return arrayOfNulls(size)
            }
        }

        @Throws(IOException::class)
        fun makeInstance(id: Int, parser: JsonReader?): SingleAction {
            return when (id) {
                GO_BACK -> GoBackSingleAction(id, parser)
                PAGE_SCROLL -> WebScrollSingleAction(id, parser)
                PAGE_AUTO_SCROLL -> AutoPageScrollAction(id, parser)
                MOUSE_POINTER -> MousePointerSingleAction(id, parser)
                FIND_ON_PAGE -> FindOnPageAction(id, parser)
                SAVE_SCREENSHOT -> SaveScreenshotSingleAction(id, parser)
                SHARE_SCREENSHOT -> ShareScreenshotSingleAction(id, parser)
                OPEN_URL -> OpenUrlSingleAction(id, parser)
                TRANSLATE_PAGE -> TranslatePageSingleAction(id, parser)
                CLOSE_TAB -> CloseTabSingleAction(id, parser)
                LEFT_TAB, RIGHT_TAB -> LeftRightTabSingleAction(id, parser)
                TAB_LIST -> TabListSingleAction(id, parser)
                SHOW_SEARCHBOX -> ShowSearchBoxAction(id, parser)
                PASTE_SEARCHBOX -> PasteSearchBoxAction(id, parser)
                PASTE_GO -> PasteGoSingleAction(id, parser)
                TOGGLE_AD_BLOCK, PRIVATE -> WithToastAction(id, parser)
                START_ACTIVITY -> StartActivitySingleAction(id, parser)
                OPEN_OPTIONS_MENU -> OpenOptionsMenuAction(id, parser)
                CUSTOM_MENU -> CustomMenuSingleAction(id, parser)
                FINISH -> FinishSingleAction(id, parser)
                CUSTOM_ACTION -> CustomSingleAction(id, parser)
                VIBRATION -> VibrationSingleAction(id, parser)
                TOAST -> ToastAction(id, parser)
                CLOSE_AUTO_SELECT -> CloseAutoSelectAction(id, parser)
                else -> SingleAction(id, parser)
            }
        }

        fun checkSubPreference(id: Int): Boolean {
            return when (id) {
                GO_BACK, PAGE_SCROLL,
                PAGE_AUTO_SCROLL,
                MOUSE_POINTER,
                FIND_ON_PAGE,
                SAVE_SCREENSHOT,
                SHARE_SCREENSHOT,
                OPEN_URL,
                TRANSLATE_PAGE,
                CLOSE_TAB,
                LEFT_TAB,
                RIGHT_TAB,
                TAB_LIST,
                SHOW_SEARCHBOX,
                PASTE_SEARCHBOX,
                PASTE_GO,
                TOGGLE_AD_BLOCK,
                START_ACTIVITY,
                OPEN_OPTIONS_MENU,
                CUSTOM_MENU,
                FINISH,
                CUSTOM_ACTION,
                VIBRATION,
                TOAST,
                PRIVATE,
                CLOSE_AUTO_SELECT -> true
                else -> false
            }
        }
    }
}
