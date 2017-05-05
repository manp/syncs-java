# syncs-java
[ ![Download](https://api.bintray.com/packages/manp/syncs/syncs/images/download.svg) ](https://bintray.com/manp/syncs/syncs/_latestVersion)

__A Java Package for Syncs Real-Time Web Applications__

_syncs-java_ is Java package to work with [Syncs](https://github.com/manp/syncs).



## Initialization
_syncs-java_ is easy to setup.


### Using Maven
Add following maven build string to POM file

```xml
<dependency>
  <groupId>io.github.manp</groupId>
  <artifactId>syncs</artifactId>
  <version>1.0.0</version>
  <type>pom</type>
</dependency>
```

### Using Gradle
Add following maven build string to POM file

```grovy
compile 'io.github.manp:syncs:1.0.0'
```

### Manual Setup
Download package file from [here](https://bintray.com/manp/syncs/download_file?file_path=io%2Fgithub%2Fmanp%2Fsyncs%2F1.0.0%2Fsyncs-1.0.0.jar) or [here](https://raw.githubusercontent.com/manp/syncs-java/master/target/syncs-1.0.0.jar), then add it to your project dependencies.

## Create Connection
Developers can create real-time connection by creating an instance of `Syncs` class.

```java
Syncs io=new Syncs("ws://localhost:8080/syncs");

```


The path parameter is required that determines Syncs server address.
The second parameter is Syncs configs object wich is an instance of `SyncsConfig` class. following fields are available in config instance:
+ `autoConnect:boolean`: If `autoConnect` is `false` then the Syncs instance will not connect to server on creation. To connect manuly to server developers should call `io.connect()` method. default value is `true`.
+ `autoReconnect:boolean`: This config makes the connection presistent on connection drop. default value is `true`.
+ `reconnectDelay: number`: time to wait befor each reconnecting try. default value is `1000`.
+ `debug:bolean`: This parameter enables debug mode on client side. default value is `false`.

```java
SyncsConfig config=new SyncsConfig();
config.reconnectDelay=1000;
Syncs io=new Syncs("ws://localhost:8080/syncs",config);

```

## Handling connection
Syncs client script can automatically connect to Syncs server. If `autoConnect` config is set to `false`, the developer should connect manual to server using `connect` method.

Using `connect` method developers can connect to defined server.

```java
  io.connect();
```

Target application can establish connection or disconnect from server using provided methods.
```java
  io.disconnect()
```

Developers can handle _disconnect_ and _close_ event with `onDisconnect` and `onClose`  method.
```java
    io.onOpen(new OnStateChangeListener() {
            @Override
            public void onSateChange(State state, Syncs syncs) {

            }
    });
```
```java
    io.onClose(new OnStateChangeListener() {
            @Override
            public void onSateChange(State state, Syncs syncs) {

            }
    });
```
Developers can use one listener to handle connection status

```java
public class Controller implements OnSateChangeListener{

    public void handleConnection(){
        io.onOpen(this);
        io.onClose(this);
        io.onDisconnect(this);
    }
    
    @Override
    public void onSateChange(State state, Syncs syncs) {

    }
    
}

```

It's also possible to check connection status using `online` property of Syncs instance.
```java
  if(io.online){
      //do semething
  }
```



## Abstraction Layers

Syncs provides four abstraction layer over its real-time functionality for developers.


### 1. onMessage Abstraction Layer

Developers can send messages using `send` method of `Syncs` instance to send `JSON` message to the server.Also all incoming messages are catchable using `onMessage`.

```java
JSONObject data=new JSONObject();
data.put("message","Hello, Syncs");
io.send(data);
```

```java
io.onMessage(new OnMessageListener() {
      @Override
      public void onMessage(JSONObject jsonObject, Syncs syncs) {
          
      }
});
```


### 2. Publish and Subscribe Abstraction Layer
 With a Publish and Subscribe solution developers normally subscribe to data using a string identifier. This is normally called a Channel, Topic or Subject.

 ```java
  io.publish('mouse-move-event',jsonData);
 ```
 ```java
 io.subscribe("weather-update", new OnEventListener() {
    @Override
    public void onEvent(String s, JSONObject jsonObject, Syncs syncs) {
      //update weather view
    }
});
 ```

  ### 3. Shared Data Abstraction Layer
Syncs provides Shared Data functionality in form of variable sharing. Shared variables can be accessible in tree level: _Global Level_, _Group Level_ and _Client Level_. Only _Client Level_ shared data can be write able with client.

To get _Client Level_ shared object use `shared` method of `Syncs` instance.
```java
  SharedObject info=io.shared('info');
  info.set("title","Syncs is cool!");
```
To get _Group Level_ shared object use `groupShared` method of `Syncs` instance. First parameter is group name and second one is shared object name.

```java
  SharedObject info=io.groupShared('vips','info');
  info.getInteger("onlineVips")+" vip member are online";
```

To get _Global Level_ shared object use `globalShared` method of `Syncs` instance.
```java
  SharedObject settings=io.globalShared('settings');
  applyBackground(settings.getString("backgrounColor"));
```


It's possible to watch changes in shared object by using shared object as a function.
```java
onChange(new OnSharedObjectChangeListener() {
    @Override
    public void onSharedChange(SharedObject sharedObject, String[] values, SharedObject.By by, Syncs syncs) {

    }
});
```
The callback function has two argument.
+ `sharedObject:SharedObject` changed ShareObject instance
+ `values:String`: an array that contains names of changed properties.
+ `by:Enum` an Enum  variable with two value ( `'SERVER'` and `'CLIENT'`) which shows who changed these properties.



### 4. Remote Method Invocation (RMI) Abstraction Layer
With help of RMI developers can call and pass argument to remote function and make it easy to develop robust and web developed application. RMI may abstract things away too much and developers might forget that they are making calls _over the wire_.

Before calling remote method from server ,developer should declare the function on client script.

`functions` method in `Syncs` instance is the place to declare functions.

```java
io.functions("showMessage", new RemoteFunction() {
    @Override
    public Object onCall(JSONArray args, Promise promise) {
        // show message....
        return null;
    }
});
```

To call remote method on server use `remote` object.

```java
  io.remote("setLocation",latitude,longitude)
```



The remote side can return a result (direct value or Promise object) which is accessible using `Promise` object provided `functions`.


```java
io.functions("askUser", new RemoteFunction() {
  @Override
  public Object onCall(JSONArray args, Promise promise) {
      // ask user and return result
      return result;
  }
});
```

```java
io.functions("startQuiz", new RemoteFunction() {
    @Override
    public Object onCall(JSONArray jsonArray, Promise promise) {
        // start quiz
        return promise;
    }
});


// after a while
promise.result(quizStatics);
```

```java
  io.remote("getWeather",cityName).then(new OnRemoteResult() {
      @Override
      public void onResult(Object o) {

      }

      @Override
      public void onError(String s) {

      }
  });
```
