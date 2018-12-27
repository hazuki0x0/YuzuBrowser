package jp.hazuki.yuzubrowser.legacy.action

import android.os.Parcel
import android.os.Parcelable
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jp.hazuki.yuzubrowser.legacy.utils.ErrorReport
import jp.hazuki.yuzubrowser.legacy.utils.JsonUtils
import jp.hazuki.yuzubrowser.legacy.utils.util.JsonConvertable
import java.io.IOException
import java.io.StringWriter
import java.util.*

class Action : ArrayList<SingleAction>, Parcelable, JsonConvertable {

    constructor() : super(1)

    constructor(action: Action) : super(action)

    constructor(action: SingleAction) : super(1) {
        add(action)
    }

    constructor(jsonStr: String) : super(1) {
        fromJsonString(jsonStr)
    }

    constructor(actions: Collection<SingleAction>) : super(actions)

    constructor(source: Parcel) : super() {
        source.readList(this, SingleAction::class.java.classLoader)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeList(this)
    }

    @Throws(IOException::class)
    fun writeAction(field_name: String, generator: JsonGenerator) {
        generator.writeFieldName(field_name)
        writeAction(generator)
    }

    @Throws(IOException::class)
    fun writeAction(generator: JsonGenerator) {
        generator.writeStartArray()
        for (action in this) {
            generator.writeStartArray()
            action.writeIdAndData(generator)
            generator.writeEndArray()
        }
        generator.writeEndArray()
    }

    @Throws(IOException::class)
    fun loadAction(parser: JsonParser): Boolean {
        if (parser.nextToken() != JsonToken.START_ARRAY) return false
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (parser.currentToken != JsonToken.START_ARRAY) return false

            parser.nextToken()
            if (parser.currentToken == JsonToken.VALUE_NUMBER_INT) {
                val id = parser.intValue

                //in makeInstance, should use getCurrentToken
                val action: SingleAction
                action = SingleAction.makeInstance(id, parser)
                //parser.skipChildren();
                if (parser.currentToken != JsonToken.END_ARRAY && parser.nextToken() != JsonToken.END_ARRAY)
                    return false
                add(action)
            } else if (parser.currentToken != JsonToken.END_ARRAY) {
                return false
            }
        }
        return true
    }

    override fun toJsonString(): String? {
        try {
            val writer = StringWriter()
            JsonUtils.getFactory().createGenerator(writer).use {
                writeAction(it)
            }
            return writer.toString()
        } catch (e: IOException) {

            ErrorReport.printAndWriteLog(e)
        }
        return null
    }

    override fun fromJsonString(str: String): Boolean {
        clear()

        try {
            JsonUtils.getFactory().createParser(str).use {
                return loadAction(it)
            }
        } catch (e: IOException) {
            ErrorReport.printAndWriteLog(e)
        }
        return false
    }

    fun toString(nameArray: ActionNameArray): String? {
        return if (isEmpty()) null else get(0).toString(nameArray)
    }

    companion object {
        private const val serialVersionUID = 1712925333386047748L

        @JvmField
        val CREATOR: Parcelable.Creator<Action> = object : Parcelable.Creator<Action> {
            override fun createFromParcel(source: Parcel): Action {
                return Action(source)
            }

            override fun newArray(size: Int): Array<Action?> {
                return arrayOfNulls(size)
            }
        }

        fun makeInstance(id: Int): Action {
            return Action(SingleAction.makeInstance(id))
        }
    }
}
