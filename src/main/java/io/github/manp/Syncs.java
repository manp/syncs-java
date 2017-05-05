package io.github.manp;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by manp on 5/5/17.
 */
public class Syncs {
    private String path;
    public SyncsConfig config;
    private String socketId;
    private boolean handledClose = false;
    public boolean online = false;

    //state change listeners
    private OnStateChangeListener onOpenListener;
    private OnStateChangeListener onCloseListener;
    private OnStateChangeListener onDisconnectListener;

    //onMessage listener
    private OnMessageListener onMessageListener;

    //event layer
    private HashMap<String,ArrayList<OnEventListener> > subscriptions=new HashMap<String, ArrayList<OnEventListener>>();
    //shared objects
    private HashMap<String,SharedObject> globalSharedObjects=new HashMap<String, SharedObject>();
    private HashMap<String,HashMap<String,SharedObject> > groupSharedObjects=new HashMap<String, HashMap<String, SharedObject>>();
    private HashMap<String,SharedObject> clientSharedObjects=new HashMap<String, SharedObject>();
    //rmi
    private HashMap<String,RemoteFunction> rmiFunctions=new HashMap<String, RemoteFunction>();
    private HashMap<String,RemoteResult> rmiResultCallbacks=new HashMap<String, RemoteResult>();

    private WebSocketClient socket;
    public Syncs(String path) {
        this.path=path;
        this.config=new SyncsConfig();
        initializeSocket();
        if(this.config.autoConnect){
            socket.connect();
        }
    }
    public Syncs(String path, SyncsConfig config) {
        this.path=path;
        this.config=config;
        initializeSocket();
        if(this.config.autoConnect){
            socket.connect();
        }

    }



    /**
     * initialize configuration with user inputs or default configurations
     */
    private void initializeSocket(){
        socket=new WebSocketClient(URI.create(path), new Draft_10()) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {

            }

            @Override
            public void onMessage(String s) {
                try {
                    onSocketMessage(URLDecoder.decode(s,"UTF-8"));
                } catch (Exception e) {
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                onSocketClose();
            }

            @Override
            public void onError(Exception e) {

            }
        };
    }


    private void onSocketOpen(){
    }

    /**
     * handle open event
     * open event will emit on first connection
     * @param onOpenListener
     * @return
     */
    public Syncs onOpen(OnStateChangeListener onOpenListener) {
        this.onOpenListener = onOpenListener;
        return this;
    }

    /**
     * handle close event
     * this event will emit on close
     * @param onCloseListener
     * @return
     */
    public Syncs onClose(OnStateChangeListener onCloseListener) {
        this.onCloseListener = onCloseListener;
        return this;
    }

    /**
     * handle disconnect event
     * this event will emit on unhandled close
     * @param onDisconnectListener
     * @return
     */
    public Syncs onDisconnect(OnStateChangeListener onDisconnectListener) {
        this.onDisconnectListener = onDisconnectListener;
        return this;
    }

    private void onSocketMessage(String message){
        JSONObject parsedMessage = this.parseMessage(message);

        if (parsedMessage!=null) {
            if (parsedMessage.has("command") && parsedMessage.has("type")) {
                this.handleCommand(parsedMessage);
            } else {
                if(this.onMessageListener!=null){
                    this.onMessageListener.onMessage(parsedMessage,this);
                }
            }
        }
    }

    /**
     * handle incoming command
     * @param command
     */
    private void handleCommand(JSONObject command) {
        if (this.config.debug) {
            System.out.println("IN: "+command.toString());
        }


        String s = command.getString("type");
        if (s.equals("getSocketId")) {
            this.sendSocketId();
            if (this.socketId != null && this.onOpenListener != null) {
                this.onOpenListener.onSateChange(OnStateChangeListener.State.OPEN, this);
            }

        } else if (s.equals("setSocketId")) {
            this.socketId = command.getString("socketId");
            this.online = true;
            if (this.socketId != null && this.onOpenListener != null) {
                this.onOpenListener.onSateChange(OnStateChangeListener.State.OPEN, this);
            }

        } else if (s.equals("event")) {
            this.handleEvent(command);

        } else if (s.equals("sync")) {
            this.handleSync(command);

        } else if (s.equals("rmi")) {
            this.handleRMICommand(command);

        } else if (s.equals("rmi-result")) {
            this.handleRmiResultCommand(command);

        }
    }

