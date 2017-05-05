package io.github.manp;

import java.lang.Object;

/**
 * delegate to handle result of RMI
 */
public interface OnRemoteResult {
    /**
     * invokes when result is successful
     * @param data
     */
    void onResult(Object data);

    /**
     * invokes when error happened in remote side
     * @param error
     */
    void onError(String error);
}
