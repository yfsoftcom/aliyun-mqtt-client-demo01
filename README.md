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


###### Changelog

Add:
- Redis
- New Thread to publish message

Fixbug:
- topic can't be changed.
