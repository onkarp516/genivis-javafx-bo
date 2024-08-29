package com.opethic.genivis.dto.reqres.receipt;

import com.opethic.genivis.dto.reqres.payment.PaymentRowDTO;

import java.util.ArrayList;
import java.util.List;

public class AccountEntryRedirectionDTO {

    String vouncherNo;

    String  receiptNo;
    String tranxDate;

    String narration;

    String redirect;
    Integer rediCurrIndex;

    String tranxType;
    Long ledgerId;
    List<PaymentRowDTO> rowData=new ArrayList<>();

    public String getVouncherNo() {
        return vouncherNo;
    }

    public void setVouncherNo(String vouncherNo) {
        this.vouncherNo = vouncherNo;
    }

    public String getReceiptNo() {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    public String getTranxDate() {
        return tranxDate;
    }

    public void setTranxDate(String tranxDate) {
        this.tranxDate = tranxDate;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public List<PaymentRowDTO> getRowData() {
        return rowData;
    }

    public void setRowData(List<PaymentRowDTO> rowData) {
        this.rowData = rowData;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;

    }

    public Integer getRediCurrIndex() {
        return rediCurrIndex;
    }

    public void setRediCurrIndex(Integer rediCurrIndex) {
        this.rediCurrIndex = rediCurrIndex;
    }

    public Long getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(Long ledgerId) {
        this.ledgerId = ledgerId;
    }

    public String getTranxType() {
        return tranxType;
    }

    public void setTranxType(String tranxType) {
        this.tranxType = tranxType;
    }
}
