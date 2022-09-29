package com.liyu.piloting;

import com.liyu.piloting.config.AlarmConf;
import com.liyu.piloting.model.Alarm;
import com.liyu.piloting.service.AlarmService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Ys7Test {

    @Autowired
    private AlarmService alarmService;
    @Autowired
    private AlarmConf alarmConf;


    @Test
    public void token_test() {
        alarmService.accessToken();
    }

    @Test
    public void getAlarmDeviceList_test() {
        String deviceSerial = "K30567517";
        List<Alarm> alarmDeviceList = null;
        for (Integer alarmType : alarmConf.getAlarmType()) {
            alarmDeviceList = alarmService.getAlarmDeviceList(deviceSerial, alarmType, alarmConf.getStatus());
        }
        if (alarmDeviceList == null || alarmDeviceList.isEmpty()) {
            System.out.println("查询为空");

        }
        alarmDeviceList.sort((a, b) -> (int) (b.getAlarmTime() - a.getAlarmTime()));
        for (Alarm alarm : alarmDeviceList) {
            System.out.println("alarm = " + alarm);
        }
    }


}
