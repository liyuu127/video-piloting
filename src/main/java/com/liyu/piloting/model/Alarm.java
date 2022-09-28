package com.liyu.piloting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author liyu
 * date 2022/9/28 19:18
 * description
 * -1:全部
 * 10000:人体感应事件
 * 10001:紧急遥控按钮事件
 * 10002:移动侦测告警
 * 10003:婴儿啼哭告警
 * 10004:门磁告警
 * 10005:烟感告警
 * 10006:可燃气体告警
 * 10008:水浸告警
 * 10009:紧急按钮告警
 * 10010:人体感应告警
 * 10011:遮挡告警
 * 10012:视频丢失
 * 10013:越界侦测
 * 10014:区域入侵
 * 10015:人脸检测事件
 * 10016:智能门铃告警
 * 10018:幕帘告警
 * 10019:单体门磁告警
 * 10020:场景变更侦测
 * 10021:虚焦侦测
 * 10022:音频异常侦测
 * 10023:物品遗留侦测
 * 10024:物品拿取侦测
 * 10025:非法停车侦测
 * 10026:人员聚集侦测
 * 10027:徘徊检测侦测
 * 10028:快速移动侦测
 * 10029:进入区域侦测
 * 10030:离开区域侦测
 * 10031:磁干扰告警
 * 10032:电池电量低告警
 * 10033:闯入告警
 * 10035:婴儿大动作告警
 * 10036:电源切换告警
 * 10079:智能检测告警
 * 10100:IO告警
 * 10101:IO-1告警
 * 10102:IO-2告警
 * 10103:IO-3告警
 * 10104:IO-4告警
 * 10105:IO-5告警
 * 10106:IO-6告警
 * 10107:IO-7告警
 * 10108:IO-8告警
 * 10109:IO-9告警
 * 10110:IO-10告警
 * 10111:IO-11告警
 * 10112:IO-12告警
 * 10113:IO-13告警
 * 10114:IO-14告警
 * 10115:IO-15告警
 * 10116:IO-16告警
 * 12000:移动侦测告警开始
 * 12001:视频信号丢失
 * 12002:遮挡告警开始
 * 12003:即时防区告警
 * 12004:即时防区恢复
 * 12005:24小时有声防区告警
 * 12006:24小时有声防区恢复
 * 12007:延时防区告警
 * 12008:延时防区恢复
 * 12009:内部延时防区告警
 * 12010:内部延时防区恢复
 * 12011:火警防区告警
 * 12012:火警防区恢复
 * 12013:周界防区告警
 * 12014:周界防区恢复
 * 12015:24小时无声防区告警
 * 12016:24小时无声防区恢复
 * 12017:24小时辅助防区告警
 * 12018:24小时辅助防区告警恢复
 * 12019:24小时震动防区告警
 * 12020:24小时震动防区告警恢复
 * 12021:防区感应器被拆
 * 12022:防区感应器被拆恢复
 * 12023:软防区紧急告警
 * 12024:软防区火警
 * 12025:软防区匪警
 * 12026:挟持报告
 * 12027:设备防拆
 * 12028:设备防拆恢复
 * 12029:交流电掉电
 * 12030:交流电恢复
 * 12031:蓄电池电压低
 * 12032:蓄电池电压正常
 * 12033:电话线断开
 * 12034:电话线连接
 * 12035:扩展总线模块掉线
 * 12036:扩展总线模块掉线恢复
 * 12037:键盘掉线
 * 12038:键盘恢复
 * 12039:键盘总线上触发器掉线
 * 12040:键盘总线上触发器恢复
 * 12041:自动布撤防失败
 * 12042:自动撤防失败
 * 12043:无线网络异常
 * 12044:无线网络恢复正常
 * 12045:SIM卡异常
 * 12046:SIM卡恢复正常
 * 12047:主机复位
 * 12048:撤防
 * 12049:布防
 * 12050:自动撤防
 * 12051:自动布防
 * 12052:消警
 * 12053:即时布防
 * 12054:钥匙防区撤防
 * 12055:钥匙防区布防
 * 12056:留守布防
 * 12057:强制布防
 * 12058:旁路
 * 12059:旁路恢复
 * 12060:子系统组旁路
 * 12061:子系统组旁路恢复
 * 12062:手动测试报告
 * 12063:定时测试报告
 * 12064:单防区撤防
 * 12065:单防区布防
 * 12066:键盘锁定
 * 12067:键盘解锁
 * 12068:打印机掉线
 * 12069:打印机恢复
 * 12070:即时撤防
 * 12071:留守撤防
 * 12072:定时开启触发器
 * 12073:定时关闭触发器
 * 12074:定时开启触发器失败
 * 12075:定时关闭触发器失败
 * 12076:进入编程
 * 12077:退出编程
 * 12078:键盘总线上GP/K掉线
 * 12079:键盘总线上GP/K恢复
 * 12080:键盘总线上MN/K掉线
 * 12081:键盘总线上MN/K恢复
 * 12082:IP冲突
 * 12083:IP正常
 * 12084:网线断
 * 12085:网线正常
 * 12086:移动侦测告警结束
 * 12087:遮挡告警结束
 * 12088:视频信号恢复
 * 12089:输入/输出视频制式不匹配
 * 12090:输入/输出视频制式恢复正常
 * 12091:视频输入异常
 * 12092:视频输入恢复正常
 * 12093:硬盘满
 * 12094:硬盘空闲
 * 12095:硬盘出错
 * 12096:硬盘恢复正常
 * 12097:图片上传失败
 * 12098:探测器离线
 * 12099:探测器恢复在线
 * 12100:探测器电量欠压
 * 12101:探测器电量恢复正常
 * 12102:防区添加探测器
 * 12103:防区删除探测器
 * 12104:WIFI通信异常
 * 12105:WIFI通信恢复正常
 * 12106:RF信号异常
 * 12107:RF信号恢复正常
 * 10037:温度过高告警
 * 10038:温度过低告警
 * 10039:湿度过高告警
 * 10040:湿度过低告警
 * 12108:主机防拆告警
 * 12109:主机防拆恢复
 * 12110:读卡器防拆告警
 * 12111:读卡器防拆恢复
 * 12112:事件输入告警
 * 12113:事件输入恢复
 * 12114:门控安全模块防拆告警
 * 12115:门控安全模块防拆恢复
 * 12116:网络断开
 * 12117:网络恢复
 * 12118:设备上电启动
 * 12119:设备掉电关闭
 * 12120:门异常打开（门磁）
 * 40001:第三方抓图
 * 40002:互联互通
 * 字段名	类型	描述
 * alarmId	String	消息ID
 * alarmName	String	告警源名称
 * alarmType	int	告警类型
 * alarmTime	long	告警时间，long格式如12323452345，精确到毫秒
 * channelNo	int	通道号
 * isEncrypt	int	是否加密：0-不加密，1-加密
 * isChecked	int	是否已读：0-未读，1-已读
 * recState	int	存储类型：0-无存储，1-萤石云存储，4-sd卡存储，5-萤石云存储和sd卡存储
 * preTime	int	预录时间：单位秒
 * delayTime	int	延迟录像时间，单位秒
 * deviceSerial	String	设备序列号,存在英文字母的设备序列号，字母需为大写
 * alarmPicUrl	String	告警图片地址
 * relationAlarms	list	关联的告警消息
 * customerType	String	透传设备参数类型
 * customerInfo	String	透传设备参数内容
 *
 * 返回码
 *
 * 返回码	返回消息	描述
 * 200	操作成功	请求成功
 * 10001	参数错误	参数为空或格式不正确
 * 10002	accessToken异常或过期	重新获取accessToken
 * 10005	appKey异常	appKey被冻结
 * 20002	设备不存在
 * 20014	deviceSerial不合法
 * 20018	该用户不拥有该设备	检查设备是否属于当前账户
 * 49999	数据异常	接口调用异常
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alarm {
    private String alarmId;
    private String alarmName;
    private Integer alarmType;
    private Long alarmTime;
    private Integer channelNo;
    private Integer isEncrypt;
    private Integer isChecked;
    private Integer recState;
    private Integer preTime;
    private Integer delayTime;
    private String deviceSerial;
    private String alarmPicUrl;
    private String customerType;
    private String customerInfo;
    private String relationAlarms;
}

