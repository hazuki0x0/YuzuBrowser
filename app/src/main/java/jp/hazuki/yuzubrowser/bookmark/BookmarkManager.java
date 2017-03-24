package jp.hazuki.yuzubrowser.bookmark;

import android.content.Context;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.ErrorReport;

public class BookmarkManager implements Serializable {
    private final File mFile;
    private BookmarkFolder mRoot = new BookmarkFolder(null, null, -1);
    private List<BookmarkItem> mItems = new ArrayList<>();

    public BookmarkManager(Context context) {
        mFile = new File(context.getDir("bookmark1", Context.MODE_PRIVATE), "bookmark1.dat");
        load();
        mRoot.title = context.getString(R.string.bookmark);
    }

    public File getBookmarkFile() {
        return mFile;
    }

    public void add(BookmarkItem item) {
        mRoot.add(item);
    }

    public void setRoot(BookmarkFolder root) {
        mRoot = root;
    }

    public void setRootTitle(String title) {
        mRoot.title = title;
    }

    public BookmarkFolder getRoot() {
        return mRoot;
    }

    public boolean load() {
        mRoot.clear();

        if (!mFile.exists() || mFile.isDirectory()) return true;

        try (InputStream is = new BufferedInputStream(new FileInputStream(mFile))) {

            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(is);
            mRoot.readForRoot(parser);
            parser.close();
            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return false;
    }

    public boolean write() {
        if (!mFile.exists()) {
            mFile.getParentFile().mkdirs();
        }

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(mFile))) {

            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(os);
            mRoot.writeForRoot(generator);
            generator.close();
            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return false;
    }

    public BookmarkItem get(long id) {
        if (id < 0) return null;
        return get(id, mRoot);
    }

    private BookmarkItem get(long id, BookmarkFolder root) {
        for (BookmarkItem item : root.list) {
            if (item.getId() == id) {
                return item;
            } else if (item instanceof BookmarkFolder) {
                BookmarkItem inner = get(id, (BookmarkFolder) item);
                if (inner != null) {
                    return inner;
                }
            }
        }
        return null;
    }
}
