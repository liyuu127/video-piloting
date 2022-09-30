package com.liyu.piloting.service;

import com.liyu.piloting.config.AlarmConf;
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

import javax.annotation.PostConstruct;
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
    @Autowired
    AlarmService alarmService;
    @Autowired
    AlarmConf alarmConf;

    private LineInstance lineInstance;
    private Deque<Point> deque;
    private long updateQueueTimestamp = System.currentTimeMillis();
    private long endJudgmentTimestamp = System.currentTimeMillis();
    /**
     * 查询告警间隔
     */
    private long searchAlarmTimestamp = System.currentTimeMillis();


    @PostConstruct
    public void init() {
        this.lineInstance = lineConfig.lineInstance();
        this.deque = new ArrayDeque<>(lineJudgmentConfig.getPositionQueueCapacity());
    }


    public void process(Point point) {

        //数据入队
        if (pointInQueue(point)) return;


//        log.info("deque.size()={},lineInstance={}", deque.size(), lineInstance.toString());
        //判断车辆是否启动及启动后方向
        if (!directionJudgment()) return;

        //确定方向后计算拉流摄像头
        pullCameraAndAlarm();


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
                    log.info("lineEndJudgment end line ");
                }
            }
        }

    }

    private void pullCameraAndAlarm() {
        //根据最近几个位置和方向判断最近的摄像头拉取
        int pullCameraJudgmentPositionCount = lineJudgmentConfig.getPullCameraJudgmentPositionCount();
        if (deque.size() >= pullCameraJudgmentPositionCount) {
            //当前是否有摄像头正在拉取，尝试判断是否驶离
            nowCameraOverJudgment();
            //判断是否拉取下一个摄像头
            pullNextCamera();
        }
    }

    private void nowCameraOverJudgment() {
        int pullCameraJudgmentPositionCount = lineJudgmentConfig.getPullCameraJudgmentPositionCount();
        Camera nowCamera = lineInstance.getNowCamera();
        if (nowCamera != null) {

            //拉取告警
            Long alarmInterval = alarmConf.getSearchTimeMillisInterval();
            if (System.currentTimeMillis() > alarmInterval + searchAlarmTimestamp) {
                alarmService.processAlarm(nowCamera.getDeviceSerial());
                searchAlarmTimestamp = System.currentTimeMillis();
            }

            int pullCameraJudgmentIntervalMeter = lineJudgmentConfig.getPullCameraJudgmentIntervalMeter();
            int pullCameraDirectionScoreThreshold = lineJudgmentConfig.getPullCameraDirectionScoreThreshold();

            Point referencePoint = new Point();
            referencePoint.setLatitude(nowCamera.getLatitude());
            referencePoint.setLongitude(nowCamera.getLongitude());
            log.info("nowCameraOverJudgment nowCameraDirection?");
            //相对摄像头方向
            int cameraDirection = directionWithReferencePoint(referencePoint, pullCameraJudgmentPositionCount, pullCameraJudgmentIntervalMeter, pullCameraDirectionScoreThreshold);

            //驶离了摄像头指定距离后 可以停止拉流
            if (cameraDirection < 0) {
                log.info("nowCameraOverJudgment cameraOver?");
                int pullCameraOverPositionCount = lineJudgmentConfig.getPullCameraOverPositionCount();
                int cameraOverSatisfyDistanceCount = lineJudgmentConfig.getCameraOverSatisfyDistanceCount();
                int cameraOverSatisfyDistanceMeter = lineJudgmentConfig.getCameraOverSatisfyDistanceMeter();
                int satisfyDistanceCountWithReferencePoint = getSatisfyDistanceCountWithReferencePoint(referencePoint, pullCameraOverPositionCount, cameraOverSatisfyDistanceMeter);
                //是否满足指定距离
                if (satisfyDistanceCountWithReferencePoint >= cameraOverSatisfyDistanceCount) {
                    log.info("nowCameraOverJudgment statusPullOver preCameraDirection={},lastCamera()={}", cameraDirection, lineInstance.getLastCamera());
                    //
                    WebSocketMessage<Camera> message = new WebSocketMessage<>();
                    message.setContent(nowCamera)
                            .setMsgType(VIDEO_PILOTING_CAMERA_OVER);
                    WebSocketSender.pushMessageToAll(message);
                    log.info("nowCameraOverJudgment  websocket camera over ={}", nowCamera.toString());

                    lineInstance.setLastCamera(nowCamera);
                    lineInstance.setNowCamera(null);
                }
            }
        }
    }

    private void pullNextCamera() {
        if (lineInstance.getNowCamera() != null) {
            //当前还有摄像头 暂时不拉取新的
            return;
        }
        Camera next = null;
        //上次是否计算过
        if (lineInstance.getNextCamera() != null) {
            next = lineInstance.getNextCamera();
        }
        //计算下一个摄像头
        if (next == null) {
            next = getNextCamera();
        }

        //判断是否到拉取的位置
        if (next != null) {
            int pullCameraJudgmentPositionCount = lineJudgmentConfig.getPullCameraJudgmentPositionCount();
            int pullCameraSatisfyDistanceMeter = lineJudgmentConfig.getPullCameraSatisfyDistanceMeter();
            int pullCameraSatisfyDistanceCount = lineJudgmentConfig.getPullCameraSatisfyDistanceCount();

            Point rp = new Point();
            rp.setLatitude(next.getLatitude());
            rp.setLongitude(next.getLongitude());
            log.info("pullNextCamera camera distance?");
            int satisfyDistanceCount = getSatisfyDistanceCountWithReferencePoint(rp, pullCameraJudgmentPositionCount, pullCameraSatisfyDistanceMeter);

            //满足具体要求时拉取摄像头
            log.info("pullNextCamera satisfyDistanceCount={},pullCameraSatisfyDistanceCount={},camera={}", satisfyDistanceCount, pullCameraSatisfyDistanceCount, next.toString());
            if (satisfyDistanceCount >= pullCameraSatisfyDistanceCount) {

                WebSocketMessage<Camera> message = new WebSocketMessage<>();
                message.setContent(next)
                        .setMsgType(VIDEO_PILOTING_CAMERA);
                WebSocketSender.pushMessageToAll(message);
                log.info("pullNextCamera websocket pull new camera ={}", next.toString());
                //查询一次告警
                alarmService.processAlarm(next.getDeviceSerial());
                searchAlarmTimestamp = System.currentTimeMillis();
                lineInstance.setNowCamera(next);
                lineInstance.setNextCamera(null);
            }
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
    private int getSatisfyDistanceCountWithReferencePoint(Point referencePoint, int calculatePositionCount,
                                                          int distanceThreshold) {
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

    private boolean directionJudgment() {
        if (lineInstance.getDirection() != null && lineInstance.getDirectionTimestamp() != null) {
            long interval = lineJudgmentConfig.getDirectionCalculateInterval();
            //方向还未失效
            if (System.currentTimeMillis() <= interval + lineInstance.getDirectionTimestamp()) {
                return true;
            }
        }
        boolean success = true;
        //根据相对起点的距离趋势判断行驶方向
        int directionJudgmentPositionCount = lineJudgmentConfig.getDirectionJudgmentPositionCount();

        if (deque.size() >= directionJudgmentPositionCount) {
            int directionJudgmentIntervalMeter = lineJudgmentConfig.getDirectionJudgmentIntervalMeter();
            int directionScoreThreshold = lineJudgmentConfig.getDirectionScoreThreshold();

            Point startPoint = new Point();
            startPoint.setLatitude(lineInstance.getStartStation().getLatitude());
            startPoint.setLongitude(lineInstance.getStartStation().getLongitude());

            Point endPoint = new Point();
            startPoint.setLatitude(lineInstance.getStartStation().getLatitude());
            startPoint.setLongitude(lineInstance.getStartStation().getLongitude());

            log.info("directionJudgment direction calculate");
            int startDirection = directionWithReferencePoint(startPoint, directionJudgmentPositionCount, directionJudgmentIntervalMeter, directionScoreThreshold);
            int endDirection = directionWithReferencePoint(endPoint, directionJudgmentPositionCount, directionJudgmentIntervalMeter, directionScoreThreshold);
            double startDistance = distanceWithReferencePoint(startPoint, 3);
            double endDistance = distanceWithReferencePoint(endPoint, 3);

            if (startDirection > 0 && endDirection > 0) {
                if (startDistance < endDistance) {
                    lineInstance.directionPositive();
                } else {
                    lineInstance.directionNegative();
                }
            } else if (startDirection < 0 && endDirection < 0) {
                if (startDistance > endDistance) {
                    lineInstance.directionPositive();
                } else {
                    lineInstance.directionNegative();
                }
            } else if (startDirection < 0 && endDirection > 0) {
                lineInstance.directionPositive();
            } else if (startDirection > 0 && endDirection < 0) {
                lineInstance.directionNegative();
            } else {
                //方向未计算出
                success = false;
            }
            log.info("directionJudgment direction={},startDirection={},endDirection={},startDistance={},endDistance={}", lineInstance.getDirection(), startDirection, endDirection, startDistance, endDistance);
        } else {
            //方向暂时无法计算
            success = false;
        }
        return success;
    }

    /**
     * 依据参照点判断方向
     *
     * @param calculatePositionCount 用于计算的点的数量
     * @param intervalMeter          判断移动的点的距离
     * @param scoreThreshold         得分有效大小
     * @return -1 反向，0 条件不足判断方向，1 正向
     */
    private int directionWithReferencePoint(Point rp, int calculatePositionCount, int intervalMeter,
                                            int scoreThreshold) {
        //方向算分
        int score = 0;
        //上一个位置距离
        double pre = 0;
        int i = 0;
        Iterator<Point> pointIterator = deque.iterator();
        while (pointIterator.hasNext()) {
            Point point = pointIterator.next();
            //遍历 需要跳过前面时间比较旧的
            if (i >= deque.size() - calculatePositionCount) {
                //计算点与线路起点的距离
                double distance = EarthMapUtil.distance(rp.getLatitude(), rp.getLongitude(), point.getLatitude(), point.getLongitude());
                //间距大于指定距离，判定为有效，相对指定点越来越近为正向
                log.info("directionWithReferencePoint pre={},distance={},score={}", pre, distance, score);
                if (pre == 0) {
                    pre = distance;
                    continue;
                }
                //是否距离之差大于满足的距离差
                if (Math.abs(distance - pre) < intervalMeter) {
                    log.info("directionWithReferencePoint interval unSatisfy ,actual={},intervalMeter={}", Math.abs(distance - pre), intervalMeter);
                }
                //比上一个距离小 正向
                if (distance <= pre) score++;
                //比上一个距离小 反向
                if (distance > pre) score--;
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
        log.info("directionWithReferencePoint score={}", score);
        if (Math.abs(score) >= scoreThreshold) {
            if (score > 0) {
                return 1;
            } else if (score < 0) {
                return -1;
            }
        }
        return 0;
    }

    /**
     * 计算指定w位置数量的点的平均距离
     *
     * @param rp
     * @param calculatePositionCount
     * @return
     */
    private double distanceWithReferencePoint(Point rp, int calculatePositionCount) {
        int i = 0;
        if (calculatePositionCount <= 0) {
            return 0.0;
        }
        double distanceSum = 0.0;
        Iterator<Point> pointIterator = deque.iterator();
        while (pointIterator.hasNext()) {
            Point p = pointIterator.next();
            //逆向遍历 需要跳过前面时间比较旧的
            if (i >= deque.size() - calculatePositionCount) {
                distanceSum += EarthMapUtil.distance(rp.getLatitude(), rp.getLongitude(), p.getLatitude(), p.getLongitude());
            }
            i++;
        }
        return distanceSum / calculatePositionCount;
    }

    private Camera getNextCamera() {
        //获取下一个摄像头
        List<Camera> cameraList = lineInstance.getCameraList();
        if (cameraList.isEmpty()) {
            log.info("pullNextCamera cameraList is empty");
        }
        int pullCameraJudgmentIntervalMeter = lineJudgmentConfig.getPullCameraJudgmentIntervalMeter();
        int pullCameraJudgmentPositionCount = lineJudgmentConfig.getPullCameraJudgmentPositionCount();
        int pullCameraDirectionScoreThreshold = lineJudgmentConfig.getPullCameraDirectionScoreThreshold();
        //计算每一个摄像头的相对方向和距离，选取方向一致，距离最近的摄像头作为下一个
        //根据方向获取第一个未拉取的摄像头
        if (lineInstance.directionIsPositive()) {
            for (int i = 0; i < cameraList.size(); i++) {
                Camera camera = cameraList.get(i);
                Point rp = new Point();
                rp.setLatitude(camera.getLatitude());
                rp.setLongitude(camera.getLongitude());
                int d = directionWithReferencePoint(rp, pullCameraJudgmentPositionCount, pullCameraJudgmentIntervalMeter, pullCameraDirectionScoreThreshold);
                if (d > 0) {
                    return camera;
                }
            }
        } else {
            for (int i = cameraList.size() - 1; i >= 0; i--) {
                Camera camera = cameraList.get(i);
                Point rp = new Point();
                rp.setLatitude(camera.getLatitude());
                rp.setLongitude(camera.getLongitude());
                int d = directionWithReferencePoint(rp, pullCameraJudgmentPositionCount, pullCameraJudgmentIntervalMeter, pullCameraDirectionScoreThreshold);
                if (d > 0) {
                    return camera;
                }
            }
        }
        return null;
    }
}