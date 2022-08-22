package com.liyu.piloting.websocket.constant;


import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * description 包装类 返回结果进行统一封装
 *
 * @author xuh
 * @since
 **/
@Data
@NoArgsConstructor
public class ResponseData<T> implements Serializable {

    private static final long serialVersionUID = 4893280118017319089L;
    // 编码
    private int code;

    // 信息
    private String message;

    // 数据
    private T result;


    ResponseData(int code, String message) {
        this(code, message, null);
    }

    ResponseData(int code, String message, T result) {
        super();
        this.code(code).message(message).result(result);
    }


    /**
     * 返回成功消息
     *
     * @return
     */
    public static <E> ResponseData<E> success(E o) {
        return new ResponseData<>(ResponseConstant.SUCCESS_CODE, ResponseConstant.SUCCESS_MESSAGE, o);
    }

    /**
     * 返回成功消息
     *
     * @return
     */
    public static <E> ResponseData<E> success() {
        return new ResponseData<>(ResponseConstant.SUCCESS_CODE, ResponseConstant.SUCCESS_MESSAGE);
    }

    /**
     * 返回失败消息
     *
     * @return
     */
    public static <E> ResponseData<E> fail() {
        return new ResponseData<>(ResponseConstant.ERROR_CODE, ResponseConstant.ERROR_MESSAGE);
    }

    /**
     * 返回失败消息
     *
     * @return
     */
    public static <E> ResponseData<E> fail(int code, String message) {
        return new ResponseData<>(code, message);
    }

    /**
     * 返回失败消息
     *
     * @return
     */
    public static <E> ResponseData<E> fail(E o) {
        return new ResponseData<>(ResponseConstant.FAIL_CODE, o.toString());
    }

    /**
     * 返回系统异常消息
     *
     * @return
     */
    public static <E> ResponseData<E> error() {
        return new ResponseData<>(ResponseConstant.ERROR_CODE, ResponseConstant.ERROR_MESSAGE);
    }

    public static <E> ResponseData<E> info(int code, String message) {
        return new ResponseData<>(code, message);
    }

    public static <E> ResponseData<E> info(int code, String message, E result) {
        return new ResponseData<>(code, message, result);
    }

    private ResponseData<T> code(int code) {
        this.setCode(code);
        return this;
    }

    private ResponseData<T> message(String message) {
        this.setMessage(message);
        return this;
    }

    private ResponseData<T> result(T result) {
        this.setResult(result);
        return this;
    }

    public String toJsonString() {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("message", message);
        json.put("result", result);
        return json.toString();
    }
}
