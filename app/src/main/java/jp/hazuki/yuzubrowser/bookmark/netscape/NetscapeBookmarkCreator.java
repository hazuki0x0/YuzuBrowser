package jp.hazuki.yuzubrowser.bookmark.netscape;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import jp.hazuki.yuzubrowser.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.bookmark.BookmarkItem;
import jp.hazuki.yuzubrowser.bookmark.BookmarkSite;

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
        for (BookmarkItem item : folder.list) {
            if (item instanceof BookmarkSite) {
                writeIndent(writer);
                writer.write("<DT><A HREF=\"");
                writer.write(((BookmarkSite) item).url);
                writer.write("\" ADD_DATE=\"0\" LAST_VISIT=\"0\" LAST_MODIFIED=\"0\">");
                writer.write(item.title);
                writer.write("</A>\n");
            } else if (item instanceof BookmarkFolder) {
                writeIndent(writer);
                writer.write("<DT><H3 ADD_DATE=\"0\" LAST_MODIFIED=\"0\">");
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
