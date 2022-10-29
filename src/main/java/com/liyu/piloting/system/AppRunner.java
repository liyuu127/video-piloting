package com.liyu.piloting.system;

import com.liyu.piloting.HKAlarm.NetSDKDemo.FMSGCallBack_V31;
import com.liyu.piloting.HKAlarm.alarm.AlarmListen;
import com.liyu.piloting.config.AlarmConf;
import com.liyu.piloting.config.LineConfig;
import com.liyu.piloting.model.AlarmModelEnum;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class AppRunner implements ApplicationRunner {
    @Autowired
    private RxtxServer rxtxServer;

    @Autowired
    private PositionService positionService;
    @Autowired
    PilotingService pilotingService;
    @Autowired
    private SerialPortParam serialPortParam;
    @Autowired
    private PositionTest positionTest;
    @Autowired
    LineConfig lineConfig;
    @Autowired
    AlarmConf alarmConf;
    @Autowired
    AlarmListen alarmListen;

    @Value("${piloting.run.serial}")
    private boolean runFromSerial;
    @Value("${piloting.run.netty}")
    private boolean runFromNetty;
    @Value("${piloting.run.generate}")
    private boolean runFromGenerate;
    @Value("${piloting.run.log}")
    private boolean runFromLogFile;

    @Override
    public void run(ApplicationArguments args) {

        //从串口设备读取
        if (runFromSerial) {
            runFromSerial();
        }

        if (runFromNetty) {
            runFromNetty();
        }
        //生成数据模拟
        if (runFromGenerate) {
            runFromGenerate();
        }

        //从日志中读取测试
        if (runFromLogFile) {
            runFromLogFile();
        }


        if(alarmConf.getModel()== AlarmModelEnum.HK.getModel()){
            runAlarmListen();
        }

    }

    private void runAlarmListen() {
        Thread alarmListener = new Thread(() -> {
            log.info("alarmListener start");
            this.alarmListen.start();
            log.info("alarmListener start");
        });

        SystemThreadPool.doExecute(alarmListener);
    }

    private void runFromNetty() {
        Thread thread = new Thread(() -> {
            log.info("netty start");
            rxtxServer.start();
            log.info("netty exit");
        });
        SystemThreadPool.doExecute(thread);
    }

    private void runFromGenerate() {
        Thread test_process = new Thread(() -> {
            log.info("test position");
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
        );
        test_process.setName("test_process");
        test_process.start();
    }

    private void runFromSerial() {
        Thread processTty = new Thread(
                () -> {
                    File serport;
                    BufferedReader bufferedReader = null;
                    //获取连接
                    while (true) {
                        log.info("listen start {}", serialPortParam.getSerialPortName());
                        try {
                            serport = new File(serialPortParam.getSerialPortName());
                            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(serport)));
                            break;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            log.error("listen error", e);

                            try {
                                Thread.sleep(1000 * 10);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                                log.error("listen sleep error", ex);
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
                            log.error("read error", e);
                        }
                        positionService.processMsg(msg);
                    }
                }
        );
        processTty.setName("ttyProcess");
        SystemThreadPool.doExecute(processTty);
    }

    private void runFromLogFile() {
        Thread test_from_log = new Thread(() -> {
            DateTimeFormatter HHmmssSSS = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
            //加载文件
            String fileName = "/video_piloting_debug.log";
            BufferedReader reader = null;
            InputStream inputStream = this.getClass().getResourceAsStream(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String startTime = "19:45:03.000";
            LocalTime sTime = LocalTime.parse(startTime, HHmmssSSS);
            String endTime = "20:39:33.000";
            LocalTime eTime = LocalTime.parse(endTime, HHmmssSSS);
            //日志上次打印时间
            long preC = 0;
            String filterS = "= $GPRMC";
            Point pre = null;
            while (true) {
                String s = null;
                try {
                    s = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
                        try {
                            Thread.sleep(millis);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
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
        });
        SystemThreadPool.doExecute(test_from_log);
    }
}
