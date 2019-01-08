package com.yoyo.smtpms;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bin.david.form.data.table.TableData;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.yoyo.smtpms.antistatic.spinnerwheel.AbstractWheel;
import com.yoyo.smtpms.antistatic.spinnerwheel.OnWheelChangedListener;
import com.yoyo.smtpms.antistatic.spinnerwheel.adapters.ArrayWheelAdapter;
import com.yoyo.smtpms.entity.MainEntity;
import com.yoyo.smtpms.util.*;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    final static String PRODUCTION_SCHEDULE_PATH = "/SMTPMS";
    final static String PRODUCTION_SCHEDULE_FILE = "/精密线体计划排产表.XLS";
    final static String PRODUCTION_SCHEDULE_SEVER_PATH = "/day27/精密线体计划排产";
    private MySmartTable<MainEntity> table;

    public boolean isRunning = true;
    private int lineNumber = 0;
    private AbstractWheel spLineNumber;
    private TextView tvLineNumber;
    private TableData<MainEntity> tableData;

    private TextView tvIpValue;
    private TextView tvNameValue;
    private TextView tvAdmin;

    private TextView tvLoadingMessage;

    private SwipeRefreshLayout swipeRefreshLayout;
    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

    private static String[] strLineNumber = {
            "T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10",
            "T11", "T12", "T13", "T14", "T15", "T16", "T17", "T18", "T19", "T20",
    };

    private boolean isAdmin = false;
    private SwipeRefreshLayout.OnRefreshListener onRefreshListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BaseUtil.setFullSreen(this);
        lineNumber = SPUtil.getInt(this, "lineNumber", 0);
        tvLineNumber = (TextView) findViewById(R.id.tv_line_number);
        tvLineNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isAdmin || SPUtil.getString(MainActivity.this, "name", "007").equals("AAA")) {
                    showDialogPlus();
                }
            }
        });
        tvAdmin = findViewById(R.id.tvAdmin);
        tvAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder normalDialog = new AlertDialog.Builder(MainActivity.this);
                normalDialog.setTitle("提示");
                normalDialog.setMessage("确定要退出管理员模式吗？");
                normalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        isAdmin = false;
                        tvAdmin.setVisibility(View.GONE);
                    }
                });
                normalDialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                normalDialog.show();
            }
        });
        tvLineNumber.setText("T" + (lineNumber + 1));
        tvLoadingMessage = findViewById(R.id.tv_loading_message);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorPrimary);
        onRefreshListener = new SwipeRefreshLayout.OnRefreshListener(){

            @Override
            public void onRefresh() {
                downloadMainExcel("http://" + SPUtil.getString(MainActivity.this, "severIP", "192.168.1.1") + ":8080" + PRODUCTION_SCHEDULE_SEVER_PATH + PRODUCTION_SCHEDULE_FILE, true);
            }
        };
        swipeRefreshLayout.setOnRefreshListener(onRefreshListener);
        table = findViewById(R.id.table);
        //申请读写权限
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            } else {
                if (IOUtils.fileIsExist(Environment.getExternalStorageDirectory().getPath() + PRODUCTION_SCHEDULE_PATH + PRODUCTION_SCHEDULE_FILE)) {
                    table.setData(ExcelUtil.parseExcel(lineNumber));
                } else {
                    downloadMainExcel("http://" + SPUtil.getString(MainActivity.this, "severIP", "192.168.1.1") + ":8080" + PRODUCTION_SCHEDULE_SEVER_PATH + PRODUCTION_SCHEDULE_FILE, false);
                }
            }
        } else {
            if (IOUtils.fileIsExist(Environment.getExternalStorageDirectory().getPath() + PRODUCTION_SCHEDULE_PATH + PRODUCTION_SCHEDULE_FILE)) {
                table.setData(ExcelUtil.parseExcel(lineNumber));
            } else {
                downloadMainExcel("http://" + SPUtil.getString(MainActivity.this, "severIP", "192.168.1.1") + ":8080" + PRODUCTION_SCHEDULE_SEVER_PATH + PRODUCTION_SCHEDULE_FILE, false);
            }
        }
        initSlidingMenu();
        new MyThread().start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }

            if (isAllGranted) {
                table.setData(ExcelUtil.parseExcel(lineNumber));
            }
        }
    }


    private void showDialogPlus() {
        final DialogPlus dialogPlus = new DialogPlus.Builder(this)
                .setContentHolder(new ViewHolder(R.layout.content))
                .setFooter(R.layout.footer)
                .setHeader(R.layout.header)
                .create();
        View holderView = dialogPlus.getHolderView();
        spLineNumber = (AbstractWheel) holderView.findViewById(R.id.line_number);
        ArrayWheelAdapter<String> adapter = new ArrayWheelAdapter<String>(this, strLineNumber);
        adapter.setTextSize(18);
        spLineNumber.setViewAdapter(adapter);
        spLineNumber.setCurrentItem(lineNumber);
        spLineNumber.addChangingListener(new OnWheelChangedListener() {
            @Override
            public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {

            }
        });
        View footerView = dialogPlus.getFooterView();
        Button closeBtn = footerView.findViewById(R.id.footer_close_button);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogPlus.dismiss();
            }
        });
        Button confirmBtn = footerView.findViewById(R.id.footer_confirm_button);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lineNumber = spLineNumber.getCurrentItem();
                SPUtil.saveInt(MainActivity.this, "lineNumber", lineNumber);
                tvLineNumber.setText("T" + (lineNumber + 1));
                table.setData(ExcelUtil.parseExcel(lineNumber));
                dialogPlus.dismiss();
            }
        });
        dialogPlus.show();
    }

    private void initSlidingMenu() {
        SlidingMenu slidingMenu = new SlidingMenu(this);
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setBehindWidth(500);
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
        slidingMenu.setMenu(R.layout.left_menu);
        View view = slidingMenu.getRootView();
        tvIpValue = view.findViewById(R.id.tv_ipValue);
        tvNameValue = view.findViewById(R.id.tv_nameValue);
        tvIpValue.setText(SPUtil.getString(MainActivity.this, "severIP", "192.168.1.1"));
        tvNameValue = view.findViewById(R.id.tv_nameValue);
        tvNameValue.setText(SPUtil.getString(MainActivity.this, "name", "007"));
        Button btnName = view.findViewById(R.id.btn_name);
        btnName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNameDialog();
            }
        });
        Button btnIp = view.findViewById(R.id.btn_ip);
        btnIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    showIpDialog();
            }
        });
        Button btnRecord = view.findViewById(R.id.btn_record);
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,RecordActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showNameDialog() {
        final EditText etUsername = new EditText(MainActivity.this);
        final EditText etPassword = new EditText(MainActivity.this);
        etPassword.setVisibility(View.GONE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        etUsername.setLayoutParams(layoutParams);
        etPassword.setLayoutParams(layoutParams);
        RelativeLayout relativeLayout = new RelativeLayout(MainActivity.this);
        relativeLayout.addView(etUsername);
        relativeLayout.addView(etPassword);
        String str = tvNameValue.getText().toString();
        etUsername.setText(str);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this).setTitle("请输入用户名：").setView(relativeLayout)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userName = etUsername.getText().toString().trim();

                if (!userName.isEmpty()) {
                    if("admin".equals(userName)){
                        alertDialog.setTitle("请输入密码");
                        etUsername.setVisibility(View.GONE);
                        if(etPassword.getVisibility() == View.VISIBLE){
                            String password = etPassword.getText().toString().trim();
                            if("9527".equals(password)){
                                isAdmin = true;
                                tvAdmin.setVisibility(View.VISIBLE);
                                alertDialog.dismiss();
                            }else{
                                alertDialog.setTitle("密码错误，请重新输入");
                            }

                        }else {
                            etPassword.setVisibility(View.VISIBLE);
                        }

                    }else{
                        tvNameValue.setText(userName);
                        SPUtil.saveString(MainActivity.this, "name", userName);
                        alertDialog.dismiss();
                    }


                } else {
                    Toast.makeText(MainActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void showIpDialog() {
        final EditText editText = new EditText(MainActivity.this);
        String str = tvIpValue.getText().toString();
        editText.setText(str);
        new AlertDialog.Builder(MainActivity.this).setTitle("设置服务器IP：").setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!editText.getText().toString().isEmpty()) {
                            if (PatternUtil.checkIp(editText.getText().toString())) {
                                tvIpValue.setText(editText.getText().toString().trim()
                                        .toUpperCase());
                                SPUtil.saveString(MainActivity.this, "severIP", tvIpValue.getText().toString().trim());
                            } else {
                                Toast.makeText(MainActivity.this, "输入IP格式不正确，请重新输入", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private void downloadMainExcel(String url, final boolean isSwipeRefresh) {

        RequestParams params = new RequestParams(url);
//自定义保存路径，Environment.getExternalStorageDirectory()：SD卡的根目录
        params.setSaveFilePath(Environment.getExternalStorageDirectory() + "/SMTPMS/");
//自动为文件命名
        params.setAutoRename(true);
        x.http().post(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {
                if (!isSwipeRefresh) {
                    tvLoadingMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(File result) {
                IOUtils.copyFile(result.getAbsolutePath(), Environment.getExternalStorageDirectory().getPath() + PRODUCTION_SCHEDULE_PATH + PRODUCTION_SCHEDULE_FILE);
                System.out.println(result.getAbsolutePath());
                result.delete();
                if (isSwipeRefresh) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    tvLoadingMessage.setVisibility(View.INVISIBLE);
                }
                table.setData(ExcelUtil.parseExcel(lineNumber));
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                if (isSwipeRefresh) {
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    tvLoadingMessage.setText("加载数据失败，请下拉刷新！");
                }
                Toast.makeText(MainActivity.this, "加载数据失败，请检查网络！", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
            }
        });
    }

    public int getLineNumber() {
        return lineNumber;
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            while (true) {
                if (isRunning) {
                    String url = "http://"
                            + SPUtil.getString(MainActivity.this, "severIP", "192.168.1.1")
                            + ":8080/day27/FileServlet?method=checkExcelUpdate";
                    RequestParams params = new RequestParams(url);
                    params.addBodyParameter("client", SPUtil.getString(MainActivity.this, "name", "007"));
                    x.http().get(params, new Callback.ProgressCallback<String>() {
                        @Override
                        public void onSuccess(String result) {
                            JSONObject jsonObject = JSON.parseObject(result);
                            String excelUpdate = jsonObject.getString("excelUpdate");
                            if ("Ture".equals(excelUpdate)) {
                                isRunning = false;
                                TextView textView = new TextView(MainActivity.this);
                                textView.setText("精密线体计划排产表已更新，请及时下来更新");
                                new AlertDialog.Builder(MainActivity.this).setTitle("提示：").setView(textView)
                                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Field field = null;
                                                try {
                                                    //通过反射设置对话框消失
                                                    field = dialog.getClass().getSuperclass()
                                                            .getSuperclass()
                                                            .getDeclaredField("mShowing");
                                                    field.setAccessible(true);
                                                    field.set(dialog, true);
                                                    swipeRefreshLayout.setRefreshing(true);
                                                    onRefreshListener.onRefresh();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                                isRunning = true;
                                            }
                                        }).show();
                            }

                        }

                        @Override
                        public void onError(Throwable ex, boolean isOnCallback) {
                            System.out.println(ex.toString());
                        }

                        @Override
                        public void onCancelled(CancelledException cex) {

                        }

                        @Override
                        public void onFinished() {

                        }

                        @Override
                        public void onWaiting() {

                        }

                        @Override
                        public void onStarted() {

                        }

                        @Override
                        public void onLoading(long total, long current, boolean isDownloading) {

                        }
                    });

                }

                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}

