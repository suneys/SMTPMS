package com.yoyo.smtpms.entity;

import com.bin.david.form.annotation.SmartColumn;
import com.bin.david.form.annotation.SmartTable;

/**
 * @author Administrator
 * @date 2019-11-02
 */
@SmartTable
public class PickingEntity {
    /**
     * 料号
     */
    @SmartColumn(id = 1, name = "料号")
    private String partNumber;
    /**
     * 元件值
     */
    @SmartColumn(id = 2, name = "元件值",width = 200)
    private String componentValue;
    /**
     * 数量
     */
    @SmartColumn(id=3, name = "数量")
    private int quantity;
    /**
     * 需求数量
     */
    @SmartColumn(id= 4, name = "需求数量")
    private int requiredQuantity;
    /**
     * 实发数量
     */
    @SmartColumn(id = 5, name = "实发数量")
    private int actualQuantity;
    /**
     *备注
     */
    @SmartColumn(id = 6, name = "备注")
    private String remark;


    public String getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(String partNumber) {
        this.partNumber = partNumber;
    }

    public String getComponentValue() {
        return componentValue;
    }

    public void setComponentValue(String componentValue) {
        this.componentValue = componentValue;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(int requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    public int getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(int actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
