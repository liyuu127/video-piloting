package com.liyu.piloting.websocket.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

/**
 * @author liyu
 * date 2021/3/8 17:52
 * description
 */
@Slf4j(topic = "WebSocketUtil")
public class WebSocketUtil {

    private final static String IP_HEADER = "X-Forwarded-For";
    private final static String X_DEVICE_ID = "X-Device-ID";

    /**
     * 获取连接的唯一标识
     *
     * @param session
     * @return
     */
    public static String getWebSocketSessionIp(WebSocketSession session) {
//        HttpHeaders handshakeHeaders = session.getHandshakeHeaders();
//        //尝试获取ip
//        List<String> ipList = handshakeHeaders.get(IP_HEADER);
//        if (ipList != null && ipList.size() > 0) {
//            return ipList.get(0);
//        }
//
//        //如果为空尝试获取X-Device-ID
//        List<String> deviceIdList = handshakeHeaders.get(X_DEVICE_ID);
//        if (deviceIdList != null && deviceIdList.size() > 0) {
//            log.info("{}连接客户端ip获取失败,获取deviceId={}", session.getAttributes().get("name"), deviceIdList.get(0));
//            return deviceIdList.get(0);
//        }
//
//        //返回远程主机地址，避免报错
//        log.info("{}连接客户端ip获取失败", session.getAttributes().get("name"));
//        String hostString = session.getRemoteAddress().getHostString();
//        if (StringUtils.isNotBlank(hostString)) {
//            return hostString;
//        }

        return session.getId();
    }

}
