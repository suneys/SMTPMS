package com.yoyo.smtpms.entity;

import com.bin.david.form.annotation.SmartTable;

/**
 * 清单页实体类
 * @author Administrator
 * @date 2018-10-15
 */
@SmartTable
public class DetailEntity {
    private String partNumber;
    private String componentValue;
    private int quantity;
    private String remark;
    private String tagNumber1;
    private String tagNumber2;
    private String tagNumber3;
    private String tagNumber4;
    private String tagNumber5;
    private String tagNumber6;
    private String tagNumber7;
    private String tagNumber8;
    private String tagNumber9;
    private String tagNumber10;
    private int requiredQiantity;

    public int getRequiredQiantity() {
        return requiredQiantity;
    }

    public void setRequiredQiantity(int requiredQiantity) {
        this.requiredQiantity = requiredQiantity;
    }



    /**
     * 0~4表示位号，5表示元件值
     */
    private int[] tagNuberIsCheck = {0,0,0,0,0,0};


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

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTagNumber1() {
        return tagNumber1;
    }

    public void setTagNumber1(String tagNumber1) {
        this.tagNumber1 = tagNumber1;
    }

    public String getTagNumber2() {
        return tagNumber2;
    }

    public void setTagNumber2(String tagNumber2) {
        this.tagNumber2 = tagNumber2;
    }

    public String getTagNumber3() {
        return tagNumber3;
    }

    public void setTagNumber3(String tagNumber3) {
        this.tagNumber3 = tagNumber3;
    }

    public String getTagNumber4() {
        return tagNumber4;
    }

    public void setTagNumber4(String tagNumber4) {
        this.tagNumber4 = tagNumber4;
    }

    public String getTagNumber5() {
        return tagNumber5;
    }

    public void setTagNumber5(String tagNumber5) {
        this.tagNumber5 = tagNumber5;
    }

    public String getTagNumber6() {
        return tagNumber6;
    }

    public void setTagNumber6(String tagNumber6) {
        this.tagNumber6 = tagNumber6;
    }

    public String getTagNumber7() {
        return tagNumber7;
    }

    public void setTagNumber7(String tagNumber7) {
        this.tagNumber7 = tagNumber7;
    }

    public String getTagNumber8() {
        return tagNumber8;
    }

    public void setTagNumber8(String tagNumber8) {
        this.tagNumber8 = tagNumber8;
    }

    public String getTagNumber9() {
        return tagNumber9;
    }

    public void setTagNumber9(String tagNumber9) {
        this.tagNumber9 = tagNumber9;
    }

    public String getTagNumber10() {
        return tagNumber10;
    }

    public void setTagNumber10(String tagNumber10) {
        this.tagNumber10 = tagNumber10;
    }

    public int[] getTagNuberIsCheck() {
        return tagNuberIsCheck;
    }

    public void setTagNuberIsCheck(int[] tagNuberIsCheck) {
        this.tagNuberIsCheck = tagNuberIsCheck;
    }
}
