package com.yoyo.smtpms;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.AttributeSet;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.bin.david.form.core.SmartTable;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.format.bg.BaseCellBackgroundFormat;
import com.bin.david.form.data.format.draw.MultiLineDrawFormat;
import com.bin.david.form.data.style.FontStyle;
import com.bin.david.form.data.table.PageTableData;
import com.bin.david.form.data.table.TableData;
import com.yoyo.smtpms.entity.MainEntity;
import com.yoyo.smtpms.entity.RecordEntity;
import com.yoyo.smtpms.util.DateUtils;
import com.yoyo.smtpms.util.ExcelUtil;
import com.yoyo.smtpms.util.JsonHelper;
import com.yoyo.smtpms.util.SPUtil;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 * @date 2018-10-14
 */

public class MySmartTable<T> extends SmartTable {

    private Context context;
    private int preRow = -1;

    public MySmartTable(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MySmartTable(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MySmartTable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init() {
//        this.getConfig().setContentCellBackgroundFormat(new BaseCellBackgroundFormat<CellInfo>() {
//            @Override
//            public int getBackGroundColor(CellInfo cellInfo) {
//                if (cellInfo.row % 2 == 0) {
//                    return ContextCompat.getColor(context, R.color.content_bg);
//                }
//                return 0;
//            }
//        });
        this.getConfig().setShowTableTitle(false);
        this.getConfig().setShowXSequence(false);
        this.getConfig().setShowYSequence(false);
        // FontStyle.setDefaultTextAlign(Paint.Align.LEFT);

    }

    @Override
    public PageTableData setData(List data) {
        TableData tableData = super.setData(data);
        if (tableData != null) {
            final List<MainEntity> t = tableData.getT();
            tableData.setOnItemClickListener(new TableData.OnItemClickListener() {
                @Override
                public void onClick(final Column column, String value, Object o, int col, final int row) {
                    if (column.getColumnName().equals("批次代码")) {
                        if (preRow == row) {
                            return;
                        }
                        if (preRow != -1) {
                            if (t.get(preRow).getCumulativeProduction() > t.get(preRow).getPlanned()) {
                                t.get(preRow).setStatusA("已完成");
                            } else {
                                t.get(preRow).setStatusA("未完成");
                            }
                        }
                        preRow = row;
                        if (t.get(row).getCumulativeProduction() < t.get(row).getPlanned()) {
                            t.get(row).setStatusA("正在生产");
                        }
                        if(!SPUtil.getString(context, "name", "007").equals("AAA")) {
                            updateToServer(t.get(row));
                            if(context instanceof MainActivity){
                                ((MainActivity) context).setMainEntity(t.get(row));
                            }
                        }
                        getConfig().setContentCellBackgroundFormat(new BaseCellBackgroundFormat<CellInfo>() {
                            @Override
                            public int getBackGroundColor(CellInfo cellInfo) {
                                if (cellInfo.row == row) {
                                    return ContextCompat.getColor(context, R.color.colorGreen);
                                }
                                return 0;
                            }
                        });
                        invalidate();
                    } else if (column.getColumnName().equals("当日产量")) {
                        if (preRow != row) {
                            return;
                        }
                        if(context instanceof MainActivity){
                            if(((MainActivity) context).showForceRefreshDialog())
                            {
                                return;
                            }
                        }
                        final EditText editText = new EditText(context);
                        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                        editText.setFocusable(true);
                        AlertDialog dialog = new AlertDialog.Builder(context)
                                //设置对话框的标题
                                .setTitle("请输入当日产量")
                                .setView(editText)
                                //设置对话框的按钮
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String text = editText.getText().toString().trim();
                                        if (text == null || text.equals("") || text.equals("0")) {
                                            Toast.makeText(context, "输入不能为空或为0", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        int integer = Integer.parseInt(text);
                                        MainEntity mainEntity = t.get(row);
                                        mainEntity.setOnDayProduction(integer);
                                        mainEntity.setCumulativeProduction(mainEntity.getCumulativeProduction() + integer);
                                        if (mainEntity.getCumulativeProduction() >= (mainEntity.getPlanned() + mainEntity.getRepairableSpares())) {
                                            mainEntity.setStatusA("已完成");
                                            t.remove(0);
                                            t.remove(mainEntity);
                                            t.add(0, mainEntity);
                                        }
                                        if(!SPUtil.getString(context, "name", "007").equals("AAA")) {
                                            ExcelUtil.updateMainExcel(((MainActivity) context).getLineNumber(), mainEntity);
                                            notifyDataChanged();
                                            //uploadFile("精密线体计划排产表.XLS");
                                            //updateMainExcelToServer(mainEntity);
                                            //updateToServer(mainEntity);
                                            RecordEntity recordEntity = new RecordEntity();
                                            recordEntity.setLineNumber("T"+(SPUtil.getInt(context, "lineNumber", 0)+1));
                                            recordEntity.setBatchNumber(mainEntity.getBatchNumber());
                                            recordEntity.setOnDayProduction(String.valueOf(mainEntity.getOnDayProduction()));
                                            recordEntity.setProgramName(mainEntity.getProgramA());
                                            recordEntity.setRecordTime(DateUtils.getUserDate("yyyyMMddHHmmss"));
                                            recordEntity.setUserName(SPUtil.getString(context, "name", "007"));
                                            recordEntity.setRequireQuantity("修改当日产量");
                                            List<RecordEntity> recordEntities = JsonHelper.getRecordEntity();
                                            if (recordEntities == null){
                                                recordEntities = new ArrayList<>();
                                            }
                                            recordEntities.add(recordEntity);
                                            JsonHelper.saveRecordEntity(recordEntities);
                                            //updateRecordToServer(recordEntity);
                                            updateMainExcelToServer(mainEntity,recordEntity);
                                        }
                                        dialog.dismiss();
                                    }
                                }).create();
                        dialog.show();
                    } else if ("A面程序名".equals(column.getColumnName()) || "B面程序名".equals(column.getColumnName())) {
                        if (preRow != row) {
                            return;
                        }
                        String detailFileName = "";
                        if ("A面程序名".equals(column.getColumnName())) {
                            detailFileName = Environment.getExternalStorageDirectory().getPath() + "/SMTPMS/" + t.get(row).getBatchNumber()
                                    + "_" + t.get(row).getProgramA() + ".XLS";
                        } else {
                            detailFileName = Environment.getExternalStorageDirectory().getPath() + "/SMTPMS/" + t.get(row).getBatchNumber()
                                    + "_" + t.get(row).getProgramB() + ".XLS";
                        }

                        Intent intent = new Intent(context, DetailActivity.class);
                        intent.putExtra("detailFileName", detailFileName);
                        intent.putExtra("programName", "A面程序名".equals(column.getColumnName()) ? t.get(row).getProgramA() : t.get(row).getProgramB());
                        intent.putExtra("batchNumber", t.get(row).getBatchNumber());
                        intent.putExtra("planned",t.get(row).getPlanned());
                        intent.putExtra("repairableSpares",t.get(row).getRepairableSpares());
                        intent.putExtra("cumulativeProduction",t.get(row).getCumulativeProduction());
                        intent.putExtra("lineNum","T"+(((MainActivity) context).lineNumber + 1));
                        context.startActivity(intent);

                    }
                }


            });
        }
        return (PageTableData) tableData;
    }

    private void updateMainExcelToServer(MainEntity mainEntity,RecordEntity recordEntity) {
        int lineNumber = SPUtil.getInt(context, "lineNumber", 0);
        RequestParams params = new RequestParams("http://"
                + SPUtil.getString(context, "severIP", "192.168.1.1") + ":8080"
                + "/day27/FileServlet?method=updateMainExcel");
        String jsonString = JSON.toJSONString(mainEntity);
        String jsonString1 = JSON.toJSONString(recordEntity);
        params.addBodyParameter("sheet",String.valueOf(lineNumber));
        params.addBodyParameter("row",String.valueOf(mainEntity.getRow()));
        params.addBodyParameter("statusA",mainEntity.getStatusA());
        params.addBodyParameter("cumulativeProduction",String.valueOf(mainEntity.getCumulativeProduction()));
        params.addBodyParameter("lineNumber",String.valueOf(lineNumber));
        params.addBodyParameter("smtpmsEntity",jsonString);
        params.addBodyParameter("record",jsonString1);
        x.http().post(params, new Callback.ProgressCallback<String>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }

    private void updateToServer(MainEntity mainEntity) {
        String jsonString = JSON.toJSONString(mainEntity);
        int lineNumber = SPUtil.getInt(context, "lineNumber", 0);
        RequestParams params = new RequestParams("http://"
                + SPUtil.getString(context, "severIP", "192.168.1.1") + ":8080"
                + "/day27/FileServlet?method=updateSmtpmsEntities");
        params.addBodyParameter("lineNumber",String.valueOf(lineNumber));
        params.addBodyParameter("smtpmsEntity",jsonString);
        x.http().post(params, new Callback.ProgressCallback<String>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }

    private void uploadFile(final String fileName) {
        RequestParams params = new RequestParams("http://"
                + SPUtil.getString(context, "severIP", "192.168.1.1") + ":8080"
                + "/day27/FileServlet?method=uploadExcel&type=appUpdateMain");
        //以表单方式上传
        params.setMultipart(true);
        //设置上传文件的路径
        params.addBodyParameter("File", new File(Environment.getExternalStorageDirectory() + "/SMTPMS/" + fileName), null, fileName);
        x.http().post(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {
                Toast.makeText(context, "成功保存到电脑", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(context, "保存到电脑失败", Toast.LENGTH_SHORT).show();
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

    private void updateRecordToServer(RecordEntity recordEntity) {
        String jsonString = JSON.toJSONString(recordEntity);
        RequestParams params = new RequestParams("http://"
                + SPUtil.getString(context, "severIP", "192.168.1.1") + ":8080"
                + "/day27/FileServlet?method=updateRecord");
        params.addBodyParameter("record",jsonString);
        x.http().post(params, new Callback.ProgressCallback<String>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(String result) {

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {

            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {

            }
        });

    }
}
