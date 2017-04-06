package com.example.sunny.backupcloud.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Sunny on 2017/4/6.
 * Email：670453367@qq.com
 * Description: 工具类
 */

public class Utils {


    public static void CustomToast(Context mContext, String msg) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

}
