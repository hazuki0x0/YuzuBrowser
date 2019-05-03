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
package jp.hazuki.yuzubrowser.bookmark.netscape

import jp.hazuki.yuzubrowser.bookmark.item.BookmarkFolder
import jp.hazuki.yuzubrowser.bookmark.item.BookmarkSite
import java.io.BufferedWriter
import java.io.IOException
import java.io.Writer

class NetscapeBookmarkCreator(private val parentFolder: BookmarkFolder) {
    private var hierarchy: Int = 0

    @Throws(IOException::class)
    fun create(writer: BufferedWriter) {
        writer.write("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n" +
                "<!-- This is an automatically generated file.\n" +
                "     It will be read and overwritten.\n" +
                "     DO NOT EDIT! -->\n" +
                "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n" +
                "<TITLE>Bookmarks</TITLE>\n" +
                "<H1>Bookmarks</H1>\n" +
                "<DL><p>\n")
        hierarchy = 1
        writeFolder(writer, parentFolder)
        writer.write("</DL><p>\n")
    }

    @Throws(IOException::class)
    private fun writeFolder(writer: Writer, folder: BookmarkFolder) {
        for (item in folder.itemList) {
            val date = java.lang.Long.toString(item.id / 1000)
            val title = item.title
            if (item is BookmarkSite) {
                writeIndent(writer)
                writer.write("<DT><A HREF=\"")
                writer.write(item.url)
                writer.write("\" ADD_DATE=\"")
                writer.write(date)
                writer.write("\" LAST_VISIT=\"")
                writer.write(date)
                writer.write("\" LAST_MODIFIED=\"")
                writer.write(date)
                writer.write("\">")
                if (title != null) writer.write(title)
                writer.write("</A>\n")
            } else if (item is BookmarkFolder) {
                writeIndent(writer)
                writer.write("<DT><H3 ADD_DATE=\"")
                writer.write(date)
                writer.write("\" LAST_MODIFIED=\"")
                writer.write(date)
                writer.write("\">")
                if (title != null) writer.write(title)
                writer.write("</H3>\n")
                writeIndent(writer)
                writer.write("<DL><p>\n")
                hierarchy++
                writeFolder(writer, item)
                hierarchy--
                writeIndent(writer)
                writer.write("</DL><p>\n")
            }
        }
    }

    @Throws(IOException::class)
    private fun writeIndent(writer: Writer) {
        if (hierarchy <= 0) {
            return
        }
        for (i in hierarchy - 1 downTo 0) {
            writer.write("    ")
        }
    }
}
