package com.liyu.piloting.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.liyu.piloting.model.AccessToken;
import com.liyu.piloting.model.Alarm;
import com.liyu.piloting.model.ResPage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @author liyu
 * date 2022/9/28 18:28
 * description
 */
@Service
@Slf4j(topic = "AlarmService")
public class AlarmService {
    @Autowired
    RestTemplate restTemplate;

    private static final String AppKey = "9bf4cdbcf95f428f91bc22fc568197c0";
    private static final String Secret = "664382388b3f6a2ea625d3d31d27e53a";
    private static final String url_access_token = "https://open.ys7.com/api/lapp/token/get";
    private static final String url_device_alarm = "https://open.ys7.com/api/lapp/alarm/device/list";


    private AccessToken accessToken;

    /**
     * 按照设备获取告警消息列表
     *
     * @param deviceSerial 设备序列号
     * @link https://open.ys7.com/doc/zh/book/index/device_alarm.html
     */
    public void queryAlarmDeviceList(String deviceSerial) {
        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("accessToken", token);
        map.add("deviceSerial", deviceSerial);//设备序列号,存在英文字母的设备序列号，字母需为大写
        map.add("alarmType", "-1");//告警类型，默认为-1（全部）
        map.add("status", "0");//告警消息状态：2-所有，1-已读，0-未读，默认为0（未读状态）
        map.add("startTime", System.currentTimeMillis() - 1000 * 60 * 60 + "");//告警查询开始时间，时间格式为1457420564508，精确到毫秒，默认为今日0点，最多查询当前时间起前推7天以内的数据
//        map.add("endTime", "0");//告警查询结束时间，时间格式为1457420771029，默认为当前时间
        map.add("pageSize", "50");//分页大小，默认为10，最大为,50
        map.add("pageStart", "0");//分页起始页，从0开始
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<String> exchange = restTemplate.exchange(url_device_alarm, HttpMethod.POST, entity, String.class);
        if (exchange.getStatusCode() != HttpStatus.OK || exchange.getBody() == null) {
            log.error("queryAlarmDeviceList error exchange={}", exchange.toString());
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(exchange.getBody());
        if (!StringUtils.equals("200", jsonObject.getString("code"))) {
            log.error("queryAlarmDeviceList error exchange={}", exchange.toString());
            return;
        }
        List<Alarm> alarms;
        JSONArray data = jsonObject.getJSONArray("data");
        alarms = data.toJavaList(Alarm.class);

        for (Alarm alarm : alarms) {
            System.out.println("alarm = " + alarm);
        }
    }

    /**
     * 获取token
     */
    public void accessToken() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("appKey", AppKey);
        map.add("appSecret", Secret);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<String> exchange = restTemplate.exchange(url_access_token, HttpMethod.POST, entity, String.class);
        if (exchange.getStatusCode() != HttpStatus.OK || exchange.getBody() == null) {
            log.error("get accessToken error exchange={}", exchange.toString());
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(exchange.getBody());
        if (!StringUtils.equals("200", jsonObject.getString("code"))) {
            log.error("get accessToken error exchange={}", exchange.toString());
            return;
        }
        JSONObject data = jsonObject.getJSONObject("data");
        accessToken = data.toJavaObject(AccessToken.class);
    }

    public String getAccessToken() {
        if (accessToken == null || (System.currentTimeMillis() + 1000 * 60 * 60) > accessToken.getExpireTime()) {
            log.info("accessToken start");
            accessToken();
            log.info("accessToken success");
        }
        return accessToken.getAccessToken();
    }
}
