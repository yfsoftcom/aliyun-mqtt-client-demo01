This is a demo project for mqtt base on Aliyun-iot framework.

1. TCP/TLS support, need global sign ROOT CA.
2. pub \ sub
3. how to:
   1) for eclipse :  ｀mvn eclipse:eclipse｀ and import
   2) main class: com.aliyun.iot.demo.sds.SimpleClient4SDS
   3) Run: `mvn compile && mvn exec:java -Dexec.mainClass="com.aliyun.iot.demo.iothub.SimpleClient4IOT" -Dexec.args="http://192.168.0.1:8080/api"`

4. package target
   1) Run: `mvn package`
   2) Run: `java -jar mqttclient.jar http://192.168.100.100:9090/api`

