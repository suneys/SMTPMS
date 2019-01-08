package com.yoyo.smtpms.util;

import android.os.Environment;
import com.yoyo.smtpms.entity.RecordEntity;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016-06-25.
 */
public class JsonHelper {

    public synchronized static  void  saveRecordEntity(List<RecordEntity> recordEntities) {
        JSONObject list = new JSONObject();
        JSONArray entityArray = new JSONArray();
        try {
            for (RecordEntity recordEntity : recordEntities) {
                JSONObject entity = new JSONObject();
                entity.put("userName",recordEntity.getUserName());
                entity.put("batchNumber",recordEntity.getBatchNumber());
                entity.put("programName",recordEntity.getProgramName());
                entity.put("onDayProduction",recordEntity.getOnDayProduction());
                entity.put("recordTime",recordEntity.getRecordTime());
                entity.put("lineNumber",recordEntity.getLineNumber());
                entityArray.put(entity);
            }
            list.put("list", entityArray);
            IOUtils.writeToFile(Environment.getExternalStorageDirectory() + "/SMTPMS/controlRecord.txt", list
                    .toString(),false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public List<RecordEntity> getRecordEntity() {
        List<RecordEntity> recordEntities = new ArrayList<>();
        try {
            String result = IOUtils.readFormFile(Environment.getExternalStorageDirectory() +
                    "/SMTPMS/controlRecord.txt","utf-8");
            if (result.isEmpty()){
                return null;
            }
            JSONObject resultJson = new JSONObject(result);
            JSONArray list = resultJson.getJSONArray("list");
            for(int i = 0; i < list.length(); i++){
                RecordEntity recordEntity = new RecordEntity();
                JSONObject entityJson = list.getJSONObject(i);
                recordEntity.setUserName(entityJson.getString("userName"));
                recordEntity.setBatchNumber(entityJson.getString("batchNumber"));
                recordEntity.setProgramName(entityJson.getString("programName"));
                recordEntity.setOnDayProduction(entityJson.getString("onDayProduction"));
                recordEntity.setRecordTime(entityJson.getString("recordTime"));
                recordEntity.setLineNumber(entityJson.getString("lineNumber"));
                recordEntities.add(recordEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return recordEntities;
    }
}
