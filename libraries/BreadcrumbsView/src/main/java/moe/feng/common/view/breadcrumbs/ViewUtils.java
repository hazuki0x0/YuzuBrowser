package moe.feng.common.view.breadcrumbs;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

class ViewUtils {

    /**
     * Check if the current language is RTL
     *
     * @param context Context
     * @return Result
     */
    static boolean isRtlLayout(Context context) {
        return context.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    /**
     * Get color attribute from current theme
     *
     * @param context Themed context
     * @param attr    The resource id of color attribute
     * @return Result
     */
    @ColorInt
    static int getColorFromAttr(Context context, @AttrRes int attr) {
        TypedArray array = context.getTheme().obtainStyledAttributes(new int[]{attr});
        int color = array.getColor(0, Color.TRANSPARENT);
        array.recycle();
        return color;
    }

    /**
     * Measure content width from ListAdapter
     *
     * @param context     Context
     * @param listAdapter The adapter that should be measured
     * @return Recommend popup window width
     */
    static int measureContentWidth(Context context, ListAdapter listAdapter) {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;

        final ListAdapter adapter = listAdapter;
        final int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            final int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }

            if (mMeasureParent == null) {
                mMeasureParent = new FrameLayout(context);
            }

            itemView = adapter.getView(i, itemView, mMeasureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);

            final int itemWidth = itemView.getMeasuredWidth();

            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }

        return maxWidth;
    }

}
