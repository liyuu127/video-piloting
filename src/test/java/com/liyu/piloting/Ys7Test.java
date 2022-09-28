package com.liyu.piloting;

import com.liyu.piloting.config.LineConfig;
import com.liyu.piloting.config.LineJudgmentConfig;
import com.liyu.piloting.model.Point;
import com.liyu.piloting.service.AlarmService;
import com.liyu.piloting.service.PositionTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayDeque;
import java.util.Iterator;

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
