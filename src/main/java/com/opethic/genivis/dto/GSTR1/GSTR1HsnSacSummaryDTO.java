package com.opethic.genivis.dto.GSTR1;
import javafx.beans.property.SimpleStringProperty;

public class GSTR1HsnSacSummaryDTO {
    private SimpleStringProperty id;
    private SimpleStringProperty hsn_sac ;
    private SimpleStringProperty description;
    private SimpleStringProperty type_of_supply;
    private SimpleStringProperty total_quantity;
    private SimpleStringProperty total_volume;
    private SimpleStringProperty tax_rate;
    private SimpleStringProperty taxable_amt;
    private SimpleStringProperty igst_amt;
    private SimpleStringProperty cgst_amt;
    private SimpleStringProperty sgst_amt;
    private SimpleStringProperty total_tax_amt;

    public GSTR1HsnSacSummaryDTO(
            String id,
            String hsn_sac,
            String description,
            String type_of_supply,
            String total_quantity,
            String total_volume,
            String tax_rate,
            String taxable_amt,
            String igst_amt,
            String cgst_amt,
            String sgst_amt,
            String total_tax_amt
    ){
        this.id = new SimpleStringProperty(id);
        this.hsn_sac = new SimpleStringProperty(hsn_sac);
        this.description = new SimpleStringProperty(description);
        this.type_of_supply = new SimpleStringProperty(type_of_supply);
        this.total_quantity = new SimpleStringProperty(total_quantity);
        this.total_volume = new SimpleStringProperty(total_volume);
        this.tax_rate = new SimpleStringProperty(tax_rate);
        this.taxable_amt = new SimpleStringProperty(taxable_amt);
        this.igst_amt = new SimpleStringProperty(igst_amt);
        this.cgst_amt = new SimpleStringProperty(cgst_amt);
        this.sgst_amt = new SimpleStringProperty(sgst_amt);
        this.total_tax_amt = new SimpleStringProperty(total_tax_amt);
    }

    public String getId() {return id.get();}
    public SimpleStringProperty idProperty() {return id;}
    public void setId(String id) { this.id = new SimpleStringProperty(id);}

    public String getHsn_sac() {return hsn_sac.get();}
    public SimpleStringProperty hsn_sacProperty() {return hsn_sac;}
    public void setHsn_sac(String hsn_sac) {this.hsn_sac = new SimpleStringProperty(hsn_sac);}

    public String getDescription() {return description.get();}
    public SimpleStringProperty descriptionProperty() {return description;}
    public void setDescription(String description) {this.description = new SimpleStringProperty(description);}

    public String getType_of_supply() {return type_of_supply.get();}
    public SimpleStringProperty type_of_supplyProperty() {return type_of_supply;}
    public void setType_of_supply(String type_of_supply) {this.type_of_supply = new SimpleStringProperty(type_of_supply);}

    public String getTotal_quantity() {return total_quantity.get();}
    public SimpleStringProperty total_quantityProperty() {return total_quantity;}
    public void setTotal_quantity(String total_quantity) {this.total_quantity = new SimpleStringProperty(total_quantity);}

    public String getTotal_volume() {return total_volume.get();}
    public SimpleStringProperty total_volumeProperty() {return total_volume;}
    public void setTotal_volume(String total_volume) {this.total_volume = new SimpleStringProperty(total_volume);}

    public String getTax_rate() {return tax_rate.get();}
    public SimpleStringProperty tax_rateProperty() {return tax_rate;}
    public void setTax_rate(String tax_rate) {this.tax_rate = new SimpleStringProperty(tax_rate);}

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
