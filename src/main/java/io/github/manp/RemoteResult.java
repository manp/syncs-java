package io.github.manp;

/**
 * remote result promise
 */
public class RemoteResult {
    private String id;
    protected OnRemoteResult listener;
    protected RemoteResult(String id) {
        this.id = id;
    }
    public void then(OnRemoteResult listener){
        this.listener=listener;
    }
}
