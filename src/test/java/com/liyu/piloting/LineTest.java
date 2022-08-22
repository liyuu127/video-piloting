package com.liyu.piloting;

import com.liyu.piloting.model.LineConfig;
import com.liyu.piloting.model.LineInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LineTest {

    @Autowired
    private LineConfig lineConfig;


    @Test
    public void lineConfig_init_test(){
        System.out.println("lineConfig = " + lineConfig);
        LineInstance lineInstance = lineConfig.lineInstance();
        System.out.println("lineInstance = " + lineInstance);
        lineInstance.getStartStation().setName("x");
        lineInstance.getCameraList().get(0).setName("xx");
        System.out.println("lineInstance = " + lineInstance);
        System.out.println("lineConfig = " + lineConfig);
        System.out.println("lineInstance.directionIsPositive() = " + lineInstance.directionIsPositive());
        System.out.println("lineInstance.lineStatusIsInit() = " + lineInstance.lineStatusIsInit());
    }
}
