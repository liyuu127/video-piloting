package com.liyu.piloting;

import com.liyu.piloting.service.AlarmService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Ys7Test {

    @Autowired
    private AlarmService alarmService;



    @Test
    public void token_test() {
        alarmService.accessToken();
    }

    @Test
    public void d_alarm_test() {
        alarmService.queryAlarmDeviceList("K30567517");
    }



}
