package com.liyu.piloting.HKAlarm.alarm;


import com.liyu.piloting.HKAlarm.CommonMethod.osSelect;
import com.liyu.piloting.HKAlarm.NetSDKDemo.FMSGCallBack;
import com.liyu.piloting.HKAlarm.NetSDKDemo.FMSGCallBack_V31;
import com.liyu.piloting.HKAlarm.NetSDKDemo.HCNetSDK;
import com.liyu.piloting.config.AlarmConf;
import com.liyu.piloting.config.LineConfig;
import com.liyu.piloting.model.Camera;
import com.liyu.piloting.model.CameraListenEnum;
import com.liyu.piloting.util.SystemThreadPool;
import com.liyu.piloting.websocket.model.WebSocketMessage;
import com.liyu.piloting.websocket.util.WebSocketSender;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.liyu.piloting.HKAlarm.NetSDKDemo.HCNetSDK.EXCEPTION_EXCHANGE;
import static com.liyu.piloting.HKAlarm.NetSDKDemo.HCNetSDK.NET_DVR_CHECK_USER_STATUS;
import static com.liyu.piloting.websocket.constant.WebSocketConstant.CAMERA_NET_LOSE;
import static com.liyu.piloting.websocket.constant.WebSocketConstant.CAMERA_NET_RECOVER;


@Slf4j
@Component
@DependsOn({"fmsgCallBack_v31"})
public class AlarmListen {

    @Autowired
    LineConfig lineConfig;
    @Autowired
    AlarmConf alarmConf;
    @Autowired
    FMSGCallBack_V31 fmsgCallBack_v31;

    private List<Camera> cameraList;
    private FMSGCallBack_V31 fMSFCallBack_V31 = null;

    static HCNetSDK hCNetSDK = null;
    static int[] lUserID = new int[]{0, 0, 0, 0, 0};//用户句柄 实现对设备登录
    static Map<Integer, Boolean> userIdStatus = new HashMap<>();
    static int[] lAlarmHandle = new int[]{-1, -1, -1, -1, -1};//报警布防句柄
    static int[] lAlarmHandle_V50 = new int[]{-1, -1, -1, -1, -1}; //v50报警布防句柄
    static int lListenHandle = -1;//报警监听句柄
    static FMSGCallBack fMSFCallBack = null;

