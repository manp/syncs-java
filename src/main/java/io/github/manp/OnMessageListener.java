package io.github.manp;

import org.json.JSONObject;

/**
 * delegate to handle incoming message
 */
public interface OnMessageListener {
    /**
     * @param message message data
     * @param syncs Syncs instance
     */
    void onMessage(JSONObject message, Syncs syncs);
}
