package com.yoyo.smtpms.util;

import android.os.Environment;

import com.yoyo.smtpms.entity.DetailEntity;
import com.yoyo.smtpms.entity.MainEntity;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.CellFormat;
import jxl.format.UnderlineStyle;
import jxl.write.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/1/10 0010.
 */
public class ExcelUtil {

    /**
     * 适用于第一行是标题行的excel
     * 每一行构成一个map，key值是列标题，value是列值。没有值的单元格其value值为null
     * @param filePath
     * @return
     * @throws Exception
     */
    public static List<Map<String, String>> readExcel(String filePath, int sheet) throws Exception {

        List<Map<String, String>> list = null;
        InputStream is = new FileInputStream(filePath);

        Workbook rwb = Workbook.getWorkbook(is);

        Sheet rst = rwb.getSheet(sheet);

        int rows = rst.getRows();
        if (rows >= 2){
            list = new ArrayList<>();
            int columns = rst.getColumns();
            for (int i = 1; i < rows; i++){
                Map<String, String> map = new HashMap<>();
                for (int j = 0; j < columns; j++){
                    Cell titleCell = rst.getCell(j,0);
                    Cell dataCell = rst.getCell(j,i);
                    map.put(titleCell.getContents(),dataCell.getContents());
                }
                list.add(map);
            }
        }

        is.close();
        return list;
    }

