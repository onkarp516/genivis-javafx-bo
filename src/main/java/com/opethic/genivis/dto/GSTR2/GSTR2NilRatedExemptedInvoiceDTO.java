package com.opethic.genivis.dto.GSTR2;
import javafx.beans.property.SimpleStringProperty;

public class GSTR2NilRatedExemptedInvoiceDTO {
    private SimpleStringProperty sr_no;
    private SimpleStringProperty id;
    private SimpleStringProperty voucher_type;
    private SimpleStringProperty particulars;
    private SimpleStringProperty nil_rated;
    private SimpleStringProperty exempted;

    public GSTR2NilRatedExemptedInvoiceDTO(
            String sr_no,
            String id,
            String voucher_type,
            String particulars,
            String nil_rated,
            String exempted
    ){
        this.sr_no = new SimpleStringProperty(sr_no);
        this.id = new SimpleStringProperty(id);
        this.voucher_type = new SimpleStringProperty(voucher_type);
        this.particulars = new SimpleStringProperty(particulars);
        this.nil_rated = new SimpleStringProperty(nil_rated);
        this.exempted = new SimpleStringProperty(exempted);
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

    public String getVoucher_type() {return voucher_type.get();}
    public SimpleStringProperty voucher_typeProperty() {return voucher_type;}
    public void setVoucher_type(String voucher_type) {this.voucher_type = new SimpleStringProperty(voucher_type);}

    public String getParticulars() {return particulars.get();}
    public SimpleStringProperty particularsProperty() {return particulars;}
    public void setParticulars(String particulars) {this.particulars = new SimpleStringProperty(particulars);}

    public String getNil_rated() {return nil_rated.get();}
    public SimpleStringProperty nil_ratedProperty() {return nil_rated;}
    public void setNil_rated(String nil_rated) {this.nil_rated = new SimpleStringProperty(nil_rated);}

    public String getExempted() {return exempted.get();}
    public SimpleStringProperty exemptedProperty() {return exempted;}
    public void setExempted(String exempted) {this.exempted = new SimpleStringProperty(exempted);}
}
