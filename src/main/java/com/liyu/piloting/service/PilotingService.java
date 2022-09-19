package com.liyu.piloting.service;

import com.liyu.piloting.config.LineConfig;
import com.liyu.piloting.config.LineJudgmentConfig;
import com.liyu.piloting.model.*;
import com.liyu.piloting.util.EarthMapUtil;
import com.liyu.piloting.websocket.model.WebSocketMessage;
import com.liyu.piloting.websocket.util.WebSocketSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import static com.liyu.piloting.websocket.constant.WebSocketConstant.VIDEO_PILOTING_CAMERA;
import static com.liyu.piloting.websocket.constant.WebSocketConstant.VIDEO_PILOTING_CAMERA_OVER;

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
    @Autowired
    LineJudgmentConfig lineJudgmentConfig;

    private LineInstance lineInstance;
    private Deque<Point> deque;
    private long updateQueueTimestamp = System.currentTimeMillis();
    private long endJudgmentTimestamp = System.currentTimeMillis();


    public void process(Point point) {

        if (pointInQueue(point)) return;


//        log.info("deque.size()={},lineInstance={}", deque.size(), lineInstance.toString());
        //判断车辆是否启动及启动后方向
        if (lineInstance.lineStatusIsInit()) {
            lineStartJudgment();
        }

        //开始启动后计算拉流摄像头及是否拉流完最后一个摄像头等待到达终点
        if (lineInstance.lineStatusIsStart()) {
            pullCameraJudgment();
        }

        //计算是否到达终点
        if (lineInstance.lineStatusIsAllPull() || lineInstance.lineStatusIsStart()) {
            lineEndJudgment();
        }

        //终点后判断是否停车后重新初始化数据
        if (lineInstance.lineStatusIsEnd()) {
            lineStopJudgment();
        }
    }

    private void lineStopJudgment() {
        //判断队列数和间隔
        int judgmentPositionCount = lineJudgmentConfig.getLineStopJudgmentPositionCount();
        long judgmentInterval = lineJudgmentConfig.getLineStopJudgmentTimeInterval();
        double varianceNum = lineJudgmentConfig.getLineStopJudgmentVariance();

        if ((deque.size() >= judgmentPositionCount) && ((judgmentInterval + endJudgmentTimestamp) > System.currentTimeMillis())) {
            Variance variance = new Variance();
            double[] distance = new double[judgmentPositionCount];
            StationPosition stationPosition;
            if (lineInstance.directionIsPositive()) {
                stationPosition = lineInstance.getEndStation();
            } else {
                stationPosition = lineInstance.getStartStation();
            }
            int i = 0, j = 0;
            Iterator<Point> pointIterator = deque.iterator();
            while (pointIterator.hasNext()) {
                Point point = pointIterator.next();
                //逆向遍历 需要跳过前面时间比较旧的
                if (i >= deque.size() - judgmentPositionCount) {
                    distance[j++] = EarthMapUtil.distance(stationPosition.getLatitude(), stationPosition.getLongitude(), point.getLatitude(), point.getLongitude());
                }
                i++;
            }

            //计算方差
            double evaluate = variance.evaluate(distance);
            log.info("lineStopJudgment evaluate={},varianceNum={}", evaluate, varianceNum);
            if (evaluate <= varianceNum) {
                log.info("lineStopJudgment stop and init");
                lineInstance = null;
                deque = null;
            }
        }


    }

    private boolean pointInQueue(Point point) {
        long now = System.currentTimeMillis();
        long positionExpireTime = lineJudgmentConfig.getPositionExpireTime();
        long positionStoreInterval = lineJudgmentConfig.getPositionStoreInterval();
        int positionQueueCapacity = lineJudgmentConfig.getPositionQueueCapacity();

        //当前数据超过2s过期
        if (now - point.getTimestamp() > positionExpireTime) return true;

        //距离上次数据更新是否超过1s
        if (now - this.updateQueueTimestamp < positionStoreInterval) return true;

        if (this.lineInstance == null) this.lineInstance = lineConfig.lineInstance();

        if (this.deque == null) this.deque = new ArrayDeque<>(5);
        if (deque.size() >= positionQueueCapacity) {
            deque.removeFirst();
        }
        deque.addLast(point);

        return false;
    }

    private void lineEndJudgment() {
        //摄像头是否拉取结束
        long unPulCount = lineInstance.getCameraList().stream().filter(Camera::statusIsUnPull).count();
        long pulledCount = lineInstance.getCameraList().stream().filter(Camera::statusIsPull).count();
        if (unPulCount == 0 && pulledCount == 0) {
            int lineEndJudgmentPositionCount = lineJudgmentConfig.getLineEndJudgmentPositionCount();
            if (deque.size() >= lineEndJudgmentPositionCount) {
                int lineEndSatisfyDistanceMeter = lineJudgmentConfig.getLineEndSatisfyDistanceMeter();
                int lineEndSatisfyDistanceCount = lineJudgmentConfig.LineEndSatisfyDistanceCount();

                Point referencePoint = new Point();
                if (lineInstance.directionIsPositive()) {
                    referencePoint.setLatitude(lineInstance.getEndStation().getLatitude());
                    referencePoint.setLongitude(lineInstance.getEndStation().getLongitude());
                } else {
                    referencePoint.setLatitude(lineInstance.getStartStation().getLatitude());
                    referencePoint.setLongitude(lineInstance.getStartStation().getLongitude());
                }
                //计算距离是否满足条件拉取的距离条件，连续指定数量的位置在200m内
                log.info("lineEndJudgment endStation?");
                int satisfyDistanceCount = getSatisfyDistanceCountWithReferencePoint(referencePoint, lineEndJudgmentPositionCount, lineEndSatisfyDistanceMeter);

                log.info("lineEndJudgment satisfyDistanceCount ={}", satisfyDistanceCount);
                if (satisfyDistanceCount > lineEndSatisfyDistanceCount) {
                    log.info("lineEndJudgment end line ,preStatus={}", lineInstance.getStatus());
                    lineInstance.lineStatusEnd();
                }
            }
        }

    }

    private void pullCameraJudgment() {
        //根据最近5个位置判断最近的摄像头拉取
        int pullCameraJudgmentPositionCount = lineJudgmentConfig.getPullCameraJudgmentPositionCount();
        if (deque.size() >= pullCameraJudgmentPositionCount) {

            if (lineInstance.getNowCamera() != null && lineInstance.getNowCamera().statusIsPull()) {
                int pullCameraJudgmentIntervalMeter = lineJudgmentConfig.getPullCameraJudgmentIntervalMeter();
                int pullCameraOverPositionCount = lineJudgmentConfig.getPullCameraOverPositionCount();
                int pullCameraDirectionScoreThreshold = lineJudgmentConfig.getPullCameraDirectionScoreThreshold();

                Point referencePoint = new Point();
                referencePoint.setLatitude(lineInstance.getNowCamera().getLatitude());
                referencePoint.setLongitude(lineInstance.getNowCamera().getLongitude());
                log.info("pullCameraJudgment cameraDirection?");
                int cameraDirection = directionWithReferencePoint(referencePoint, pullCameraJudgmentPositionCount, pullCameraJudgmentIntervalMeter, pullCameraDirectionScoreThreshold);

                //驶离了摄像头 可以停止拉流
                if (cameraDirection > 0) {
                    log.info("pullCameraJudgment cameraOver?");
                    int cameraOverSatisfyDistanceCount = lineJudgmentConfig.getCameraOverSatisfyDistanceCount();
                    int cameraOverSatisfyDistanceMeter = lineJudgmentConfig.getCameraOverSatisfyDistanceMeter();

                    int satisfyDistanceCountWithReferencePoint = getSatisfyDistanceCountWithReferencePoint(referencePoint, pullCameraOverPositionCount, cameraOverSatisfyDistanceMeter);

                    if (satisfyDistanceCountWithReferencePoint >= cameraOverSatisfyDistanceCount) {
                        log.info("pullCameraJudgment statusPullOver preCameraDirection={},lastCamera()={}", cameraDirection, lineInstance.getLastCamera());
                        lineInstance.getNowCamera().statusPullOver();
                        WebSocketMessage<Camera> message = new WebSocketMessage<>();
                        message.setContent(lineInstance.getNowCamera())
                                .setMsgType(VIDEO_PILOTING_CAMERA_OVER);
                        WebSocketSender.pushMessageToAll(message);
                        log.info("pullCameraJudgment  camera over ={}", lineInstance.getNowCamera().toString());

                        long unPulCount = lineInstance.getCameraList().stream().filter(Camera::statusIsUnPull).count();
                        long pulledCount = lineInstance.getCameraList().stream().filter(Camera::statusIsPull).count();
                        if (unPulCount == 0 && pulledCount == 0) {
                            //是否所有摄像头都被拉取
                            log.info("pullCameraJudgment camera all pulled");
                            lineInstance.lineStatusAllPull();
                        }
                    }
                }
            }
            pullNextCamera();
        }
    }

    private void pullNextCamera() {
        //获取下一个摄像头
        Camera next = null;
        List<Camera> cameraList = lineInstance.getCameraList();
        if (!cameraList.isEmpty()) {
            //根据方向获取第一个未拉取的摄像头
            if (lineInstance.directionIsPositive()) {
                for (int i = 0; i < cameraList.size(); i++) {
                    if (cameraList.get(i).statusIsUnPull()) {
                        next = cameraList.get(i);
                        break;
                    }

                }
            } else {
                for (int i = cameraList.size() - 1; i > -1; i--) {
                    if (cameraList.get(i).statusIsUnPull()) {
                        next = cameraList.get(i);
                        break;
                    }
                }
            }
            if (next != null) {
                int pullCameraJudgmentPositionCount = lineJudgmentConfig.getPullCameraJudgmentPositionCount();
                int pullCameraSatisfyDistanceMeter = lineJudgmentConfig.getPullCameraSatisfyDistanceMeter();
                int pullCameraSatisfyDistanceCount = lineJudgmentConfig.getPullCameraSatisfyDistanceCount();

                Point referencePoint = new Point();
                referencePoint.setLatitude(next.getLatitude());
                referencePoint.setLongitude(next.getLongitude());
                log.info("pullCameraJudgment camera next?");
                int satisfyDistanceCount = getSatisfyDistanceCountWithReferencePoint(referencePoint, pullCameraJudgmentPositionCount, pullCameraSatisfyDistanceMeter);

                //满足具体要求时拉取摄像头
                log.info("pullCameraJudgment satisfyDistanceCount={},camera={}", satisfyDistanceCount, next.toString());
                if (satisfyDistanceCount >= pullCameraSatisfyDistanceCount) {
                    lineInstance.setLastCamera(lineInstance.getNowCamera());
                    next.statusPull();
                    lineInstance.setNowCamera(next);

                    WebSocketMessage<Camera> message = new WebSocketMessage<>();
                    message.setContent(next)
                            .setMsgType(VIDEO_PILOTING_CAMERA);
                    WebSocketSender.pushMessageToAll(message);
                    log.info("pullCameraJudgment pull new camera ={}", next.toString());
                }
            }
        } else {
            log.info("pullCameraJudgment cameraList is empty");
        }
    }

    /**
     * 满足指定距离的点数量
     *
     * @param referencePoint         参考点
     * @param calculatePositionCount 计算点数量
     * @param distanceThreshold      满足距离
     * @return
     */
    private int getSatisfyDistanceCountWithReferencePoint(Point referencePoint, int calculatePositionCount, int distanceThreshold) {
        //计算距离是否满足条件拉取的距离条件，连续指定数量的位置在1000m内
        int satisfyDistanceCount = 0;

        int i = 0;
        Iterator<Point> pointIterator = deque.iterator();
        while (pointIterator.hasNext()) {
            Point point = pointIterator.next();
            //逆向遍历 需要跳过前面时间比较旧的
            if (i >= deque.size() - calculatePositionCount) {
                double distance = EarthMapUtil.distance(referencePoint.getLatitude(), referencePoint.getLongitude(), point.getLatitude(), point.getLongitude());
                log.info("getSatisfyDistanceCountWithReferencePoint distance={},distanceThreshold={}", distance, distanceThreshold);
                if (distance <= distanceThreshold) {
                    satisfyDistanceCount++;
                }
            }
            i++;
        }
        return satisfyDistanceCount;
    }

    private void lineStartJudgment() {
        //根据最近4个位置判断起始点在哪里
        int lineStartJudgmentPositionCount = lineJudgmentConfig.getLineStartJudgmentPositionCount();

        if (deque.size() >= lineStartJudgmentPositionCount) {
            int startJudgmentIntervalMeter = lineJudgmentConfig.getStartJudgmentIntervalMeter();
            int startDirectionScoreThreshold = lineJudgmentConfig.getStartDirectionScoreThreshold();

            Point referencePoint = new Point();
            referencePoint.setLatitude(lineInstance.getStartStation().getLatitude());
            referencePoint.setLongitude(lineInstance.getStartStation().getLongitude());

            log.info("lineStartJudgment lineDirection?");
            int direction = directionWithReferencePoint(referencePoint, lineStartJudgmentPositionCount, startJudgmentIntervalMeter, startDirectionScoreThreshold);
            if (direction == 1) {
                lineInstance.directionPositive();
                lineInstance.lineStatusStart();
                log.info("directionWithReferencePoint start line directionPositive={}", lineInstance.directionIsPositive());
            } else if (direction == -1) {
                lineInstance.directionNegative();
                lineInstance.lineStatusStart();
                log.info("directionWithReferencePoint start line directionPositive={}", lineInstance.directionIsPositive());
            }


        }
    }

    /**
     * 依据参照点判断方向
     *
     * @param calculatePositionCount 用于计算的点的数量
     * @param intervalMeter          判断移动的点的距离
     * @param scoreThreshold         得分有效大小
     * @return -1 反向，0 条件不足判断方向，1 正向
     */
    private int directionWithReferencePoint(Point referencePoint, int calculatePositionCount, int intervalMeter, int scoreThreshold) {
        //方向算分
        int score = 0;
        //上一个位置距离
        double pre = 0;
        int i = 0;
        Iterator<Point> pointIterator = deque.iterator();
        while (pointIterator.hasNext()) {
            Point point = pointIterator.next();
            //逆向遍历 需要跳过前面时间比较旧的
            if (i >= deque.size() - calculatePositionCount) {
                //计算点与线路起点的距离
                double distance = EarthMapUtil.distance(referencePoint.getLatitude(), referencePoint.getLongitude(), point.getLatitude(), point.getLongitude());
                //间距大于指定距离，判定为开始，如果距离起始站越来越远则为正向，反之反向
                log.info("directionWithReferencePoint pre={},distance={},score={}", pre, distance, score);
                if (pre != 0 && Math.abs(distance - pre) > intervalMeter) {
                    //比上一个距离大 正向
                    if (distance > pre) score++;
                    //比上一个距离小 反向
                    if (distance < pre) score--;
                }
                pre = distance;
                //score2
//                    int score2 = 0;
//                    double start = EarthMapUtil.distance(startStation.getLatitude(), startStation.getLongitude(), p.getLatitude(), p.getLongitude());
//                    double end = EarthMapUtil.distance(startStation.getLatitude(), startStation.getLongitude(), p.getLatitude(), p.getLongitude());
//                    if (start < end) score2++;
//                    else score2--;
            }
            i++;

        }
        //根据score计算方向
        log.info("directionWithReferencePoint score={},preStatus={}", score, lineInstance.getStatus());
        if (Math.abs(score) >= scoreThreshold) {
            if (score > 0) {
                return 1;
            } else if (score < 0) {
                return -1;
            }
        }
        return 0;
    }

}