    // 异常回调
    private HCNetSDK.FExceptionCallBack exceptionCallBack = new HCNetSDK.FExceptionCallBack() {
        @Override
        public void invoke(int dwType, int lUserID, int lHandle, Pointer pUser) {
            NativeLong deviceid = pUser.getNativeLong(0);

            System.out.println("预览异常：dwType=" + dwType + ",lUserID=" + lUserID + ",lHandle=" + lHandle + ",pUser="
                    + deviceid.intValue());

            //宏定义 宏定义值 含义
            //EXCEPTION_EXCHANGE 0x8000 用户交互时异常（注册心跳超时，心跳间隔为2分钟）
            //EXCEPTION_AUDIOEXCHANGE 0x8001 语音对讲异常
            //EXCEPTION_ALARM 0x8002 报警异常
            //EXCEPTION_PREVIEW 0x8003 网络预览异常
            //EXCEPTION_SERIAL 0x8004 透明通道异常
            //EXCEPTION_RECONNECT 0x8005 预览时重连
            //EXCEPTION_ALARMRECONNECT 0x8006 报警时重连
            //EXCEPTION_SERIALRECONNECT 0x8007 透明通道重连
            //SERIAL_RECONNECTSUCCESS 0x8008 透明通道重连成功
            //EXCEPTION_PLAYBACK 0x8010 回放异常
            //EXCEPTION_DISKFMT 0x8011 硬盘格式化
            //EXCEPTION_PASSIVEDECODE 0x8012 被动解码异常
            //EXCEPTION_EMAILTEST 0x8013 邮件测试异常
            //EXCEPTION_BACKUP 0x8014 备份异常
            //PREVIEW_RECONNECTSUCCESS 0x8015 预览时重连成功
            //ALARM_RECONNECTSUCCESS 0x8016 报警时重连成功
            //RESUME_EXCHANGE 0x8017 用户交互恢复
            //NETWORK_FLOWTEST_EXCEPTION 0x8018 网络流量检测异常
            //EXCEPTION_PICPREVIEWRECONNECT 0x8019 图片预览重连
            //PICPREVIEW_RECONNECTSUCCESS 0x8020 图片预览重连成功
            //EXCEPTION_PICPREVIEW 0x8021 图片预览异常
            //EXCEPTION_MAX_ALARM_INFO 0x8022 报警信息缓存已达上限
            //EXCEPTION_LOST_ALARM 0x8023 报警丢失
            //EXCEPTION_PASSIVETRANSRECONNECT 0x8024 被动转码重连
            //PASSIVETRANS_RECONNECTSUCCESS 0x8025 被动转码重连成功
            //EXCEPTION_PASSIVETRANS 0x8026 被动转码异常
            //EXCEPTION_RELOGIN 0x8040 用户重登陆
            //RELOGIN_SUCCESS 0x8041 用户重登陆成功
            //EXCEPTION_PASSIVEDECODE_RECONNNECT 0x8042 被动解码重连
            //EXCEPTION_CLUSTER_CS_ARMFAILED 0x8043 集群报警异常
            //EXCEPTION_RELOGIN_FAILED 0x8044 重登陆失败，停止重登陆
            //EXCEPTION_PREVIEW_RECONNECT_CLOSED 0x8045 关闭预览重连功能
            //EXCEPTION_ALARM_RECONNECT_CLOSED 0x8046 关闭报警重连功能
            //EXCEPTION_SERIAL_RECONNECT_CLOSED 0x8047 关闭透明通道重连功能
            //EXCEPTION_PIC_RECONNECT_CLOSED 0x8048 关闭回显重连功能
            //EXCEPTION_PASSIVE_DECODE_RECONNECT_CLOSED 0x8049 关闭被动解码重连功能
            //EXCEPTION_PASSIVE_TRANS_RECONNECT_CLOSED 0x804a 关闭被动转码重连功能
            if (dwType == EXCEPTION_EXCHANGE) {
                userIdStatus.put(lUserID, false);
                WebSocketMessage<Camera> message = new WebSocketMessage<>();
                message.setContent(cameraList.get(0))
                        .setMsgType(CAMERA_NET_LOSE);
                WebSocketSender.pushMessageToAll(message);
            } else if (dwType == 0x8017) {
                userIdStatus.put(lUserID, true);
                WebSocketMessage<Camera> message = new WebSocketMessage<>();
                message.setContent(cameraList.get(0))
                        .setMsgType(CAMERA_NET_RECOVER);
            }

        }

    };

    @PostConstruct
    public void init() {
        log.info("HK AlarmListen init start");
        this.cameraList = lineConfig.getCameraList();
        this.fMSFCallBack_V31 = fmsgCallBack_v31;

        if (cameraList.isEmpty()) {
            log.info("HK Alarm cameraList is empty");
        }
        int size = cameraList.size() + 10;
        lUserID = new int[size];
        Arrays.fill(lUserID, 0);
        lAlarmHandle = new int[size];
        Arrays.fill(lAlarmHandle, -1);
        lAlarmHandle_V50 = new int[size];
        Arrays.fill(lAlarmHandle_V50, -1);

        if (hCNetSDK == null) {
            if (!CreateSDKInstance()) {
                log.error("HK Alarm Load SDK fail");
                return;
            }
        }
        //linux系统建议调用以下接口加载组件库
        if (osSelect.isLinux()) {
            HCNetSDK.BYTE_ARRAY ptrByteArray1 = new HCNetSDK.BYTE_ARRAY(256);
            HCNetSDK.BYTE_ARRAY ptrByteArray2 = new HCNetSDK.BYTE_ARRAY(256);
            //这里是库的绝对路径，请根据实际情况修改，注意改路径必须有访问权限
            String strPath1 = "/home/LinuxSDK/libcrypto.so.1.1";
            String strPath2 = "/home/LinuxSDK/libssl.so.1.1";

            System.arraycopy(strPath1.getBytes(), 0, ptrByteArray1.byValue, 0, strPath1.length());
            ptrByteArray1.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArray1.getPointer());

            System.arraycopy(strPath2.getBytes(), 0, ptrByteArray2.byValue, 0, strPath2.length());
            ptrByteArray2.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArray2.getPointer());

