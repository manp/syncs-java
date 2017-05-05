package io.github.manp;

/**
 * handles connection state change
 */
public interface OnStateChangeListener {
    /**
     * @param state
     * @param syncs
     */
    void onSateChange(State state, Syncs syncs);


    enum State{
        OPEN,CLOSE,DISCONNECT;
    }
}



