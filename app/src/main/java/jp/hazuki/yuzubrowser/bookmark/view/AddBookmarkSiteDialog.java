package jp.hazuki.yuzubrowser.bookmark.view;

import android.content.Context;
import android.view.View;

import jp.hazuki.yuzubrowser.bookmark.BookmarkManager;
import jp.hazuki.yuzubrowser.bookmark.BookmarkSite;

public class AddBookmarkSiteDialog extends AddBookmarkDialog<BookmarkSite, String> {
    public AddBookmarkSiteDialog(Context context, BookmarkManager manager, BookmarkSite item) {
        super(context, manager, item, item.title, item.url);
    }

    public AddBookmarkSiteDialog(Context context, String title, String url) {
        super(context, null, null, title, url);
    }

    @Override
    protected void initView(View view, String title, String url) {
        super.initView(view, title, url);
        titleEditText.setText((title == null) ? url : title);
        urlEditText.setText(url);
    }

    @Override
    protected BookmarkSite makeItem(BookmarkSite item, String title, String url) {
        if (item == null) {
            return new BookmarkSite(title, url.trim());
        } else {
            item.title = title;
            item.url = url.trim();
            return null;
        }
    }
}
