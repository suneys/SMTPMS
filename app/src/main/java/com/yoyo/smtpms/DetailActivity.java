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
import android.text.InputType;
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
import com.bin.david.form.listener.OnColumnItemClickListener;
import com.yoyo.smtpms.entity.DetailEntity;
import com.yoyo.smtpms.entity.RecordEntity;
import com.yoyo.smtpms.util.*;
import com.yoyo.smtpms.view.MyDialog;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.yoyo.smtpms.util.ExcelUtil.parseDetailExcel;

public class DetailActivity extends AppCompatActivity {

    final static String BATCHLIST_PATB = "/day27/批次清单/";

    private MySmartTable<DetailEntity> detailTable;
    private TextView tvBatchNumber;
    private TextView tvProgramName;
    private TextView tvloadingMessage;
    private Button btnFirst;
    //领料
    private Button btnPicking;
    private Button btnSort;
    private TextView tvFind;

    private String batchNumber;
    private String programName;
    private String lineNum;

    //计划数量
    private int planned;
    //维备数量
    private int repairableSpares;
    //累计数量
    private int cumulativeProduction;

    private AlertDialog.Builder builder;

    private boolean firstCheck = false;
    private boolean isSortColumn = false;
    private String detailFileName = "";

    public boolean isPicking;

    private List<CellInfo> cellInfos = new ArrayList<>();
    private TableData<DetailEntity> detailEntityTableData;
    private List<DetailEntity> detailEntities;


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
        btnPicking = findViewById(R.id.btn_picking);
        btnSort = findViewById(R.id.btn_sort);
        tvFind = findViewById(R.id.tv_find);
        programName = intent.getStringExtra("programName");
        batchNumber = intent.getStringExtra("batchNumber");
        lineNum = intent.getStringExtra("lineNum");
        planned = intent.getIntExtra("planned",0);
        repairableSpares = intent.getIntExtra("repairableSpares",0);
        cumulativeProduction = intent.getIntExtra("cumulativeProduction",0);
        tvProgramName.setText("程序名:" + programName);
        tvBatchNumber.setText("批次号:" + batchNumber);
        detailTable = findViewById(R.id.detail_table);
        tvloadingMessage = findViewById(R.id.tv_detail_loading_message);

        final Column<String> partNumber = new Column<>("料号", "partNumber");
        partNumber.setAutoMerge(true);
        final Column<String> componentValue = new Column<>("元件值", "componentValue");
        componentValue.setDrawFormat(new MultiLineDrawFormat<String>(this, 200));
        componentValue.setAutoMerge(true);
        final Column<String> quantity = new Column<>("数量", "quantity");
        final Column<String> remark = new Column<>("备注", "remark");
        final Column<String> tagNumber1 = new Column<>("位号1", "tagNumber1");
        final Column<String> tagNumber2 = new Column<>("位号2", "tagNumber2");
        final Column<String> tagNumber3 = new Column<>("位号3", "tagNumber3");
        final Column<String> tagNumber4 = new Column<>("位号4", "tagNumber4");
        final Column<String> tagNumber5 = new Column<>("位号5", "tagNumber5");
        final Column<String> requirtedQuantity = new Column<>("需求数量", "requiredQiantity");
        detailFileName = intent.getStringExtra("detailFileName");
        if (IOUtils.fileIsExist(detailFileName)) {
            detailEntities = ExcelUtil.parseDetailExcel(detailFileName);
            int rangeStartPosition = -1;
            //合并数据
            List<int[]> ranges = new ArrayList<>();
            for (int i = 0; i < detailEntities.size(); i++) {
                if (detailEntities.get(i).getQuantity() == 0) {
                    if (rangeStartPosition == -1) {
                        rangeStartPosition = i - 1;
                    }
                } else {
                    if (rangeStartPosition != -1) {
                        int[] range = {rangeStartPosition, i - 1};
                        ranges.add(range);
                        rangeStartPosition = -1;
                    }
                }
            }
            quantity.setRanges(ranges);
            remark.setRanges(ranges);
            requirtedQuantity.setRanges(ranges);
            detailEntityTableData =
                    new TableData<>("", detailEntities, partNumber, componentValue,
                            quantity, requirtedQuantity, remark, tagNumber1, tagNumber2, tagNumber3, tagNumber4, tagNumber5);
            detailEntityTableData.setOnItemClickListener(new TableData.OnItemClickListener() {
                @Override
                public void onClick(Column column, String value, Object o, final int col, final int row) {

                    try {
                        if ("需求数量".equals(column.getColumnName())) {
                            MyDialog myDialog = new MyDialog(DetailActivity.this);
                            myDialog.setText("请输入需求数量").setCancelListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                }
                            }).setComfirmListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String text = ((EditText) view).getText().toString().trim();
                                    if (text == null || text.equals("")) {
                                        Toast.makeText(DetailActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    int integer = Integer.parseInt(text);
                                    if (detailEntities != null) {
                                        detailEntities.get(row).setRequiredQiantity(integer);
                                    }
                                    detailTable.notifyDataChanged();
                                }
                            }).show();

                        } else {
                            changefirstCheckTextColor(column, col, row);
                        }
//                        if ("备注".equals(column.getColumnName()) && (isSortColumn == false)) {
////                            quantity.setRanges(null);
////                            remark.setRanges(null);
//                            detailTable.setSortColumn(remark, true);
//
//                            isSortColumn = true;
//
//                        }
                    } catch (
                            Exception e)

