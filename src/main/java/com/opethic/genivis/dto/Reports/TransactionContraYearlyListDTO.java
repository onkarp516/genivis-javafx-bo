package com.opethic.genivis.dto.Reports;

import javafx.beans.property.SimpleStringProperty;

public class TransactionContraYearlyListDTO {
    private SimpleStringProperty month;
    private SimpleStringProperty vouchers;
    private SimpleStringProperty amount;

    public TransactionContraYearlyListDTO(String month, String vouchers, String amount) {
            this.month = new SimpleStringProperty(month);
            this.vouchers = new SimpleStringProperty(vouchers);
            this.amount = new SimpleStringProperty(amount);
        }

    public String getMonth() {
        return month.get();
    }

    public SimpleStringProperty monthProperty() {
        return month;
    }

    public void setMonth(String month) {
        this.month.set(month);
    }

    public String getVouchers() {
        return vouchers.get();
    }

    public SimpleStringProperty vouchersProperty() {
        return vouchers;
    }

    public void setVouchers(String vouchers) {
        this.vouchers.set(vouchers);
    }

    public String getAmount() {
        return amount.get();
    }

    public SimpleStringProperty amountProperty() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount.set(amount);
    }
}
