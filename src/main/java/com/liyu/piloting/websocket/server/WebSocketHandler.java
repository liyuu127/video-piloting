package com.liyu.piloting.websocket.server;

import com.liyu.piloting.websocket.config.WsSessionManager;
import com.liyu.piloting.websocket.constant.ResponseData;
import com.liyu.piloting.websocket.util.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * @author liyu
 * date 2021/3/8 17:00
 * description
 */
@Component
@Slf4j(topic = "WebSocketHandler")
public class WebSocketHandler extends TextWebSocketHandler {

    /**
     * socket 建立成功事件
     * socket 连接成功后被触发，同原生注解里的 @OnOpen 功能
     *
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userName = (String) session.getAttributes().get("name");
        String type = (String) session.getAttributes().get("type");
        try {
            WsSessionManager.add(userName, type, session);
            WebSocketSender.sendMessage(userName, ResponseData.success().toJsonString(), session.getId(), session);
        } catch (Exception e) {
            WebSocketSender.sendMessage(userName, ResponseData.fail().toJsonString(), session.getId(), session);
            log.info("userName={},type={}建立连接异常", userName, type);
            e.printStackTrace();
        }
    }

    /**
     * 接收消息事件
     * 客户端发送信息时触发，同原生注解里的 @OnMessage 功能
     *
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 获得客户端传来的消息
        String payload = message.getPayload();
        log.debug(">>>message from client:{}<<<", payload);
        session.sendMessage(new TextMessage(ResponseData.success().toJsonString()));
    }

    /**
     * socket 断开连接时
     * socket 连接关闭后被触发，同原生注解里的 @OnClose 功能
     *
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userName = (String) session.getAttributes().get("name");
        log.info("{}==>socket主动断开连接",userName);
        WsSessionManager.removeAndClose(userName, session);
    }

}
