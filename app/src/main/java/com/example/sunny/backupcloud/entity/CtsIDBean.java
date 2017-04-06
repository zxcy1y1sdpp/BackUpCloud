package com.example.sunny.backupcloud.entity;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.util.Log;

import com.example.sunny.backupcloud.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;

/**
 * Created by Sunny on 2017/4/4.
 * Email：670453367@qq.com
 * Description: TOOD
 */

public class CtsIDBean extends BmobObject {

    private int id_;
    private UpStateListener upStateListener;
    private Context mContext;

    public CtsIDBean() {
    }

    public CtsIDBean(Context context) {
        this.mContext = context;
    }

    public CtsIDBean(int id_) {
        this.id_ = id_;
    }

    public int getId_() {
        return id_;
    }

    public void setId_(int id_) {
        this.id_ = id_;
    }

    // 获取本地contacts表中所有联系人ID
    public List<CtsIDBean> queryCtsID() {
        List<CtsIDBean> CtsID = new ArrayList<>(); // 本地联系人ID
        ContentResolver cr = mContext.getContentResolver();
        String str[] = {ContactsContract.CommonDataKinds.Phone.CONTACT_ID};
        Cursor cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, str, null, null, null);
        if (cur != null) {
            while (cur.moveToNext()) {
                int ID = cur.getInt(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                CtsIDBean cts = new CtsIDBean(ID);
                CtsID.add(cts);
            }
        }

        return CtsID;
    }


