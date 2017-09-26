package jp.hazuki.yuzubrowser.pattern.action

import android.annotation.SuppressLint
import android.content.Context
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.pattern.PatternAction
import jp.hazuki.yuzubrowser.tab.manager.MainTabData
import java.io.IOException

class WebSettingPatternAction : PatternAction {

    var userAgentString: String? = null
        private set
    var javaScriptSetting: Int = UNDEFINED
        private set
    var navLock: Int = UNDEFINED
        private set
    var loadImage: Int = UNDEFINED
        private set

    constructor(ua: String?, js: Int, navLock: Int, image: Int) {
        userAgentString = ua
        javaScriptSetting = js
        this.navLock = navLock
        loadImage = image
    }

    @Throws(IOException::class)
    constructor(parser: JsonParser) {
        if (parser.nextToken() != JsonToken.START_OBJECT) return
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            if (parser.currentToken != JsonToken.FIELD_NAME) return
            if (FIELD_NAME_UA == parser.currentName) {
                if (parser.nextToken() != JsonToken.VALUE_STRING) return
                userAgentString = parser.text
                continue
            }
            if (FIELD_NAME_JS == parser.currentName) {
                if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return
                javaScriptSetting = parser.intValue
                continue
            }
            if (FIELD_NAME_NAV_LOCK == parser.currentName) {
                if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return
                navLock = parser.intValue
                continue
            }
            parser.skipChildren()
        }
    }

    override fun getTypeId(): Int {
        return PatternAction.WEB_SETTING
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.pattern_change_websettings)
    }

    @Throws(IOException::class)
    override fun write(generator: JsonGenerator): Boolean {
        generator.writeNumber(PatternAction.WEB_SETTING)
        generator.writeStartObject()
        if (userAgentString != null)
            generator.writeStringField(FIELD_NAME_UA, userAgentString)
        generator.writeNumberField(FIELD_NAME_JS, javaScriptSetting)
        generator.writeNumberField(FIELD_NAME_NAV_LOCK, navLock)
        generator.writeEndObject()
        return true
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun run(context: Context, tab: MainTabData, url: String): Boolean {
        val settings = tab.mWebView.settings

        if (userAgentString != null)
            settings.userAgentString = userAgentString

        when (javaScriptSetting) {
            ENABLE -> settings.javaScriptEnabled = true
            DISABLE -> settings.javaScriptEnabled = false
        }

        when (navLock) {
            ENABLE -> tab.isNavLock = true
            DISABLE -> tab.isNavLock = false
        }

        when (loadImage) {
            ENABLE -> settings.blockNetworkImage = false
            DISABLE -> settings.blockNetworkImage = true
        }
        return false
    }

    companion object {
        const val UNDEFINED = 0
        const val ENABLE = 1
        const val DISABLE = 2

        private const val FIELD_NAME_UA = "0"
        private const val FIELD_NAME_JS = "1"
        private const val FIELD_NAME_NAV_LOCK = "2"
    }
}
