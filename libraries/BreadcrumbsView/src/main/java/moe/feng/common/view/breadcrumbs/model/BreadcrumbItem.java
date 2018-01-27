package moe.feng.common.view.breadcrumbs.model;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.List;

public class BreadcrumbItem {

    private int mSelectedIndex = -1;
    private List<String> mItems;

    public BreadcrumbItem(@NonNull List<String> items) {
        this(items, 0);
    }

    public BreadcrumbItem(@NonNull List<String> items, int selectedIndex) {
        if (items != null && !items.isEmpty()) {
            this.mItems = items;
            this.mSelectedIndex = selectedIndex;
        } else {
            throw new IllegalArgumentException("Items shouldn\'t be null empty.");
        }
    }

    public void setSelectedItem(@NonNull String selectedItem) {
        this.mSelectedIndex = mItems.indexOf(selectedItem);
        if (mSelectedIndex == -1) {
            throw new IllegalArgumentException("This item does not exist in items.");
        }
    }

    /**
     * Select a item by index
     *
     * @param selectedIndex The index of the item should be selected
     */
    public void setSelectedIndex(int selectedIndex) {
        this.mSelectedIndex = selectedIndex;
    }

    /**
     * Get selected item index
     *
     * @return The index of selected item
     */
    public int getSelectedIndex() {
        return this.mSelectedIndex;
    }

    /**
     * Get selected item
     *
     * @return The selected item
     */
    public @NonNull
    String getSelectedItem() {
        return this.mItems.get(getSelectedIndex());
    }

    /**
     * Check if there are other items
     *
     * @return Result
     */
    public boolean hasMoreSelect() {
        return this.mItems.size() > 1;
    }

    /**
     * Set a new items list
     *
     * @param items Items list
     */
    public void setItems(@NonNull List<String> items) {
        this.setItems(items, 0);
    }

    /**
     * Set a new items list with selecting a item
     *
     * @param items         Items list
     * @param selectedIndex The selected item index
     */
    public void setItems(@NonNull List<String> items, int selectedIndex) {
        if (items != null && !items.isEmpty()) {
            this.mItems = items;
            this.mSelectedIndex = selectedIndex;
        } else {
            throw new IllegalArgumentException("Items shouldn\'t be null empty.");
        }
    }

    /**
     * Get items list
     *
     * @return Items List
     */
    public @NonNull
    List<String> getItems() {
        return mItems;
    }

    /**
     * Create a simple BreadcrumbItem with single item
     *
     * @param title Item title
     * @return Simple BreadcrumbItem
     * @see BreadcrumbItem
     */
    public static BreadcrumbItem createSimpleItem(@NonNull String title) {
        return new BreadcrumbItem(Collections.singletonList(title));
    }

}
