package com.yoyo.smtpms;

import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.yoyo.smtpms.entity.RecordEntity;
import com.yoyo.smtpms.util.IOUtils;
import com.yoyo.smtpms.util.JsonHelper;

import java.util.List;

public class RecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        final MySmartTable table = findViewById(R.id.table_record);
        final List<RecordEntity> recordEntities = JsonHelper.getRecordEntity();
        table.setData(recordEntities);
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
}
