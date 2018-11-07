package com.skyworth.easysocket;

/**
 * 作者：Ice Nation
 * 日期：2018/5/8 14:21
 * 邮箱：tangjie@skyworth.com
 */

public class Protocol {

    public static final String UDP_MESSAGE_HEAD = "@#SKYWORTH_GD_UDP_MSG_HEADER+*";

    public static final int UDP_PORT = 9810;
    public static final int TCP_PORT = 9880;

    public static final int UDP_TYPE = 100;
    public static final int UDP_CODE = 0;

    //协议头
    public static final int HEAD = 0x0A;
    //协议版本
    public static final int VERSION = 0x01;
    //心跳内容
    public static final String HEART_TAG = "hello";

    //请求/响应类型
    public static final int HEART = 1000;
    public static final int VERIFY = 1100;
    public static final int FRAME = 1200;
    public static final int CONTROL = 1300;

    public static final int AAC = 1400;

    //请求/响应码

    //心跳
    public static final int HEART_ASK = 1001;//心跳发送
    public static final int HEART_ANSWER = 1051;//心跳回应
    public static final int HEART_ERROR = 1052;//心跳错误

    //验证
    public static final int VERIFY_ASK = 1101;//验证发送
    public static final int VERIFY_FAIL = 1151;//验证失败

    //H264流
    public static final int FRAME_START = 1201;//新frame
    public static final int FRAME_START_VERIFY = 1202;//新frame验证
    public static final int FRAME_DATA = 1203;//数据
    public static final int FRAME_DATA_VERIFY = 1204;//数据验证
    public static final int FRAME_FINISH = 1205;//完毕
    public static final int FRAME_FINISH_VERIFY = 1206;//完毕验证

    public static final int FRAME_START_OK = 1251;//新frame验证通过
    public static final int FRAME_START_FAIL = 1252;//新frame验证失败
    public static final int FRAME_DATA_OK = 1253;//新frame验证通过
    public static final int FRAME_DATA_FAIL = 1254;//数据验证失败
    public static final int FRAME_FINISH_OK = 1255;//完毕验证通过
    public static final int FRAME_FINISH_FAIL = 1256;//完毕验证失败

    //编码器控制
    public static final int COM_CONFIG_VIDEO = 1301;//配置分辨率
    public static final int COM_CONFIG_AUDIO = 1302;//配置解码器

    public static final int COM_START_CODEC = 1303;//配置解码器
    public static final int COM_STOP_CODEC = 1303;//配置解码器
    public static final int COM_CONFIG_OK = 1351;


    public static final int COM_START_OK = 1352;
    public static final int COM_STOP_OK = 1353;
}
