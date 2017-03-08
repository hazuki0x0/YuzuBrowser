package jp.hazuki.yuzubrowser.utils.database;

import android.content.Context;
import android.database.Cursor;

public class ImplementedCursorLoader extends CursorLoaderBase {
    private final CursorLoadable mLoadable;

    public ImplementedCursorLoader(Context context, CursorLoadable loadable) {
        super(context);
        mLoadable = loadable;
    }

    @Override
    public Cursor loadInBackground() {
        return mLoadable.getLoadableCursor();
    }
}
