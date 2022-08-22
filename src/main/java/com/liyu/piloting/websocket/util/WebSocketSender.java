package com.liyu.piloting.websocket.util;

import com.alibaba.fastjson.JSONObject;
import com.liyu.piloting.websocket.config.WsSessionManager;
import com.liyu.piloting.websocket.model.WebSocketMessage;
import com.liyu.piloting.websocket.model.WebSocketSessionHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Laixiaopeng
 */
@Slf4j(topic = "WebSocketSender")
public class WebSocketSender {

    /**
     * 单点发送:转发
     *
     * @param sender
     * @param msg
     * @param receiver
     */
    public static void transferMessageToUser(String sender, WebSocketMessage<?> msg, String receiver) {
        sendMessage(receiver, JSONObject.toJSONStringWithDateFormat(msg, "yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 单点发送：推送
     *
     * @param msg
     * @param receiver
     */
    public static void pushMessageToUser(WebSocketMessage<?> msg, String receiver) {
        sendMessage(receiver, JSONObject.toJSONStringWithDateFormat(msg, "yyyy-MM-dd HH:mm:ss"));
    }


    /**
     * 群发
     *
     * @param msg
     */
    public static void pushMessageToAll(WebSocketMessage<?> msg) {
        sendMessageByGroup(JSONObject.toJSONStringWithDateFormat(msg, "yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 群发(根据信息需求类别获取:location;warn;order)
     *
     * @param msg
     */
    public static void pushMessageToAllByType(WebSocketMessage<?> msg, String type, Set<String> exceptionList) {
        sendMessageByType(JSONObject.toJSONStringWithDateFormat(msg, "yyyy-MM-dd HH:mm:ss"), type, exceptionList);
    }

    public static void pushMessageByTypeAndGroupIn(WebSocketMessage<?> msg, String type, Set<String> containsSet) {
        sendMessageByTypeAndGroupIn(JSONObject.toJSONStringWithDateFormat(msg, "yyyy-MM-dd HH:mm:ss"), type, containsSet);
    }


    public static void sendMessage(String receiver, String message) {
        sendMessage(receiver, message, null);
    }

    public static void sendMessage(String receiver, String message, ConcurrentHashMap<String, WebSocketSessionHolder> usernameSession) {
        sendMessage(receiver, null, message, usernameSession);
    }

    /**
     * 推送信息
     *
     * @param receiver
     * @param message
     * @param targetSessionId
     * @param session
     */
    public static void sendMessage(String receiver, String message, String targetSessionId, WebSocketSession session) {
        try {
            sendMessageWithSession(message, session);
        } catch (IOException e) {
            log.info("username={},sessionId={},消息推送失败推送失败", receiver, targetSessionId);
            e.printStackTrace();
        }
    }

    public static void sendMessage(String message, WebSocketSessionHolder sessionHolder) {
        try {
            log.info("username={},sessionId={},消息推送成功", sessionHolder.getUsername(), sessionHolder.getSessionId());
            sendMessageWithSession(message, sessionHolder.getSession());
        } catch (IOException e) {
            log.info("username={},sessionId={},消息推送失败推送失败", sessionHolder.getUsername(), sessionHolder.getSessionId());
            e.printStackTrace();
        }
    }

    /**
     * 条件广播
     *
     * @param message         发送的消息
     * @param usernameSession
     * @param type            发送的类型（单个）
     * @param exceptionSet    排除的用户
     * @param containsSet     指定发送的用户
     */
    public static void sendMessage(String message, ConcurrentHashMap<String, WebSocketSessionHolder> usernameSession, String type, Set<String> exceptionSet, Set<String> containsSet) {

        for (WebSocketSessionHolder sessionHolder : usernameSession.values()) {
            if (StringUtils.isNotBlank(type)) {
                Set<String> typeSet = sessionHolder.getTypeSet();
                if (!typeSet.contains(type)) {
                    continue;
                }
            }
            if (exceptionSet != null && exceptionSet.size() > 0) {
                if (exceptionSet.contains(sessionHolder.getUsername())) {
                    continue;
                }
            }
            if (containsSet != null && containsSet.size() > 0) {
                if (!containsSet.contains(sessionHolder.getUsername())) {
                    continue;
                }
            }
            sendMessage(message, sessionHolder);
        }
    }

    public static void sendMessage(String message, ConcurrentHashMap<String, WebSocketSessionHolder> usernameSession) {
        for (WebSocketSessionHolder sessionHolder : usernameSession.values()) {
            sendMessage(message, sessionHolder);
        }
    }

    /**
     * 指定用户 sessionId 发送
     *
     * @param receiver
     * @param targetSessionId
     * @param message
     * @param usernameSession
     */
    public static void sendMessage(String receiver, String targetSessionId, String message, ConcurrentHashMap<String, WebSocketSessionHolder> usernameSession) {
        if (usernameSession == null) {
            usernameSession = WsSessionManager.get(receiver);
        }
        if(usernameSession == null){
            log.info("{} 未连接或已断开",receiver);
            return;
        }
        usernameSession.forEach(
                (sessionId, sessionHolder) -> {
                    if (targetSessionId == null || Objects.equals(targetSessionId, sessionId))
                        sendMessage(message, sessionHolder);
                }
        );

    }


    public static void sendMessageWithSession(String message, WebSocketSession session) throws IOException {
        session.sendMessage(new TextMessage(message));
    }

    /**
     * 广播
     *
     * @param message
     */
    public static void sendMessageByGroup(String message) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSessionHolder>> sessionPool = WsSessionManager.getSESSION_POOL();
        sessionPool.forEach(
                (username, usernameSession) -> sendMessage(message, usernameSession)
        );
    }


    /**
     * 广播
     *
     * @param message
     */
    public static void sendMessageByType(String message, String type, Set<String> exceptionSet) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSessionHolder>> sessionPool = WsSessionManager.getSESSION_POOL();
        sessionPool.forEach(
                (username, usernameSession) -> sendMessage(message, usernameSession, type, exceptionSet, null)
        );
    }

    /**
     * 广播
     *
     * @param message
     */
    public static void sendMessageByTypeAndGroupIn(String message, String type, Set<String> containsSet) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSessionHolder>> sessionPool = WsSessionManager.getSESSION_POOL();
        sessionPool.forEach(
                (username, usernameSession) -> sendMessage(message, usernameSession, type, null, containsSet)
        );
    }
}
