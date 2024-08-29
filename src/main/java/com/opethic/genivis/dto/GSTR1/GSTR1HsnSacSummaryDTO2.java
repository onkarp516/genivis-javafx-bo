package com.opethic.genivis.dto.GSTR1;
import javafx.beans.property.SimpleStringProperty;

public class GSTR1HsnSacSummaryDTO2 {
    private SimpleStringProperty sr_no;
    private SimpleStringProperty id;
    private SimpleStringProperty dates;
    private SimpleStringProperty invoice_no;
    private SimpleStringProperty particulars;
    private SimpleStringProperty gstin_no;
    private SimpleStringProperty voucher_type;
    private SimpleStringProperty unit_name;
    private SimpleStringProperty taxable_amt;
    private SimpleStringProperty igst_amt;
    private SimpleStringProperty cgst_amt;
    private SimpleStringProperty sgst_amt;
    private SimpleStringProperty total_tax_amt;

    public GSTR1HsnSacSummaryDTO2(
            String sr_no,
            String id,
            String dates,
            String invoice_no,
            String particulars,
            String gstin_no,
            String voucher_type,
            String unit_name,
            String taxable_amt,
            String igst_amt,
            String cgst_amt,
            String sgst_amt,
            String total_tax_amt
    ){
        this.sr_no = new SimpleStringProperty(sr_no);
        this.id = new SimpleStringProperty(id);
        this.dates = new SimpleStringProperty(dates);
        this.invoice_no = new SimpleStringProperty(invoice_no);
        this.particulars = new SimpleStringProperty(particulars);
        this.gstin_no = new SimpleStringProperty(gstin_no);
        this.voucher_type = new SimpleStringProperty(voucher_type);
        this.unit_name = new SimpleStringProperty(unit_name);
        this.taxable_amt = new SimpleStringProperty(taxable_amt);
        this.igst_amt = new SimpleStringProperty(igst_amt);
        this.cgst_amt = new SimpleStringProperty(cgst_amt);
        this.sgst_amt = new SimpleStringProperty(sgst_amt);
        this.total_tax_amt = new SimpleStringProperty(total_tax_amt);
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

    public String getDates() {return dates.get();}
    public SimpleStringProperty datesProperty() {return dates;}
    public void setDates(String dates) {this.dates = new SimpleStringProperty(dates);}

    public String getInvoice_no() {return invoice_no.get();}
    public SimpleStringProperty invoice_noProperty() {return invoice_no;}
    public void setInvoice_no(String invoice_no) {this.invoice_no = new SimpleStringProperty(invoice_no);}

    public String getParticulars() {return particulars.get();}
    public SimpleStringProperty particularsProperty() {return particulars;}
    public void setParticulars(String particulars) {this.particulars = new SimpleStringProperty(particulars);}

    public String getGstin_no() {return gstin_no.get();}
    public SimpleStringProperty gstin_noProperty() {return gstin_no;}
    public void setGstin_no(String gstin_no) {this.gstin_no = new SimpleStringProperty(gstin_no);}

    public String getVoucher_type() {return voucher_type.get();}
    public SimpleStringProperty voucher_typeProperty() {return voucher_type;}
    public void setVoucher_type(String voucher_type) {this.voucher_type = new SimpleStringProperty(voucher_type);}

    public String getUnit_name() {return unit_name.get();}
    public SimpleStringProperty unit_nameProperty() {return unit_name;}
    public void setUnit_name(String unit_name) {this.unit_name = new SimpleStringProperty(unit_name);}

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

    public String getTotal_tax_amt() {return total_tax_amt.get();}
    public SimpleStringProperty total_tax_amtProperty() {return total_tax_amt;}
    public void setTotal_tax_amt(String total_tax_amt) {this.total_tax_amt = new SimpleStringProperty(total_tax_amt);}

}
