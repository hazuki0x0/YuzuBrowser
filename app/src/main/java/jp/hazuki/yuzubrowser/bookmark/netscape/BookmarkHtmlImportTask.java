package jp.hazuki.yuzubrowser.bookmark.netscape;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import jp.hazuki.yuzubrowser.bookmark.BookmarkFolder;
import jp.hazuki.yuzubrowser.bookmark.BookmarkManager;

public class BookmarkHtmlImportTask extends AsyncTaskLoader<Boolean> {

    private Context context;
    private File html;
    private BookmarkManager manager;
    private BookmarkFolder folder;
    private Handler handler;

    public BookmarkHtmlImportTask(Context context, File html, BookmarkManager manager, BookmarkFolder folder, Handler handler) {
        super(context);
        this.context = context;
        this.html = html;
        this.manager = manager;
        this.folder = folder;
        this.handler = handler;
    }

    @Override
    public Boolean loadInBackground() {
        try (FileReader fileReader = new FileReader(html);
             BufferedReader reader = new BufferedReader(fileReader)) {

            NetscapeBookmarkParser parser = new NetscapeBookmarkParser(folder);
            parser.parse(reader);
            manager.write();
            return Boolean.TRUE;

        } catch (NetscapeBookmarkException e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "This file is not bookmark html file.", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return Boolean.FALSE;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
