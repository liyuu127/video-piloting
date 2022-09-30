package com.liyu.piloting;

import com.liyu.piloting.config.LineConfig;
import com.liyu.piloting.config.LineJudgmentConfig;
import com.liyu.piloting.model.LineInstance;
import com.liyu.piloting.model.Point;
import com.liyu.piloting.service.PilotingService;
import com.liyu.piloting.service.PositionTest;
import com.liyu.piloting.util.EarthMapUtil;
import com.liyu.piloting.util.TestUtil;
import com.liyu.piloting.util.TimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class LineTest {

    @Autowired
    private LineConfig lineConfig;
    @Autowired
    private LineJudgmentConfig lineJudgmentConfig;
    @Autowired
    private PositionTest positionTest;

    @Autowired
    PilotingService pilotingService;

    @Test
    public void dequeue_test() {
        Deque<Integer> deque = new ArrayDeque<>(10);
        deque.addLast(1);
        deque.addLast(2);
        deque.addLast(3);
        deque.addLast(4);
        deque.addLast(5);
        deque.addLast(6);
        deque.addLast(7);
        deque.removeFirst();

        int calculatePositionCount = 3;
        int i = 0;
        Iterator<Integer> p = deque.iterator();
        while (p.hasNext()) {
            Integer next = p.next();
            //逆向遍历 需要跳过前面时间比较旧的
            if (i >= deque.size() - calculatePositionCount) {
                System.out.println("next = " + next);
            }
            i++;
        }

    }

    @Test
    public void lineConfig_init_test() {
//        System.out.println("lineConfig = " + lineConfig);
//        LineInstance lineInstance = lineConfig.lineInstance();
//        System.out.println("lineInstance = " + lineInstance);
//        lineInstance.getStartStation().setName("x");
//        lineInstance.getCameraList().get(0).setName("xx");
//        System.out.println("lineInstance = " + lineInstance);
//        System.out.println("lineConfig = " + lineConfig);
//        System.out.println("lineInstance.directionIsPositive() = " + lineInstance.directionIsPositive());
//        System.out.println("lineInstance.lineStatusIsInit() = " + lineInstance.lineStatusIsInit());
        System.out.println("lineJudgmentConfig.toString() = " + lineJudgmentConfig.toString());
    }

    @Test
    public void position_creation_test() {
        //新秀
        Point xinxiu = new Point();
        xinxiu.setLongitude(114.149408);
        xinxiu.setLatitude(22.547202);

        //前海湾
        Point qianhaiwan = new Point();
        qianhaiwan.setLongitude(113.897924);
        qianhaiwan.setLatitude(22.536818);

        positionTest.sendPoint(xinxiu, qianhaiwan, 0.0001, 0.0001, 1000);
    }

    @Test
    public void test_queue() {
//        PriorityQueue<Point> queue = new PriorityQueue<>(20, (a, b) -> (int) (a.getTimestamp() - b.getTimestamp()));
//
//
//
//        for (int j = 0; j < 10; j++) {
//            Point point = new Point();
//            point.setLongitude(114.149408+j);
//            point.setLatitude(22.547202);
//            point.setTimestamp(j);
//            queue.add(point);
//        }
//        Iterator<Point> iterator = queue.iterator();
//        int i = 0;
//        int size = 5;
//        while (iterator.hasNext() && i < size) {
//            Point next = iterator.next();
//            i++;
//            System.out.println("next = " + next);
//        }


        ArrayDeque<Point> pointArrayDeque = new ArrayDeque<>(5);
        for (int j = 0; j < 10; j++) {
            Point point = new Point();
            point.setLongitude(114.149408 + j);
            point.setLatitude(22.547202);
            point.setTimestamp(j);
            pointArrayDeque.addLast(point);
        }
//        pointArrayDeque.removeFirst();
        pointArrayDeque.removeLast();
        Iterator<Point> iterator = pointArrayDeque.descendingIterator();
        int i = 0;
        int size = 5;
        while (iterator.hasNext() && i < size) {
            Point next = iterator.next();
            i++;
            System.out.println("next = " + next);
        }
    }

    @Test
    public void test_position_from_log() throws IOException, InterruptedException {
        DateTimeFormatter HHmmssSSS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        //加载文件
        String fileName = "C:\\Users\\liyu\\Desktop\\log-0930\\video_piloting_debug.log";
        BufferedReader reader = null;
        reader = new BufferedReader(new InputStreamReader(Files.newInputStream(new File(fileName).toPath())));

        String startTime = "19:45:03.000";
        LocalTime sTime = LocalTime.parse(startTime, HHmmssSSS);
        String endTime = "20:39:33.000";
        LocalTime eTime = LocalTime.parse(endTime, HHmmssSSS);
        //日志上次打印时间
        long preC = 0;
        String filterS = "= $GPRMC";
        Point pre = null;
        while (true) {
            String s = reader.readLine();
            if (StringUtils.isBlank(s) || !s.contains(filterS)) {
                continue;
            }
            //处理设定时间范围的
            String curTime = s.substring(0, 12);
            LocalTime cTime = LocalTime.parse(curTime, HHmmssSSS);
            if (cTime.isBefore(sTime)) {
                continue;
            }
            if (cTime.isAfter(eTime)) {
                break;
            }
            int sIndex = s.indexOf(filterS) + 2;
            String msg = s.substring(sIndex);
            Point point = TestUtil.extrcPoint(msg);
            //处理和接收到的误差时间
            long curTs = LocalDateTime.of(LocalDate.now().plusDays(-1), cTime).toInstant(ZoneOffset.of("+8")).toEpochMilli();

            if (preC == 0) {
                preC = curTs;
            } else {
                long millis = curTs - preC;
                //跳过停的时间段
                if (cTime.isAfter(LocalTime.parse("19:47:10.000", HHmmssSSS)) && cTime.isBefore(LocalTime.parse("19:48:16.000", HHmmssSSS))) {

                } else if (cTime.isAfter(LocalTime.parse("19:50:36.000", HHmmssSSS)) && cTime.isBefore(LocalTime.parse("20:06:18.000", HHmmssSSS))) {

                } else if (cTime.isAfter(LocalTime.parse("20:09:51.000", HHmmssSSS)) && cTime.isBefore(LocalTime.parse("20:26:58.000", HHmmssSSS))) {

                } else if (cTime.isAfter(LocalTime.parse("20:29:47.000", HHmmssSSS)) && cTime.isBefore(LocalTime.parse("20:33:28.000", HHmmssSSS))) {

                } else if (cTime.isAfter(LocalTime.parse("20:35:47.000", HHmmssSSS)) && cTime.isBefore(LocalTime.parse("20:37:41.000", HHmmssSSS))) {

                } else {
                    log.info("sleep time={}", millis);
                    Thread.sleep(millis);
                }
            }
            log.info("data dataTime={},cTime={}, dif={}", point.getDataTime().toLocalTime(), cTime, curTs - point.getTimestamp());
            log.info("data speed={}", 0.5144444 * point.getSpeed());

            if (pre == null) {
                pre = point;
                continue;
            }
            log.info("data distance interval={}", TestUtil.getDistance(pre, point));
            pre = point;
            preC = curTs;
            pilotingService.process(point);
        }

    }



    @Test
    public void test_distance() {

        Point p1 = new Point();
        p1.setLatitude(21.5252517);
        p1.setLongitude(111.5227741);

        Point p2 = new Point();
        p2.setLatitude(21.5214423);
        p2.setLongitude(111.5258672);

        Point p3 = new Point();
        p3.setLatitude(21.5150798);
        p3.setLongitude(111.5265642);

        Double d1 = TestUtil.getDistance(p1, p2);
        Double d2 = TestUtil.getDistance(p2, p3);
        Double d3 = TestUtil.getDistance(p1, p3);
        log.info("d1={},d2={},d3={}", d1, d2, d3);
    }

}

