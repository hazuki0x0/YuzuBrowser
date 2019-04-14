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

package jp.hazuki.yuzubrowser.adblock.filter.abp

import assertk.assertThat
import assertk.assertions.isEqualTo
import jp.hazuki.yuzubrowser.adblock.filter.abp.io.AbpFilterReader
import jp.hazuki.yuzubrowser.adblock.filter.abp.io.AbpFilterWriter
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class AbpFilterDecoderTest {

    @Test
    fun testDecodeAndWriteAndRead() {
        val test = "[Adblock Plus 2.0]\n" +
                "! Title: 280blocker for japanese mobile site\n" +
                "! Homepage: https://280blocker.net\n" +
                "! Licence: CC BY-NC-ND 4.0 (https://creativecommons.org/licenses/by-nc-nd/4.0/)\n" +
                "! Last updated: 2019-04-01\n" +
                "\n" +
                "||media.line.naver.jp^\$third-party,domain=~naver.jp|~tsukuba.ac.jp|~bando.lg.jp\n" +
                "||media.line.me/js/line-button.js\$domain=~tokyo-gas.co.jp|~tsukuba.ac.jp|~bando.lg.jp\n" +
                "!||player.performgroup.com\n" +
                "||player.performgroup.com\$domain=full-count.jp|the-ans.jp|mainichi.jp\n" +
                "||player.performgroup.com\$domain=full-count.jp|mainichi.jp\n" +
                "bbhylife.com##a[href^=\"https://sswhylife.com/\"]\n" +
                "mainichi.jp###cxPageModalBnr\n" +
                "work-mikke.jp##.blog-footer-pickup-container\n" +
                "zai.diamond.jp##.sp-floating-banner\n" +
                "yomiuri.co.jp##.p-article-action-btn\n" +
                "pokemon-matome.net##.rssinline_bbs\n" +
                "\n" +
                "!2019/03/24\n" +
                "matomedane.jp###AdvHeader2\n" +
                "viralhighway.com###ist_overlay2\n" +
                "viralhighway.com##a[href^=\"http://taurus-news.com/\"]\n" +
                "romasagars-matome.site##.adm_overlay\n" +
                "netnavinavi.net##.ffixbanner\n" +
                "news.mixi.jp##.shareButton02\n" +
                "||bdv.bidvertiser.com\n" +
                "||pushance.com\n" +
                "||tharbadir.com\n" +
                "||go.oclasrv.com\n" +
                "||58040d4c01949f0c1.com\n" +
                "||js.winc-ad.com\n" +
                "||fam-8.net\n" +
                "!###gn_interstitial_area\n" +
                "##.act-sbmlist\n"

        val decoder = AbpFilterDecoder()
        val reader = test.byteInputStream().bufferedReader()
        assertThat(decoder.checkHeader(reader)).isEqualTo(true)
        val set = decoder.decode(reader, "")
        assertThat(set.filterInfo.title).isEqualTo("280blocker for japanese mobile site")
        assertThat(set.filterInfo.homePage).isEqualTo("https://280blocker.net")
        assertThat(set.filterInfo.lastUpdate).isEqualTo("2019-04-01")
        assertThat(set.filterInfo.version).isEqualTo(null)
        assertThat(set.filterInfo.expires).isEqualTo(null)

        val byteArray = ByteArrayOutputStream()
        val writer = AbpFilterWriter()
        writer.write(byteArray, set.blackList)

        val input = ByteArrayInputStream(byteArray.toByteArray())
        val fr = AbpFilterReader(input)
        assertThat(fr.checkHeader()).isEqualTo(true)
        val decoded = fr.readAll()

        assertThat(decoded == set.blackList).isEqualTo(true)
    }
}