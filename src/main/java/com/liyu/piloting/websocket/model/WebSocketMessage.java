package com.liyu.piloting.websocket.model;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author Laixiaopeng
 */
@Data
@Accessors(chain = true)
public class WebSocketMessage<T> {
    private String sender;
    private String receiver;
    private String sendType;
    private String msgType;
    private T content;
}