    // 将未备份的联系人ID以及详细信息上传至服务器
    public void Up(List<CtsIDBean> CtsID) {

        List<BmobObject> data = new ArrayList<BmobObject>();
        for (int i = 0; i < CtsID.size(); i++) {
            data.add(CtsID.get(i));
        }
        new BmobBatch().insertBatch(data).doBatch(new QueryListListener<BatchResult>() {

            @Override
            public void done(List<BatchResult> o, BmobException e) {
                if (e == null) {
                    for (int i = 0; i < o.size(); i++) {
                        BatchResult result = o.get(i);
                        BmobException ex = result.getError();
                        if (ex == null) {
                            Log.d("upData", "第" + i + "个数据批量添加成功：" + result.getCreatedAt() + "," + result.getObjectId() + "," + result.getUpdatedAt());
                        } else {
                            Log.d("upData", "第" + i + "个数据批量添加失败：" + ex.getMessage() + "," + ex.getErrorCode());
                        }
                    }
                } else {
                    Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });

        // 根据ID查询详细信息
        List<CtsInfBean> CtsInf = queryCtsInf(CtsID);

        // 将本地有而云端无的联系人批量添加到服务器数据库
        List<BmobObject> data2 = new ArrayList<BmobObject>();
        for (int i = 0; i < CtsInf.size(); i++) {
            data2.add(CtsInf.get(i));
        }
        new BmobBatch().insertBatch(data2).doBatch(new QueryListListener<BatchResult>() {

            @Override
            public void done(List<BatchResult> o, BmobException e) {
                if (e == null) {
                    for (int i = 0; i < o.size(); i++) {
                        BatchResult result = o.get(i);
                        BmobException ex = result.getError();
                        if (ex == null) {
                            Log.d("upData", "第" + i + "个数据批量添加成功：" + result.getCreatedAt() + "," + result.getObjectId() + "," + result.getUpdatedAt());
                        } else {
                            Log.d("upData", "第" + i + "个数据批量添加失败：" + ex.getMessage() + "," + ex.getErrorCode());
                        }
                    }
                    upStateListener.UpOver();
                } else {
                    Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });

    }

    // 恢复联系人到本地
    public void DownLoad(String[] CtsIDCld) {
        List<BmobQuery<CtsInfBean>> queries = new ArrayList<BmobQuery<CtsInfBean>>();
        for (String aCtsIDCld : CtsIDCld) {
            BmobQuery<CtsInfBean> eq1 = new BmobQuery<CtsInfBean>();
            eq1.addWhereEqualTo("data_id", Integer.valueOf(aCtsIDCld));
            queries.add(eq1);
        }

        BmobQuery<CtsInfBean> mainQuery = new BmobQuery<CtsInfBean>();
        mainQuery.or(queries);
        mainQuery.findObjects(new FindListener<CtsInfBean>() {
            @Override
            public void done(List<CtsInfBean> object, BmobException e) {
                if (e == null) {
                    for (int i = 0; i < object.size(); i++) {

                        ContentValues values = new ContentValues();

                        Uri rawContactUri = mContext.getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
                        long rawContactId = ContentUris.parseId(rawContactUri);

                        values.clear();

                        values.put(Data.RAW_CONTACT_ID, rawContactId);
                        values.put(Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
                        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, object.get(i).getName());
                        mContext.getContentResolver().insert(Data.CONTENT_URI, values);
                        values.clear();

                        for (int j = 0; j < object.get(i).getPhone().size(); j++) {
                            values.put(Data.RAW_CONTACT_ID, rawContactId);
                            values.put(Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, object.get(i).getPhone().get(j));
                            values.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                            mContext.getContentResolver().insert(Data.CONTENT_URI, values);
                            values.clear();
                        }

                        for (int k = 0; k < object.get(i).getEmail().size(); k++) {
                            values.put(Data.RAW_CONTACT_ID, rawContactId);
                            values.put(Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
                            values.put(ContactsContract.CommonDataKinds.Email.DATA, object.get(i).getEmail().get(k));
                            values.put(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
                            mContext.getContentResolver().insert(Data.CONTENT_URI, values);
                        }

                    }
                    Utils.CustomToast(mContext, "恢复成功");
                    // 此处暂时存在BUG，从云端恢复到本地的联系人ID变成系统给的新ID，与服务器存储的ID不同
                } else {
                    Log.i("bmob", "失败：" + e.getMessage() + "," + e.getErrorCode());
                }
            }
        });
    }

    // 获取本地联系人详情信息
    private List<CtsInfBean> queryCtsInf(List<CtsIDBean> CtsID) {

        List<CtsInfBean> CtsInf = new ArrayList<>();

        ContentResolver resolver = mContext.getContentResolver();
        for (int i = 0; i < CtsID.size(); i++) {
            CtsInfBean data = new CtsInfBean();
            List<String> phone = new ArrayList<>();
            List<String> email = new ArrayList<>();

            data.setData_id(CtsID.get(i).getId_());

            //从一个Cursor获取所有的信息
            Cursor contactInfoCursor = resolver.query(
                    android.provider.ContactsContract.Data.CONTENT_URI,
                    new String[]{android.provider.ContactsContract.Data.CONTACT_ID,
                            android.provider.ContactsContract.Data.MIMETYPE,
                            android.provider.ContactsContract.Data.DATA1
                    }, android.provider.ContactsContract.Data.CONTACT_ID + "=" + CtsID.get(i).getId_(), null, null);

            while (contactInfoCursor.moveToNext()) {
                String mimeType = contactInfoCursor.getString(
                        contactInfoCursor.getColumnIndex(android.provider.ContactsContract.Data.MIMETYPE));
                String value = contactInfoCursor.getString(
                        contactInfoCursor.getColumnIndex(android.provider.ContactsContract.Data.DATA1));
                if (mimeType.contains("/name")) {
                    data.setName(value);
                } else if (mimeType.contains("/email")) {
                    email.add(value);
                } else if (mimeType.contains("/phone")) {
                    phone.add(value);
                }
            }
            data.setEmail(email);
            data.setPhone(phone);
            CtsInf.add(data);
            contactInfoCursor.close();
        }

        return CtsInf;
    }

    public void setUpStateListener(UpStateListener upStateListener) {
        this.upStateListener = upStateListener;
    }

    //上传结束事件回调
    public interface UpStateListener {
        void UpOver();
    }

}
