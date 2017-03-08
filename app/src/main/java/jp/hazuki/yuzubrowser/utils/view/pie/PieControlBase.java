package jp.hazuki.yuzubrowser.utils.view.pie;

import android.content.Context;
import android.view.ViewGroup;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionCallback;

public abstract class PieControlBase implements PieMenu.PieController {
    private final Context mContext;
    private final ActionCallback mActionCallback;
    private final int mItemSize;
    private final PieMenu mPie;

    public PieControlBase(Context context, ActionCallback actionCallback) {
        mContext = context;
        mActionCallback = actionCallback;
        mItemSize = (int) context.getResources().getDimension(R.dimen.qc_item_size);
        mPie = new PieMenu(context);
        mPie.setController(this);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mPie.setLayoutParams(lp);
    }

    public Context getContext() {
        return mContext;
    }

    public void attachToLayout(ViewGroup layout) {
        layout.addView(mPie);
        makeItems();
    }

    public void detachFromLayout(ViewGroup layout) {
        layout.removeView(mPie);
    }

    protected abstract void makeItems();

    protected PieItem addItem(Action action, int l) {
        PieItem item = new PieItem(mContext, mItemSize, action, mActionCallback, l);
        mPie.addItem(item);
        return item;
    }

    @Override
    public boolean onOpen() {
        mPie.notifyChangeState();
        return true;
    }

    public PieMenu getPieMenu() {
        return mPie;
    }
}
