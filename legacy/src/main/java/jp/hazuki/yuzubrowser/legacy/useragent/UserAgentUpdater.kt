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

package jp.hazuki.yuzubrowser.legacy.useragent

fun UserAgentList.init() {
    add(UserAgent("android", USER_AGENT_ANDROID))
    add(UserAgent("android Tablet", USER_AGENT_ANDROID_TAB))
    add(UserAgent("iPhone", USER_AGENT_IPHONE))
    add(UserAgent("iPad", USER_AGENT_IPAD))
    add(UserAgent("PC", USER_AGENT_PC))
}

fun UserAgentList.upgrade() {
    forEach {
        when (it.name) {
            "android" -> {
                it.useragent = USER_AGENT_ANDROID
            }
            "android Tablet" -> {
                it.useragent = USER_AGENT_ANDROID_TAB
            }
            "iPhone" -> {
                it.useragent = USER_AGENT_IPHONE
            }
            "iPad" -> {
                it.useragent = USER_AGENT_IPAD
            }
            "PC" -> {
                it.useragent = USER_AGENT_PC
            }
        }
    }
}


private const val USER_AGENT_ANDROID = "Mozilla/5.0 (Linux; Android 9; Pixel 3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.90 Mobile Safari/537.36"
private const val USER_AGENT_ANDROID_TAB = "Mozilla/5.0 (Linux; Android 7.1.1; Nexus 9 Build/N4F26M) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.90 Safari/537.36"
private const val USER_AGENT_IPHONE = "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1"
private const val USER_AGENT_IPAD = "Mozilla/5.0 (iPad; CPU OS 12_0_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.0 Mobile/15E148 Safari/604.1"
private const val USER_AGENT_PC = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36"