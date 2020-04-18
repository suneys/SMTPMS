package com.yoyo.smtpms;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.yoyo.smtpms.entity.PickingEntity;
import com.yoyo.smtpms.util.ExcelUtil;
import com.yoyo.smtpms.util.IOUtils;
import org.apache.poi.hpsf.Util;

import java.util.List;

public class PickingActivity extends AppCompatActivity {

    private String detailFilePath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picking);
        Intent intent = this.getIntent();
        String detailFileName = intent.getStringExtra("detailFileName");
        MySmartTable table = findViewById(R.id.picking_table);
        TextView tvBatchNumber = findViewById(R.id.batchNumber);
        tvBatchNumber.setText("批次号："+detailFileName.split("_")[0]);
        TextView tvProgramNumber = findViewById(R.id.programName);
        tvProgramNumber.setText("程序名："+detailFileName.split("_")[1]);
        detailFilePath = Environment.getExternalStorageDirectory() + "/SMTPMS/"+detailFileName;
        List<PickingEntity> pickingEntities = ExcelUtil.parsePickingExcel(detailFilePath);
        table.setData(pickingEntities);
    }

    @Override
    public void finish() {
        IOUtils.deleteFoder(detailFilePath);
        super.finish();
    }
}
