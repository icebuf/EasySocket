package com.skyworth.led.easyclient.bean;

import android.app.Activity;

/**
 * 作者：Ice Nation
 * 日期：2018/11/6 17:52
 * 邮箱：tangjie@skyworth.com
 */
public class TestCase {

    public String name;

    public String detail;

    public Class<? extends Activity> activity;

    public TestCase(String name, String detail, Class<? extends Activity> activityClass) {
        this.name = name;
        this.detail = detail;
        this.activity = activityClass;
    }
}
