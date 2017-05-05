package io.github.manp;

/**
 * delegate to handle shared objects changes
 */
public interface OnSharedObjectChangeListener {
    /**
     * @param object Shared Object instance
     * @param changedProperties array of changed properties
     * @param by
     * @param syncs syncs instance
     */
    void onSharedChange(SharedObject object, String[] changedProperties , SharedObject.By by, Syncs syncs);
}