    /**
     * parse incoming message to JSONObject
     * @param message
     * @return
     */
    private JSONObject parseMessage(String message) {
        try {
            return new JSONObject(message);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * handles WebSocket close
     */
    private void onSocketClose(){
        this.online = false;
        if (this.handledClose || !this.config.autoReconnect) {
            this.handledClose = false;
            if(this.onCloseListener!=null){
                this.onCloseListener.onSateChange(OnStateChangeListener.State.CLOSE,this);
            }

        }
        else {
            if(this.onDisconnectListener!=null){
                this.onDisconnectListener.onSateChange(OnStateChangeListener.State.DISCONNECT,this);
            }
            try {
                Thread.sleep(this.config.reconnectDelay);
            } catch (Exception e) {
            }
            if(!this.online){
                initializeSocket();
                this.connect();
            }

        }

    }

    /**
     * handle incoming message
     * @param onMessageListener
     */
    public void onMessage(OnMessageListener onMessageListener) {
        this.onMessageListener = onMessageListener;
    }

    /**
     * enables debug mode
     */
    public void enableDebugMode() {
        this.config.debug = true;
    }

    /**
     * disables debug mode
     */
    public void disableDebugMode() {
        this.config.debug = false;
    }



    /**
     * connects to io.github.manp.Syncs server
     */
    public void connect() {
        if(this.online){
            return;
        }
        this.socket.connect();
    }

    /**
     * disconnect from Syncs server
     */
    public void disconnect() {
        this.handledClose = true;
        this.socket.close();
    }

    /**
     * send socketId to io.github.manp.Syncs server
     */
    private void sendSocketId() {
        JSONObject json=new JSONObject();
        json.put("type", "reportSocketId");

        if (this.socketId!=null) {
            json.put("socketId",this.socketId);
            this.sendCommand(json);
            this.online = true;
        } else {
            json.put("socketId",false);
            this.sendCommand(json);
        }
    }

    /**
     * send message as syncs-command
     * @param {any} message
     * @return {boolean}
     */
    protected boolean sendCommand(JSONObject command) {
        try {
            command.put("command",true);
            if (this.config.debug) {
                System.out.println("OUT: "+command.toString());
            }
//            System.out.println(URLEncoder.encode(command.toString(),"UTF-8"));
            this.socket.send(command.toString());
            return true;
        } catch(Exception e){
            return false;
        }
    }



    public boolean send(JSONObject message) {
        if(this.online){
            this.socket.send(message.toString());
            return true;
        }
        return false;

    }



    /**************  EVENT LAYER ******************/

    /**
     * handle incomming event
     * @param {any} command
     */
    private void handleEvent(JSONObject command) {
        if (command.has("event")) {
            ArrayList<OnEventListener> listeners = this.subscriptions.get(command.getString("event"));
            if (listeners!=null) {
                for(OnEventListener listener:listeners){
                    listener.onEvent(command.getString("event"),command.getJSONObject("data"),this);
                }
            }
        }
    }

    /**
     * subscribe on incoming event
     * @param {string} event
     * @param { (data: any) => void } callback
     */
    public Syncs subscribe(String event, OnEventListener listener) {
        ArrayList<OnEventListener> listeners=this.subscriptions.get(event);
        if (listeners==null) {
            listeners=new ArrayList<OnEventListener>();
            this.subscriptions.put(event,listeners);
        }
        listeners.add(listener);
        return this;
    }

    /**
     * un-subscribe from event
     * @param {string} event
     * @param {callback: (data: any) => void} callback
     */
    public Syncs unSubscribe(String event, OnEventListener listener) {
        ArrayList<OnEventListener> listeners=this.subscriptions.get(event);
        if (listeners==null) {
            return this;
        }
        listeners.remove(listener);
        return this;
    }

    /**
     * publish an event to Syncs Server
     * @param {string} event
     * @param {any} data
     * @return {boolean}
     */
    public boolean publish(String event, JSONObject data) {
        JSONObject command=new JSONObject();
        command.put("type","event");
        command.put("event",event);
        command.put("data",data);
        return this.sendCommand(command);
    }


    /**************  SHARED OBJECT LAYER ******************/

    /**
     * handle shared object sync command
     * @param command
     */
    private void handleSync(JSONObject command) {
        String scope = command.getString("scope");
        if (scope.equals("GLOBAL")) {
            this.setGlobalSharedObject(command);

        } else if (scope.equals("GROUP")) {
           this.setGroupSharedObject(command);

        } else if (scope.equals("CLIENT")) {
            this.setClientSharedObject(command);

        }
    }
    /**
     * changes global shared object value
     * @param command
     */
    private void setGlobalSharedObject(JSONObject command) {
        String name=command.getString("name");
        if (this.globalSharedObjects.containsKey(name)) {
            this.globalSharedObjects.get(name).setProperties(command.getJSONObject("values"));
        }
        else {
            this.globalSharedObjects.put(name, SharedObject.globalLevel(name, new HashMap<String, Object>(), this));
        }
    }

    /**
     * changes group shared object value
     * @param command
     */
    private void setGroupSharedObject(JSONObject command) {
        String name=command.getString("name");
        String groupName=command.getString("group");

        if (!this.groupSharedObjects.containsKey(groupName)) {
            this.groupSharedObjects.put(groupName,new HashMap<String, SharedObject>());
        }
        HashMap<String,SharedObject> group = this.groupSharedObjects.get(groupName);
        if (group.containsKey(name)) {
            group.get(name).setProperties(command.getJSONObject("values"));
        }
        else {
            group.put(name, SharedObject.groupLevel(name,new HashMap<String, Object>(), this));
        }
    }
//
    /**
     * changes client shared object value
     * @param command
     */
    private void setClientSharedObject(JSONObject command) {
        String name=command.getString("name");

        if (this.clientSharedObjects.containsKey(name)) {
            this.clientSharedObjects.get(name).setProperties(command.getJSONObject("values"));
        }
        else {
            this.clientSharedObjects.put(name, SharedObject.clientLevel(name, new HashMap<String, Object>(), this));
        }
    }
//
    /**
     * returns client level shared object
     * @param {string} name
     * @return {SharedObject}
     */
    public SharedObject shared(String name) {
        if (!this.clientSharedObjects.containsKey(name)) {
            this.clientSharedObjects.put(name, SharedObject.clientLevel(name, new HashMap<String, Object>(), this));
        }
        return this.clientSharedObjects.get(name);

    }
//
    /**
     * return group level shared object
     * @param {string} group
     * @param {string} name
     * @return {SharedObject}
     */
    public SharedObject groupShared(String group, String name) {
        if (!this.groupSharedObjects.containsKey(group)) {
            this.groupSharedObjects.put(group, new HashMap<String, SharedObject>());
        }
        if (!this.groupSharedObjects.get(group).containsKey(name)) {
            this.groupSharedObjects.get(group).put(name, SharedObject.groupLevel(name, new HashMap<String, Object>(), this));
        }
        return this.groupSharedObjects.get(group).get(name);
    }
//
    /**
     *
     * @param name
     * @return {SharedObject}
     */
    public SharedObject globalShared(String name) {
        if (!this.globalSharedObjects.containsKey(name)) {
            this.globalSharedObjects.put(name, SharedObject.globalLevel(name,new HashMap<String, Object>(), this));
        }
        return this.globalSharedObjects.get(name);
    }




    /**************  RMI LAYER ******************/

    /**
     * returns functions array
     * functions array is the place to initialize rmi functions
     */
    public void functions(String name,RemoteFunction remoteMethod) {
        this.rmiFunctions.put(name,remoteMethod);
    }

    /**
     * handle incoming rmi command
     */
    private void handleRMICommand(JSONObject command) {
        String name=command.getString("name");
        String id=command.getString("id");
        if(rmiFunctions.containsKey(name)){
            try{
                Object result=rmiFunctions.get(name).onCall(command.getJSONArray("args"),new Promise(id,this));
                if(result instanceof Promise){

                }else {
                    this.sendRmiResultCommand(result,null,id);
                }
            }catch (Exception e){
                this.sendRmiResultCommand(null, "function error", id);
            }


        }else{
            this.sendRmiResultCommand(null, "undefined", id);
        }
    }
//
    /**
     * returns an remote functions object
     * remote functions object is the place to call remote functions
     * called method will return Promise to get result from remote
     * @return {RemoteResult}
     */
    public RemoteResult remote(String name, JSONObject ...args){
        String id=generateRMIRequestUID();
        RemoteResult result=new RemoteResult(id);
        rmiResultCallbacks.put(id,result);
        sendRMICommand(name,new JSONArray(args),id);
        return result;
    }

    /**
     * generates request id for RMI
     * @return {string}
     */
    private String generateRMIRequestUID() {
        return UUID.randomUUID().toString();
    }
//
    /**
     * handles rmi-result command
     * @param command
     */
    private void handleRmiResultCommand(JSONObject command) {
        String id=command.getString("id");
        String error=command.isNull("error")?null:command.getString("error");
        RemoteResult res= this.rmiResultCallbacks.get(id);
        if (error==null) {

            res.listener.onResult(command.get("result"));
        } else {
            res.listener.onError(error);
        }
        this.rmiResultCallbacks.remove(id);
    }

    /**
     * sends rmi calling command to Syncs server;
     * @param {String} name
     * @param {JSONArray} args
     * @param {String} id
     */
    private void sendRMICommand(String name,JSONArray args,String id) {
        JSONObject command=new JSONObject();
        command.put("type","rmi");
        command.put("id",id);
        command.put("name",name);
        command.put("args",args);

        this.sendCommand(command);
    }
    /**
     * send rmi-result command to SyncsServer
     * @param result
     * @param error
     * @param id
     */
    protected void sendRmiResultCommand(Object result, String error, String id) {
        JSONObject command=new JSONObject();
        command.put("type","rmi-result");
        command.put("id",id);
        command.put("result",result);
        command.put("error",error);

        this.sendCommand(command);
    }
}
