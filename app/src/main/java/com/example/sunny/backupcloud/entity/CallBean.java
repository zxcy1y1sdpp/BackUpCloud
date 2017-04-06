package com.example.sunny.backupcloud.entity;

import cn.bmob.v3.BmobObject;

/**
 * Created by Sunny on 2017/4/6.
 * Email：670453367@qq.com
 * Description: 通话记录实体类
 */

public class CallBean extends BmobObject{

    private int type;
    private String name;
    private String number;
    private String date;
    private String duration;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