    public static void updateCell(String filePath, int sheet,int r, int l,String content)  {
        InputStream is = null;
        WritableWorkbook wbe = null;
        try {
            is = new FileInputStream(filePath);
            Workbook rwb = Workbook.getWorkbook(is);
            wbe = Workbook.createWorkbook(new File(filePath),rwb);
            WritableSheet rst = wbe.getSheet(sheet);
            WritableCell cell = rst.getWritableCell(l, r);
            CellFormat cf = cell.getCellFormat();
            Label label = new Label(l, r, content);
            if(cf != null) {
                label.setCellFormat(cf);
            }
            rst.addCell(label);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(wbe != null) {
                    wbe.write();
                    wbe.close();
                }
                if(is != null) {
                    is.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


    }

    public static void updateMainExcel(int sheet,MainEntity mainEntity){
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/SMTPMS/精密线体计划排产表.xls";
        try {
            updateCell(filePath,sheet,mainEntity.getRow(),11,mainEntity.getStatusA());
            updateCell(filePath,sheet,mainEntity.getRow(),13,String.valueOf(mainEntity.getCumulativeProduction()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<MainEntity> parseExcel(int sheet) {
        List<MainEntity> mainEntities = null;
        MainEntity temp = null;
        try {
            List<Map<String, String>> datas = ExcelUtil.readExcel(Environment.getExternalStorageDirectory().getPath() + "/SMTPMS/精密线体计划排产表.XLS", sheet);
            mainEntities = new ArrayList<>();
            for (int i = 0; i < datas.size(); i++
                    ) {
                Map<String, String> data = datas.get(i);
                MainEntity mainEntity = new MainEntity();
                mainEntity.setRow(i+1);
                mainEntity.setStatusA(data.get("状态A"));
                if (data.get("累计产量") != null && !(data.get("累计产量").trim().equals(""))) {
                    mainEntity.setCumulativeProduction(Integer.parseInt(data.get("累计产量").trim()));
                } else {
                    mainEntity.setCumulativeProduction(0);
                }
                if (data.get("计划数量") != null && !(data.get("计划数量").trim().equals(""))) {
                    mainEntity.setPlanned(Integer.parseInt(data.get("计划数量").trim()));
                } else {
                    mainEntity.setPlanned(0);
                }
                if (data.get("当日产量") != null && !(data.get("当日产量").trim().equals(""))) {
                    mainEntity.setOnDayProduction(Integer.parseInt(data.get("当日产量").trim()));
                } else {
                    mainEntity.setOnDayProduction(0);
                }
                mainEntity.setBatchNumber(data.get("批次代码"));
                mainEntity.setBoardNumber(data.get("板号"));
                mainEntity.setDateTime(data.get("日期"));
                mainEntity.setMachineNo(data.get("机型"));
                mainEntity.setProgramA(data.get("A面程序名"));
                mainEntity.setProgramB(data.get("B面程序名"));
                mainEntity.setRatedProduction(data.get("定额产量"));
                mainEntity.setFurnaceTemp(data.get("炉温"));
                mainEntity.setRemark(data.get("备注"));
                if (data.get("维备件") != null && !(data.get("维备件").trim().equals(""))) {
                    mainEntity.setRepairableSpares(Integer.parseInt(data.get("维备件").trim()));
                }else {
                    mainEntity.setRepairableSpares(0);
                }
                mainEntity.setStatusB(data.get("状态B"));
                if(mainEntity.getCumulativeProduction() >= (mainEntity.getPlanned() + mainEntity.getRepairableSpares())){
                    temp = mainEntity;
                    continue;
                }
                mainEntities.add(mainEntity);
            }
            if(temp != null) {
                mainEntities.add(0, temp);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mainEntities;
    }

    public static List<DetailEntity> parseDetailExcel(String fileName){
        List<DetailEntity> detailEntities = null;
        try {
            List<Map<String, String>> datas = ExcelUtil.readExcel(fileName, 0);
            detailEntities = new ArrayList<>();
            for (int i = 0 ; i < datas.size(); i++){
                Map<String, String> data = datas.get(i);
                DetailEntity detailEntity = new DetailEntity();
                if (Integer.parseInt(data.get("sum")) == 0){
                    String partNumber = detailEntities.get(i - 1).getPartNumber();
                    detailEntity.setPartNumber(partNumber);
                    String componentValue = detailEntities.get(i - 1).getComponentValue() + data.get("zhi");
                    detailEntity.setComponentValue(componentValue);
                    detailEntities.get(i-1).setComponentValue(componentValue);
                }
                else {
                    detailEntity.setPartNumber(data.get("disp"));
                    detailEntity.setComponentValue(data.get("zhi"));
                }
                detailEntity.setRemark(data.get("fac"));
                detailEntity.setQuantity(Integer.parseInt(data.get("sum")));
                detailEntity.setTagNumber1(data.get("posi1"));
                detailEntity.setTagNumber2(data.get("posi2"));
                detailEntity.setTagNumber3(data.get("posi3"));
                detailEntity.setTagNumber4(data.get("posi4"));
                detailEntity.setTagNumber5(data.get("posi5"));
                detailEntities.add(detailEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return detailEntities;
    }

    public static void creatDetailExcel(List<DetailEntity> detailEntities,String fileName) throws Exception
    {
        fileName = Environment.getExternalStorageDirectory().getPath() + "/SMTPMS/" + fileName;
        File file = new File(fileName);
        if(!file.exists()){
            file.createNewFile();
        }
        WritableWorkbook wwb = Workbook.createWorkbook(file);
        //创建sheet
        WritableSheet ws = wwb.createSheet("首检记录", 0);
        Label labelPartNumber = new Label(0, 0, "料号");
        Label componentValue = new Label(1, 0, "元件值");
        Label quantity = new Label(2, 0, "数量");
        Label remark = new Label(3, 0, "备注");
        Label tagNumber1 = new Label(4, 0, "位号1");
        Label tagNumber2 = new Label(5, 0, "位号2");
        Label tagNumber3 = new Label(6, 0, "位号3");
        Label tagNumber4 = new Label(7, 0, "位号4");
        Label tagNumber5 = new Label(8, 0, "位号5");
        Label tagNumber6 = new Label(9, 0, "位号6");
        Label tagNumber7 = new Label(10, 0, "位号7");
        Label tagNumber8 = new Label(11, 0, "位号8");
        Label tagNumber9 = new Label(12, 0, "位号9");
        Label tagNumber10 = new Label(13, 0, "位号10");
        ws.addCell(labelPartNumber);
        ws.addCell(componentValue);
        ws.addCell(quantity);
        ws.addCell(remark);
        ws.addCell(tagNumber1);
        ws.addCell(tagNumber2);
        ws.addCell(tagNumber3);
        ws.addCell(tagNumber4);
        ws.addCell(tagNumber5);
        ws.addCell(tagNumber6);
        ws.addCell(tagNumber7);
        ws.addCell(tagNumber8);
        ws.addCell(tagNumber9);
        ws.addCell(tagNumber10);
        for(int i = 0; i < detailEntities.size(); i++){
            Label PartNumberC = new Label(0, (i+1), detailEntities.get(i).getPartNumber());
            Label componentValueC = new Label(1, (i+1), detailEntities.get(i).getComponentValue());
            Label quantityC = new Label(2,  (i+1), String.valueOf(detailEntities.get(i).getQuantity()));
            Label remarkC = new Label(3, (i+1), String.valueOf(detailEntities.get(i).getRemark()));
            Label tagNumber1C;
            if(detailEntities.get(i).getTagNuberIsCheck()[0] == 1){
                WritableFont wfc = new WritableFont(WritableFont.ARIAL,10,WritableFont.NO_BOLD, false,
                        UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.RED);
                WritableCellFormat wcfFC = new WritableCellFormat(wfc);
                tagNumber1C = new Label(4, (i + 1), detailEntities.get(i).getTagNumber1(),wcfFC);
            }else {
                tagNumber1C = new Label(4, (i + 1), detailEntities.get(i).getTagNumber1());
            }
            Label tagNumber2C;
            if(detailEntities.get(i).getTagNuberIsCheck()[1] == 1){
                WritableFont wfc = new WritableFont(WritableFont.ARIAL,10,WritableFont.NO_BOLD, false,
                        UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.RED);
                WritableCellFormat wcfFC = new WritableCellFormat(wfc);
                tagNumber2C = new Label(5, (i + 1), detailEntities.get(i).getTagNumber2(),wcfFC);
            }else {
                tagNumber2C = new Label(5, (i + 1), detailEntities.get(i).getTagNumber2());
            }
            Label tagNumber3C;
            if(detailEntities.get(i).getTagNuberIsCheck()[2] == 1){
                WritableFont wfc = new WritableFont(WritableFont.ARIAL,10,WritableFont.NO_BOLD, false,
                        UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.RED);
                WritableCellFormat wcfFC = new WritableCellFormat(wfc);
                tagNumber3C = new Label(6, (i + 1), detailEntities.get(i).getTagNumber3(),wcfFC);
            }else {
                tagNumber3C = new Label(6, (i + 1), detailEntities.get(i).getTagNumber3());
            }
            Label tagNumber4C ;
            if(detailEntities.get(i).getTagNuberIsCheck()[3] == 1){
                WritableFont wfc = new WritableFont(WritableFont.ARIAL,10,WritableFont.NO_BOLD, false,
                        UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.RED);
                WritableCellFormat wcfFC = new WritableCellFormat(wfc);
                tagNumber4C = new Label(7, (i + 1), detailEntities.get(i).getTagNumber4(),wcfFC);
            }else {
                tagNumber4C = new Label(7, (i + 1), detailEntities.get(i).getTagNumber4());
            }
            Label tagNumber5C;
            if(detailEntities.get(i).getTagNuberIsCheck()[4] == 1){
                WritableFont wfc = new WritableFont(WritableFont.ARIAL,10,WritableFont.NO_BOLD, false,
                        UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.RED);
                WritableCellFormat wcfFC = new WritableCellFormat(wfc);
                tagNumber5C = new Label(8, (i + 1), detailEntities.get(i).getTagNumber5(),wcfFC);
            }else {
                tagNumber5C = new Label(8, (i + 1), detailEntities.get(i).getTagNumber5());
            }
            Label tagNumber6C ;
            if(detailEntities.get(i).getTagNuberIsCheck()[5] == 1){
                WritableFont wfc = new WritableFont(WritableFont.ARIAL,10,WritableFont.NO_BOLD, false,
                        UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.RED);
                WritableCellFormat wcfFC = new WritableCellFormat(wfc);
                tagNumber6C = new Label(9, (i + 1), detailEntities.get(i).getTagNumber6(),wcfFC);
            }else {
                tagNumber6C = new Label(9, (i + 1), detailEntities.get(i).getTagNumber6());
            }
            Label tagNumber7C ;
            if(detailEntities.get(i).getTagNuberIsCheck()[6] == 1){
                WritableFont wfc = new WritableFont(WritableFont.ARIAL,10,WritableFont.NO_BOLD, false,
                        UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.RED);
                WritableCellFormat wcfFC = new WritableCellFormat(wfc);
                tagNumber7C = new Label(10, (i + 1), detailEntities.get(i).getTagNumber7(),wcfFC);
            }else {
                tagNumber7C = new Label(10, (i + 1), detailEntities.get(i).getTagNumber7());
            }

            Label tagNumber8C ;
            if(detailEntities.get(i).getTagNuberIsCheck()[7] == 1){
                WritableFont wfc = new WritableFont(WritableFont.ARIAL,10,WritableFont.NO_BOLD, false,
                        UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.RED);
                WritableCellFormat wcfFC = new WritableCellFormat(wfc);
                tagNumber8C = new Label(11, (i + 1), detailEntities.get(i).getTagNumber8(),wcfFC);
            }else {
                tagNumber8C = new Label(11, (i + 1), detailEntities.get(i).getTagNumber8());
            }
            Label tagNumber9C ;
            if(detailEntities.get(i).getTagNuberIsCheck()[8] == 1){
                WritableFont wfc = new WritableFont(WritableFont.ARIAL,10,WritableFont.NO_BOLD, false,
                        UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.RED);
                WritableCellFormat wcfFC = new WritableCellFormat(wfc);
                tagNumber9C = new Label(12, (i + 1), detailEntities.get(i).getTagNumber9(),wcfFC);
            }else {
                tagNumber9C = new Label(12, (i + 1), detailEntities.get(i).getTagNumber9());
            }

            Label tagNumber10C ;
            if(detailEntities.get(i).getTagNuberIsCheck()[9] == 1){
                WritableFont wfc = new WritableFont(WritableFont.ARIAL,10,WritableFont.NO_BOLD, false,
                        UnderlineStyle.NO_UNDERLINE, jxl.format.Colour.RED);
                WritableCellFormat wcfFC = new WritableCellFormat(wfc);
                tagNumber10C = new Label(13, (i + 1), detailEntities.get(i).getTagNumber10(),wcfFC);
            }else {
                tagNumber10C = new Label(13, (i + 1), detailEntities.get(i).getTagNumber10());
            }
            ws.addCell(PartNumberC);
            ws.addCell(componentValueC);
            ws.addCell(quantityC);
            ws.addCell(remarkC);
            ws.addCell(tagNumber1C);
            ws.addCell(tagNumber2C);
            ws.addCell(tagNumber3C);
            ws.addCell(tagNumber4C);
            ws.addCell(tagNumber5C);
            ws.addCell(tagNumber6C);
            ws.addCell(tagNumber7C);
            ws.addCell(tagNumber8C);
            ws.addCell(tagNumber9C);
            ws.addCell(tagNumber10C);

        }
        wwb.write();// 写入数据
        wwb.close();// 关闭文件

    }
}
