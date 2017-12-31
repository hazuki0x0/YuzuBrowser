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

package jp.hazuki.yuzubrowser.adblock.mining

import android.net.Uri
import android.webkit.WebResourceResponse
import jp.hazuki.yuzubrowser.utils.fastmatch.ContainsHost
import jp.hazuki.yuzubrowser.utils.fastmatch.SimpleHost
import jp.hazuki.yuzubrowser.utils.fastmatch.SimpleUrl
import java.io.IOException
import java.io.InputStream

class MiningProtector {
    val dummy = WebResourceResponse("text/plain", "UTF-8", EmptyInputStream())

    private val blackList = arrayListOf(
            // host block list
            SimpleHost("cnhv.co"),
            SimpleHost("coinhive.com"),
            SimpleHost("coin-hive.com"),
            SimpleHost("gus.host"),
            ContainsHost("jsecoin.com"),
            SimpleHost("static.reasedoper.pw"),
            SimpleHost("mataharirama.xyz"),
            SimpleHost("listat.biz"),
            SimpleHost("lmodr.biz"),
            ContainsHost("crypto-loot.com"),
            ContainsHost("2giga.link"),
            SimpleHost("coinerra.com"),
            SimpleHost("coin-have.com"),
            ContainsHost("afminer.com"),
            ContainsHost("coinblind.com"),
            SimpleUrl("monerominer.rocks"),
            ContainsHost("cloudcoins.co"),
            SimpleHost("coinlab.biz"),
            SimpleHost("papoto.com"),
            SimpleHost("rocks.io"),
            ContainsHost("adminer.com"),
            ContainsHost("ad-miner.com"),
            SimpleHost("party-nngvitbizn.now.sh"),
            ContainsHost("bitporno.com"),
            SimpleHost("cryptoloot.pro"),
            SimpleHost("load.jsecoin.com"),
            SimpleHost("miner.pr0gramm.com"),
            SimpleHost("minemytraffic.com"),
            SimpleHost("ppoi.org"),
            SimpleHost("projectpoi.com"),
            SimpleHost("api.inwemo.com"),
            SimpleHost("jsccnn.com"),
            SimpleHost("jscdndel.com"),
            SimpleHost("coinhiveproxy.com"),
            SimpleHost("coinnebula.com"),
            SimpleHost("cdn.cloudcoins.co"),
            SimpleHost("go.megabanners.cf"),
            SimpleHost(" cryptoloot.pro"),
            SimpleHost("bjorksta.men"),
            SimpleHost("crypto.csgocpu.com"),
            SimpleHost("noblock.pro"),
            SimpleHost("1q2w3.me"),
            SimpleHost("minero.pw"),
            SimpleHost("webmine.cz"),

            // url block list
            SimpleUrl("://kisshentai.net/Content/js/c-hive.js"),
            SimpleUrl("://kiwifarms.net/js/Jawsh/xmr/xmr.min.js"),
            SimpleUrl("://anime.reactor.cc/js/ch/cryptonight.wasm"),
            SimpleUrl("cookiescript.info/libs/"),
            SimpleUrl("://cookiescriptcdn.pro/libs/"),
            SimpleUrl("://baiduccdn1.com/lib/")
    )

    fun isBlock(pageUri: Uri, uri: Uri): Boolean {
        if (pageUri.host == pageUri.host) return false

        return blackList.any { it.match(uri) }
    }

    private class EmptyInputStream : InputStream() {
        @Throws(IOException::class)
        override fun read(): Int = -1
    }
}