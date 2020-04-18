package com.yoyo.smtpms;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.bin.david.form.data.column.Column;
import com.bin.david.form.data.table.TableData;
import com.yoyo.smtpms.entity.RecordEntity;
import com.yoyo.smtpms.util.ExcelUtil;
import com.yoyo.smtpms.util.IOUtils;
import com.yoyo.smtpms.util.JsonHelper;
import com.yoyo.smtpms.util.SPUtil;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.io.File;
import java.util.List;

public class RecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        final MySmartTable table = findViewById(R.id.table_record);
        final List<RecordEntity> recordEntities = JsonHelper.getRecordEntity();
        table.setData(recordEntities);
        table.getTableData().setOnItemClickListener(new TableData.OnItemClickListener() {
            @Override
            public void onClick(Column column, String value, Object o, int col, int row) {
                if("内容".equals(column.getColumnName()) && "领料".equals(value)){
                    String fileName = recordEntities.get(row).getBatchNumber()+"_"+recordEntities.get(row).getProgramName()
                            +"_"+recordEntities.get(row).getLineNumber()+"_"+recordEntities.get(row).getRecordTime()+".XLS";
                    downloadXml(fileName);
                }
            }
        });
        Button btnDel = findViewById(R.id.btn_del);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recordEntities ==null || recordEntities.size() == 0) {
                    return;
                }
                final TextView editText = new TextView(RecordActivity.this);
                editText.setText("你确定要删除吗？");
                editText.setPadding(50, 50, 0, 0);
                editText.setTextSize(15);
                new AlertDialog.Builder(RecordActivity.this).setTitle("提示").setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                table.getTableData().getT().clear();
                                table.notifyDataChanged();
                                IOUtils.deleteFoder(Environment.getExternalStorageDirectory() +
                                        "/SMTPMS/controlRecord.txt");
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });
    }

    private void downloadXml(final String fileName) {
        String url = "http://" + SPUtil.getString(RecordActivity.this, "severIP", "192.168.1.1") + ":8080/day27/发料/"+fileName;
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

            }

            @Override
            public void onLoading(long total, long current, boolean isDownloading) {

            }

            @Override
            public void onSuccess(File result) {
                IOUtils.copyFile(result.getAbsolutePath(),Environment.getExternalStorageDirectory() + "/SMTPMS/"+fileName);
                result.delete();
                Intent intent = new Intent(RecordActivity.this, PickingActivity.class);
                intent.putExtra("detailFileName", fileName);
                RecordActivity.this.startActivity(intent);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(RecordActivity.this,"清单不存在",Toast.LENGTH_LONG).show();
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
