/**
 * aliyun.com Inc.
 * Copyright (c) 2004-2017 All Rights Reserved.
 */
package com.aliyun.iot.demo.iothub;

import java.net.InetAddress;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import java.io.InputStream;
import java.io.IOException;
import okhttp3.*;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.aliyun.iot.util.LogUtil;
import com.aliyun.iot.util.SignUtil;

/**
 * IoT套件JAVA版设备接入demo
 */
public class SimpleClient4IOT {

    public static final MediaType CONTENT_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient client = new OkHttpClient();

    private static String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(CONTENT_TYPE, json);
        Request request = new Request.Builder()
            .url(url)
            .post(body)
            .build();
        try{
            Response response = client.newCall(request).execute();
            return response.body().string();
        }catch(Exception e){
            // e.printStackTrace();
            return "ERROR";
        }
        
    }
	
	/******这里是客户端需要的参数*******/
    public static String deviceName = "newgui";
    public static String productKey = "a15QCAOJErA";
    public static String secret = "h914uk0MPqdjL1ccmvOlVzNZPhos86yU";

    //用于测试的topic
    private static String subTopic = "/" + productKey + "/" + deviceName + "/get";
    private static String pubTopic = "/" + productKey + "/" + deviceName + "/update";

    private static String apiurl = "http://localhost:8080/api";

    public static void main(String... strings) throws Exception {
        Properties properties = new Properties();
        // 使用ClassLoader加载properties配置文件生成对应的输入流
        InputStream in = SimpleClient4IOT.class.getClassLoader().getResourceAsStream("config.properties");
        // 使用properties对象加载输入流
        properties.load(in);
        //获取key对应的value值
        deviceName = properties.getProperty("deviceName");
        productKey = properties.getProperty("productKey");
        secret = properties.getProperty("secret");
        //客户端设备自己的一个标记，建议是MAC或SN，不能为空，32字符内
        String clientId = properties.getProperty("clientId"); //InetAddress.getLocalHost().getHostAddress();

        if(strings.length > 0){
            apiurl = strings[0];
        }
        System.out.println(apiurl);
        

        //设备认证
        Map<String, String> params = new HashMap<String, String>();
        params.put("productKey", productKey); //这个是对应用户在控制台注册的 设备productkey
        params.put("deviceName", deviceName); //这个是对应用户在控制台注册的 设备name
        params.put("clientId", clientId);
        String t = System.currentTimeMillis() + "";
        params.put("timestamp", t);

        //MQTT服务器地址，TLS连接使用ssl开头
        String targetServer = "ssl://" + productKey + ".iot-as-mqtt.cn-shanghai.aliyuncs.com:1883";

        //客户端ID格式，两个||之间的内容为设备端自定义的标记，字符范围[0-9][a-z][A-Z]
        String mqttclientId = clientId + "|securemode=2,signmethod=hmacsha1,timestamp=" + t + "|";
        String mqttUsername = deviceName + "&" + productKey; //mqtt用户名格式
        String mqttPassword = SignUtil.sign(params, secret, "hmacsha1"); //签名

        System.err.println("mqttclientId=" + mqttclientId);

        connectMqtt(targetServer, mqttclientId, mqttUsername, mqttPassword, deviceName);
    }

    public static void connectMqtt(String url, String clientId, String mqttUsername,
                                   String mqttPassword, final String deviceName) throws Exception {
        MemoryPersistence persistence = new MemoryPersistence();
        SSLSocketFactory socketFactory = createSSLSocket();
        final MqttClient sampleClient = new MqttClient(url, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setMqttVersion(4); // MQTT 3.1.1
        connOpts.setSocketFactory(socketFactory);

        //设置是否自动重连
        connOpts.setAutomaticReconnect(true);

        //如果是true，那么清理所有离线消息，即QoS1或者2的所有未接收内容
        connOpts.setCleanSession(false);

        connOpts.setUserName(mqttUsername);
        connOpts.setPassword(mqttPassword.toCharArray());
        connOpts.setKeepAliveInterval(65);

        LogUtil.print(clientId + "进行连接, 目的地: " + url);
        sampleClient.connect(connOpts);

        sampleClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                LogUtil.print("连接失败,原因:" + cause);
                cause.printStackTrace();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                // LogUtil.print("Message Arrived: " + topic);
                // LogUtil.print("Payload: " + new String(message.getPayload(), "UTF-8"));
                LogUtil.print("JSON: " + "{\"topic\":\"" + topic + "\", \"message\":\"" + new String(message.getPayload(), "UTF-8") + "\"}");
                String ret = post(apiurl, "{\"topic\":\"" + topic + "\", \"message\":\"" + new String(message.getPayload(), "UTF-8") + "\"}");
                LogUtil.print("Post message return:" + ret);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //如果是QoS0的消息，token.resp是没有回复的
                LogUtil.print("消息发送成功! " + ((token == null || token.getResponse() == null) ? "null"
                    : token.getResponse().getKey()));
            }
        });
        LogUtil.print("连接成功:---");

        //这里测试发送一条消息
        String content = "{'content':'msg from :" + clientId + "," + System.currentTimeMillis() + "'}";

        MqttMessage message = new MqttMessage(content.getBytes("utf-8"));
        message.setQos(0);
        //System.out.println(System.currentTimeMillis() + "消息发布:---");
        sampleClient.publish(pubTopic, message);

        //一次订阅永久生效 
        //这个是第一种订阅topic方式，回调到统一的callback
        sampleClient.subscribe(subTopic);

        //这个是第二种订阅方式, 订阅某个topic，有独立的callback
        //sampleClient.subscribe(subTopic, new IMqttMessageListener() {
        //    @Override
        //    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //
        //        LogUtil.print("收到消息：" + message + ",topic=" + topic);
        //    }
        //});

        //回复RRPC响应
        final ExecutorService executorService = new ThreadPoolExecutor(2,
            4, 600, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(100), new CallerRunsPolicy());

        String reqTopic = "/sys/" + productKey + "/" + deviceName + "/rrpc/request/+";
        sampleClient.subscribe(reqTopic, new IMqttMessageListener() {
            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                LogUtil.print("收到请求：" + message + ", topic=" + topic);
                String messageId = topic.substring(topic.lastIndexOf('/') + 1);
                final String respTopic = "/sys/" + productKey + "/" + deviceName + "/rrpc/response/" + messageId;
                String content = "hello world";
                final MqttMessage response = new MqttMessage(content.getBytes());
                response.setQos(0); //RRPC只支持QoS0
                //不能在回调线程中调用publish，会阻塞线程，所以使用线程池
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sampleClient.publish(respTopic, response);
                            LogUtil.print("回复响应成功，topic=" + respTopic);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private static SSLSocketFactory createSSLSocket() throws Exception {
        SSLContext context = SSLContext.getInstance("TLSV1.2");
        context.init(null, new TrustManager[] {new ALiyunIotX509TrustManager()}, null);
        SSLSocketFactory socketFactory = context.getSocketFactory();
        return socketFactory;
    }
}
