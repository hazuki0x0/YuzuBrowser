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

package jp.hazuki.yuzubrowser.toolbar.sub

import android.content.Context
import android.view.LayoutInflater
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener

import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.toolbar.SubToolbar
import jp.hazuki.yuzubrowser.webkit.CustomWebView
import kotlinx.android.synthetic.main.page_fast_scroll.view.*

class WebViewPageFastScroller(context: Context) : SubToolbar(context) {
    private var mOnEndListener: (() -> Boolean)? = null
    private var mWeb: CustomWebView? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.page_fast_scroll, this)

        buttonEnd.setOnClickListener { close() }
    }

    fun show(web: CustomWebView) {
        mWeb = web

        web.setMyOnScrollChangedListener { _, _, _, _ ->
            seekBar.run {
                max = web.computeVerticalScrollRangeMethod() - web.computeVerticalScrollExtentMethod()
                progress = web.computeVerticalScrollOffsetMethod()
            }
        }
        buttonUp.run {
            setOnClickListener { web.pageUp(false) }
            setOnLongClickListener {
                web.pageUp(true)
                true
            }
        }
        buttonDown.run {
            setOnClickListener { web.pageDown(false) }
            setOnLongClickListener {
                web.pageDown(true)
                true
            }
        }
        seekBar.run {
            max = web.computeVerticalScrollRangeMethod() - web.computeVerticalScrollExtentMethod()
            progress = web.computeVerticalScrollOffsetMethod()
            setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    web.scrollTo(web.computeHorizontalScrollOffsetMethod(), seekBar.progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    seekBar.max = web.computeVerticalScrollRangeMethod() - web.computeVerticalScrollExtentMethod()
                }

                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        web.scrollTo(web.computeHorizontalScrollOffsetMethod(), progress)
                    }
                }
            })
        }
    }

    fun close() {
        mWeb?.run {
            setMyOnScrollChangedListener(null)
            mWeb = null
        }
        mOnEndListener?.invoke()
    }

    fun setOnEndListener(l: (() -> Boolean)?) {
        mOnEndListener = l
    }
}
