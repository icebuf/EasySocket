package com.skyworth.easysocket;


import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 作者：Ice Nation
 * 日期：2018/4/10 19:41
 * 邮箱：tangjie@skyworth.com
 */

public class Utils {

    public static final String TAG = "Utils";


    public static final int INT_BYTES = Integer.SIZE / Byte.SIZE;
    public static final int LONG_BYTES = Long.SIZE / Byte.SIZE;


    /**
     * 判断IP格式和范围
     */
    public static boolean isIP(String address) {

        if(address.length() < 7 || address.length() > 15 || "".equals(address))
            return false;

        String exp = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pat = Pattern.compile(exp);
        Matcher mat = pat.matcher(address);
        boolean ipAddress = mat.find();
        //============对之前的ip判断的bug在进行判断
        if (ipAddress){
            String ips[] = address.split("\\.");
            if(ips.length==4){
                try{
                    for(String ip : ips){
                        if(Integer.parseInt(ip)<0||Integer.parseInt(ip)>255){
                            return false;
                        }
                    }
                }catch (Exception e){
                    return false;
                }
                return true;
            }else{
                return false;
            }
        }
        return false;
    }

    /**
     * 判断网络是否连接
     * @return 没有数据和wifi连接返回false
     */
    public static boolean isNetConnect(Intent intent) {
        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
        Log.i("222","wifiState = " + wifiState );
        if(wifiState == WifiManager.WIFI_STATE_DISABLED){
            return false;
        }else if(wifiState == WifiManager.WIFI_STATE_ENABLED){
            return true;
        }
        return false;
    }

    /**
     * 把IP地址转化为int
     * @param ipAddress 输入IP串
     * @return int IP串对应的int值
     * @throws Exception 转换失败错误
     */
    public static int ipToInt(String ipAddress) throws Exception {
        byte[] bytes = InetAddress.getByName(ipAddress).getAddress();
        return readInt2(bytes,0);
    }

    /**
     * 从byte数组中的<code>start</code>位置起读取一个int型数据
     * 其中<code>start</code>位置为int型数据的最高位
     * @param bytes 源数组
     * @param start 起始位
     * @return 结果数据
     */
    public static int readInt(byte[] bytes,int start) {
        if(bytes.length - start < INT_BYTES) {
            throw new ArrayIndexOutOfBoundsException("can not read a int value!");
        }
        int value = 0;
        for (int i = start; i < start + INT_BYTES; i++) {
            value |= (bytes[i] & 0xFF) << ((start + INT_BYTES - i - 1) * 8 ) ;
        }
        return value;
    }


    /**
     * 从byte数组中的<code>start</code>位置起读取一个int型数据
     * 其中<code>start + 4</code>位置为int型数据的最高位
     * @param bytes 源数组
     * @param start 起始位
     * @return 结果数据
     */
    public static int readInt2(byte[] bytes,int start) {
        if(bytes.length - start < INT_BYTES) {
            throw new ArrayIndexOutOfBoundsException("can not read a int value!");
        }
        int value = 0;

        for (int i = 0; i < INT_BYTES; i++) {
            value |= (bytes[i] & 0xFF) << (i * 8 ) ;
        }
        return value;
    }

    /**
     * 讲一个byte数组转换为<class>String</class>类型字符串
     * @param bytes 输入数组
     * @param charsetName 输入字符集名称
     * @return 转换后的字符串
     */
    public static String byteArray2String(byte[] bytes,String charsetName){
        try {
            return new String(bytes, charsetName);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG,"Unknown error::byteArray2String() failed!");
            return "";
        }
    }

    /**
     * 获取ip地址
     * @return 如果有线网络连接 则返回有线IP
     */
    public static String getHostIp() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.i("Util", "SocketException");
            e.printStackTrace();
        }
        return hostIp;

    }

    public static List<String> listHostIp() {

        List<String> hostIps = new ArrayList<>();
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    hostIps.add(ia.getHostAddress());
                }
            }
        } catch (SocketException e) {
            Log.i("Util", "SocketException");
            e.printStackTrace();
        }
        return hostIps;

    }

    /**
     * 获取无线网络IP地址
     * @return ip地址
     */
    public static String getWiFiIp(Context context) {

        WifiManager wifiManager = (WifiManager) context
                .getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = null;
        if (wifiManager != null) {
            wifiInfo = wifiManager.getConnectionInfo();
        }
        int i = wifiInfo != null ? wifiInfo.getIpAddress() : 0;
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF);

    }

    public static void putInt(byte[] src, int offset, int value) {
        byte[] bytes = new byte[INT_BYTES];
        for (int i = bytes.length - 1; i >= 0; i--) {
            bytes[i] = (byte) value;
            value >>>= 8;
        }
        System.arraycopy(bytes,0,src,offset,INT_BYTES);
    }

    public static String getHostName() {
        String hostName = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    hostName = ia.getHostName();
                        break;

                }
            }
        } catch (SocketException e) {
            Log.i(TAG, "error::getHostName()" + e.getMessage());
            hostName = "null";
        }
        return hostName;
    }

    public static byte[] getBytes(int value) {
        byte[] bytes = new byte[INT_BYTES];
        for (int i=0;i<INT_BYTES;i++){
            bytes[i] = (byte) (value >> (8 * (INT_BYTES-1-i)));
        }
        return bytes;
    }

    public static boolean isPort(int port) {
        return port > 0 && port <= 65535;
    }
}
