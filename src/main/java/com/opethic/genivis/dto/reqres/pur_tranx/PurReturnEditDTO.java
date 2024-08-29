package com.opethic.genivis.dto.reqres.pur_tranx;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.opethic.genivis.dto.GstDetailsDTO;

import java.io.Serializable;
import java.util.List;

public class PurReturnEditDTO implements Serializable {
    @SerializedName("tcs_amt")
    @Expose
    private Double tcsAmt;
    @SerializedName("tcs_per")
    @Expose
    private Double tcsPer;
    @SerializedName("tcs_mode")
    @Expose
    private String tcsMode;
    @SerializedName("narration")
    @Expose
    private String narration;
    @SerializedName("paymentMode")
    @Expose
    private String paymentMode;
    @SerializedName("discountLedgerId")
    @Expose
    private Long discountLedgerId;
    @SerializedName("discountInAmt")
    @Expose
    private Double discountInAmt;
    @SerializedName("discountInPer")
    @Expose
    private Double discountInPer;
    @SerializedName("totalPurchaseDiscountAmt")
    @Expose
    private Double totalPurchaseDiscountAmt;
    @SerializedName("totalQty")
    @Expose
    private Long totalQty;
    @SerializedName("totalFreeQty")
    @Expose
    private Double totalFreeQty;
    @SerializedName("grossTotal")
    @Expose
    private Double grossTotal;
    @SerializedName("totalTax")
    @Expose
    private Double totalTax;
    @SerializedName("additionLedger1")
    @Expose
    private Long additionLedger1;
    @SerializedName("additionLedgerAmt1")
    @Expose
    private Double additionLedgerAmt1;
    @SerializedName("additionLedger2")
    @Expose
    private Long additionLedger2;
    @SerializedName("additionLedgerAmt2")
    @Expose
    private Double additionLedgerAmt2;
    @SerializedName("additionLedger3")
    @Expose
    private Long additionLedger3;
    @SerializedName("additionLedgerAmt3")
    @Expose
    private Double additionLedgerAmt3;
    @SerializedName("barcode_list")
    @Expose
    private List<Barcode> barcodeList;
    @SerializedName("additionalCharges")
    @Expose
    private List<AdditionalCharge> additionalCharges;
    @SerializedName("gstDetails")
    @Expose
    private List<GstDetailsDTO> gstDetails;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("responseStatus")
    @Expose
    private Integer responseStatus;
    @SerializedName("invoice_data")
    @Expose
    private InvoiceData invoiceData;
    @SerializedName("row")
    @Expose
    private List<TranxPurRowResEditDTO> row;
    @SerializedName("billLst")
    @Expose
    private List<PurRtnAdjustmentBillsDTO> bills;

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public List<PurRtnAdjustmentBillsDTO> getBills() {
        return bills;
    }

    public void setBills(List<PurRtnAdjustmentBillsDTO> bills) {
        this.bills = bills;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public Long getDiscountLedgerId() {
        return discountLedgerId;
    }

    public void setDiscountLedgerId(Long discountLedgerId) {
        this.discountLedgerId = discountLedgerId;
    }

    public Double getDiscountInAmt() {
        return discountInAmt;
    }

    public void setDiscountInAmt(Double discountInAmt) {
        this.discountInAmt = discountInAmt;
    }

    public Double getDiscountInPer() {
        return discountInPer;
    }

    public void setDiscountInPer(Double discountInPer) {
        this.discountInPer = discountInPer;
    }

    public Double getTotalPurchaseDiscountAmt() {
        return totalPurchaseDiscountAmt;
    }

    public void setTotalPurchaseDiscountAmt(Double totalPurchaseDiscountAmt) {
        this.totalPurchaseDiscountAmt = totalPurchaseDiscountAmt;
    }

    public Long getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(Long totalQty) {
        this.totalQty = totalQty;
    }

    public Double getTotalFreeQty() {
        return totalFreeQty;
    }

    public void setTotalFreeQty(Double totalFreeQty) {
        this.totalFreeQty = totalFreeQty;
    }

    public Double getGrossTotal() {
        return grossTotal;
    }

    public void setGrossTotal(Double grossTotal) {
        this.grossTotal = grossTotal;
    }

    public Double getTotalTax() {
        return totalTax;
    }

    public void setTotalTax(Double totalTax) {
        this.totalTax = totalTax;
    }

    public Long getAdditionLedger1() {
        return additionLedger1;
    }

    public void setAdditionLedger1(Long additionLedger1) {
        this.additionLedger1 = additionLedger1;
    }

    public Double getAdditionLedgerAmt1() {
        return additionLedgerAmt1;
    }

    public void setAdditionLedgerAmt1(Double additionLedgerAmt1) {
        this.additionLedgerAmt1 = additionLedgerAmt1;
    }

    public Long getAdditionLedger2() {
        return additionLedger2;
    }

    public void setAdditionLedger2(Long additionLedger2) {
        this.additionLedger2 = additionLedger2;
    }

    public Double getAdditionLedgerAmt2() {
        return additionLedgerAmt2;
    }

    public void setAdditionLedgerAmt2(Double additionLedgerAmt2) {
        this.additionLedgerAmt2 = additionLedgerAmt2;
    }

    public Long getAdditionLedger3() {
        return additionLedger3;
    }

    public void setAdditionLedger3(Long additionLedger3) {
        this.additionLedger3 = additionLedger3;
    }

    public Double getAdditionLedgerAmt3() {
        return additionLedgerAmt3;
    }

    public void setAdditionLedgerAmt3(Double additionLedgerAmt3) {
        this.additionLedgerAmt3 = additionLedgerAmt3;
    }

    public List<Barcode> getBarcodeList() {
        return barcodeList;
    }

    public void setBarcodeList(List<Barcode> barcodeList) {
        this.barcodeList = barcodeList;
    }

    public List<AdditionalCharge> getAdditionalCharges() {
        return additionalCharges;
    }

    public void setAdditionalCharges(List<AdditionalCharge> additionalCharges) {
        this.additionalCharges = additionalCharges;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public InvoiceData getInvoiceData() {
        return invoiceData;
    }

    public void setInvoiceData(InvoiceData invoiceData) {
        this.invoiceData = invoiceData;
    }

    public List<TranxPurRowResEditDTO> getRow() {
        return row;
    }

    public void setRow(List<TranxPurRowResEditDTO> row) {
        this.row = row;
    }

    public List<GstDetailsDTO> getGstDetails() {
        return gstDetails;
    }

    public void setGstDetails(List<GstDetailsDTO> gstDetails) {
        this.gstDetails = gstDetails;
    }

    public Double getTcsAmt() {
        return tcsAmt;
    }

    public void setTcsAmt(Double tcsAmt) {
        this.tcsAmt = tcsAmt;
    }

    public Double getTcsPer() {
        return tcsPer;
    }

    public void setTcsPer(Double tcsPer) {
        this.tcsPer = tcsPer;
    }

    public String getTcsMode() {
        return tcsMode;
    }

    public void setTcsMode(String tcsMode) {
        this.tcsMode = tcsMode;
    }
}
