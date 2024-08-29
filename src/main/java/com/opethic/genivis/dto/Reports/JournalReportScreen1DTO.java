package com.opethic.genivis.dto.Reports;

import javafx.beans.property.SimpleStringProperty;

public class JournalReportScreen1DTO {

    private SimpleStringProperty d_start_date;    //start and end date for filter
    private SimpleStringProperty d_end_date;
    private SimpleStringProperty company_name;
    private SimpleStringProperty row_id;
    private SimpleStringProperty transaction_date;      //date of receipt created
    private SimpleStringProperty voucher_no;           //payment number
    private SimpleStringProperty voucher_id;
    private SimpleStringProperty particulars;       //name of ledger
    private SimpleStringProperty voucher_type;
    private SimpleStringProperty credit;             //credit amount
    private SimpleStringProperty debit;

    public JournalReportScreen1DTO(String d_start_date, String d_end_date, String company_name, String row_id, String transaction_date, String voucher_no, String voucher_id, String particulars, String voucher_type, String credit, String debit) {
        this.d_start_date =new SimpleStringProperty(d_start_date);
        this.d_end_date =new SimpleStringProperty(d_end_date);
        this.company_name =new SimpleStringProperty(company_name);
        this.row_id =new SimpleStringProperty(row_id);
        this.transaction_date =new SimpleStringProperty(transaction_date);
        this.voucher_no =new SimpleStringProperty(voucher_no);
        this.voucher_id =new SimpleStringProperty(voucher_id);
        this.particulars =new SimpleStringProperty(particulars);
        this.voucher_type =new SimpleStringProperty(voucher_type);
        this.credit =new SimpleStringProperty(credit);
        this.debit =new SimpleStringProperty(debit);
    }

    public String getD_start_date() {
        return d_start_date.get();
    }

    public SimpleStringProperty d_start_dateProperty() {
        return d_start_date;
    }

    public void setD_start_date(String d_start_date) {
        this.d_start_date.set(d_start_date);
    }

    public String getD_end_date() {
        return d_end_date.get();
    }

    public SimpleStringProperty d_end_dateProperty() {
        return d_end_date;
    }

    public void setD_end_date(String d_end_date) {
        this.d_end_date.set(d_end_date);
    }

    public String getCompany_name() {
        return company_name.get();
    }

    public SimpleStringProperty company_nameProperty() {
        return company_name;
    }

    public void setCompany_name(String company_name) {
        this.company_name.set(company_name);
    }

    public String getRow_id() {
        return row_id.get();
    }

    public SimpleStringProperty row_idProperty() {
        return row_id;
    }

    public void setRow_id(String row_id) {
        this.row_id.set(row_id);
    }

    public String getTransaction_date() {
        return transaction_date.get();
    }

    public SimpleStringProperty transaction_dateProperty() {
        return transaction_date;
    }

    public void setTransaction_date(String transaction_date) {
        this.transaction_date.set(transaction_date);
    }

    public String getVoucher_no() {
        return voucher_no.get();
    }

    public SimpleStringProperty voucher_noProperty() {
        return voucher_no;
    }

    public void setVoucher_no(String voucher_no) {
        this.voucher_no.set(voucher_no);
    }

    public String getVoucher_id() {
        return voucher_id.get();
    }

    public SimpleStringProperty voucher_idProperty() {
        return voucher_id;
    }

    public void setVoucher_id(String voucher_id) {
        this.voucher_id.set(voucher_id);
    }

    public String getParticulars() {
        return particulars.get();
    }

    public SimpleStringProperty particularsProperty() {
        return particulars;
    }

    public void setParticulars(String particulars) {
        this.particulars.set(particulars);
    }

    public String getVoucher_type() {
        return voucher_type.get();
    }

    public SimpleStringProperty voucher_typeProperty() {
        return voucher_type;
    }

    public void setVoucher_type(String voucher_type) {
        this.voucher_type.set(voucher_type);
    }

    public String getCredit() {
        return credit.get();
    }

    public SimpleStringProperty creditProperty() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit.set(credit);
    }

    public String getDebit() {
        return debit.get();
    }

    public SimpleStringProperty debitProperty() {
        return debit;
    }

    public void setDebit(String debit) {
        this.debit.set(debit);
    }
}