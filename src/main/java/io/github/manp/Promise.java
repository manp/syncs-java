package io.github.manp;

import org.json.JSONObject;

/**
 * an promise wich can be return if result of RMI call is not ready yet
 */
public class Promise {
    protected String id;
    protected Syncs server;
    protected Promise(String id,Syncs server) {
        this.id=id;
        this.server=server;
    }

    /**
     * call when result is ready
     * @param data
     */
    public void result(JSONObject data){
        server.sendRmiResultCommand(data,null,id);
    }
}
