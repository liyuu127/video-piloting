package com.liyu.piloting.websocket.config;

import com.liyu.piloting.websocket.constant.WebSocketConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * @author liyu
 * date 2021/3/8 17:03
 * description 握手拦截器
 */
@Component
public class WebSocketInterceptor implements HandshakeInterceptor {

    /**
     * 握手前
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param attributes
     * @return
     * @throws Exception
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
//        HttpHeaders headers = request.getHeaders();
//        headers.setExpires(WebSocketConstant.session_Expired_time);
        String uri = request.getURI().getPath();
        AntPathMatcher matcher = new AntPathMatcher();
        Map<String, String> variablesParamMap = matcher.extractUriTemplateVariables(WebSocketConstant.SERVER_ENDPOINT_URL, uri);
        if (variablesParamMap != null
                && StringUtils.isNotBlank(variablesParamMap.get("name"))
                && StringUtils.isNotBlank(variablesParamMap.get("type"))) {
            // 获得请求参数
            // HashMap<String, String> paramMap = HttpUtil.decodeParamMap(request.getURI().getQuery(), "utf-8");
            attributes.putAll(variablesParamMap);
            return true;
        }
        return false;
    }

    /**
     * 握手后
     *
     * @param request
     * @param response
     * @param wsHandler
     * @param exception
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
    }
}
