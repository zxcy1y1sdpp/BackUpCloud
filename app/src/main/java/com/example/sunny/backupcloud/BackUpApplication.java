package com.example.sunny.backupcloud;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.sunny.backupcloud.entity.CtsIDBean;

import java.util.List;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;

/**
 * Created by Sunny on 2017/4/4.
 * Email：670453367@qq.com
 * Description: BackUpApplication
 */

public class BackUpApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Bmob.initialize(this, "0226c5b3cf253a60fea315e5d19899b9");

        queryCtsIDCld();
    }

    // 查询服务器存储的联系人ID
    public void queryCtsIDCld() {

        BmobQuery<CtsIDBean> query = new BmobQuery<CtsIDBean>();
        query.findObjects(new FindListener<CtsIDBean>() {
            @Override
            public void done(List<CtsIDBean> object, BmobException e) {
                if (e == null) {
                    Log.i("bmob", "查询成功：共" + object.size() + "条数据。");
                    StringBuilder stringBuilder = new StringBuilder();
                    if (object.size() == 1) {
                        stringBuilder.append(object.get(0).getId_());
                    } else {
                        for (CtsIDBean contacts : object) {
                            stringBuilder.append(contacts.getId_());
                            stringBuilder.append("_");
                        }
                    }

                    SharedPreferences sp = getSharedPreferences("BackUpSP", Context.MODE_PRIVATE);
                    sp.edit().putString("CtsIDCld_sp", stringBuilder.toString()).apply();
                } else {
                    Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

}
