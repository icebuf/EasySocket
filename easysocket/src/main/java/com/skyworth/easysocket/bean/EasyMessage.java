package com.skyworth.easysocket.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.skyworth.easysocket.Protocol;
import com.skyworth.easysocket.Utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.skyworth.easysocket.Utils.INT_BYTES;
import static com.skyworth.easysocket.Utils.LONG_BYTES;


/**
 * 作者：Ice Nation
 * 日期：2018/5/11 13:37
 * 邮箱：tangjie@skyworth.com
 */

public class EasyMessage {

    //响应类型开始位
    public static final int START_TYPE = 0;
    //响应码开始位
    public static final int START_RES_CODE = 4;
    //附加数据开始位
    public static final int START_DATA = 8;

    //响应类型
    public int type;
    //响应码
    public int code;

    //接收到的数据包内容
    protected byte[] bytes = null;
    //接收到的数据包长度
    protected int length;
    //当前索引
    private int index;


    public EasyMessage(byte[] bytes, int len){
        this.bytes = bytes;
        this.length = len;
        index = 0;
        type = readType();
        code = readCode();
    }

    //重置索引位置
    public void reset(){
        index = 0;
    }

    public int getPosition(){
        return index;
    }

    public void setPosition(int position){
        index = position;
    }

    public byte[] getBytes(){
        return bytes;
    }

    public int length(){
        return length;
    }

    /**
     * 从规定响应类型起始位处读取响应类型,参考{@link Protocol}
     * @return 响应类型编号
     */
    public int readType() {
        return  readInt(START_TYPE);
    }

    /**
     * 从规定响应类型起始位处读取响应码,参考{@link Protocol}
     * @return 响应码
     */
    public int readCode() {
        return  readInt(START_RES_CODE);
    }

    /**
     * 从当前索引<value>index<value/>处读取下一个整型数
     * @return 读取到的数字
     */
    public int nextInt() {
        return readInt(index);
    }

    /**
     * 从当前索引<value>index<value/>处读取下一个整型数
     * @return 读取到的数字
     */
    public long nextLong() {
        return readLong(index);
    }

