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

@file:Suppress("NOTHING_TO_INLINE")

package jp.hazuki.yuzubrowser.core.utility.extensions

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import org.jetbrains.anko.*
import org.jetbrains.anko.internals.AnkoInternals
import java.io.Serializable

inline fun <reified T : Activity> androidx.fragment.app.Fragment.startActivity(vararg params: Pair<String, Any?>) = AnkoInternals.internalStartActivity(activity!!, T::class.java, params)

inline fun <reified T : Activity> androidx.fragment.app.Fragment.startActivityForResult(requestCode: Int, vararg params: Pair<String, Any?>) =
        startActivityForResult(AnkoInternals.createIntent(activity!!, T::class.java, params), requestCode)

inline fun <reified T : Service> androidx.fragment.app.Fragment.startService(vararg params: Pair<String, Any?>) =
        AnkoInternals.internalStartService(activity!!, T::class.java, params)

inline fun <reified T : Service> androidx.fragment.app.Fragment.stopService(vararg params: Pair<String, Any?>) =
        AnkoInternals.internalStopService(activity!!, T::class.java, params)

inline fun <reified T : Any> androidx.fragment.app.Fragment.intentFor(vararg params: Pair<String, Any?>): Intent =
        AnkoInternals.createIntent(activity!!, T::class.java, params)

fun Fragment.intentFor(targetActivity: String, vararg params: Pair<String, Any?>): Intent {
    return Intent().apply {
        setClassName(activity!!, targetActivity)
        if (params.isNotEmpty()) fillIntentArguments(this, params)
    }
}

inline fun androidx.fragment.app.Fragment.browse(url: String, newTask: Boolean = false) = activity!!.browse(url, newTask)

inline fun androidx.fragment.app.Fragment.share(text: String, subject: String = "") = activity!!.share(text, subject)

inline fun androidx.fragment.app.Fragment.email(email: String, subject: String = "", text: String = "") = activity!!.email(email, subject, text)

inline fun androidx.fragment.app.Fragment.makeCall(number: String): Boolean = activity!!.makeCall(number)

inline fun androidx.fragment.app.Fragment.sendSMS(number: String, text: String = ""): Boolean = activity!!.sendSMS(number, text)

private fun fillIntentArguments(intent: Intent, params: Array<out Pair<String, Any?>>) {
    params.forEach {
        val value = it.second
        when (value) {
            null -> intent.putExtra(it.first, null as Serializable?)
            is Int -> intent.putExtra(it.first, value)
            is Long -> intent.putExtra(it.first, value)
            is CharSequence -> intent.putExtra(it.first, value)
            is String -> intent.putExtra(it.first, value)
            is Float -> intent.putExtra(it.first, value)
            is Double -> intent.putExtra(it.first, value)
            is Char -> intent.putExtra(it.first, value)
            is Short -> intent.putExtra(it.first, value)
            is Boolean -> intent.putExtra(it.first, value)
            is Serializable -> intent.putExtra(it.first, value)
            is Bundle -> intent.putExtra(it.first, value)
            is Parcelable -> intent.putExtra(it.first, value)
            is Array<*> -> when {
                value.isArrayOf<CharSequence>() -> intent.putExtra(it.first, value)
                value.isArrayOf<String>() -> intent.putExtra(it.first, value)
                value.isArrayOf<Parcelable>() -> intent.putExtra(it.first, value)
                else -> throw AnkoException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
            }
            is IntArray -> intent.putExtra(it.first, value)
            is LongArray -> intent.putExtra(it.first, value)
            is FloatArray -> intent.putExtra(it.first, value)
            is DoubleArray -> intent.putExtra(it.first, value)
            is CharArray -> intent.putExtra(it.first, value)
            is ShortArray -> intent.putExtra(it.first, value)
            is BooleanArray -> intent.putExtra(it.first, value)
            else -> throw AnkoException("Intent extra ${it.first} has wrong type ${value.javaClass.name}")
        }
        return@forEach
    }
}