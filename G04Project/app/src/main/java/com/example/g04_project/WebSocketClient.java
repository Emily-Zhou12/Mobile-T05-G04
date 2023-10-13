package com.example.g04_project;

import android.os.Handler;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class WebSocketClient {

    private OkHttpClient client;
    private WebSocket webSocket;
    private OnMessageReceivedListener listener;
    private Handler handler = new Handler();
    private Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            sendLocationUpdate();
            handler.postDelayed(this, 30000); // 30 seconds
        }
    };
    public interface OnMessageReceivedListener {
        void onMessageReceived(String message);
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.listener = listener;
    }

    private void sendLocationUpdate() {
        // 获取位置信息（这里只是一个示例，你可能需要从GPS或其他位置服务获取实际的位置数据）
        double latitude = 40.7128;
        double longitude = -74.0060;
        long timestamp = System.currentTimeMillis();

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("user_id", "user123");
        locationData.put("latitude", latitude);
        locationData.put("longitude", longitude);
        locationData.put("timestamp", timestamp);

        sendMessage("user_location", locationData);
    }
    public void start() {
        client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("ws://10.0.2.2:8080/ws/user123")
                .build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, okhttp3.Response response) {
                // 当WebSocket连接打开时调用
                sendMessage("auth", Collections.singletonMap("token", "YOUR_SECRET_TOKEN"));
                listener.onMessageReceived("Trying to connect");
                handler.post(locationUpdateRunnable);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                try {
                    Message message = new Gson().fromJson(text, Message.class);
                    if ("Connection established".equals(text)) {
                        if (listener != null) {
                            listener.onMessageReceived("Connection established");
                        }
                        return;
                    }
                    if ("Authentication failed.".equals(text)) {
                        // 处理身份验证失败的情况
                        // 例如，你可以关闭WebSocket连接或通知用户身份验证失败
                        listener.onMessageReceived("Connection failed_0");
                        webSocket.close(1000, "Authentication failed.");
                        return;
                    }
                    if ("user_location".equals(message.getType())) {
                        // 解析并处理用户位置数据
                        UserLocation userLocation = new Gson().fromJson(new Gson().toJson(message.getData()), UserLocation.class);
                        listener.onMessageReceived(userLocation.toString());
                        // ... 处理userLocation数据 ...
                    }
                    else {
                        listener.onMessageReceived("wrong_message_type");
                    }
                } catch (JsonSyntaxException e) {
                    // JSON格式不正确
                    e.printStackTrace();
                    // 你可以在这里添加其他的错误处理逻辑，例如日志记录或用户提示
                } catch (Exception e) {
                    // 其他可能的异常
                    e.printStackTrace();
                    // 你可以在这里添加其他的错误处理逻辑
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                // 当从服务器接收到二进制消息时调用
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, okhttp3.Response response) {
                // 当连接失败或发生错误时调用
                listener.onMessageReceived("Connection failed_1");
                handler.removeCallbacks(locationUpdateRunnable);
                t.printStackTrace();
            }
        });
    }

    public void stop() {
        if (webSocket != null) {
            webSocket.close(1000, "Goodbye!");
        }
    }

    public void sendMessage(String type, Map<String, Object> data) {
        if (webSocket != null) {
            Message message = new Message(type, data);
            String json = new Gson().toJson(message);
            webSocket.send(json);
        }
    }
}