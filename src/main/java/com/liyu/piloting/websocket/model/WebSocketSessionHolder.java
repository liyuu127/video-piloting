package com.liyu.piloting.websocket.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author liyu
 * date 2021/3/8 21:48
 * description
 */
@Slf4j(topic = "WebSocketSessionHolder")
public class WebSocketSessionHolder {
    @Getter
    private String username;
    @Getter
    private String sessionId;
    @Getter
    private WebSocketSession session;
    @Getter
    private Set<String> typeSet;

    public WebSocketSessionHolder(WebSocketSession session, String username, String type) {
        this.username = username;
        this.session = session;
        this.sessionId = session.getId();
        Set<String> typeSet = Stream.of(type.split(",")).collect(Collectors.toSet());
        this.typeSet = typeSet;
    }

    public void destroy() {
        try {
            session.close();
            log.info("username={},ip={}关闭连接", username, sessionId);
        } catch (IOException e) {
            log.error("username={},ip={}关闭连接异常", username, sessionId);
            e.printStackTrace();
        } finally {
            username = null;
            session = null;
            typeSet = null;
            sessionId = null;
        }

    }

}
