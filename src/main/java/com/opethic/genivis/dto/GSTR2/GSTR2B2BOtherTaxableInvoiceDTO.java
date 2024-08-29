package com.opethic.genivis.dto.GSTR2;
import javafx.beans.property.SimpleStringProperty;

public class GSTR2B2BOtherTaxableInvoiceDTO {
    private SimpleStringProperty sr_no;
    private SimpleStringProperty id;
    private SimpleStringProperty particulars;
    private SimpleStringProperty gstin_uin;
    private SimpleStringProperty voucher_count;
    private SimpleStringProperty taxable_amt;
    private SimpleStringProperty igst_amt;
    private SimpleStringProperty cgst_amt;
    private SimpleStringProperty sgst_amt;
    private SimpleStringProperty cess_amt;
    private SimpleStringProperty tax_amt;
    private SimpleStringProperty invoice_amt;

    public GSTR2B2BOtherTaxableInvoiceDTO(
            String sr_no,
            String id,
            String particulars,
            String gstin_uin,
            String voucher_count,
            String taxable_amt,
            String igst_amt,
            String cgst_amt,
            String sgst_amt,
            String cess_amt,
            String tax_amt,
            String invoice_amt
    ){
        this.sr_no = new SimpleStringProperty(sr_no);
        this.id = new SimpleStringProperty(id);
        this.particulars = new SimpleStringProperty(particulars);
        this.gstin_uin = new SimpleStringProperty(gstin_uin);
        this.voucher_count = new SimpleStringProperty(voucher_count);
        this.taxable_amt = new SimpleStringProperty(taxable_amt);
        this.igst_amt = new SimpleStringProperty(igst_amt);
        this.cgst_amt = new SimpleStringProperty(cgst_amt);
        this.sgst_amt = new SimpleStringProperty(sgst_amt);
        this.cess_amt = new SimpleStringProperty(cess_amt);
        this.tax_amt = new SimpleStringProperty(tax_amt);
        this.invoice_amt = new SimpleStringProperty(invoice_amt);
    }

    public String getSr_no() {
        return sr_no.get();
    }

    public SimpleStringProperty sr_noProperty() {
        return sr_no;
    }

    public void setSr_no(String sr_no) {
        this.sr_no.set(sr_no);
    }

    public String getId() {return id.get();}
    public SimpleStringProperty idProperty() {return id;}
    public void setId(String id) { this.id = new SimpleStringProperty(id);}

    public String getParticulars() {return particulars.get();}
    public SimpleStringProperty particularsProperty() {return particulars;}
    public void setParticulars(String particulars) {this.particulars = new SimpleStringProperty(particulars);}

    public String getGstin_uin() {return gstin_uin.get();}
    public SimpleStringProperty gstin_uinProperty() {return gstin_uin;}
    public void setGstin_uin(String gstin_uin) {this.gstin_uin = new SimpleStringProperty(gstin_uin);}

    public String getVoucher_count() {return voucher_count.get();}
    public SimpleStringProperty voucher_countProperty() {return voucher_count;}
    public void setVoucher_count(String voucher_count) {this.voucher_count = new SimpleStringProperty(voucher_count);}

    public String getTaxable_amt() {return taxable_amt.get();}
    public SimpleStringProperty taxable_amtProperty() {return taxable_amt;}
    public void setTaxable_amt(String taxable_amt) {this.taxable_amt = new SimpleStringProperty(taxable_amt);}

    public String getIgst_amt() {return igst_amt.get();}
    public SimpleStringProperty igst_amtProperty() {return igst_amt;}
    public void setIgst_amt(String igst_amt) {this.igst_amt = new SimpleStringProperty(igst_amt);}

    public String getCgst_amt() {return cgst_amt.get();}
    public SimpleStringProperty cgst_amtProperty() {return cgst_amt;}
    public void setCgst_amt(String cgst_amt) {this.cgst_amt = new SimpleStringProperty(cgst_amt);}

    public String getSgst_amt() {return sgst_amt.get();}
    public SimpleStringProperty sgst_amtProperty() {return sgst_amt;}
    public void setSgst_amt(String sgst_amt) {this.sgst_amt = new SimpleStringProperty(sgst_amt);}

    public String getCess_amt() {return cess_amt.get();}
    public SimpleStringProperty cess_amtProperty() {return cess_amt;}
    public void setCess_amt(String cess_amt) {this.cess_amt = new SimpleStringProperty(cess_amt);}

    public String getTax_amt() {return tax_amt.get();}
    public SimpleStringProperty tax_amtProperty() {return tax_amt;}
    public void setTax_amt(String tax_amt) {this.tax_amt = new SimpleStringProperty(tax_amt);}

    public String getInvoice_amt() {return invoice_amt.get();}
    public SimpleStringProperty invoice_amtProperty() {return invoice_amt;}
    public void setInvoice_amt(String invoice_amt) {this.invoice_amt = new SimpleStringProperty(invoice_amt);}
}
