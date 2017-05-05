package io.github.manp;

/**
 * Created by manp on 5/5/17.
 */
public class SyncsConfig {
    /**
     * auto connect on create
     */
    public boolean autoConnect=true;
    /**
     * auto connect on unhandled connection close
     */
    public boolean autoReconnect=true;
    /**
     * auto reconnection delay
     */
    public int reconnectDelay=1000;
    /**
     * enables debug mode
     */
    public boolean debug=false;
}
