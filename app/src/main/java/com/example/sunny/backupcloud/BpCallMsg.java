package com.example.sunny.backupcloud;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;

import com.example.sunny.backupcloud.entity.CallBean;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.datatype.BmobQueryResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.SQLQueryListener;

/**
 * Created by Sunny on 2017/4/6.
 * Email：670453367@qq.com
 * Description: 备份通话记录和短信
 */

class BpCallMsg {

    private Context mContext;

    public BpCallMsg() {
    }

    BpCallMsg(Context mContext) {
        this.mContext = mContext;

    }

    public void Up() {
        List<CallBean> callBeans = queryCalls();
        UpCallBeans(callBeans);
    }

    public void down() {

    }

    public List<CallBean> queryCalls() {

        List<CallBean> callBeans = new ArrayList<>();
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DEFAULT_SORT_ORDER);

        while (cursor.moveToNext()) {
            CallBean callBean = new CallBean();
            callBean.setType(cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)));//获取通话类型：1.呼入2.呼出3.未接
            callBean.setName(cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
            callBean.setNumber(cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));
            callBean.setDate(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE)));
            callBean.setDuration(cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION)));
            callBeans.add(callBean);
        }
        return callBeans;
    }

    private void UpCallBeans(final List<CallBean> callBeans) {
        List<BmobObject> object = new ArrayList<BmobObject>();
        for (int i = 0; i < callBeans.size(); i++) {
            object.add(callBeans.get(i));
        }

        // 上传之前先删除云端数据库所有数据
        String bql = "select * from CallBean";
        new BmobQuery<CallBean>().doSQLQuery(bql, new SQLQueryListener<CallBean>() {
            List<String> deleteIDs = new ArrayList<String>();
            @Override
            public void done(BmobQueryResult<CallBean> result, BmobException e) {
                if (e == null) {
                    List<CallBean> list = (List<CallBean>) result.getResults();
                    if (list != null && list.size() > 0) {
                        for (int i = 0; i < list.size(); i++) {
                            deleteIDs.add(list.get(i).getObjectId());
                        }
                        deleteAll(deleteIDs, callBeans);
                    } else {
                        Log.i("smile", "查询成功，无数据返回");
                    }
                } else {
                    Log.i("smile", "错误码：" + e.getErrorCode() + "，错误描述：" + e.getMessage());
                }
            }
        });

        new BmobBatch().insertBatch(object).doBatch(new QueryListListener<BatchResult>() {

            @Override
            public void done(List<BatchResult> o, BmobException e) {
                if (e == null) {
                    for (int i = 0; i < o.size(); i++) {
                        BatchResult result = o.get(i);
                        BmobException ex = result.getError();
                        if (ex == null) {
                            Log.i("UpCallBeans", "第" + i + "个数据批量添加成功：" + result.getCreatedAt() + "," + result.getObjectId() + "," + result.getUpdatedAt());
                        } else {
                            Log.i("UpCallBeans", "第" + i + "个数据批量添加失败：" + ex.getMessage() + "," + ex.getErrorCode());
                        }
                    }
                } else {
                    Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    // 删除云端所有数据
    private void deleteAll(List<String> deleteIDs, List<CallBean> callBeans) {
        for (int i = 0; i < deleteIDs.size(); i++) {
            Log.i("DeleteID", deleteIDs.get(i));
        }
    }

}
