package com.liyu.piloting.service;

import com.liyu.piloting.HKAlarm.alarm.AlarmListen;
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
    @Autowired
    LineService lineService;
    @Autowired
    AlarmListen alarmListen;

    private Deque<Point> deque;
    private long updateQueueTimestamp = System.currentTimeMillis();
    private long endJudgmentTimestamp = System.currentTimeMillis();
    private long lastRefreshPushInterval = System.currentTimeMillis();
    private long lastRefreshOverInterval = System.currentTimeMillis();
    /**
     * 查询告警间隔
     */
    private long searchAlarmTimestamp = System.currentTimeMillis();
    private long alarmFirstDelayTimestamp = System.currentTimeMillis();


    @PostConstruct
    public void init() {
        this.deque = new ArrayDeque<>(lineJudgmentConfig.getPositionQueueCapacity());
    }


    public void process(Point point) {

        if (!validPoint(point)) {
            return;
        }
        //数据入队
        pointInQueue(point);

//        log.info("deque.size()={},lineInstance={}", deque.size(), lineInstance.toString());
        //判断车辆是否启动及启动后方向
        //没计算出方向返回false
        direction();

        pullCamera();


    }

    private void direction() {
        if (lineJudgmentConfig.getDirectionEnable()) {
            directionJudgment();
        }
    }

    private void pullCamera() {
        if (lineJudgmentConfig.getModel() == JudgmentModelEnum.DIRECTION.getModel()) {
            //确定方向后计算拉流摄像头
            if (validDirection()) {
                pullCameraAndAlarm();
            }
        } else if (lineJudgmentConfig.getModel() == JudgmentModelEnum.DISTANCE.getModel()) {
            pullCameraAndAlarm();
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
            if (getLineInstance().directionIsPositive()) {
                stationPosition = getLineInstance().getEndStation();
            } else {
                stationPosition = getLineInstance().getStartStation();
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
                lineService.setLineInstance(null);
                deque = null;
            }
        }


    }

    /**
     * 数据入队
     *
     * @param point 采集点
     * @return false 数据有效 true 数据过滤
     */
    private void pointInQueue(Point point) {

        int positionQueueCapacity = lineJudgmentConfig.getPositionQueueCapacity();
        if (deque.size() >= positionQueueCapacity) {
            deque.removeFirst();
        }
        deque.addLast(point);
        updateQueueTimestamp = System.currentTimeMillis();
    }

    /**
     * 判断当前位置是否有效
     *
     * @param point
     * @return
     */
    private boolean validPoint(Point point) {
        long now = System.currentTimeMillis();
        long positionExpireTime = lineJudgmentConfig.getPositionExpireTime();
        long positionStoreInterval = lineJudgmentConfig.getPositionStoreInterval();

//        if (now - point.getTimestamp() > positionExpireTime) {
//            //当前数据过期
//            log.debug("validPoint expire pre={},positionExpireTime={}", point.getTimestamp(), positionExpireTime);
//            return false;
//        }

        if (now - this.updateQueueTimestamp < positionStoreInterval) {
            //距离上次数据更新还未超过1s
            log.debug("validPoint interval pre={},positionStoreInterval={}", updateQueueTimestamp, positionStoreInterval);
            return false;
        }
        return true;
    }

    private void lineEndJudgment() {
        //摄像头是否拉取结束
        long unPulCount = getLineInstance().getCameraList().stream().filter(Camera::statusIsUnPull).count();
        long pulledCount = getLineInstance().getCameraList().stream().filter(Camera::statusIsPull).count();
        if (unPulCount == 0 && pulledCount == 0) {
            int lineEndJudgmentPositionCount = lineJudgmentConfig.getLineEndJudgmentPositionCount();
            if (deque.size() >= lineEndJudgmentPositionCount) {
                int lineEndSatisfyDistanceMeter = lineJudgmentConfig.getLineEndSatisfyDistanceMeter();
                int lineEndSatisfyDistanceCount = lineJudgmentConfig.LineEndSatisfyDistanceCount();

                Point referencePoint = new Point();
                if (getLineInstance().directionIsPositive()) {
                    referencePoint.setLatitude(getLineInstance().getEndStation().getLatitude());
                    referencePoint.setLongitude(getLineInstance().getEndStation().getLongitude());
                } else {
                    referencePoint.setLatitude(getLineInstance().getStartStation().getLatitude());
                    referencePoint.setLongitude(getLineInstance().getStartStation().getLongitude());
                }
                //计算距离是否满足条件拉取的距离条件，连续指定数量的位置在200m内
                log.info("lineEndJudgment endStation?");
                int satisfyDistanceCount = getSatisfyDistanceCountWithReferencePoint(referencePoint, lineEndJudgmentPositionCount, lineEndSatisfyDistanceMeter, false);

                log.info("lineEndJudgment satisfyDistanceCount ={}", satisfyDistanceCount);
                if (satisfyDistanceCount > lineEndSatisfyDistanceCount) {
                    log.info("lineEndJudgment end line ");
                }
            }
        }

    }


    /**
     * 拉取取摄像头
     */
    private void pullCameraAndAlarm() {
        //根据最近几个位置和方向判断最近的摄像头拉取
        int pullCameraJudgmentPositionCount = lineJudgmentConfig.getPullCameraJudgmentPositionCount();
        if (deque.size() >= pullCameraJudgmentPositionCount) {
            //当前是否有摄像头正在拉取，尝试判断是否驶离距离范围
            nowCameraOverJudgment();
            //判断是否拉取下一个摄像头
            pullNextCamera();
        } else {
            log.info("pullCameraAndAlarm count low deque.size()={},pullCameraJudgmentPositionCount={}", deque.size(), pullCameraJudgmentPositionCount);
        }
    }

    /**
     * 判断当前摄像头是否结束
     */
    private void nowCameraOverJudgment() {
        Camera nowCamera = getLineInstance().getNowCamera();
        if (nowCamera != null) {

            //首次告警
            //小于第一次延迟
            long now = System.currentTimeMillis();
            if (this.alarmFirstDelayTimestamp < now) {
                if (!alarmListen.queryDeviceOnLine(nowCamera.getId())) {
                    log.info("nowCameraOverJudgment  nowCamera not online ={}", nowCamera);
                } else {
                    searchAlarm(nowCamera);
                }
            } else {
                log.info("nowCameraOverJudgment  alarmFirstDelayTimestamp={} ,now={}", this.alarmFirstDelayTimestamp, now);
            }

            boolean over = judgmentCameraOver(nowCamera);
            if (over) {
                log.info("nowCameraOverJudgment  camera ,camera={}", nowCamera);
                WebSocketMessage<Camera> message = new WebSocketMessage<>();
                message.setContent(nowCamera)
                        .setMsgType(VIDEO_PILOTING_CAMERA_OVER);
                WebSocketSender.pushMessageToAll(message);
                log.info("nowCameraOverJudgment  websocket camera over ={}", nowCamera);

                getLineInstance().setLastCamera(nowCamera);
                getLineInstance().setNowCamera(null);
            } else {
                refreshPushCamera(nowCamera);
            }

        }

    }

    private void searchAlarm(Camera nowCamera) {
        //拉取告警
        if (alarmConf.getModel() == AlarmModelEnum.HK.getModel()) {
            //直接发一个无告警报文
            alarmService.sendNoAlarm(true);
        } else if (alarmConf.getModel() == AlarmModelEnum.YS7.getModel()) {
            Long alarmInterval = alarmConf.getSearchTimeMillisInterval();
            long now = System.currentTimeMillis();

            //超过告警时间
            if (now > alarmInterval + searchAlarmTimestamp) {
                alarmService.processAlarm(nowCamera.getDeviceSerial());
                searchAlarmTimestamp = now;
            } else {
                log.debug("search alarm pre={},alarmInterval={}", searchAlarmTimestamp, alarmInterval);
            }
        }
    }

    private void refreshPushCamera(Camera nowCamera) {
        long refreshPushInterval = lineJudgmentConfig.getNowCameraRefreshPushInterval();
        //上次刷新时间+刷新间隔小于当前时间，再次刷新
        if (lastRefreshPushInterval + refreshPushInterval < System.currentTimeMillis()) {
            log.info("refreshPushCamera camera={}", nowCamera);
            WebSocketMessage<Camera> message = new WebSocketMessage<>();
            message.setContent(nowCamera)
                    .setMsgType(VIDEO_PILOTING_CAMERA);
            WebSocketSender.pushMessageToAll(message);
            lastRefreshPushInterval = System.currentTimeMillis();
        }
    }

    private void refreshOverCamera(Camera camera) {
        long refreshOverInterval = lineJudgmentConfig.getNowCameraRefreshOverInterval();
        //上次刷新时间+刷新间隔小于当前时间，再次刷新
        if (lastRefreshOverInterval + refreshOverInterval < System.currentTimeMillis()) {
            log.info("refreshOverCamera camera={}", camera);
            WebSocketMessage<Camera> message = new WebSocketMessage<>();
            message.setContent(camera)
                    .setMsgType(VIDEO_PILOTING_CAMERA_OVER);
            WebSocketSender.pushMessageToAll(message);
            lastRefreshOverInterval = System.currentTimeMillis();
        }
    }

    /**
     * 判断当前摄像头是否驶离
     *
     * @param nowCamera
     * @return
     */
    private boolean judgmentCameraOver(Camera nowCamera) {
        boolean over = false;
        Point referencePoint = new Point();
        referencePoint.setLatitude(nowCamera.getLatitude());
        referencePoint.setLongitude(nowCamera.getLongitude());

        int pullCameraOverPositionCount = lineJudgmentConfig.getPullCameraOverPositionCount();
        int cameraOverSatisfyDistanceMeter = lineJudgmentConfig.getCameraOverSatisfyDistanceMeter();
        int cameraOverSatisfyDistanceCount = lineJudgmentConfig.getCameraOverSatisfyDistanceCount();
        if (lineJudgmentConfig.getModel() == JudgmentModelEnum.DIRECTION.getModel()) {
            int pullCameraJudgmentPositionCount = lineJudgmentConfig.getPullCameraJudgmentPositionCount();
            double pullCameraJudgmentIntervalMeter = lineJudgmentConfig.getPullCameraJudgmentIntervalMeter();
            int pullCameraDirectionScoreThreshold = lineJudgmentConfig.getPullCameraDirectionScoreThreshold();

            log.info("nowCameraOverJudgment nowCameraDirection?");
            int cameraDirection = directionWithReferencePoint(referencePoint, pullCameraJudgmentPositionCount, pullCameraJudgmentIntervalMeter, pullCameraDirectionScoreThreshold);
            over = cameraOverJudgmentWithDirection(referencePoint, pullCameraOverPositionCount, cameraOverSatisfyDistanceMeter, cameraOverSatisfyDistanceCount, cameraDirection);
        } else if (lineJudgmentConfig.getModel() == JudgmentModelEnum.DISTANCE.getModel()) {
//            over = cameraOverJudgmentWithDistance(referencePoint, pullCameraOverPositionCount, cameraOverSatisfyDistanceMeter, cameraOverSatisfyDistanceCount);
            over = cameraOverJudgmentWithDistanceAndDirection(referencePoint);

        }
        return over;
    }

    /**
     * 根据道口位置和方向判断是否应该停止拉流
     *
     * @param referencePoint 参考点 即道口位置
     * @return 拉流时否结束
     */
    private boolean cameraOverJudgmentWithDistanceAndDirection(Point referencePoint) {
        //先判断相对道口方向,如果相对方向为负说明正在驶离道口
        int pullCameraJudgmentPositionCount = lineJudgmentConfig.getPullCameraJudgmentPositionCount();
        double pullCameraJudgmentIntervalMeter = lineJudgmentConfig.getPullCameraJudgmentIntervalMeter();
        int pullCameraDirectionScoreThreshold = lineJudgmentConfig.getPullCameraDirectionScoreThreshold();
        int cameraDirection = directionWithReferencePoint(referencePoint, pullCameraJudgmentPositionCount, pullCameraJudgmentIntervalMeter, pullCameraDirectionScoreThreshold);
        log.info("判断相对道口方向 cameraDirection={}", cameraDirection);
        if (cameraDirection >= 0) {
            return false;
        }
        //方向满足后判定驶离举例是否满足
        //摄像头离开位置的数量
        int pullCameraOverPositionCount = lineJudgmentConfig.getPullCameraOverPositionCount();
        //驶离多远算摄像头离站
        int cameraOverSatisfyDistanceMeter = lineJudgmentConfig.getCameraOverSatisfyDistanceMeter();
        //摄像头离站效位置数量
        int cameraOverSatisfyDistanceCount = lineJudgmentConfig.getCameraOverSatisfyDistanceCount();
        int satisfyDistanceCountWithReferencePoint = getSatisfyDistanceCountWithReferencePoint(referencePoint, pullCameraOverPositionCount, cameraOverSatisfyDistanceMeter, false);
        boolean cameraOver = satisfyDistanceCountWithReferencePoint >= cameraOverSatisfyDistanceCount;
        log.info("判断驶离道口距离是否满足 cameraOver={},satisfyDistanceCountWithReferencePoint={},cameraOverSatisfyDistanceCount={}", cameraOver, satisfyDistanceCountWithReferencePoint, cameraOverSatisfyDistanceCount);

        return cameraOver;
    }


    /**
     * 根据距离判断是否超出拉流距离
     * 超出返回true
     * 未超出返回false
     *
     * @param referencePoint                 计算点 相机位置
     * @param calculatePositionCount         计算采点的数量
     * @param distanceThreshold              超出阈值
     * @param cameraOverSatisfyDistanceCount 满足条件的最低得分
     * @return
     */
    private boolean cameraOverJudgmentWithDistance(Point referencePoint, int calculatePositionCount, int distanceThreshold, int cameraOverSatisfyDistanceCount) {
        int satisfyDistanceCountWithReferencePoint = getSatisfyDistanceCountWithReferencePoint(referencePoint, calculatePositionCount, distanceThreshold, false);
        boolean cameraOver = satisfyDistanceCountWithReferencePoint >= cameraOverSatisfyDistanceCount;
        log.info("cameraOverJudgmentWithDistance cameraOver={},want={},actual={}", cameraOver, cameraOverSatisfyDistanceCount, satisfyDistanceCountWithReferencePoint);
        return cameraOver;
    }

    /**
     * 在距离判断的基础上对方向进行筛选
     * 根据方向和距离判断是否超出拉流距离
     * 拉流结束返回true
     * 拉流继续返回false
     *
     * @param referencePoint                 计算点 相机位置
     * @param calculatePositionCount         计算采点的数量
     * @param distanceThreshold              超出阈值
     * @param cameraOverSatisfyDistanceCount 满足条件的最低得分
     * @param relativeDirection              相对计算点的方向
     * @return
     */
    private boolean cameraOverJudgmentWithDirection(Point referencePoint, int calculatePositionCount, int distanceThreshold, int cameraOverSatisfyDistanceCount, int relativeDirection) {

        //相对方向未计算出或者正向,不考虑拉流结束
        if (relativeDirection >= 0) {
            log.info("cameraOverJudgmentWithDirection relativeDirection={},not over", relativeDirection);
            return false;
        }
        int satisfyDistanceCountWithReferencePoint = getSatisfyDistanceCountWithReferencePoint(referencePoint, calculatePositionCount, distanceThreshold, false);
        boolean cameraOver = satisfyDistanceCountWithReferencePoint >= cameraOverSatisfyDistanceCount;
        log.info("cameraOverJudgmentWithDistance cameraOver={},want={},actual={}", cameraOver, cameraOverSatisfyDistanceCount, satisfyDistanceCountWithReferencePoint);
        return cameraOver;
    }

    private void pullNextCamera() {
        if (getLineInstance().getNowCamera() != null) {
            //当前还有摄像头 暂时不拉取新的
            return;
        }
        Camera next = null;
        if (lineJudgmentConfig.getModel() == JudgmentModelEnum.DIRECTION.getModel()) {
            next = pullNextCameraWithDirection();

        } else if (lineJudgmentConfig.getModel() == JudgmentModelEnum.DISTANCE.getModel()) {
            next = pullNextCameraWithDistance();
        }

        if (next == null) {
            log.info("pullNextCamera next is null");
            //重复播放停止拉流
            Camera lastCamera = getLineInstance().getLastCamera();
            if (lastCamera != null) {
                refreshOverCamera(lastCamera);
            }
            return;
        }

        //拉取到新的摄像头
        WebSocketMessage<Camera> message = new WebSocketMessage<>();
        message.setContent(next)
                .setMsgType(VIDEO_PILOTING_CAMERA);
        WebSocketSender.pushMessageToAll(message);
        log.info("pullNextCamera websocket pull new camera ={}", next);

        long now = System.currentTimeMillis();
        //第一次延迟告警
        this.alarmFirstDelayTimestamp = alarmConf.getFirstDelayTime() + now;

//        if (alarmConf.getModel() == AlarmModelEnum.HK.getModel()) {
//            //直接发一个无告警报文
//            alarmService.sendNoAlarm(true);
//        } else if (alarmConf.getModel() == AlarmModelEnum.YS7.getModel()) {
//            //查询一次告警
//            alarmService.processAlarm(next.getDeviceSerial());
//            searchAlarmTimestamp = now;
//        }
        this.lastRefreshPushInterval = now;
        getLineInstance().setNowCamera(next);
        getLineInstance().setNextCamera(null);

    }

    /**
     * 根据方向和距离拉取下个摄像头
     *
     * @return null 不符合拉取条件，camera 拉取成功
     */
    public Camera pullNextCameraWithDirection() {
        Camera next = null;
        //上次是否计算过 上次计算过就不要计算
        if (getLineInstance().getNextCamera() != null) {
            next = getLineInstance().getNextCamera();
        }
        //计算下一个摄像头
        if (next == null) {
            next = getNextCameraWithDirection();
        }
        if (next == null) {
            log.info("pullNextCameraWithDirection next is null");
            return null;
        }

        //判断是否符合拉去距离
        if (cameraPositionInDistance(next)) {
            return next;
        }
        log.info("pullNextCameraWithDirection next is null");
        return null;
    }


    /**
     * 遍历所有摄像头找出满足的距离最小的拉取
     *
     * @return
     */
    public Camera pullNextCameraWithDistance() {

        List<Camera> cameraList = getLineInstance().getCameraList();
        double min = Double.MAX_VALUE;
        Camera next = null;
        for (Camera camera : cameraList) {
            //判断相对方向,驶离方向不拉流
            Point referencePoint = new Point();
            referencePoint.setLatitude(camera.getLatitude());
            referencePoint.setLongitude(camera.getLongitude());
            int cameraDirection = directionWithReferencePoint(referencePoint, lineJudgmentConfig.getPullCameraJudgmentPositionCount(),
                    lineJudgmentConfig.getPullCameraJudgmentIntervalMeter(), lineJudgmentConfig.getPullCameraDirectionScoreThreshold());
            log.info("判断相对摄像头方向 cameraDirection={}", cameraDirection);
            //驶离的时候判断不了方向是可以的
            if (cameraDirection < 0) {
                continue;
            }
            //计算是否满足距离
            if (!cameraPositionInDistance(camera)) {
                log.info("pullNextCameraWithDistance unsatisfied camera={}", camera);
                continue;
            }

            //计算平均距离
            Point rp = new Point();
            rp.setLatitude(camera.getLatitude());
            rp.setLongitude(camera.getLongitude());
            double distance = distanceWithReferencePoint(rp, lineJudgmentConfig.getPullCameraSatisfyDistanceCount());
            log.info("pullNextCameraWithDistance camera distance={}, camera={}", distance, camera);
            if (distance <= min) {
                min = distance;
                next = camera;
            }
        }
        return next;
    }

    /**
     * 摄像机位置是否在拉取范围内
     * 根据方向判断左右距离
     *
     * @param next 摄像机
     * @return true 在拉取位置内 false 不在
     */
    private boolean cameraPositionInDistance(Camera next) {
        //获取方向
        int pullCameraSatisfyDistanceMeter;
        //正向行驶
        if (getLineInstance().getDirection() == null || getLineInstance().getDirection() == 0) {
            log.info("拉取摄像头 无法判断行驶方向 direction={}", getLineInstance().getDirection());
            Point startPoint = new Point();
            startPoint.setLatitude(getLineInstance().getStartStation().getLatitude());
            startPoint.setLongitude(getLineInstance().getStartStation().getLongitude());
            Point endPoint = new Point();
            endPoint.setLatitude(getLineInstance().getEndStation().getLatitude());
            endPoint.setLongitude(getLineInstance().getEndStation().getLongitude());
            double startDistance = distanceWithReferencePoint(startPoint, 3);
            double endDistance = distanceWithReferencePoint(endPoint, 3);
            if (startDistance < endDistance) {
                log.info("拉取摄像头 使用startDistance方向距离");
                pullCameraSatisfyDistanceMeter = lineJudgmentConfig.getPullCameraSatisfyDistancePositiveMeter();
            }else {
                log.info("拉取摄像头 endDistance");
                pullCameraSatisfyDistanceMeter = lineJudgmentConfig.getPullCameraSatisfyDistanceNegativeMeter();
            }
        } else if (getLineInstance().getDirection() > 0) {
            log.info("拉取摄像头 正向行驶方向 direction={}", getLineInstance().getDirection());
            pullCameraSatisfyDistanceMeter = lineJudgmentConfig.getPullCameraSatisfyDistancePositiveMeter();
        } else {
            log.info("拉取摄像头 反向行驶方向 direction={}", getLineInstance().getDirection());
            pullCameraSatisfyDistanceMeter = lineJudgmentConfig.getPullCameraSatisfyDistanceNegativeMeter();
        }

        int pullCameraJudgmentPositionCount = lineJudgmentConfig.getPullCameraJudgmentPositionCount();
        int pullCameraSatisfyDistanceCount = lineJudgmentConfig.getPullCameraSatisfyDistanceCount();

        Point rp = new Point();
        rp.setLatitude(next.getLatitude());
        rp.setLongitude(next.getLongitude());
        log.info("cameraPositionInDistance camera distance?");
        int satisfyDistanceCount = getSatisfyDistanceCountWithReferencePoint(rp, pullCameraJudgmentPositionCount, pullCameraSatisfyDistanceMeter, true);

        //满足具体要求时拉取摄像头
        boolean inDistance = satisfyDistanceCount >= pullCameraSatisfyDistanceCount;
        log.info("cameraPositionInDistance inDistance={},want={},actual={},camera={}", inDistance, pullCameraSatisfyDistanceCount, satisfyDistanceCount, next.toString());
        return inDistance;
    }


    /**
     * 满足指定距离的点数量
     *
     * @param referencePoint         参考点
     * @param calculatePositionCount 计算点数量
     * @param distanceThreshold      满足距离
     * @param in                     在distanceThreshold中还是外，如离开应选择外
     * @return
     */
    private int getSatisfyDistanceCountWithReferencePoint(Point referencePoint, int calculatePositionCount,
                                                          int distanceThreshold, boolean in) {
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
                if (in) {
                    if (distance <= distanceThreshold) {
                        satisfyDistanceCount++;
                    }
                } else {
                    if (distance >= distanceThreshold) {
                        satisfyDistanceCount++;
                    }
                }

            }
            i++;
        }
        return satisfyDistanceCount;
    }

    /**
     * 计算车辆方向
     *
     * @return
     */
    private boolean directionJudgment() {
        //方向有效不进行计算
        if (validDirection()) return true;
        boolean success = true;
        //根据相对起点的距离趋势判断行驶方向
        int directionJudgmentPositionCount = lineJudgmentConfig.getDirectionJudgmentPositionCount();

        if (deque.size() >= directionJudgmentPositionCount) {
            double directionJudgmentIntervalMeter = lineJudgmentConfig.getDirectionJudgmentIntervalMeter();
            int directionScoreThreshold = lineJudgmentConfig.getDirectionScoreThreshold();

            Point startPoint = new Point();
            startPoint.setLatitude(getLineInstance().getStartStation().getLatitude());
            startPoint.setLongitude(getLineInstance().getStartStation().getLongitude());

            Point endPoint = new Point();
            endPoint.setLatitude(getLineInstance().getEndStation().getLatitude());
            endPoint.setLongitude(getLineInstance().getEndStation().getLongitude());

            log.info("directionJudgment direction calculate");
            int startDirection = directionWithReferencePoint(startPoint, directionJudgmentPositionCount, directionJudgmentIntervalMeter, directionScoreThreshold);
            int endDirection = directionWithReferencePoint(endPoint, directionJudgmentPositionCount, directionJudgmentIntervalMeter, directionScoreThreshold);
            double startDistance = distanceWithReferencePoint(startPoint, 3);
            double endDistance = distanceWithReferencePoint(endPoint, 3);

            if (startDirection > 0 && endDirection > 0) {
                if (startDistance < endDistance) {
                    getLineInstance().directionPositive();
                } else {
                    getLineInstance().directionNegative();
                }
            } else if (startDirection < 0 && endDirection < 0) {
                if (startDistance > endDistance) {
                    getLineInstance().directionPositive();
                } else {
                    getLineInstance().directionNegative();
                }
            } else if (startDirection < 0 && endDirection > 0) {
                getLineInstance().directionPositive();
            } else if (startDirection > 0 && endDirection < 0) {
                getLineInstance().directionNegative();
            } else {
                //方向未计算出
                log.info("directionJudgment direction cal fail,try use pre direction direction={}", getLineInstance().getDirection());

                success = false;
            }
            log.info("directionJudgment direction={},startDirection={},endDirection={},startDistance={},endDistance={}", getLineInstance().getDirection(), startDirection, endDirection, startDistance, endDistance);
        } else {
            //方向暂时无法计算
            log.info("directionJudgment direction cal queue min,try use pre direction direction={}", getLineInstance().getDirection());
            success = false;
        }
        return success;
    }

    /**
     * 判断当前方向是否有效 方向值存在且未过期
     *
     * @return boolean true 有效的，false 无效的
     */
    private boolean validDirection() {
        if (getLineInstance().getDirection() != null && getLineInstance().getDirectionTimestamp() != null) {
            long interval = lineJudgmentConfig.getDirectionCalculateInterval();
            //方向还未失效
            if (System.currentTimeMillis() <= interval + getLineInstance().getDirectionTimestamp()) {
                log.debug("directionJudgment not expire pre={},interval={}", getLineInstance().getDirectionTimestamp(), interval);
                return true;
            }
        }
        return false;
    }

    /**
     * 依据参照点判断方向
     *
     * @param calculatePositionCount 用于计算的点的数量
     * @param intervalMeter          判断移动的点的距离
     * @param scoreThreshold         得分有效大小
     * @return -1 反向，0 条件不足判断方向，1 正向
     */
    private int directionWithReferencePoint(Point rp, int calculatePositionCount, double intervalMeter,
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
                log.info("directionWithReferencePoint pre={},distance={},d={},score={}", pre, distance, distance - pre, score);
                if (pre == 0) {
                    pre = distance;
                    continue;
                }
                //是否距离之差大于满足的距离差
                if (Math.abs(distance - pre) < intervalMeter) {
                    log.info("directionWithReferencePoint interval unSatisfy ,actual={},intervalMeter={}", Math.abs(distance - pre), intervalMeter);
                    pre = distance;
                    continue;
                }
                //比上一个距离小 正向
                if (distance <= pre) score++;
                //比上一个距离小 反向
                if (distance > pre) score--;

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
        log.info("directionWithReferencePoint score={},scoreThreshold={}", score, scoreThreshold);
        if (Math.abs(score) >= scoreThreshold) {
            if (score > 0) {
                return 1;
            } else if (score < 0) {
                return -1;
            }
        } else {
            log.info("directionWithReferencePoint fail scoreThreshold");
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

    /**
     * 通过行驶方向计算下个应该拉取的摄像头
     *
     * @return
     */
    private Camera getNextCameraWithDirection() {
        //获取下一个摄像头
        List<Camera> cameraList = getLineInstance().getCameraList();
        if (cameraList.isEmpty()) {
            log.info("getNextCameraWithDirection cameraList is empty");
        }
        double pullCameraJudgmentIntervalMeter = lineJudgmentConfig.getPullCameraJudgmentIntervalMeter();
        int pullCameraJudgmentPositionCount = lineJudgmentConfig.getPullCameraJudgmentPositionCount();
        int pullCameraDirectionScoreThreshold = lineJudgmentConfig.getPullCameraDirectionScoreThreshold();
        //计算每一个摄像头的相对方向和距离，选取方向一致，距离最近的摄像头作为下一个
        //根据方向获取第一个未拉取的摄像头
        if (getLineInstance().directionIsPositive()) {
            for (int i = 0; i < cameraList.size(); i++) {
                Camera camera = cameraList.get(i);
                Point rp = new Point();
                rp.setLatitude(camera.getLatitude());
                rp.setLongitude(camera.getLongitude());
                log.info("getNextCameraWithDirection direction ?");
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
                log.info("getNextCameraWithDirection direction ?");
                int d = directionWithReferencePoint(rp, pullCameraJudgmentPositionCount, pullCameraJudgmentIntervalMeter, pullCameraDirectionScoreThreshold);
                if (d > 0) {
                    return camera;
                }
            }
        }
        return null;
    }

    private LineInstance getLineInstance() {
        return lineService.getLineInstance();
    }
}