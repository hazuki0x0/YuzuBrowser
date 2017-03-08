package jp.hazuki.yuzubrowser.bookmark.view;

/**
 * Created by hazuki on 17/03/01.
 */

public interface BookmarkItemCallBack {
    void itemOpen(int index, int target);

    void itemsOpenAll(int target);

    void itemOpenAll(int index, int target);

    void itemShare(int index);

    void itemCopy(int index);

    void itemEdit(int index);

    void itemMoveTo(int index);

    void itemsMoveTo();

    void itemMove(int index, boolean up);

    void itemDelete(int index);

    void itemsDelete();
}
