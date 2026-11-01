package com.sky.websocket;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 管理端 WebSocket，新订单时广播消息。 */
@Component
@ServerEndpoint("/ws/{sid}")
public class WebSocketServer {

    private static final Map<String, Session> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        SESSIONS.put(sid, session);
    }

    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        SESSIONS.remove(sid);
    }

    @OnMessage
    public void onMessage(String message) {
        // 本功能只做服务端推送，客户端消息不处理
    }

    @OnError
    public void onError(Session session, Throwable error) {
        SESSIONS.values().remove(session);
    }

    /** 给所有在线管理端广播。 */
    public static void sendToAll(String message) {
        SESSIONS.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(message);
                }
            } catch (Exception ignored) {
                // 单个连接异常不影响其他连接
            }
        });
    }
}
