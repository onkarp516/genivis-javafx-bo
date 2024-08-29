package com.opethic.genivis.dto.reqres.pur_tranx;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/*** Adjustment DTO while Purchase return : selecting adjust option ****/
public class PurRtnAdjustmentBillsDTO implements Serializable {
    @SerializedName("invoice_id")
    @Expose
    private Long invoiceId;
    @SerializedName("invoice_unique_id")
    @Expose
    private String invoiceUniqueId;
    @SerializedName("id")
    @Expose
    private Long id;
    @SerializedName("total_amt")
    @Expose
    private Double totalAmt;
    @SerializedName("source")
    @Expose
    private String source;
    @SerializedName("paid_amt")
    @Expose
    private Double paidAmt;
    @SerializedName("remaining_amt")
    @Expose
    private Double remainingAmt;
    @SerializedName("invoice_no")
    @Expose
    private String invoiceNo;

    public PurRtnAdjustmentBillsDTO(Long invoiceId, String invoiceUniqueId, Long id, Double totalAmt, String source,
                                    Double paidAmt, Double remainingAmt, String invoiceNo) {
        this.invoiceId = invoiceId;
        this.invoiceUniqueId = invoiceUniqueId;
        this.id = id;
        this.totalAmt = totalAmt;
        this.source = source;
        this.paidAmt = paidAmt;
        this.remainingAmt = remainingAmt;
        this.invoiceNo = invoiceNo;
    }

    public Long getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceUniqueId() {
        return invoiceUniqueId;
    }

    public void setInvoiceUniqueId(String invoiceUniqueId) {
        this.invoiceUniqueId = invoiceUniqueId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getTotalAmt() {
        return totalAmt;
    }

    public void setTotalAmt(Double totalAmt) {
        this.totalAmt = totalAmt;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Double getPaidAmt() {
        return paidAmt;
    }

    public void setPaidAmt(Double paidAmt) {
        this.paidAmt = paidAmt;
    }

    public Double getRemainingAmt() {
        return remainingAmt;
    }

    public void setRemainingAmt(Double remainingAmt) {
        this.remainingAmt = remainingAmt;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }
}
