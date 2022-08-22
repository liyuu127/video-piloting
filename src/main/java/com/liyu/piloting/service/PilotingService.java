package com.liyu.piloting.service;

import com.liyu.piloting.model.*;
import com.liyu.piloting.util.EarthMapUtil;
import com.liyu.piloting.websocket.model.WebSocketMessage;
import com.liyu.piloting.websocket.util.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import static com.liyu.piloting.websocket.constant.WebSocketConstant.VIDEO_PILOTING;

/**
 * @author liyu
 * date 2022/8/22 10:59
 * description
 */
@Service
@Slf4j
public class PilotingService {
    @Autowired
    LineConfig lineConfig;

    private LineInstance lineInstance;
    private Deque<Point> deque;
    private long updateTimestamp = System.currentTimeMillis();


    private void process(Point point) {

        //当前数据超过2s过期
        long now = System.currentTimeMillis();
        if (now - point.getTimestamp() > 2 * 1000) return;
        //距离上次数据更新是否超过1s
        if (now - this.updateTimestamp < 1 * 1000) return;

        if (this.lineInstance == null) this.lineInstance = lineConfig.lineInstance();

        if (this.deque == null) this.deque = new ArrayDeque<>(5);
        if (deque.size() >= 10) {
            deque.poll();
            deque.offer(point);
        }

        //是否开始启动
        if (lineInstance.lineStatusIsInit()) {
            lineStartJudgment();
        }

        if (lineInstance.lineStatusIsStart()) {
            pullCameraJudgment();
        }

        if (lineInstance.lineStatusIsAllPull() || lineInstance.lineStatusIsStart()) {
            lineEndJudgment();
        }

    }

    private void lineEndJudgment() {
        //根据最近10个位置判断是否到站
        if (deque.size() > 10) {
            int score = 0;
            StationPosition endStation = lineInstance.getEndStation();
            for (Point p : deque) {
                double distance = EarthMapUtil.distance(endStation.getLatitude(), endStation.getLongitude(), p.getLatitude(), p.getLongitude());
                //间距小于1
                if (distance < 1) {
                    score++;
                } else {
                    score--;
                }
            }
            log.info("lineStatusIsAllPull score={}", score);
            if (score > 5) {
                log.info("end line ,preStatus={}", lineInstance.getStatus());
                lineInstance.lineStatusEnd();
            }
        }
    }

    private void pullCameraJudgment() {
        //根据最近5个位置判断最近的摄像头拉取
        if (deque.size() >= 5) {
            //获取下一个摄像头
            Camera next = null;
            List<Camera> cameraList = lineInstance.getCameraList();
            if (!cameraList.isEmpty()) {
                if (lineInstance.directionIsPositive()) {
                    for (int i = 0; i < cameraList.size(); i++) {
                        if (cameraList.get(i).statusIsUnPull()) {
                            next = cameraList.get(i);
                        }

                    }
                } else {
                    for (int i = cameraList.size() - 1; i > -1; i--) {
                        if (cameraList.get(i).statusIsUnPull()) {
                            next = cameraList.get(i);
                        }
                    }
                }
                if (next == null) {
                    log.info("camera all pulled");
                    lineInstance.lineStatusAllPull();

                } else {
                    //计算距离是否满足条件
                    int score = 0;
                    for (Point p : deque) {
                        double distance = EarthMapUtil.distance(next.getLatitude(), next.getLongitude(), p.getLatitude(), p.getLongitude());
                        if (distance <= 1000) {
                            score++;
                        }
                    }
                    //分数大于3切换
                    if (score > 3) {
                        log.info("pull new camera,score={},camera={}", score, next.toString());
                        lineInstance.setLastCamera(lineInstance.getNowCamera());
                        lineInstance.setNowCamera(next);
                        WebSocketMessage<Camera> message = new WebSocketMessage<>();
                        message.setContent(next)
                                .setMsgType(VIDEO_PILOTING);
                        WebSocketSender.pushMessageToAll(message);
                    } else {
                        log.info("pull new camera score low,score={}", score);
                    }

                }
            } else {
                log.info("cameraList is empty");
            }


        }
    }

    private void lineStartJudgment() {
        //根据最近4个位置判断起始点在哪里
        if (deque.size() >= 4) {

            //score1
            int score1 = 0;
            double pre = 0;
            StationPosition startStation = lineInstance.getStartStation();
            for (Point p : deque) {
                double distance = EarthMapUtil.distance(startStation.getLatitude(), startStation.getLongitude(), p.getLatitude(), p.getLongitude());
                //间距大于1
                if (pre != 0 && Math.abs(distance = pre) > 2) {
                    if (distance > pre) score1++;
                    if (distance < pre) score1--;
                }
                pre = distance;


                //score2
//                    int score2 = 0;
//                    double start = EarthMapUtil.distance(startStation.getLatitude(), startStation.getLongitude(), p.getLatitude(), p.getLongitude());
//                    double end = EarthMapUtil.distance(startStation.getLatitude(), startStation.getLongitude(), p.getLatitude(), p.getLongitude());
//                    if (start < end) score2++;
//                    else score2--;
            }

            log.info("start score={}", score1);
            if (score1 > 0) {
                log.info("start line directionPositive,preStatus={}", lineInstance.getStatus());
                lineInstance.directionPositive();
                lineInstance.lineStatusStart();
            } else if (score1 < 0) {
                log.info("start line directionNegative,preStatus={}", lineInstance.getStatus());
                lineInstance.directionNegative();
                lineInstance.lineStatusStart();
            }

        }
    }
}