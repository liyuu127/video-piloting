package com.liyu.piloting.websocket.config;

import com.liyu.piloting.websocket.model.WebSocketSessionHolder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liyu
 * date 2021/3/8 16:55
 * description
 */
@Slf4j
public class WsSessionManager {
    /**
     * 保存连接 session 的地方
     * <username,<sessionId,WebSocketSessionHolder>>
     */
    @Getter
    private static ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSessionHolder>> SESSION_POOL = new ConcurrentHashMap<>();


    /**
     * 添加 session
     * 覆盖同一ip的用户的session和type
     *
     * @param
     */
    public static void add(String userName, String type, WebSocketSession session) throws IOException {
        // 添加 session
        String sessionId = session.getId();
        if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(type)) {
            WebSocketSessionHolder sessionHolder = new WebSocketSessionHolder(session, userName, type);
            ConcurrentHashMap<String, WebSocketSessionHolder> usernameSession = SESSION_POOL.get(userName);
            if (usernameSession == null) {
                usernameSession = new ConcurrentHashMap<>();
                usernameSession.put(sessionId, sessionHolder);
            } else {
                WebSocketSessionHolder oldSessionHolder = usernameSession.get(sessionId);
                log.info("{}==>socket重复加入断开之前连接", userName);
                if (oldSessionHolder != null) {
                    oldSessionHolder.destroy();
                    oldSessionHolder = null;
                }
                usernameSession.put(sessionId, sessionHolder);
            }
            SESSION_POOL.put(userName, usernameSession);

            log.info(">>>user:{} connected,sessionId={},current connected:{}<<<", userName, sessionId, SESSION_POOL.keySet());
        }
    }


    /**
     * 删除 session,会返回删除的 session
     *
     * @param
     * @return
     */
    public static WebSocketSession remove(String userName, WebSocketSession session) {
        // 删除 session
        String sessionId = session.getId();
        ConcurrentHashMap<String, WebSocketSessionHolder> usernameSession = SESSION_POOL.get(userName);
        if (usernameSession != null) {
            return usernameSession.remove(sessionId).getSession();
        }
        return null;
    }

    /**
     * 删除并同步关闭连接
     *
     * @param
     */
    public static void removeAndClose(String userName, WebSocketSession session) {
        WebSocketSession unSession = remove(userName, session);
        log.info(">>>user:{} removeAndClose,sessionId={},current connected:{}<<<", userName, session.getId(), SESSION_POOL.keySet());
        if (unSession != null) {
            try {
                // 关闭连接
                unSession.close();
            } catch (IOException e) {
                // todo: 关闭出现异常处理
                e.printStackTrace();
            }
        }
    }

    /**
     * 获得 session
     *
     * @param userName
     * @return
     */
    public static ConcurrentHashMap<String, WebSocketSessionHolder> get(String userName) {
        // 获得 session
        ConcurrentHashMap<String, WebSocketSessionHolder> usernameSession = SESSION_POOL.get(userName);
        return usernameSession;
    }
}
