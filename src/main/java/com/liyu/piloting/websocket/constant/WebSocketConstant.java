package com.liyu.piloting.websocket.constant;

/**
 * @author Laixiaopeng
 * @date 2021/1/26 15:59
 * @description
 **/
public class WebSocketConstant {
    /**
     * websocket 消息发送类型
     */
    public static final String SINGLE_SEND_TYPE = "single";
    public static final String GROUP_SEND_TYPE = "group";

    /**
     * websocket 消息类型
     */
    public static final String VIDEO_PILOTING_CAMERA = "video_piloting_camera";
    public static final String VIDEO_PILOTING_CAMERA_OVER = "video_piloting_camera_over";
    public static final String VIDEO_PILOTING_ALARM = "video_piloting_alarm";
    public static final String VIDEO_PILOTING_NO_ALARM = "video_piloting_no_alarm";


    /**
     * session过期时间 毫秒值
     */
    public static final long session_Expired_time = 1 * 60 * 1000;
    /**
     * webSocket暴露地址
     */
    public static final String SERVER_ENDPOINT_URL = "/websocket/{name}/{type}";


}
