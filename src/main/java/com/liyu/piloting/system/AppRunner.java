package com.liyu.piloting.system;

import com.liyu.piloting.model.Point;
import com.liyu.piloting.rxtx.RxtxServer;
import com.liyu.piloting.rxtx.SerialPortParam;
import com.liyu.piloting.service.PositionService;
import com.liyu.piloting.service.PositionTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
@Slf4j
public class AppRunner implements ApplicationRunner {
//    @Autowired
//    private RxtxServer rxtxServer;

    @Autowired
    private PositionService positionService;
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

//        new Thread(
//                () -> {
//                    File serport;
//                    FileInputStream mSerR = null;
//                    BufferedReader bufferedReader;
//                    //获取连接
//                    while (true) {
//                        log.info("listen start {}", serialPortParam.getSerialPortName());
//                        try {
//                            serport = new File(serialPortParam.getSerialPortName());
//                            mSerR = new FileInputStream(serport);
//                            bufferedReader = new BufferedReader(new InputStreamReader(mSerR));
//                            break;
//                        } catch (FileNotFoundException e) {
//                            log.error("listen error", e);
//                            e.printStackTrace();
//                            try {
//                                Thread.sleep(1000 * 10);
//                            } catch (InterruptedException ex) {
//                                ex.printStackTrace();
//                            }
//                        }
//                    }
//
//                    log.info("listen success {}", serialPortParam.getSerialPortName());
//                    log.info("process start");
//                    //开始处理
//                    while (true) {
//                        String msg = null;
//                        try {
//                            msg = bufferedReader.readLine();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        log.debug("listen data = {}", msg);
//                        positionService.processMsg(msg);
//                    }
//
//                }
//        ).start();

        new Thread(() -> {
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
        ).start();
    }


}
