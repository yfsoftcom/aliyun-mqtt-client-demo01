This is a demo project for mqtt base on Aliyun-iot framework.

1. TCP/TLS support, need global sign ROOT CA.
2. pub \ sub
3. how to:
   1) for eclipse :  ｀mvn eclipse:eclipse｀ and import
   2) main class: com.aliyun.iot.demo.sds.SimpleClient4SDS
   3) Run: `mvn compile && mvn exec:java -Dexec.mainClass="com.aliyun.iot.demo.iothub.SimpleClient4IOT" -Dexec.args="http://192.168.0.1:8080/api"`

4. package target
   1) Run: `mvn package`
   2) Run: `package.bat`
   3) Run: `run.bat`


## Publish Message To Device

Use Redis Pub/Sub to publish message to device.

- Test from the docker
  - Run command to publish

    ```
    $ docker exec -it redis_server redis-cli
    $ SUBSCRIBE $s2d/publish
    $ publish $s2d/publish test
    ```

#### Known Issue

- 连接失败,原因:已断开连接 (32109)

  ```java
  2018-11-09 09:12:52.788 - [SimpleClient4IOT.java] - connectionLost(177):连接失败,原因:已断开连接 (32109) - java.io.EOFException
已断开连接 (32109) - java.io.EOFException
        at org.eclipse.paho.client.mqttv3.internal.CommsReceiver.run(CommsReceiver.java:146)
        at java.lang.Thread.run(Thread.java:745)
Caused by: java.io.EOFException
        at java.io.DataInputStream.readByte(DataInputStream.java:267)
        at org.eclipse.paho.client.mqttv3.internal.wire.MqttInputStream.readMqttWireMessage(MqttInputStream.java:65)
        at org.eclipse.paho.client.mqttv3.internal.CommsReceiver.run(CommsReceiver.java:107)
        ... 1 more
  ```

  > 这个 bug 可能是应为多个终端链接了 服务器，导致服务器拒绝；如出现此情况，请关闭其他的链接客户端。

###### Changelog

Add:
- Redis
- publish message

Fixbug:
- topic can't be changed.