    /**
     * 从当前索引<value>index<value/>处读取一个长度为<value>len<value/>字节的数组
     * @return 读取到的字节数组
     */
    public byte[] nextBytes(int len) {
        try {
            return readBytes(index,len);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 从当前索引<value>index<value/>处读取一个长度为<value>len<value/>的字符串
     * @return 读取到字符串，在读取出错时返回空字符串""
     */
    public String nextString(int len) {
        try {
            return new String(readBytes(index,len));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 从数组的<value>start</value>位置起读取一个<type>int</type>型数据
     * @param start 读取起点
     * @return 读取的数据
     */
    public int readInt(int start) {
        byte[] bytes = new byte[0];
        try {
            bytes = readBytes(start, INT_BYTES);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        int value = 0;
        for (int i = 0; i < INT_BYTES; i++) {
            value |= (bytes[i] & 0xFF) << ((INT_BYTES - i - 1) * 8 ) ;
        }
        return value;
    }

    /**
     * 从数组的<value>start</value>位置起读取一个<type>float</type>型数据
     * @param start 读取起点
     * @return 读取的数据
     */
    public float readFloat(int start) {
        byte[] bytes = new byte[0];
        try {
            bytes = readBytes(start, INT_BYTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        BigInteger floatBits = new BigInteger(bytes);

        return floatBits.floatValue();
    }

    /**
     * 从数组的<value>start</value>位置起读取一个<type>long</type>型数据
     * @param start 读取起点
     * @return 读取的数据
     */
    public long readLong(int start) {
        byte[] bytes = new byte[0];
        try {
            bytes = readBytes(start, LONG_BYTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new BigInteger(bytes).longValue();
    }

    /**
     * 从数组的<value>start</value>位置起读取一个<type>double</type>型数据
     * @param start 读取起点
     * @return 读取的数据
     */
    public double readDouble(int start) {
        byte[] bytes = new byte[0];
        try {
            bytes = readBytes(start, LONG_BYTES);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BigInteger(bytes).doubleValue();
    }

    /**
     * 从数组的<value>start</value>位置拷贝长度为<value>len</value>的byte数组
     * 索引位置会向后偏移<value>len<value/>个字节
     * @param start 复制起点
     * @param len 复制长度
     * @return 复制得到的数组
     */
    public byte[] readBytes(int start, int len) throws Exception{
        if (bytes == null) {
            throw new NullPointerException("bytes is null");
        }
        if(len <= 0){
            throw new IndexOutOfBoundsException("read len <= 0");
        }
        byte[] tempBytes = new byte[len];
        System.arraycopy(bytes, start, tempBytes, 0, len);
        index += len;
        return tempBytes;
    }

    /**
     * 从数组的<value>start</value>位置拷贝长度为<value>len</value>的byte数组
     * 并使用默认字符集<string>utf-8</string>转换为相应的字符串
     * @param start 读取起始位置
     * @param strLen 读取长度
     * @return 读取到的字符串
     */
    public String readString(int start,int strLen) {
        return readString(start,strLen,"utf-8");
    }

    /**
     * 从数组的<value>start</value>位置拷贝长度为<value>len</value>的byte数组
     * 并将其根据输入字符集<value>charsetName<value/>转换为相应的字符串
     * @param start 读取起始位置
     * @param strLen 读取长度
     * @param charsetName 字符集名称
     * @return 读取到的字符串
     */
    public String readString(int start,int strLen, String charsetName) {
        byte[] bytes = new byte[0];
        try {
            bytes = readBytes(start,strLen);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        return Utils.byteArray2String(bytes,charsetName);
    }

    /**
     * 将长度为<value>len</value>数组拷贝到此<obj>message</obj>的当前索引位置
     * 索引位置会向后偏移<value>strLen<value/>个字节
     * 所有put数据的方法均为私有，由内部构造器{@link Builder}类对外实现实例化
     * @param bytes 被拷贝数组
     * @param len 拷贝长度
     */
    private void putBytes(byte[] bytes, int len) {
        if (bytes == null || len <= 0)
            throw new NullPointerException("Null Pointer error::bytes = null");
        try {
            System.arraycopy(bytes,0,this.bytes,index,len);
            index += len;
        } catch (Exception e) {
            throw new RuntimeException("Array copy error::" + e.getMessage());
        }
    }

    /**
     * 将<type>int<type/>型数转化为byte数组，并拷贝到此<obj>message</obj>的当前索引位置
     * @param value 被拷贝数组
     */
    private void putInt(int value) {

        byte[] bytes = new byte[INT_BYTES];
        for (int i = INT_BYTES - 1; i >= 0; i--) {
            bytes[i] = (byte) value;
            value >>>= 8;
        }

        putBytes(bytes, INT_BYTES);
    }

    /**
     * 将<type>float<type/>型数转化为byte数组，并拷贝到此<obj>message</obj>的当前索引位置
     * @param valueF 被拷贝数组
     */
    private void putFloat(float valueF) {
        putInt(Float.floatToIntBits(valueF));
    }

    /**
     * 将<type>long<type/>型数转化为byte数组，并拷贝到此<obj>message</obj>的当前索引位置
     * @param value 被拷贝数组
     */
    private void putLong(long value) {

        byte[] bytes = new byte[LONG_BYTES];
        for (int i = bytes.length - 1; i >= 0; i--) {
            bytes[i] = (byte) value;
            value >>>= 8;
        }
        putBytes(bytes, bytes.length);
    }

    /**
     * 将<type>double<type/>型数转化为byte数组，并拷贝到此<obj>message</obj>的当前索引位置
     * @param value 被拷贝数组
     */
    private void putDouble(double value) {
        putLong(Double.doubleToLongBits(value));
    }

    /**
     * 将<type>string<type/>字符串根据输入字符集型数转化为byte数组，并拷贝到此
     * <obj>message</obj>的当前索引位置
     * @param str 被拷贝数组
     */
    private void putString(String str) {
        putString(str,"utf-8");
    }

    /**
     * 将<type>string<type/>根据输入字符集型数转化为byte数组，并拷贝到此<obj>message</obj>
     * 的当前索引位置
     * @param str 输入字符串
     * @param charsetName 字符集名称
     */
    private void putString(String str,String charsetName) {
        try {
            byte[] bytes = str.getBytes(charsetName);
            putBytes(bytes, bytes.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将<type>int<type/>型数转化为byte数组，并拷贝到此<obj>message</obj>的当前索引位置
     * 索引位置会向后偏移<value>strLen<value/>个字节
     * @param value 存入的值
     */
    private void putByte(byte value) {
        bytes[index] = value;
        index++;
    }

    /**
     * 静态创建一个EasyMessage实例
     * @param type 请求/响应类型
     * @param code 响应码
     * @return EasyMessage实例
     */
    public static EasyMessage create(int type,int code){
        return new Builder()
                .setType(type)
                .setCode(code)
                .build();
    }

    @Override
    public String toString() {
        return Arrays.toString(bytes);
    }

    public static class Builder {

        public static final int ORDER_INT = 1;
        public static final int ORDER_BYTE = 2;
        public static final int ORDER_BYTES = 3;
        public static final int ORDER_STRING = 4;
        public static final int ORDER_LONG = 5;
        public static final int ORDER_LENGTH = 6;


        private List<Integer>   mIntegerList = null;
        private List<Byte>      mByteList = null;
        private List<byte[]>    mByteArrayList = null;
        private List<String>    mStringList = null;
        private List<Long>      mLongList = null;
        private List<Integer>   mLengthList = null;
        //数据的写入顺序
        private List<Integer> mOrderList = null;

        private int dataLen;
        private int packLen;
        //协议版本
        private int version;
        //数据包类型
        private int packType;
        //响应码
        private int packCode;

        private EasyMessage message = null;


        public Builder(){
            mIntegerList = new ArrayList<>();
            mByteList = new ArrayList<>();
            mByteArrayList = new ArrayList<>();
            mStringList = new ArrayList<>();
            mOrderList = new ArrayList<>();
            mLongList = new ArrayList<>();
            mLengthList = new ArrayList<>();

        }

        public Builder setVersion(int version){
            this.version = version;
            return this;
        }

        public Builder setType(int packType){
            packLen += INT_BYTES;
            this.packType = packType;
            return this;
        }

        public Builder setCode(int code){
            packLen += INT_BYTES;
            this.packCode = code;
            return this;
        }

        public EasyMessage build(){

            byte[] messageBytes = new byte[packLen];
            message = new EasyMessage(messageBytes,packLen);
            message.reset();
            message.putInt(packType);
            message.putInt(packCode);

            //顺序写入message封装中
            for (Integer index: mOrderList){
                switch (index){
                    case ORDER_INT:
                        if(!mIntegerList.isEmpty()){
                            message.putInt(mIntegerList.get(0));
                            mIntegerList.remove(0);
                        }
                        break;
                    case ORDER_BYTE:
                        if(!mByteList.isEmpty()){
                            message.putByte(mByteList.get(0));
                            mByteList.remove(0);
                        }
                        break;
                    case ORDER_BYTES:
                        if(!mByteArrayList.isEmpty()){
                            byte[] bytes = mByteArrayList.get(0);
                            message.putBytes(bytes,bytes.length);
                            mByteArrayList.remove(0);
                        }
                        break;
                    case ORDER_STRING:
                        if(!mStringList.isEmpty()){
                            String str = mStringList.get(0);
                            message.putString(str);
                            mStringList.remove(0);
                        }
                        break;
                    case ORDER_LONG:
                        if(!mLongList.isEmpty()){
                            long data = mLongList.get(0);
                            message.putLong(data);
                            mLongList.remove(0);
                        }
                        break;
                    case ORDER_LENGTH:
                        if(!mLengthList.isEmpty()){
                            int data = mLengthList.get(0);
                            message.putInt(data);
                            mLengthList.remove(0);
                        }
                        break;
                    default:
                        break;
                }
            }

            //Log.i(getClass().getSimpleName(),"packLen = " + packLen);
            return message;
        }

        public Builder addData(int data){
            mIntegerList.add(data);
            mOrderList.add(ORDER_INT);

            packLen += INT_BYTES;
            return this;
        }

        public Builder addData(long data){
            mLongList.add(data);
            mOrderList.add(ORDER_LONG);

            packLen += LONG_BYTES;
            return this;
        }

        public Builder addData(byte data){
            mByteList.add(data);
            mOrderList.add(ORDER_BYTE);

            packLen ++;
            return this;
        }

        public Builder addData(byte[] bytes, int len){
            return addData(bytes,0,len);
        }

        public Builder addData(byte[] bytes, int offset, int len){
            byte[] dst = new byte[len];
            System.arraycopy(bytes,offset,dst,0,len);

            mByteArrayList.add(dst);
            mOrderList.add(ORDER_BYTES);

            packLen += len;
            return this;
        }

        public Builder addData(String str){
            addData(str,"utf-8");
            return this;
        }


        public Builder addData(String str, String charsetName){
            if(str == null) {
                dataLen = INT_BYTES;
                str = "";
            }

            try {
                dataLen = str.getBytes(charsetName).length;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                dataLen = INT_BYTES;
            }

            mLengthList.add(dataLen);
            mOrderList.add(ORDER_LENGTH);
            packLen += INT_BYTES;

            mStringList.add(str);
            mOrderList.add(ORDER_STRING);
            packLen += dataLen;

            return this;
        }

        /**
         * Testing
         * @param parcelable
         * @return
         */
        public Builder addParcelable(Parcelable parcelable){
            Parcel parcel = Parcel.obtain();
            parcelable.writeToParcel(parcel,0);
            byte[] array = new byte[parcel.dataSize()];
            parcel.readByteArray(array);
            parcel.recycle();
            addData(array,array.length);
            return this;
        }

    }
}
