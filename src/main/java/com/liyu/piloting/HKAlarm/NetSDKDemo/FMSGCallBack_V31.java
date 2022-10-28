
package com.liyu.piloting.HKAlarm.NetSDKDemo;

import com.liyu.piloting.service.AlarmService;
import com.sun.jna.Pointer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31 {

    @Autowired
    AlarmService alarmService;
    //报警信息回调函数
    public boolean invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
//        AlarmDataParse.alarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
        alarmService.alarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
        return true;
    }
}







