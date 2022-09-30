package com.liyu.piloting;

import com.liyu.piloting.config.LineConfig;
import com.liyu.piloting.config.LineJudgmentConfig;
import com.liyu.piloting.model.LineInstance;
import com.liyu.piloting.model.Point;
import com.liyu.piloting.service.PositionTest;
import com.liyu.piloting.util.EarthMapUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LineTest {

    @Autowired
    private LineConfig lineConfig;
    @Autowired
    private LineJudgmentConfig lineJudgmentConfig;
    @Autowired
    private PositionTest positionTest;


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
}