                    {
                        e.printStackTrace();
                    }
                }
            });
            detailTable.setTableData(detailEntityTableData);
        } else

        {
            String url = "http://"
                    + SPUtil.getString(DetailActivity.this, "severIP", "192.168.1.1") + ":8080"
                    + BATCHLIST_PATB + intent.getStringExtra("batchNumber") + "_"
                    + intent.getStringExtra("programName") + ".XLS";

            RequestParams params = new RequestParams(url);
            //自定义保存路径，Environment.getExternalStorageDirectory()：SD卡的根目录
            params.setSaveFilePath(Environment.getExternalStorageDirectory() + "/SMTPMS/");
            //自动为文件命名
            params.setAutoRename(true);
            x.http().post(params, new Callback.ProgressCallback<File>() {
                @Override
                public void onSuccess(File result) {
                    IOUtils.copyFile(result.getAbsolutePath(), detailFileName);
                    result.delete();
                    tvloadingMessage.setVisibility(View.GONE);
                    detailEntities = ExcelUtil.parseDetailExcel(detailFileName);
                    int rangeStartPosition = -1;
                    //合并数据
                    List<int[]> ranges = new ArrayList<>();
                    for (int i = 0; i < detailEntities.size(); i++) {
                        if (detailEntities.get(i).getQuantity() == 0) {
                            if (rangeStartPosition == -1) {
                                rangeStartPosition = i - 1;
                            }
                        } else {
                            if (rangeStartPosition != -1) {
                                int[] range = {rangeStartPosition, i - 1};
                                ranges.add(range);
                                rangeStartPosition = -1;
                            }
                        }
                    }
                    quantity.setRanges(ranges);
                    remark.setRanges(ranges);
                    requirtedQuantity.setRanges(ranges);
                    detailEntityTableData = new TableData<>("", detailEntities, partNumber, componentValue,
                            quantity, requirtedQuantity, remark, tagNumber1, tagNumber2, tagNumber3, tagNumber4, tagNumber5);
                    detailEntityTableData.setOnItemClickListener(new TableData.OnItemClickListener() {
                        @Override
                        public void onClick(Column column, String value, Object o, int col, final int row) {
                            try {
                                if ("需求数量".equals(column.getColumnName())) {
                                    if (isPicking) {
                                        MyDialog myDialog = new MyDialog(DetailActivity.this);
                                        myDialog.setText("请输入需求数量").setCancelListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                            }
                                        }).setComfirmListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                String text = ((EditText) view).getText().toString().trim();
                                                if (text == null || text.equals("")) {
                                                    Toast.makeText(DetailActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                int integer = Integer.parseInt(text);
                                                if (detailEntities != null) {
                                                    if (detailEntities != null) {
                                                        detailEntities.get(row).setRequiredQiantity(integer);
                                                    }
                                                }
                                                detailTable.notifyDataChanged();
                                            }
                                        }).show();


                                    }
                                } else {
                                    changefirstCheckTextColor(column, col, row);
                                }
//                                if ("备注".equals(column.getColumnName()) && (isSortColumn == false)) {
////                                    quantity.setRanges(null);
////                                    remark.setRanges(null);
//                                    detailTable.setSortColumn(remark, true);
//                                    isSortColumn = true;
//
//                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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

        btnFirst.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {

                if (!firstCheck) {
                    firstCheck = true;
                    Toast.makeText(DetailActivity.this, "已进入首检模式", Toast.LENGTH_SHORT);
                    btnFirst.setText("退出首检");
                } else {
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
                                            uploadFile(fileName, "firstCheck");
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

        btnPicking.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                if (detailEntityTableData == null || detailEntityTableData.getT() == null) {
                    return;
                }
                if (isPicking == false) {
                    View pickingView = DetailActivity.this.getLayoutInflater().inflate(R.layout.picking, null);
                    final RadioButton rb1 = pickingView.findViewById(R.id.rb1);
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this).setTitle("请选择领料方式").setView(pickingView)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if (rb1.isChecked()) {

                                        MyDialog myDialog = new MyDialog(DetailActivity.this);
                                        myDialog.setText("请输入领料数量").setCancelListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {

                                            }
                                        }).setComfirmListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                String text = ((EditText) view).getText().toString().trim();
                                                if (text == null || text.equals("")) {
                                                    Toast.makeText(DetailActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                                int integer = Integer.parseInt(text);
                                                if (detailEntities != null) {
                                                    for (int j = 0; j < detailEntities.size(); j++) {
                                                        if (detailEntities.get(j).getComponentValue().equals("合计")) {
                                                            detailEntities.get(j).setRequiredQiantity(0);
                                                        } else {
                                                            detailEntities.get(j).setRequiredQiantity(detailEntities.get(j).getQuantity() * integer);
                                                        }
                                                    }
                                                }
                                                detailTable.notifyDataChanged();
                                            }
                                        }).show();
                                    }
                                    isPicking = true;
                                    btnPicking.setText("退出领料");

                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                } else {
                    isPicking = false;
                    btnPicking.setText("领料");
                    String rescordTime = DateUtils.getUserDate("yyyyMMddHHmmss");
                    String fileName = batchNumber +
                            "_" + programName +
                            "_" + lineNum+"_"+rescordTime + ".XLS";
                    try {
                        ExcelUtil.creatPickingExcel(detailEntityTableData.getT(), fileName);
                        uploadFile(fileName, "picking");
                        RecordEntity recordEntity = new RecordEntity();
                        recordEntity.setLineNumber(lineNum);
                        recordEntity.setBatchNumber(batchNumber);
                        recordEntity.setProgramName(programName);
                        recordEntity.setOnDayProduction("");
                        recordEntity.setRecordTime(rescordTime);
                        recordEntity.setUserName(SPUtil.getString(DetailActivity.this, "name", "007"));
                        recordEntity.setRequireQuantity("领料");
                        List<RecordEntity> recordEntities = JsonHelper.getRecordEntity();
                        if (recordEntities == null){
                            recordEntities = new ArrayList<>();
                        }
                        recordEntities.add(recordEntity);
                        JsonHelper.saveRecordEntity(recordEntities);
                        DetailActivity.this.finish();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        tvFind.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                final EditText editText = new EditText(DetailActivity.this);
                final TextView textView = new TextView(DetailActivity.this);
                textView.setText("正在查找...");
                textView.setGravity(Gravity.CENTER);
                textView.setVisibility(View.GONE);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
                        if (textView.getVisibility() == View.VISIBLE) {
                            alertDialog.dismiss();
                            return;
                        }
                        String findContent = editText.getText().toString().toString();
                        if ("".equals(findContent)) {
                            Toast.makeText(DetailActivity.this, "内容不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        alertDialog.setTitle("提示");
                        editText.setVisibility(View.GONE);
                        textView.setVisibility(View.VISIBLE);
                        int count = findContentInDetailEntities(findContent);
                        textView.setText("查到" + count + "个结果，以蓝色标记出来。");
                        detailTable.getConfig().setContentCellBackgroundFormat(new ICellBackgroundFormat<CellInfo>() {
                            @Override
                            public void drawBackground(Canvas canvas, Rect rect, CellInfo cellInfo, Paint paint) {
                                boolean isTure = (cellInfo.col >= 5 && cellInfo.col <= 13 && detailEntityTableData.getT().get(cellInfo.row).getTagNuberIsCheck()[cellInfo.col - 5] == 2)
                                        || (cellInfo.col == 1 && detailEntityTableData.getT().get(cellInfo.row).getTagNuberIsCheck()[5] == 2);
                                if (isTure) {
                                    paint.setColor(Color.BLUE);
                                    paint.setStyle(Paint.Style.FILL);
                                    canvas.drawRect(rect, paint);
                                }
                            }

                            @Override
                            public int getTextColor(CellInfo cellInfo) {
                                if (cellInfo.col >= 5 && cellInfo.col <= 13 && detailEntityTableData.getT().get(cellInfo.row).getTagNuberIsCheck()[cellInfo.col - 5] == 2) {
                                    return ContextCompat.getColor(DetailActivity.this, R.color.content_bg);
                                } else if (cellInfo.col == 1 && detailEntityTableData.getT().get(cellInfo.row).getTagNuberIsCheck()[5] == 2) {
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
        btnSort.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                quantity.setRanges(null);
                remark.setRanges(null);
                requirtedQuantity.setRanges(null);
                detailTable.setSortColumn(remark, true);

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
        for (int i = 0; i < detailEntities.size(); i++) {
            DetailEntity detailEntity = detailEntities.get(i);

            for (int j = 0; j < detailEntity.getTagNuberIsCheck().length; j++) {
                if (detailEntity.getTagNuberIsCheck()[j] == 2) {
                    detailEntity.getTagNuberIsCheck()[j] = 0;
                }
            }
            if (detailEntity.getTagNumber1().toLowerCase().contains(findContent)) {
                detailEntity.getTagNuberIsCheck()[0] = 2;
                count++;
            }
            if (detailEntity.getTagNumber2().toLowerCase().contains(findContent)) {
                detailEntity.getTagNuberIsCheck()[1] = 2;
                count++;
            }
            if (detailEntity.getTagNumber3().toLowerCase().contains(findContent)) {
                detailEntity.getTagNuberIsCheck()[2] = 2;
                count++;
            }
            if (detailEntity.getTagNumber4().toLowerCase().contains(findContent)) {
                detailEntity.getTagNuberIsCheck()[3] = 2;
                count++;
            }
            if (detailEntity.getTagNumber5().toLowerCase().contains(findContent)) {
                detailEntity.getTagNuberIsCheck()[4] = 2;
                count++;
            }
            if (detailEntity.getComponentValue().toLowerCase().contains(findContent)) {
                detailEntity.getTagNuberIsCheck()[5] = 2;
                count++;
            }
        }
        return count;
    }

    private void changefirstCheckTextColor(Column column, int col, int row) {
        if (!firstCheck) {
            return;
        }

        if (!"位号1".equals(column.getColumnName()) && !"位号2".equals(column.getColumnName())
                && !"位号3".equals(column.getColumnName()) && !"位号4".equals(column.getColumnName())
                && !"位号5".equals(column.getColumnName()) && !"位号6".equals(column.getColumnName())
                && !"位号7".equals(column.getColumnName()) && !"位号8".equals(column.getColumnName())
                && !"位号9".equals(column.getColumnName()) && !"位号10".equals(column.getColumnName())
                ) {
            return;
        }
        if (detailEntityTableData.getT().get(row).getTagNuberIsCheck()[col - 5] == 1) {
            detailEntityTableData.getT().get(row).getTagNuberIsCheck()[col - 5] = 0;
        } else {
            detailEntityTableData.getT().get(row).getTagNuberIsCheck()[col - 5] = 1;
        }
        detailTable.getConfig().setContentCellBackgroundFormat(new ICellBackgroundFormat<CellInfo>() {
            @Override
            public void drawBackground(Canvas canvas, Rect rect, CellInfo cellInfo, Paint paint) {

            }

            @Override
            public int getTextColor(CellInfo cellInfo) {
                if (cellInfo.col >= 5 && cellInfo.col <= 13 && detailEntityTableData.getT().get(cellInfo.row).getTagNuberIsCheck()[cellInfo.col - 5] == 1) {
                    return ContextCompat.getColor(DetailActivity.this, R.color.colorAccent);
                }
                return 0;
            }
        });
    }

    private void uploadFile(final String fileName, String type) {
        RequestParams params = new RequestParams("http://"
                + SPUtil.getString(DetailActivity.this, "severIP", "192.168.1.1") + ":8080"
                + "/day27/FileServlet?method=uploadExcel&type=" + type + "&line=" + lineNum + "&planned="+String.valueOf(planned)
                + "&repairableSpares="+String.valueOf(repairableSpares) + "&cumulativeProduction="+String.valueOf(cumulativeProduction));
        //以表单方式上传
        params.setMultipart(true);
        //设置上传文件的路径
        params.addBodyParameter("File", new File(Environment.getExternalStorageDirectory() + "/SMTPMS/" + fileName), null, fileName);
        x.http().post(params, new Callback.ProgressCallback<File>() {
            @Override
            public void onSuccess(File result) {
                // result.delete();
                Toast.makeText(DetailActivity.this, "成功保存到电脑", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(DetailActivity.this, "保存到电脑失败", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(CancelledException cex) {

            }

            @Override
            public void onFinished() {
                new File(Environment.getExternalStorageDirectory() + "/SMTPMS/" + fileName).delete();
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
