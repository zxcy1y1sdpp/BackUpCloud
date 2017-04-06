package com.example.sunny.backupcloud;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.ColorRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.sunny.backupcloud.entity.CtsIDBean;
import com.example.sunny.backupcloud.utils.StatusBarUtil;
import com.example.sunny.backupcloud.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, CtsIDBean.UpStateListener {

    private ImageView iv_main_backup;
    private ImageView iv_main_backup_mid;
    private ImageView iv_main_backup_oval;
    private Button nav_header_bt_close;
    private List<CtsIDBean> CtsID = new ArrayList<>(); // 本地联系人ID
    private boolean CanClick = true;
    private RelativeLayout rl_user_contact;
    private RelativeLayout rl_user_sync;

    private CtsIDBean cts;

    AnimatorSet setMid;
    AnimatorSet setOval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cts = new CtsIDBean(MainActivity.this);
        initView();
        // 按钮动画
        setMid = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, R.animator.anim_btn_sync_mid);
        setOval = (AnimatorSet) AnimatorInflater.loadAnimator(MainActivity.this, R.animator.anim_btn_sync_oval);
    }

    /**
     * 1.在Application中请求服务器数据，得到服务器存储的联系人ID，拼接字符串存储到SharedPreferences
     * 2.通过ContentResolver查询，将本地联系人ID存储到List中
     * 3.对上面的数据进行遍历，当发现第一步中查询的ID也在第二步的数据中存在的时候，删除第二步查询的此数据
     * 4.将第二步查询的ID上传至服务器
     */
    private void choseUp() {

        CanClick = false;
        cts.setUpStateListener(this);
        CtsID = cts.queryCtsID();  //本地联系人ID
        SharedPreferences sp = getSharedPreferences("BackUpSP", Context.MODE_PRIVATE);
        String CtsIDCld_sp = sp.getString("CtsIDCld_sp", "");  //云端联系人ID
        String[] CtsIDCld = CtsIDCld_sp.split("_");

        if (CtsIDCld.length > 0 && CtsIDCld[0] != "") {
            for (String aCtsIDCld : CtsIDCld) {
                for (int j = 0; j < CtsID.size(); j++) {
                    if (CtsID.get(j).getId_() == Integer.valueOf(aCtsIDCld)) {
                        CtsID.remove(j);
                    }
                }
            }
        }

        if (CtsID.size() > 0) {
            SyncBtAnimStart();
            cts.Up(CtsID); //上传至服务器
        } else {
            Toast.makeText(MainActivity.this, "无新联系人需要备份", Toast.LENGTH_SHORT).show();
            CanClick = true;
        }

    }

    /**
     * 1.在Application中请求服务器数据，得到服务器存储的联系人ID，拼接字符串存储到SharedPreferences
     * 2.通过ContentResolver查询，将本地联系人ID存储到List中
     * 3.对上面的数据进行遍历，当发现第一步中查询的ID也在第二步的数据中存在的时候，删除第二步查询的此数据
     * 4.根据第二步查询的ID从服务器下载相应数据，添加到本地联系人中
     */
    private void choseDown() {

        SharedPreferences sp = getSharedPreferences("BackUpSP", Context.MODE_PRIVATE);
        String CtsIDCld_sp = sp.getString("CtsIDCld_sp", "");  //云端联系人ID
        String[] CtsIDCld = CtsIDCld_sp.split("_");

        CtsID = cts.queryCtsID();  //本地联系人ID
        if (CtsIDCld.length > 0 && CtsIDCld[0] != "") {
            for (int j = 0; j < CtsID.size(); j++) {
                for (int i = 0; i < CtsIDCld.length; i++) {
                    Log.e("equals", CtsID.get(j).getId_() + " " + Integer.valueOf(CtsIDCld[i]));
                    if (CtsID.get(j).getId_() == Integer.valueOf(CtsIDCld[i])) {

                        CtsIDCld[i] = CtsIDCld[CtsIDCld.length - 1];
                        CtsIDCld = Arrays.copyOf(CtsIDCld, CtsIDCld.length - 1);
                    }
                }
            }
        }

        if (CtsIDCld.length == 0) {
            Utils.CustomToast(MainActivity.this, "无需恢复");
        } else {
            cts.DownLoad(CtsIDCld);
        }

    }

    private void initView() {
        // 修改状态栏颜色
        changeStatusBarColor(R.color.colorBlue);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        getSupportActionBar().setElevation(0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            toolbar.setElevation(0);
        }

        iv_main_backup = (ImageView) findViewById(R.id.iv_main_backup);
        iv_main_backup.setOnClickListener(this);
        iv_main_backup_mid = (ImageView) findViewById(R.id.iv_main_backup_mid);
        iv_main_backup_oval = (ImageView) findViewById(R.id.iv_main_backup_oval);
        NavigationView nav_view = (NavigationView) findViewById(R.id.nav_view);
        // 控制侧滑栏占满整个屏幕
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        int width = metric.widthPixels;     // 屏幕宽度（像素）
        ViewGroup.LayoutParams params = nav_view.getLayoutParams();
        params.width = width;
        nav_view.setLayoutParams(params);
        nav_header_bt_close = (Button) findViewById(R.id.nav_header_bt_close);
        nav_header_bt_close.setOnClickListener(this);
        rl_user_contact = (RelativeLayout) findViewById(R.id.rl_user_contact);
        rl_user_contact.setOnClickListener(this);
        rl_user_sync = (RelativeLayout) findViewById(R.id.rl_user_sync);
        rl_user_sync.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_main_backup:
                if (CanClick) {
                    choseUp(); //上传本地有而云端无的联系人ID
                    BpCallMsg bpCallMsg = new BpCallMsg(MainActivity.this);
                    bpCallMsg.Up();
                }
                break;
            case R.id.nav_header_bt_close:
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.rl_user_contact:
                choseDown();
                break;
            case R.id.rl_user_sync:
                BpCallMsg bpCallMsg = new BpCallMsg(MainActivity.this);
                bpCallMsg.down();
                break;
        }
    }

    private void SyncBtAnimStart() {
        iv_main_backup_mid.setImageResource(R.drawable.btn_sync_02);
        setMid.setTarget(iv_main_backup_mid);
        setMid.start();
        setOval.setInterpolator(new LinearInterpolator());
        setOval.setTarget(iv_main_backup_oval);
        iv_main_backup_oval.setVisibility(View.VISIBLE);
        setOval.start();
    }

    private void SyncBtAnimOver() {
        iv_main_backup_mid.setImageResource(R.drawable.btn_sync_01);
        iv_main_backup_oval.setVisibility(View.GONE);
        setMid.end();
        setOval.end();
    }

    // 改变状态栏颜色
    public void changeStatusBarColor(@ColorRes int color) {
        StatusBarUtil.setStatusBarColor(this, color);
    }

    @Override
    public void UpOver() {

        // 防止上传太快，动画无法展示完全，使用handler延迟一秒结束动画
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0x01:
                        SyncBtAnimOver();
                        CanClick = true;
                        break;
                }
            }
        };

        new Thread() {
            public void run() {
                try {
                    Thread.sleep(500);
                    Message msg = Message.obtain();
                    msg.what = 0x01;
                    handler.sendMessage(msg);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