            String strPathCom = "/home/LinuxSDK/";
            HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
            System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
            struComPath.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());
        }

        /**初始化*/
        hCNetSDK.NET_DVR_Init();
        /**加载日志*/
        hCNetSDK.NET_DVR_SetLogToFile(3, "../sdklog", false);
        //设置报警回调函数
        if (fMSFCallBack_V31 != null) {
            Pointer pUser = null;
            if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser)) {
                log.error("HK Alarm set callback func fail");
                return;
            } else {
                log.info("HK Alarm set callback func success");
            }
        }
        /** 设备上传的报警信息是COMM_VCA_ALARM(0x4993)类型，
         在SDK初始化之后增加调用NET_DVR_SetSDKLocalCfg(enumType为NET_DVR_LOCAL_CFG_TYPE_GENERAL)设置通用参数NET_DVR_LOCAL_GENERAL_CFG的byAlarmJsonPictureSeparate为1，
         将Json数据和图片数据分离上传，这样设置之后，报警布防回调函数里面接收到的报警信息类型为COMM_ISAPI_ALARM(0x6009)，
         报警信息结构体为NET_DVR_ALARM_ISAPI_INFO（与设备无关，SDK封装的数据结构），更便于解析。*/
        HCNetSDK.NET_DVR_LOCAL_GENERAL_CFG struNET_DVR_LOCAL_GENERAL_CFG = new HCNetSDK.NET_DVR_LOCAL_GENERAL_CFG();
        struNET_DVR_LOCAL_GENERAL_CFG.byAlarmJsonPictureSeparate = 1;   //设置JSON透传报警数据和图片分离
        struNET_DVR_LOCAL_GENERAL_CFG.write();
        Pointer pStrNET_DVR_LOCAL_GENERAL_CFG = struNET_DVR_LOCAL_GENERAL_CFG.getPointer();
        hCNetSDK.NET_DVR_SetSDKLocalCfg(17, pStrNET_DVR_LOCAL_GENERAL_CFG);
        log.info("HK AlarmListen init end");
    }


    public void start() {
        log.info("HK AlarmListen start");
        //设置每个摄像头
        for (Camera camera : cameraList) {
//            Login_V30(camera.getId(), camera.getIp(), camera.getPort(), camera.getUser(), camera.getPsw());
            Thread register = new Thread(
                    () -> {
                        log.info("camera i={} start register", camera.getId());
                        Login_V40(camera.getId(), camera.getIp(), camera.getPort(), camera.getUser(), camera.getPsw());  //登录设备
                        SetAlarm(camera.getId());
                        log.info("camera i={} start register success", camera.getId());
                    }
            );
            SystemThreadPool.doExecute(register);


//            startListen(camera.getIp(), camera.getPort());//报警监听，不需要登陆设备
        }
        if (CameraListenEnum.LISTEN.getValue() == alarmConf.getCameraListen()) {
            setExceptionCallBack();
            log.info("setExceptionCallBack..........");
        }
        while (true) {
            try {
                log.info("HK Alarm thread is running");
                Thread.sleep(1000 * 10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                log.error("HK Alarm listen sleep error", ex);
            }
        }

    }

    @PreDestroy
    public void destroy() {
        log.info("HK AlarmListen destroy start");
        for (Camera camera : cameraList) {
            Logout(camera.getId());
        }
        //释放SDK
        hCNetSDK.NET_DVR_Cleanup();
        log.info("HK AlarmListen destroy end");
    }

    public boolean queryDeviceOnLine(int cameraId) {

        boolean online = true;
        if (CameraListenEnum.LISTEN.getValue() == alarmConf.getCameraListen()) {
            if (lUserID[cameraId] == -1) {
                online = false;
            } else {
                Boolean b = userIdStatus.get(lUserID[cameraId]);
                if (b == null || !b) {
                    online = false;
                }
            }
        } else if (CameraListenEnum.QUERY.getValue() == alarmConf.getCameraListen()) {
            int userId = lUserID[cameraId];
            online = hCNetSDK.NET_DVR_RemoteControl(userId, NET_DVR_CHECK_USER_STATUS, null, 0);
        }
        log.info("queryDeviceOnLine cameraId={},online={}", cameraId, online);
        return online;
    }

    //设备在线状态监测异步
    public void setExceptionCallBack() {
        hCNetSDK.NET_DVR_SetExceptionCallBack_V30(0, 0, exceptionCallBack, null);
    }


    /**
     * 设备登录V40 与V30功能一致
     *
     * @param i    登录设备编号
     * @param ip   设备IP
     * @param port SDK端口，默认设备的8000端口
     * @param user 设备用户名
     * @param psw  设备密码
     */
    public void Login_V40(int i, String ip, short port, String user, String psw) {
        //注册
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();//设备登录信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();//设备信息

        String m_sDeviceIP = ip;//设备ip地址
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());

        String m_sUsername = user;//设备用户名
        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());

        String m_sPassword = psw;//设备密码
        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());

        m_strLoginInfo.wPort = port;
        m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
