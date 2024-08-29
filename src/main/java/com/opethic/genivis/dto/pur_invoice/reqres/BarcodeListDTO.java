package com.opethic.genivis.dto.pur_invoice.reqres;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BarcodeListDTO {


    @SerializedName("product_id")
    @Expose
    private Integer productId;
    @SerializedName("product_name")
    @Expose
    private String productName;
    @SerializedName("barcode_id")
    @Expose
    private Integer barcodeId;
    @SerializedName("barcode_no")
    @Expose
    private String barcodeNo;
    @SerializedName("batch_id")
    @Expose
    private String batchId;
    @SerializedName("mrp")
    @Expose
    private Float mrp;
    @SerializedName("tranx_date")
    @Expose
    private String tranxDate;
    @SerializedName("transaction_id")
    @Expose
    private Integer transactionId;
    @SerializedName("packing_id")
    @Expose
    private String packingId;
    @SerializedName("packing_name")
    @Expose
    private String packingName;
    @SerializedName("units_id")
    @Expose
    private String unitsId;
    @SerializedName("units_name")
    @Expose
    private String unitsName;
    @SerializedName("levela_id")
    @Expose
    private String levelaId;
    @SerializedName("levela_name")
    @Expose
    private String levelaName;
    @SerializedName("levelb_id")
    @Expose
    private String levelbId;
    @SerializedName("levelb_name")
    @Expose
    private String levelbName;
    @SerializedName("levelc_id")
    @Expose
    private String levelcId;
    @SerializedName("levelc_name")
    @Expose
    private String levelcName;
    @SerializedName("print_qty")
    @Expose
    private Integer printQty;
    @SerializedName("product_qty")
    @Expose
    private Integer productQty;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getBarcodeId() {
        return barcodeId;
    }

    public void setBarcodeId(Integer barcodeId) {
        this.barcodeId = barcodeId;
    }

    public String getBarcodeNo() {
        return barcodeNo;
    }

    public void setBarcodeNo(String barcodeNo) {
        this.barcodeNo = barcodeNo;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Float getMrp() {
        return mrp;
    }

    public void setMrp(Float mrp) {
        this.mrp = mrp;
    }

    public String getTranxDate() {
        return tranxDate;
    }

    public void setTranxDate(String tranxDate) {
        this.tranxDate = tranxDate;
    }

    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public String getPackingId() {
        return packingId;
    }

    public void setPackingId(String packingId) {
        this.packingId = packingId;
    }

    public String getPackingName() {
        return packingName;
    }

    public void setPackingName(String packingName) {
        this.packingName = packingName;
    }

    public String getUnitsId() {
        return unitsId;
    }

    public void setUnitsId(String unitsId) {
        this.unitsId = unitsId;
    }

    public String getUnitsName() {
        return unitsName;
    }

    public void setUnitsName(String unitsName) {
        this.unitsName = unitsName;
    }

    public String getLevelaId() {
        return levelaId;
    }

    public void setLevelaId(String levelaId) {
        this.levelaId = levelaId;
    }

    public String getLevelaName() {
        return levelaName;
    }

    public void setLevelaName(String levelaName) {
        this.levelaName = levelaName;
    }

    public String getLevelbId() {
        return levelbId;
    }

    public void setLevelbId(String levelbId) {
        this.levelbId = levelbId;
    }

    public String getLevelbName() {
        return levelbName;
    }

    public void setLevelbName(String levelbName) {
        this.levelbName = levelbName;
    }

    public String getLevelcId() {
        return levelcId;
    }

    public void setLevelcId(String levelcId) {
        this.levelcId = levelcId;
    }

    public String getLevelcName() {
        return levelcName;
    }

    public void setLevelcName(String levelcName) {
        this.levelcName = levelcName;
    }

    public Integer getPrintQty() {
        return printQty;
    }

    public void setPrintQty(Integer printQty) {
        this.printQty = printQty;
    }

    public Integer getProductQty() {
        return productQty;
    }

    public void setProductQty(Integer productQty) {
        this.productQty = productQty;
    }


    @Override
    public String toString() {
        return "BarcodeListDTO{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", barcodeId=" + barcodeId +
                ", barcodeNo='" + barcodeNo + '\'' +
                ", batchId='" + batchId + '\'' +
                ", mrp=" + mrp +
                ", tranxDate='" + tranxDate + '\'' +
                ", transactionId=" + transactionId +
                ", packingId='" + packingId + '\'' +
                ", packingName='" + packingName + '\'' +
                ", unitsId='" + unitsId + '\'' +
                ", unitsName='" + unitsName + '\'' +
                ", levelaId='" + levelaId + '\'' +
                ", levelaName='" + levelaName + '\'' +
                ", levelbId='" + levelbId + '\'' +
                ", levelbName='" + levelbName + '\'' +
                ", levelcId='" + levelcId + '\'' +
                ", levelcName='" + levelcName + '\'' +
                ", printQty=" + printQty +
                ", productQty=" + productQty +
                '}';
    }
}