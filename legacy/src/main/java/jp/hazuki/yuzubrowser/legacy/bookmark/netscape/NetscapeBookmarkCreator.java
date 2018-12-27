/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.hazuki.yuzubrowser.legacy.bookmark.netscape;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkItem;
import jp.hazuki.yuzubrowser.legacy.bookmark.BookmarkSite;

public class NetscapeBookmarkCreator {

    private BookmarkFolder parentFolder;
    private int hierarchy;

    public NetscapeBookmarkCreator(BookmarkFolder rootFolder) {
        parentFolder = rootFolder;
    }

    public void create(BufferedWriter writer) throws IOException {
        writer.write("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n" +
                "<!-- This is an automatically generated file.\n" +
                "     It will be read and overwritten.\n" +
                "     DO NOT EDIT! -->\n" +
                "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n" +
                "<TITLE>Bookmarks</TITLE>\n" +
                "<H1>Bookmarks</H1>\n" +
                "<DL><p>\n");
        hierarchy = 1;
        writeFolder(writer, parentFolder);
        writer.write("</DL><p>\n");
    }

    private void writeFolder(Writer writer, BookmarkFolder folder) throws IOException {
        for (BookmarkItem item : folder.getItemList()) {
            String date = Long.toString(item.getId() / 1000);
            if (item instanceof BookmarkSite) {
                writeIndent(writer);
                writer.write("<DT><A HREF=\"");
                writer.write(((BookmarkSite) item).url);
                writer.write("\" ADD_DATE=\"");
                writer.write(date);
                writer.write("\" LAST_VISIT=\"");
                writer.write(date);
                writer.write("\" LAST_MODIFIED=\"");
                writer.write(date);
                writer.write("\">");
                writer.write(item.title);
                writer.write("</A>\n");
            } else if (item instanceof BookmarkFolder) {
                writeIndent(writer);
                writer.write("<DT><H3 ADD_DATE=\"");
                writer.write(date);
                writer.write("\" LAST_MODIFIED=\"");
                writer.write(date);
                writer.write("\">");
                writer.write(item.title);
                writer.write("</H3>\n");
                writeIndent(writer);
                writer.write("<DL><p>\n");
                hierarchy++;
                writeFolder(writer, (BookmarkFolder) item);
                hierarchy--;
                writeIndent(writer);
                writer.write("</DL><p>\n");
            }
        }
    }

    private void writeIndent(Writer writer) throws IOException {
        if (hierarchy <= 0) {
            return;
        }
        for (int i = hierarchy - 1; i >= 0; i--) {
            writer.write("    ");
        }
    }
}
