package moe.feng.common.view.breadcrumbs;

import moe.feng.common.view.breadcrumbs.model.BreadcrumbItem;

/**
 * Simple callback to be invoked when BreadcrumbsView's item was clicked or changed.
 * Assist you to handle navigate actions.
 *
 * @see moe.feng.common.view.breadcrumbs.BreadcrumbsCallback
 */
public abstract class DefaultBreadcrumbsCallback implements BreadcrumbsCallback {

    @Override
    public void onItemClick(BreadcrumbsView view, int position) {
        if (position == view.getItems().size() - 1) return;
        view.removeItemAfter(position + 1);
        this.onNavigateBack(view.getItems().get(position), position);
    }

    @Override
    public void onItemChange(BreadcrumbsView view, int parentPosition, String nextItem) {
        BreadcrumbItem nextBreadcrumb = view.getItems().get(parentPosition + 1);
        nextBreadcrumb.setSelectedItem(nextItem);
        view.removeItemAfter(parentPosition + 2);
        if (parentPosition + 2 >= view.getItems().size()) {
            view.notifyItemChanged(parentPosition + 1);
        }
        this.onNavigateNewLocation(nextBreadcrumb, parentPosition + 1);
    }

    /**
     * Called when page should navigate back to a location.
     *
     * @param item     The item that was navigated to
     * @param position The position of new location
     */
    public abstract void onNavigateBack(BreadcrumbItem item, int position);

    /**
     * Called when page should navigate to the location where was changed by popup menu
     *
     * @param newItem         The item that was navigated to
     * @param changedPosition The position of changed location
     */
    public abstract void onNavigateNewLocation(BreadcrumbItem newItem, int changedPosition);

}
