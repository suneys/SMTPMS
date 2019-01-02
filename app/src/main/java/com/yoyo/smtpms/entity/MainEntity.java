package com.yoyo.smtpms.entity;

import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;

import java.util.Date;

/**
 *  主界面实体类
 *
 * @author yoyo
 * @date 2018/09/12
 */
@SmartTable
public class MainEntity {
    private int row;
    @SmartColumn(id = 1, name = "日期")
    private String dateTime;
    @SmartColumn(id = 2, name = "机型")
    private String machineNo;
    @SmartColumn(id = 3, name = "批次代码")
    private String batchNumber;
    @SmartColumn(id = 4, name = "A面程序名")
    private String programA;
    @SmartColumn(id = 5, name = "B面程序名")
    private String programB;
    @SmartColumn(id = 6, name = "状态B")
    private String statusB;
    @SmartColumn(id = 7, name = "板号")
    private String boardNumber;
    @SmartColumn(id = 8, name = "计划数量")
    private int planned;
    @SmartColumn(id = 9, name = "维备件")
    private int repairableSpares;
    @SmartColumn(id = 10, name = "状态A")
    private String statusA;
    @SmartColumn(id = 11, name = "当日产量")
    private int onDayProduction;
    @SmartColumn(id = 12, name = "累计产量")
    private int cumulativeProduction;
    @SmartColumn(id = 13, name = "定额产量")
    private String ratedProduction;
    @SmartColumn(id = 14, name = "炉温")
    private String furnaceTemp;
    @SmartColumn(id = 15, name = "备注")
    private String remark;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getMachineNo() {
        return machineNo;
    }

    public void setMachineNo(String machineNo) {
        this.machineNo = machineNo;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getProgramA() {
        return programA;
    }

    public void setProgramA(String programA) {
        this.programA = programA;
    }

    public String getProgramB() {
        return programB;
    }

    public void setProgramB(String programB) {
        this.programB = programB;
    }

    public String getStatusB() {
        return statusB;
    }

    public void setStatusB(String statusB) {
        this.statusB = statusB;
    }

    public String getBoardNumber() {
        return boardNumber;
    }

    public void setBoardNumber(String boardNumber) {
        this.boardNumber = boardNumber;
    }

    public int getPlanned() {
        return planned;
    }

    public void setPlanned(int planned) {
        this.planned = planned;
    }

    public int getRepairableSpares() {
        return repairableSpares;
    }

    public void setRepairableSpares(int repairableSpares) {
        this.repairableSpares = repairableSpares;
    }

    public String getStatusA() {
        return statusA;
    }

    public void setStatusA(String statusA) {
        this.statusA = statusA;
    }

    public int getOnDayProduction() {
        return onDayProduction;
    }

    public void setOnDayProduction(int onDayProduction) {
        this.onDayProduction = onDayProduction;
    }

    public int getCumulativeProduction() {
        return cumulativeProduction;
    }

    public void setCumulativeProduction(int cumulativeProduction) {
        this.cumulativeProduction = cumulativeProduction;
    }

    public String getRatedProduction() {
        return ratedProduction;
    }

    public void setRatedProduction(String ratedProduction) {
        this.ratedProduction = ratedProduction;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getFurnaceTemp() {
        return furnaceTemp;
    }

    public void setFurnaceTemp(String furnaceTemp) {
        this.furnaceTemp = furnaceTemp;
    }
}
