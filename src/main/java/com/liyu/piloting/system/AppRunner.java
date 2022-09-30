package com.liyu.piloting.system;

import com.liyu.piloting.model.Point;
import com.liyu.piloting.rxtx.RxtxServer;
import com.liyu.piloting.rxtx.SerialPortParam;
import com.liyu.piloting.service.PilotingService;
import com.liyu.piloting.service.PositionService;
import com.liyu.piloting.service.PositionTest;
import com.liyu.piloting.util.SystemThreadPool;
import com.liyu.piloting.util.TestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class AppRunner implements ApplicationRunner {
//    @Autowired
//    private RxtxServer rxtxServer;

    @Autowired
    private PositionService positionService;
    @Autowired
    PilotingService pilotingService;
    @Autowired
    private SerialPortParam serialPortParam;
    @Autowired
    private PositionTest positionTest;

    @Override

    public void run(ApplicationArguments args) throws Exception {

//        new Thread(() -> {
//            log.info("RxtxServer start");
//            rxtxServer.start();
//            log.info("RxtxServer start end");
//        }).start();

        Thread processTty = new Thread(
                () -> {
                    File serport;
                    FileInputStream mSerR = null;
                    BufferedReader bufferedReader;
                    //获取连接
                    while (true) {
                        log.info("listen start {}", serialPortParam.getSerialPortName());
                        try {
                            serport = new File(serialPortParam.getSerialPortName());
                            mSerR = new FileInputStream(serport);
                            bufferedReader = new BufferedReader(new InputStreamReader(mSerR));
                            break;
                        } catch (FileNotFoundException e) {
                            log.error("listen error", e);
                            e.printStackTrace();
                            try {
                                Thread.sleep(1000 * 10);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }

                    log.info("listen success {}", serialPortParam.getSerialPortName());
                    log.info("process start");
                    //开始处理
                    while (true) {
                        String msg = null;
                        try {
                            msg = bufferedReader.readLine();
                            if (StringUtils.isNotBlank(msg) && msg.contains("$GPRM")) {
                                log.debug("listen data = {}", msg);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        positionService.processMsg(msg);
                    }

                }
        );
        processTty.setName("ttyProcess");
        SystemThreadPool.doExecute(processTty);
//        processTty.start();

//生成数据模拟
//        Thread test_process = new Thread(() -> {
//            log.info("test position");
//            //新秀
//            Point xinxiu = new Point();
//            xinxiu.setLongitude(114.149408);
//            xinxiu.setLatitude(22.547202);
//
//            //前海湾
//            Point qianhaiwan = new Point();
//            qianhaiwan.setLongitude(113.897924);
//            qianhaiwan.setLatitude(22.536818);
//
//            positionTest.sendPoint(xinxiu, qianhaiwan, 0.0001, 0.0001, 1000);
//        }
//        );
//        test_process.setName("test_process");
//        test_process.start();

//从日志中读取测试
//        Thread test_from_log = new Thread(() -> {
//            DateTimeFormatter HHmmssSSS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
//            //加载文件
//            String fileName = "C:\\Users\\liyu\\Desktop\\log-0930\\video_piloting_debug.log";
//            BufferedReader reader = null;
//            try {
//                reader = new BufferedReader(new InputStreamReader(Files.newInputStream(new File(fileName).toPath())));
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//
//            String startTime = "19:45:03.000";
//            LocalTime sTime = LocalTime.parse(startTime, HHmmssSSS);
//            String endTime = "20:39:33.000";
//            LocalTime eTime = LocalTime.parse(endTime, HHmmssSSS);
//            //日志上次打印时间
//            long preC = 0;
//            String filterS = "= $GPRMC";
//            Point pre = null;
//            while (true) {
//                String s = null;
//                try {
//                    s = reader.readLine();
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                if (StringUtils.isBlank(s) || !s.contains(filterS)) {
//                    continue;
//                }
//                //处理设定时间范围的
//                String curTime = s.substring(0, 12);
//                LocalTime cTime = LocalTime.parse(curTime, HHmmssSSS);
//                if (cTime.isBefore(sTime)) {
//                    continue;
//                }
//                if (cTime.isAfter(eTime)) {
//                    break;
//                }
//                int sIndex = s.indexOf(filterS) + 2;
//                String msg = s.substring(sIndex);
//                Point point = TestUtil.extrcPoint(msg);
//                //处理和接收到的误差时间
//                long curTs = LocalDateTime.of(LocalDate.now().plusDays(-1), cTime).toInstant(ZoneOffset.of("+8")).toEpochMilli();
//
//                if (preC == 0) {
//                    preC = curTs;
//                } else {
//                    long millis = curTs - preC;
//                    //跳过停的时间段
//                    if (cTime.isAfter(LocalTime.parse("19:47:10.000", HHmmssSSS)) && cTime.isBefore(LocalTime.parse("19:48:16.000", HHmmssSSS))) {
//
//                    } else if (cTime.isAfter(LocalTime.parse("19:50:36.000", HHmmssSSS)) && cTime.isBefore(LocalTime.parse("20:06:18.000", HHmmssSSS))) {
//
//                    } else if (cTime.isAfter(LocalTime.parse("20:09:51.000", HHmmssSSS)) && cTime.isBefore(LocalTime.parse("20:26:58.000", HHmmssSSS))) {
//
//                    } else if (cTime.isAfter(LocalTime.parse("20:29:47.000", HHmmssSSS)) && cTime.isBefore(LocalTime.parse("20:33:28.000", HHmmssSSS))) {
//
//                    } else if (cTime.isAfter(LocalTime.parse("20:35:47.000", HHmmssSSS)) && cTime.isBefore(LocalTime.parse("20:37:41.000", HHmmssSSS))) {
//
//                    } else {
//                        log.info("sleep time={}", millis);
//                        try {
//                            Thread.sleep(millis);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                }
//                log.info("data dataTime={},cTime={}, dif={}", point.getDataTime().toLocalTime(), cTime, curTs - point.getTimestamp());
//                log.info("data speed={}", 0.5144444 * point.getSpeed());
//
//                if (pre == null) {
//                    pre = point;
//                    continue;
//                }
//                log.info("data distance interval={}", TestUtil.getDistance(pre, point));
//                pre = point;
//                preC = curTs;
//                pilotingService.process(point);
//            }
//        });
//        SystemThreadPool.doExecute(test_from_log);

    }
}
