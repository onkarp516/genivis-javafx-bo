package com.opethic.genivis.dto.reqres.sales_tranx;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Row1 {
    @SerializedName("details_id")
    @Expose
    private Integer detailsId;
    @SerializedName("product_id")
    @Expose
    private Integer productId;
    @SerializedName("product_name")
    @Expose
    private String productName;
    @SerializedName("level_a_id")
    @Expose
    private String levelAId;
    @SerializedName("level_b_id")
    @Expose
    private String levelBId;
    @SerializedName("level_c_id")
    @Expose
    private String levelCId;
    @SerializedName("level_a")
    @Expose
    private String levelA;

    public String getLevelA() {
        return levelA;
    }

    public void setLevelA(String levelA) {
        this.levelA = levelA;
    }

    public String getLevelB() {
        return levelB;
    }

    public void setLevelB(String levelB) {
        this.levelB = levelB;
    }

    public String getLevelC() {
        return levelC;
    }

    public void setLevelC(String levelC) {
        this.levelC = levelC;
    }

    @SerializedName("level_b")
    @Expose
    private String levelB;
    @SerializedName("level_c")
    @Expose
    private String levelC;
    @SerializedName("pack_name")
    @Expose
    private String packName;
    @SerializedName("unit_name")
    @Expose
    private String unitName;
    @SerializedName("unitId")
    @Expose
    private Integer unitId;
    @SerializedName("unit_conv")
    @Expose
    private Double unitConv;
    @SerializedName("qty")
    @Expose
    private Double qty;
    @SerializedName("rate")
    @Expose
    private Double rate;
    @SerializedName("base_amt")
    @Expose
    private Double baseAmt;
    @SerializedName("dis_amt")
    @Expose
    private Double disAmt;
    @SerializedName("dis_per")
    @Expose
    private Double disPer;
    @SerializedName("dis_per_cal")
    @Expose
    private Double disPerCal;
    @SerializedName("dis_amt_cal")
    @Expose
    private Double disAmtCal;
    @SerializedName("total_amt")
    @Expose
    private Double totalAmt;
    @SerializedName("gst")
    @Expose
    private Double gst;
    @SerializedName("igst")
    @Expose
    private Double igst;
    @SerializedName("cgst")
    @Expose
    private Double cgst;
    @SerializedName("sgst")
    @Expose
    private Double sgst;
    @SerializedName("total_igst")
    @Expose
    private Double totalIgst;
    @SerializedName("total_cgst")
    @Expose
    private Double totalCgst;
    @SerializedName("total_sgst")
    @Expose
    private Double totalSgst;
    @SerializedName("final_amt")
    @Expose
    private Double finalAmt;
    @SerializedName("free_qty")
    @Expose
    private Double freeQty;
    @SerializedName("dis_per2")
    @Expose
    private Double disPer2;
    @SerializedName("row_dis_amt")
    @Expose
    private Double rowDisAmt;
    @SerializedName("gross_amt")
    @Expose
    private Double grossAmt;
    @SerializedName("grossAmt1")
    @Expose
    private Double grossAmt1;
    @Expose
    private Double invoiceDisAmt;
//    @SerializedName("levelAOpt")
//    @Expose
//    private List<LevelAOpt> levelAOpt;
   @SerializedName("reference_id")
   @Expose
    private Integer referenceId;
    @SerializedName("reference_type")
    @Expose
    private String referenceType;
    @SerializedName("b_detailsId")
    @Expose
    private String bDetailsId;
    @SerializedName("is_batch")
    @Expose
    private Boolean isBatch;

    public Integer getDetailsId() {
        return detailsId;
    }

    public void setDetailsId(Integer detailsId) {
        this.detailsId = detailsId;
    }

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

    public String getLevelAId() {
        return levelAId;
    }

    public void setLevelAId(String levelAId) {
        this.levelAId = levelAId;
    }

    public String getLevelBId() {
        return levelBId;
    }

    public void setLevelBId(String levelBId) {
        this.levelBId = levelBId;
    }

    public String getLevelCId() {
        return levelCId;
    }

    public void setLevelCId(String levelCId) {
        this.levelCId = levelCId;
    }

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    public Integer getUnitId() {
        return unitId;
    }

    public void setUnitId(Integer unitId) {
        this.unitId = unitId;
    }

    public Double getUnitConv() {
        return unitConv;
    }

    public void setUnitConv(Double unitConv) {
        this.unitConv = unitConv;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public Double getBaseAmt() {
        return baseAmt;
    }

    public void setBaseAmt(Double baseAmt) {
        this.baseAmt = baseAmt;
    }

    public Double getDisAmt() {
        return disAmt;
    }

    public void setDisAmt(Double disAmt) {
        this.disAmt = disAmt;
    }

    public Double getDisPer() {
        return disPer;
    }

    public void setDisPer(Double disPer) {
        this.disPer = disPer;
    }

    public Double getDisPerCal() {
        return disPerCal;
    }

    public void setDisPerCal(Double disPerCal) {
        this.disPerCal = disPerCal;
    }

    public Double getDisAmtCal() {
        return disAmtCal;
    }

    public void setDisAmtCal(Double disAmtCal) {
        this.disAmtCal = disAmtCal;
    }

    public Double getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(Double totalAmt) {
        this.totalAmt = totalAmt;
    }

    public Double getGst() {
        return gst;
    }

    public void setGst(Double gst) {
        this.gst = gst;
    }

    public Double getIgst() {
        return igst;
    }

    public void setIgst(Double igst) {
        this.igst = igst;
    }

    public Double getCgst() {
        return cgst;
    }

    public void setCgst(Double cgst) {
        this.cgst = cgst;
    }

    public Double getSgst() {
        return sgst;
    }

    public void setSgst(Double sgst) {
        this.sgst = sgst;
    }

    public Double getTotalIgst() {
        return totalIgst;
    }

    public void setTotalIgst(Double totalIgst) {
        this.totalIgst = totalIgst;
    }

    public Double getTotalCgst() {
        return totalCgst;
    }

    public void setTotalCgst(Double totalCgst) {
        this.totalCgst = totalCgst;
    }

    public Double getTotalSgst() {
        return totalSgst;
    }

    public void setTotalSgst(Double totalSgst) {
        this.totalSgst = totalSgst;
    }

    public Double getFinalAmt() {
        return finalAmt;
    }

    public void setFinalAmt(Double finalAmt) {
        this.finalAmt = finalAmt;
    }

    public Double getFreeQty() {
        return freeQty;
    }

    public void setFreeQty(Double freeQty) {
        this.freeQty = freeQty;
    }

    public Double getDisPer2() {
        return disPer2;
    }

    public void setDisPer2(Double disPer2) {
        this.disPer2 = disPer2;
    }

    public Double getRowDisAmt() {
        return rowDisAmt;
    }

    public void setRowDisAmt(Double rowDisAmt) {
        this.rowDisAmt = rowDisAmt;
    }

    public Double getGrossAmt() {
        return grossAmt;
    }

    public void setGrossAmt(Double grossAmt) {
        this.grossAmt = grossAmt;
    }

    public Double getGrossAmt1() {
        return grossAmt1;
    }

    public void setGrossAmt1(Double grossAmt1) {
        this.grossAmt1 = grossAmt1;
    }

    public Double getInvoiceDisAmt() {
        return invoiceDisAmt;
    }

    public void setInvoiceDisAmt(Double invoiceDisAmt) {
        this.invoiceDisAmt = invoiceDisAmt;
    }

//    public List<LevelAOpt> getLevelAOpt() {
//        return levelAOpt;
//    }

//    public void setLevelAOpt(List<LevelAOpt> levelAOpt) {
//        this.levelAOpt = levelAOpt;
//    }

    public Integer getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Integer referenceId) {
        this.referenceId = referenceId;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getbDetailsId() {
        return bDetailsId;
    }

    public void setbDetailsId(String bDetailsId) {
        this.bDetailsId = bDetailsId;
    }

    public Boolean getIsBatch() {
        return isBatch;
    }

    public void setIsBatch(Boolean isBatch) {
        this.isBatch = isBatch;
    }

//    @Override
//    public String toString() {
//        return "Row1{" +
//                "detailsId=" + detailsId +
//                ", productId=" + productId +
//                ", productName='" + productName + '\'' +
//                ", levelAId='" + levelAId + '\'' +
//                ", levelBId='" + levelBId + '\'' +
//                ", levelCId='" + levelCId + '\'' +
//                ", levelA='" + levelA + '\'' +
//                ", levelB='" + levelB + '\'' +
//                ", levelC='" + levelC + '\'' +
//                ", packName='" + packName + '\'' +
//                ", unitName='" + unitName + '\'' +
//                ", unitId=" + unitId +
//                ", unitConv=" + unitConv +
//                ", qty=" + qty +
//                ", rate=" + rate +
//                ", baseAmt=" + baseAmt +
//                ", disAmt=" + disAmt +
//                ", disPer=" + disPer +
//                ", disPerCal=" + disPerCal +
//                ", disAmtCal=" + disAmtCal +
//                ", totalAmt=" + totalAmt +
//                ", gst=" + gst +
//                ", igst=" + igst +
//                ", cgst=" + cgst +
//                ", sgst=" + sgst +
//                ", totalIgst=" + totalIgst +
//                ", totalCgst=" + totalCgst +
//                ", totalSgst=" + totalSgst +
//                ", finalAmt=" + finalAmt +
//                ", freeQty=" + freeQty +
//                ", disPer2=" + disPer2 +
//                ", rowDisAmt=" + rowDisAmt +
//                ", grossAmt=" + grossAmt +
//                ", grossAmt1=" + grossAmt1 +
//                ", invoiceDisAmt=" + invoiceDisAmt +
//                ", referenceId=" + referenceId +
//                ", referenceType='" + referenceType + '\'' +
//                ", bDetailsId='" + bDetailsId + '\'' +
//                ", isBatch=" + isBatch +
//                '}';
//    }
}
