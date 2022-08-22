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
    public static final String WARN_MSG_TYPE = "warn";
    public static final String IRON_SHOE_MSG_TYPE = "ironShoe";
    public static final String ORDER_MSG_TYPE = "order";
    public static final String LOCATION_MSG_TYPE = "location";
    public static final String CAMERA_MSG_TYPE = "camera";
    public static final String JOB_PLAN_MSG_TYPE = "jobPlan";
    public static final String SPEED_MSG_TYPE = "speed";
    public static final String AI_WARN_TYPE="aiWarn";
    public static final String PERSON_OFFLINE="offline";
    public static final String BRAKE_MSG_TYPE="brake";

    /**
     * session过期时间 毫秒值
     */
    public static final long session_Expired_time = 1 * 60 * 1000;
    /**
     * webSocket暴露地址
     */
    public static final String SERVER_ENDPOINT_URL = "/websocket/{name}/{type}";


}
