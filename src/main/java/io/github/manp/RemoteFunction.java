package io.github.manp;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * remote function interface
 */
public interface RemoteFunction {
    /**
     * invokes this method on remote call
     * @param args
     * @param promise
     * @return {String|Integer|Boolean|JSONObject|Promise}
     */
    Object onCall(JSONArray args, Promise promise);
}
