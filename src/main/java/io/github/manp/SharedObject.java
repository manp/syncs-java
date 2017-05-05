package io.github.manp;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Shared Object Class to simulate shared variable
 */
public class SharedObject {
    public String name;
    protected HashMap<String,Object> rawData=new HashMap<String, Object>();
    public Type type;
    private boolean readOnly = true;
    private Syncs server;
    private OnSharedObjectChangeListener onChangeHandler;



    private SharedObject() {
    }

    /**
     * creates a global level synced object
     * @param name
     * @param initializeData
     * @param server
     * @return {SharedObject}
     */
    public static SharedObject globalLevel(String name, HashMap<String,Object>  initializeData, Syncs server) {
        SharedObject result = new SharedObject();
        result.name = name;
        result.type = Type.GLOBAL;
        result.readOnly = true;
        result.rawData = initializeData;
        result.server = server;
        return result;
    }

    /**
     * creates group level synced object
     * @param name
     * @param initializeData
     * @param server
     * @return {SharedObject}
     */
    public static SharedObject groupLevel(String name, HashMap<String,Object>  initializeData, Syncs server) {
        SharedObject result = new SharedObject();
        result.name = name;
        result.type = Type.GROUP;
        result.readOnly = true;
        result.rawData = initializeData;
        result.server = server;
        return result;
    }

    /**
     * creates client level synced object
     * @param name
     * @param initializeData
     * @param server
     * @return {SharedObject}
     */
    public static SharedObject clientLevel(String name, HashMap<String,Object>  initializeData, Syncs server) {
        SharedObject result = new SharedObject();
        result.name = name;
        result.type = Type.CLIENT;
        result.readOnly = false;
        result.rawData = initializeData;
        result.server = server;

        return result;
    }


    /**
     * get shared variable as String
     * @param property
     * @return {String|null}
     */
    public String getString(String property){
        if(rawData.containsKey(property)){
            return rawData.get(property).toString();
        }
        return null;
    }
    /**
    * get shared variable as Integer
    * @param property
    * @return {Integer|null}
    */
    public Integer getInteger(String property){
        if(rawData.containsKey(property)){
            return (Integer) rawData.get(property);
        }
        return null;
    }

    /**
     * get shared variable as Boolean
     * @param property
     * @return {Boolean|null}
     */
    public Boolean getBoolean(String property){
        if(rawData.containsKey(property)){
            return (Boolean) rawData.get(property);
        }
        return null;
    }


    /**
     * set shared variable as String
     * @param key
     * @param value
     * @return {SharedObject} enables chain call
     */
    public SharedObject set(String key,String value){
        return setData(key,value);
    }
    /**
     * set shared variable as Integer
     * @param key
     * @param value
     * @return {SharedObject} enables chain call
     */
    public SharedObject set(String key,Integer value){
        return setData(key,value);
    }
    /**
     * set shared variable as Boolean
     * @param key
     * @param value
     * @return {SharedObject} enables chain call
     */
    public SharedObject set(String key,Boolean value){
        return setData(key,value);
    }

    /**
     * set incoming changes in shared variable
     * @param key
     * @param value
     * @return
     */
    private SharedObject setData(String key,Object value){
        if(this.readOnly){
            return this;
        }
        rawData.put(key,value);
        if(this.onChangeHandler!=null){
            String changedProperties[] =new String[]{key};
            this.onChangeHandler.onSharedChange(this,changedProperties,By.CLIENT,server);
        }
        this.sendSyncCommand(key);

        return this;
    }
    private void sendSyncCommand(String key) {
        JSONObject command=new JSONObject();
        command.put("type","sync");
        command.put("name",name);
        command.put("scope","CLIENT");
        command.put("key",key);
        command.put("value",rawData.get(key));

        this.server.sendCommand(command);
    }

    protected void setProperties(JSONObject values) {
        Iterator<String> keys = values.keys();

        while(keys.hasNext()){
            String key=keys.next();
            rawData.put(key,values.get(key));
        }
        if(onChangeHandler!=null){
            String[] changedKeys=values.keySet().toArray(new String[values.keySet().size()]);
            this.onChangeHandler.onSharedChange(this,changedKeys,By.SERVER,server);
        }
    }

    /**
     * handle change in shared variable
     * @param listener
     * @return
     */
    public SharedObject onChange(OnSharedObjectChangeListener listener){
        this.onChangeHandler=listener;
        return this;
    }



    public enum Type{
        GLOBAL,GROUP,CLIENT
    }
    public enum By{
        SERVER,CLIENT
    }
}
