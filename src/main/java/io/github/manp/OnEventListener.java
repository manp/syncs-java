package io.github.manp;

import org.json.JSONObject;

/**
 * Delegate to handle incoming event
 */
public interface OnEventListener {
    /**
     *
     * @param event event name
     * @param data event published data
     * @param syncs Syncs instance
     */
     void onEvent(String event,JSONObject data, Syncs syncs);
}
