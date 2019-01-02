package com.yoyo.smtpms;

import android.app.ActionBar;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.bin.david.form.core.TableConfig;
import com.bin.david.form.data.CellInfo;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.format.bg.BaseBackgroundFormat;
import com.bin.david.form.data.format.bg.BaseCellBackgroundFormat;
import com.bin.david.form.data.format.bg.ICellBackgroundFormat;
import com.bin.david.form.data.format.draw.MultiLineDrawFormat;
import com.bin.david.form.data.format.selected.BaseSelectFormat;
import com.bin.david.form.data.table.TableData;
import com.yoyo.smtpms.entity.DetailEntity;
import com.yoyo.smtpms.util.*;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.yoyo.smtpms.util.ExcelUtil.parseDetailExcel;

public class DetailActivity extends AppCompatActivity {

    final static String BATCHLIST_PATB = "/day27/批次清单/";

    private MySmartTable<DetailEntity> detailTable;
    private TextView tvBatchNumber;
    private TextView tvProgramName;
    private TextView tvloadingMessage;
    private Button btnFirst;
    private TextView tvFind;

    private String batchNumber;
    private String programName;

    private AlertDialog.Builder builder;

    private boolean firstCheck = false;
    private String detailFileName = "";

    private List<CellInfo> cellInfos = new ArrayList<>();
    private TableData<DetailEntity> detailEntityTableData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        BaseUtil.setFullSreen(this);
        Intent intent = this.getIntent();
        detailTable = findViewById(R.id.detail_table);
        tvBatchNumber = findViewById(R.id.batchNumber);
        tvProgramName = findViewById(R.id.programName);
        btnFirst = findViewById(R.id.btn_first);
        tvFind = findViewById(R.id.tv_find);
        programName = intent.getStringExtra("programName");
        batchNumber = intent.getStringExtra("batchNumber");
        tvProgramName.setText("程序名:" + programName);
        tvBatchNumber.setText("批次号:" + batchNumber);
        detailTable = findViewById(R.id.detail_table);
        tvloadingMessage = findViewById(R.id.tv_detail_loading_message);

        final Column<String> partNumber = new Column<>("料号", "partNumber");
        partNumber.setAutoMerge(true);
        final Column<String> componentValue = new Column<>("元件值", "componentValue");
        componentValue.setDrawFormat(new MultiLineDrawFormat<String>(this,200));
        componentValue.setAutoMerge(true);
        final Column<String> quantity = new Column<>("数量", "quantity");
        final Column<String> remark = new Column<>("备注", "remark");
        final Column<String> tagNumber1 = new Column<>("位号1", "tagNumber1");
        final Column<String> tagNumber2 = new Column<>("位号2", "tagNumber2");
        final Column<String> tagNumber3 = new Column<>("位号3", "tagNumber3");
        final Column<String> tagNumber4 = new Column<>("位号4", "tagNumber4");
        final Column<String> tagNumber5 = new Column<>("位号5", "tagNumber5");
           detailFileName = intent.getStringExtra("detailFileName");
        if (IOUtils.fileIsExist(detailFileName)) {
            final List<DetailEntity> detailEntities = ExcelUtil.parseDetailExcel(detailFileName);
            int rangeStartPosition= -1;
            //合并数据
            List<int[]> ranges = new ArrayList<>();
            for (int i = 0; i < detailEntities.size(); i ++){
                if(detailEntities.get(i).getQuantity() == 0){
                    if(rangeStartPosition == -1){
                        rangeStartPosition = i - 1;
                    }
                }else{
                    if(rangeStartPosition != -1){
                        int[] range = {rangeStartPosition, i-1};
                        ranges.add(range);
                        rangeStartPosition =-1;
                    }
                }
            }
            quantity.setRanges(ranges);
            remark.setRanges(ranges);
            detailEntityTableData =
                    new TableData<>("", detailEntities, partNumber, componentValue,
                    quantity, remark, tagNumber1,tagNumber2,tagNumber3,tagNumber4,tagNumber5);
            detailEntityTableData.setOnItemClickListener(new TableData.OnItemClickListener() {
                @Override
                public void onClick(Column column, String value, Object o, final int col, final int row) {

                    changefirstCheckTextColor(column, col, row);
                }
            });
            detailTable.setTableData(detailEntityTableData);
        } else {
            String url = "http://"
                    + SPUtil.getString(DetailActivity.this, "severIP", "192.168.1.1")+":8080"
                    + BATCHLIST_PATB + intent.getStringExtra("batchNumber")+"_"
                    + intent.getStringExtra("programName") + ".XLS";

            RequestParams params = new RequestParams(url);
            //自定义保存路径，Environment.getExternalStorageDirectory()：SD卡的根目录
            params.setSaveFilePath(Environment.getExternalStorageDirectory() + "/SMTPMS/");
            //自动为文件命名
            params.setAutoRename(true);
            x.http().post(params, new Callback.ProgressCallback<File>() {
                @Override
                public void onSuccess(File result) {
                    IOUtils.copyFile(result.getAbsolutePath(),detailFileName);
                    result.delete();
                    tvloadingMessage.setVisibility(View.GONE);
                    List<DetailEntity> detailEntities = ExcelUtil.parseDetailExcel(detailFileName);
                    int rangeStartPosition= -1;
                    //合并数据
                    List<int[]> ranges = new ArrayList<>();
                    for (int i = 0; i < detailEntities.size(); i ++){
                        if(detailEntities.get(i).getQuantity() == 0){
                            if(rangeStartPosition == -1){
                                rangeStartPosition = i - 1;
                            }
                        }else{
                            if(rangeStartPosition != -1){
                                int[] range = {rangeStartPosition, i-1};
                                ranges.add(range);
                                rangeStartPosition =-1;
                            }
                        }
                    }
                    quantity.setRanges(ranges);
                    remark.setRanges(ranges);
                    detailEntityTableData = new TableData<>("", detailEntities, partNumber, componentValue,
                            quantity, remark, tagNumber1,tagNumber2,tagNumber3,tagNumber4,tagNumber5);
                    detailEntityTableData.setOnItemClickListener(new TableData.OnItemClickListener() {
                        @Override
                        public void onClick(Column column, String value, Object o, int col, int row) {
                            changefirstCheckTextColor(column, col, row);
                        }
                    });
                    detailTable.setTableData(detailEntityTableData);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    tvloadingMessage.setText("加载数据失败，请确认清单文件是否存在！");
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
                    tvloadingMessage.setVisibility(View.VISIBLE);
                }

                @Override
                public void onLoading(long total, long current, boolean isDownloading) {

                }
            });
        }

        btnFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!firstCheck){
                    firstCheck = true;
                    Toast.makeText(DetailActivity.this,"已进入首检模式",Toast.LENGTH_SHORT);
                    btnFirst.setText("退出首检");
                }else{
                    {
                        final TextView textView = new TextView(DetailActivity.this);
                        textView.setText("将要退出首检模式，是否保存当前首检内容？");
                        textView.setGravity(Gravity.CENTER);
                        AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this).setTitle("提示").setView(textView)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        firstCheck = false;
                                        btnFirst.setText("首检");
                                        dialog.dismiss();
                                        String fileName = batchNumber +
                                                "#" + programName +
                                                "#" + DateUtils.getUserDate("yyyyMMddHHmmss") + ".XLS";
                                        try {
                                            ExcelUtil.creatDetailExcel(detailEntityTableData.getT(), fileName);
                                            uploadFile(fileName);
                                            detailTable.getConfig().setContentCellBackgroundFormat(new ICellBackgroundFormat<CellInfo>() {
                                                @Override
                                                public void drawBackground(Canvas canvas, Rect rect, CellInfo cellInfo, Paint paint) {

                                                }

                                                @Override
                                                public int getTextColor(CellInfo cellInfo) {
                                                    return 0;
                                                }
                                            });
                                            detailTable.invalidate();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        firstCheck = false;
                                        btnFirst.setText("首检");
                                        dialog.dismiss();
                                        detailTable.getConfig().setContentCellBackgroundFormat(new ICellBackgroundFormat<CellInfo>() {
                                            @Override
                                            public void drawBackground(Canvas canvas, Rect rect, CellInfo cellInfo, Paint paint) {

                                            }

                                            @Override
                                            public int getTextColor(CellInfo cellInfo) {
                                                return 0;
                                            }
                                        });
                                        detailTable.invalidate();
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                    }
                }
            }

        });

        tvFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText editText = new EditText(DetailActivity.this);
                final TextView textView = new TextView(DetailActivity.this);
                textView.setText("正在查找...");
                textView.setGravity(Gravity.CENTER);
                textView.setVisibility(View.GONE);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
                editText.setLayoutParams(layoutParams);
                textView.setLayoutParams(layoutParams);
                RelativeLayout relativeLayout = new RelativeLayout(DetailActivity.this);
                relativeLayout.addView(editText);
                relativeLayout.addView(textView);

                builder = new AlertDialog.Builder(DetailActivity.this).setTitle("请输入要查找的内容").setView(relativeLayout)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                final AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(textView.getVisibility() == View.VISIBLE){
                            alertDialog.dismiss();
                            return;
                        }
                        String findContent = editText.getText().toString().toString();
                        if("".equals(findContent)){
                            Toast.makeText(DetailActivity.this,"内容不能为空",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        alertDialog.setTitle("提示");
                        editText.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                        int count = findContentInDetailEntities(findContent);
                        textView.setText("查到"+count+"个结果，以蓝色标记出来。");
                        detailTable.getConfig().setContentCellBackgroundFormat(new ICellBackgroundFormat<CellInfo>() {
                            @Override
                            public void drawBackground(Canvas canvas, Rect rect, CellInfo cellInfo, Paint paint) {
                                boolean isTure = (cellInfo.col>=4 && cellInfo.col <= 13 && detailEntityTableData.getT().get(cellInfo.row).getTagNuberIsCheck()[cellInfo.col - 4] == 2)
                                        || (cellInfo.col == 1 && detailEntityTableData.getT().get(cellInfo.row).getTagNuberIsCheck()[5] == 2);
                                if(isTure){
                                    paint.setColor(Color.BLUE);
                                    paint.setStyle(Paint.Style.FILL);
                                    canvas.drawRect(rect, paint);
                                }
                            }

                            @Override
                            public int getTextColor(CellInfo cellInfo) {
                                if(cellInfo.col>=4 && cellInfo.col <= 13 && detailEntityTableData.getT().get(cellInfo.row).getTagNuberIsCheck()[cellInfo.col - 4] == 2){
                                    return ContextCompat.getColor(DetailActivity.this, R.color.content_bg);
                                }else if(cellInfo.col == 1 && detailEntityTableData.getT().get(cellInfo.row).getTagNuberIsCheck()[5] == 2){
                                    return ContextCompat.getColor(DetailActivity.this, R.color.content_bg);
                                }
                                return 0;
                            }
                        });
                        detailTable.invalidate();
                    }
                });
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IOUtils.deleteFoder(detailFileName);

    }

    private int findContentInDetailEntities(String findContent) {
        List<DetailEntity> detailEntities = detailEntityTableData.getT();
        findContent = findContent.toLowerCase();
        int count = 0;
        for(int i = 0; i < detailEntities.size(); i++){
            DetailEntity detailEntity = detailEntities.get(i);

            for(int j = 0 ; j < detailEntity.getTagNuberIsCheck().length; j++)
            {
                if(detailEntity.getTagNuberIsCheck()[j] == 2)
                {
                    detailEntity.getTagNuberIsCheck()[j] = 0;
                }
            }
            if(detailEntity.getTagNumber1().toLowerCase().contains(findContent)){
                detailEntity.getTagNuberIsCheck()[0] = 2;
                count++;
            }
            if(detailEntity.getTagNumber2().toLowerCase().contains(findContent)){
                detailEntity.getTagNuberIsCheck()[1] = 2;
                count++;
            }
            if(detailEntity.getTagNumber3().toLowerCase().contains(findContent)){
                detailEntity.getTagNuberIsCheck()[2] = 2;
                count++;
            }
            if(detailEntity.getTagNumber4().toLowerCase().contains(findContent)){
                detailEntity.getTagNuberIsCheck()[3] = 2;
                count++;
            }
            if(detailEntity.getTagNumber5().toLowerCase().contains(findContent)){
                detailEntity.getTagNuberIsCheck()[4] = 2;
                count++;
            }
            if(detailEntity.getComponentValue().toLowerCase().contains(findContent)){
                detailEntity.getTagNuberIsCheck()[5] = 2;
                count++;
            }
        }
        return count;
    }

    private void changefirstCheckTextColor(Column column, int col, int row) {
        if(!firstCheck){
            return;
        }

        if(!"位号1".equals(column.getColumnName()) && !"位号2".equals(column.getColumnName())
            && !"位号3".equals(column.getColumnName()) && !"位号4".equals(column.getColumnName())
                && !"位号5".equals(column.getColumnName()) && !"位号6".equals(column.getColumnName())
                && !"位号7".equals(column.getColumnName()) && !"位号8".equals(column.getColumnName())
                && !"位号9".equals(column.getColumnName()) && !"位号10".equals(column.getColumnName())
                )
        {
            return;
        }
        if(detailEntityTableData.getT().get(row).getTagNuberIsCheck()[col - 4] == 1){
            detailEntityTableData.getT().get(row).getTagNuberIsCheck()[col - 4] = 0;
        }else{
            detailEntityTableData.getT().get(row).getTagNuberIsCheck()[col - 4] = 1;
        }
        detailTable.getConfig().setContentCellBackgroundFormat(new ICellBackgroundFormat<CellInfo>() {
            @Override
            public void drawBackground(Canvas canvas, Rect rect, CellInfo cellInfo, Paint paint) {

            }

            @Override
            public int getTextColor(CellInfo cellInfo) {
                if(cellInfo.col>=4 && cellInfo.col <= 13 && detailEntityTableData.getT().get(cellInfo.row).getTagNuberIsCheck()[cellInfo.col - 4] == 1){
                    return ContextCompat.getColor(DetailActivity.this, R.color.colorAccent);
                }
                return 0;
            }
        });
    }

    private void uploadFile(final String fileName) {
        RequestParams params = new RequestParams("http://"
                + SPUtil.getString(DetailActivity.this, "severIP", "192.168.1.1")+":8080"
                +"/day27/FileServlet?method=uploadExcel&type=firstCheck");
        //以表单方式上传
        params.setMultipart(true);
        //设置上传文件的路径
        params.addBodyParameter("File",new File(Environment.getExternalStorageDirectory()+"/SMTPMS/" + fileName),null,fileName);
        x.http().post(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {
               // result.delete();
                Toast.makeText(DetailActivity.this,"成功保存到电脑",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(DetailActivity.this,"保存到电脑失败",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                new File(Environment.getExternalStorageDirectory()+"/SMTPMS/" + fileName).delete();
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


}
