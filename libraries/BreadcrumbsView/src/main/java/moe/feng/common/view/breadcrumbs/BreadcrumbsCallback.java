package moe.feng.common.view.breadcrumbs;

/**
 * Interface definition for a callback to be invoked when BreadcrumbsView's item was clicked or changed.
 */
public interface BreadcrumbsCallback {

    /**
     * Called when BreadcrumbsView's item has been clicked
     *
     * @param view     Parent view
     * @param position The position of clicked item
     */
    void onItemClick(BreadcrumbsView view, int position);

    /**
     * Called when BreadcrumbsView's item has been changed
     *
     * @param view           Parent view
     * @param parentPosition The position of the parent item of changed item
     * @param nextItem       Next item title
     */
    void onItemChange(BreadcrumbsView view, int parentPosition, String nextItem);

}
