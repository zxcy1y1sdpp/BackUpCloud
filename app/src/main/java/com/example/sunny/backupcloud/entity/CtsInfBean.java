package com.example.sunny.backupcloud.entity;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobObject;

/**
 * Created by Sunny on 2017/4/4.
 * Email：670453367@qq.com
 * Description: TOOD
 */

public class CtsInfBean extends BmobObject {

    private int data_id;
    private String name;
    private List<String> phone = new ArrayList<>();
    private List<String> email = new ArrayList<>();

    public int getData_id() {
        return data_id;
    }

    public void setData_id(int data_id) {
        this.data_id = data_id;
    }

    public List<String> getPhone() {
        return phone;
    }

    public void setPhone(List<String> phone) {
        this.phone = phone;
    }

    public List<String> getEmail() {
        return email;
    }

    public void setEmail(List<String> email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }




}