//        m_strLoginInfo.byLoginMode=1;  //ISAPI登录
        m_strLoginInfo.write();

        int login_v40 = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        if (login_v40 == -1) {
            log.info("Login_V40 camera_i={}, fail code={}", i, hCNetSDK.NET_DVR_GetLastError());
            //loop for login
            while (login_v40 == -1) {
                //sleep
                try {
                    Thread.sleep(1000 * 3);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    log.error("Login_V40 camera_i={} sleep error", i, ex);
                }
                // try again
                login_v40 = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
                log.info("Login_V40 camera_i={} try again, login_v40={}", i, login_v40);
            }

        } else {
            log.info("Login_V40 camera_i={}, success", i);
        }
        lUserID[i] = login_v40;

        userIdStatus.put(login_v40, true);
    }

    /**
     * 设备登录V30
     *
     * @param i    登录设备编号
     * @param ip   设备IP
     * @param port SDK端口，默认设备的8000端口
     * @param user 设备用户名
     * @param psw  设备密码
     */
    public static void Login_V30(int i, String ip, short port, String user, String psw) {
        HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        lUserID[i] = hCNetSDK.NET_DVR_Login_V30(ip, port, user, psw, m_strDeviceInfo);
        System.out.println("UsID:" + lUserID[i]);
        if ((lUserID[i] == -1) || (lUserID[i] == 0xFFFFFFFF)) {
            System.out.println("登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return;
        } else {
            System.out.println(ip + ":设备登录成功！");
            return;
        }
    }

    /**
     * 报警布防接口
     *
     * @param i
     */
    public static void SetAlarm(int i) {
        if (lAlarmHandle[i] < 0)//尚未布防,需要布防
        {
            //报警布防参数设置
            HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
            m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
            m_strAlarmInfo.byLevel = 0;  //布防等级
            m_strAlarmInfo.byAlarmInfoType = 1;   // 智能交通报警信息上传类型：0- 老报警信息（NET_DVR_PLATE_RESULT），1- 新报警信息(NET_ITS_PLATE_RESULT)
            m_strAlarmInfo.byDeployType = 0;   //布防类型：0-客户端布防，1-实时布防
            m_strAlarmInfo.write();
            int alarmChan_v41 = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID[i], m_strAlarmInfo);
            lAlarmHandle[i] = alarmChan_v41;
            log.info("SetAlarm camera_i={},lAlarmHandle={} ", i, alarmChan_v41);
            if (alarmChan_v41 == -1) {
                log.info("SetAlarm camera_i={}, fail code={}", i, hCNetSDK.NET_DVR_GetLastError());
                //loop for SetAlarm
                while (alarmChan_v41 == -1) {
                    //sleep
                    try {
                        Thread.sleep(1000 * 3);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                        log.error("SetAlarm camera_i={} sleep error", i, ex);
                    }
                    // try again
                    alarmChan_v41 = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID[i], m_strAlarmInfo);
                    log.info("SetAlarm camera_i={} try again, alarmChan_v41={}", i, alarmChan_v41);
                }
            } else {
                log.info("SetAlarm camera_i={}, success", i);
            }
            lAlarmHandle[i] = alarmChan_v41;
        } else {
            log.error("设备已经布防，请先撤防！");
        }
    }


    /**
     * 报警布防V50接口，功能和V41一致
     *
     * @param i
     */
    public static void setAlarm_V50(int i) {

        if (lAlarmHandle_V50[i] < 0)//尚未布防,需要布防
        {
            //报警布防参数设置
            HCNetSDK.NET_DVR_SETUPALARM_PARAM_V50 m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM_V50();
            m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
            m_strAlarmInfo.byLevel = 1;  //布防等级
            m_strAlarmInfo.byAlarmInfoType = 1;   // 智能交通报警信息上传类型：0- 老报警信息（NET_DVR_PLATE_RESULT），1- 新报警信息(NET_ITS_PLATE_RESULT)
            m_strAlarmInfo.byDeployType = 1;   //布防类型 0：客户端布防 1：实时布防
            m_strAlarmInfo.write();
            lAlarmHandle[i] = hCNetSDK.NET_DVR_SetupAlarmChan_V50(lUserID[i], m_strAlarmInfo, Pointer.NULL, 0);
            System.out.println("lAlarmHandle: " + lAlarmHandle[i]);
            if (lAlarmHandle[i] == -1) {
                System.out.println("布防失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
                return;
            } else {
                System.out.println("布防成功");

            }

        } else {

            System.out.println("设备已经布防，请先撤防！");
        }
        return;

    }


    /**
     * 开启监听
     *
     * @param ip   监听IP
     * @param port 监听端口
     */
    public void startListen(String ip, short port) {
//        if (fMSFCallBack == null) {
//            fMSFCallBack = new FMSGCallBack();
//        }
        lListenHandle = hCNetSDK.NET_DVR_StartListen_V30(ip, port, fMSFCallBack_V31, null);
        if (lListenHandle == -1) {
            log.error("HK Alarm startListen fail   " + hCNetSDK.NET_DVR_GetLastError());
            return;
        } else {
            log.info("HK Alarm startListen success");
        }
    }

    /**
     * 设备注销
     *
     * @param i
     */
    public static void Logout(int i) {

        if (lAlarmHandle[i] > -1) {
            if (!hCNetSDK.NET_DVR_CloseAlarmChan(lAlarmHandle[i])) {
                log.info("Logout i={} cancel alarm success", i);
            }
        }
        if (lListenHandle > -1) {
            if (!hCNetSDK.NET_DVR_StopListen_V30(lListenHandle)) {
                log.info("Logout i={} cancel listen success", i);
            }
        }
        if (hCNetSDK.NET_DVR_Logout(lUserID[i])) {
            log.info("Logout i={} logout success", i);
        }

        return;
    }

    /**
     * 动态库加载
     *
     * @return
     */
    private static boolean CreateSDKInstance() {
        if (hCNetSDK == null) {
            synchronized (HCNetSDK.class) {
                String strDllPath = "";
                try {
                    if (osSelect.isWindows())
                        //win系统加载库路径
                        strDllPath = System.getProperty("user.dir") + "\\lib\\HCNetSDK.dll";
                    else if (osSelect.isLinux())
                        //Linux系统加载库路径
                        strDllPath = "/home/LinuxSDK/libhcnetsdk.so";
                    hCNetSDK = (HCNetSDK) Native.loadLibrary(strDllPath, HCNetSDK.class);
                } catch (Exception ex) {
//                    System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                    log.error("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }


}
