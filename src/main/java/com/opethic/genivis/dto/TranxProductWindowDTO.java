package com.opethic.genivis.dto;

import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

public class TranxProductWindowDTO {
    private SimpleStringProperty index = new SimpleStringProperty("");
    private SimpleStringProperty id;
    private SimpleStringProperty code;
    private SimpleStringProperty product_name;
    private SimpleStringProperty packing;
    private SimpleStringProperty barcode;
    private SimpleStringProperty brand;
    private SimpleStringProperty mrp;
    private SimpleStringProperty current_stock;
    private SimpleStringProperty unit;
    private SimpleStringProperty sales_rate;
    private SimpleStringProperty is_negative;
    private SimpleStringProperty batch_expiry;
    private SimpleStringProperty tax_per;
    private SimpleStringProperty is_batch;
    private SimpleStringProperty purchaserate;
    private List<UnitRateList> unitRateList= new ArrayList<>();

    public TranxProductWindowDTO(String index, String id, String code,
                                 String product_name, String packing,
                                 String barcode, String brand, String mrp,
                                 String current_stock, String unit,
                                 String sales_rate, String is_negative,
                                 String batch_expiry, String tax_per,
                                 String is_batch, String purchaserate,
                                 List<UnitRateList> unitRateList) {
        this.index = new SimpleStringProperty(index);
        this.id = new SimpleStringProperty(id);
        this.code = new SimpleStringProperty(code);
        this.product_name = new SimpleStringProperty(product_name);
        this.packing = new SimpleStringProperty(packing);
        this.barcode = new SimpleStringProperty(barcode);
        this.brand = new SimpleStringProperty(brand);
        this.mrp = new SimpleStringProperty(mrp);
        this.current_stock = new SimpleStringProperty(current_stock);
        this.unit = new SimpleStringProperty(unit);
        this.sales_rate = new SimpleStringProperty(sales_rate);
        this.is_negative = new SimpleStringProperty(is_negative);
        this.batch_expiry = new SimpleStringProperty(batch_expiry);
        this.tax_per = new SimpleStringProperty(tax_per);
        this.is_batch = new SimpleStringProperty(is_batch);
        this.purchaserate = new SimpleStringProperty(purchaserate);
        this.unitRateList = unitRateList;
    }

    public TranxProductWindowDTO(String index, String id, String code, String product_name, String packing, String barcode, String brand, String mrp, String current_stock,
                                 String unit, String sales_rate, String is_negative, String batch_expiry, String tax_per, String is_batch, String purchaserate) {
        this.index = new SimpleStringProperty(index);
        this.id = new SimpleStringProperty(id);
        this.code = new SimpleStringProperty(code);
        this.product_name = new SimpleStringProperty(product_name);
        this.packing = new SimpleStringProperty(packing);
        this.barcode = new SimpleStringProperty(barcode);
        this.brand = new SimpleStringProperty(brand);
        this.mrp = new SimpleStringProperty(mrp);
        this.current_stock = new SimpleStringProperty(current_stock);
        this.unit = new SimpleStringProperty(unit);
        this.sales_rate = new SimpleStringProperty(sales_rate);
        this.is_negative = new SimpleStringProperty(is_negative);
        this.batch_expiry = new SimpleStringProperty(batch_expiry);
        this.tax_per = new SimpleStringProperty(tax_per);
        this.is_batch = new SimpleStringProperty(is_batch);
        this.purchaserate = new SimpleStringProperty(purchaserate);

    }

    public TranxProductWindowDTO(String id, String code, String product_name, String packing, String barcode, String brand, String mrp, String current_stock,
                                 String unit, String sales_rate, String is_negative, String batch_expiry, String tax_per, String is_batch, String purchaserate) {
        this.id = new SimpleStringProperty(id);
        this.code = new SimpleStringProperty(code);
        this.product_name = new SimpleStringProperty(product_name);
        this.packing = new SimpleStringProperty(packing);
        this.barcode = new SimpleStringProperty(barcode);
        this.brand = new SimpleStringProperty(brand);
        this.mrp = new SimpleStringProperty(mrp);
        this.current_stock = new SimpleStringProperty(current_stock);
        this.unit = new SimpleStringProperty(unit);
        this.sales_rate = new SimpleStringProperty(sales_rate);
        this.is_negative = new SimpleStringProperty(is_negative);
        this.batch_expiry = new SimpleStringProperty(batch_expiry);
        this.tax_per = new SimpleStringProperty(tax_per);
        this.is_batch = new SimpleStringProperty(is_batch);
        this.purchaserate = new SimpleStringProperty(purchaserate);

    }

    public List<UnitRateList> getUnitRateList() {
        return unitRateList;
    }

    public void setUnitRateList(List<UnitRateList> unitRateList) {
        this.unitRateList = unitRateList;
    }

    public String getIndex() {
        return index.get();
    }

    public SimpleStringProperty indexProperty() {
        return index;
    }

    public void setIndex(String index) {
        this.index.set(index);
    }

    public String getId() {
        return id.get();
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id = new SimpleStringProperty(id);
    }

    public String getCode() {
        return code.get();
    }

    public SimpleStringProperty codeProperty() {
        return code;
    }

    public void setCode(String code) {
        this.code = new SimpleStringProperty(code);
    }

    public String getProduct_name() {
        return product_name.get();
    }

    public SimpleStringProperty product_nameProperty() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = new SimpleStringProperty(product_name);
    }

    public String getPacking() {
        return packing.get();
    }

    public SimpleStringProperty packingProperty() {
        return packing;
    }

    public void setPacking(String packing) {
        this.packing = new SimpleStringProperty(packing);
    }

    public String getBarcode() {
        return barcode.get();
    }

    public SimpleStringProperty barcodeProperty() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = new SimpleStringProperty(barcode);
    }

    public String getBrand() {
        return brand.get();
    }

    public SimpleStringProperty brandProperty() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = new SimpleStringProperty(brand);
    }

    public String getMrp() {
        return mrp.get();
    }

    public SimpleStringProperty mrpProperty() {
        return mrp;
    }

    public void setMrp(String mrp) {
        this.mrp = new SimpleStringProperty(mrp);
    }

    public String getCurrent_stock() {
        return current_stock.get();
    }

    public SimpleStringProperty current_stockProperty() {
        return current_stock;
    }

    public void setCurrent_stock(String current_stock) {
        this.current_stock = new SimpleStringProperty(current_stock);
    }

    public String getUnit() {
        return unit.get();
    }

    public SimpleStringProperty unitProperty() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = new SimpleStringProperty(unit);
    }

    public String getSales_rate() {
        return sales_rate.get();
    }

    public SimpleStringProperty sales_rateProperty() {
        return sales_rate;
    }

    public void setSales_rate(String sales_rate) {
        this.sales_rate = new SimpleStringProperty(sales_rate);
    }

    public String getIs_negative() {
        return is_negative.get();
    }

    public SimpleStringProperty is_negativeProperty() {
        return is_negative;
    }

    public void setIs_negative(String is_negative) {
        this.is_negative = new SimpleStringProperty(is_negative);
    }

    public String getBatch_expiry() {
        return batch_expiry.get();
    }

    public SimpleStringProperty batch_expiryProperty() {
        return batch_expiry;
    }

    public void setBatch_expiry(String batch_expiry) {
        this.batch_expiry = new SimpleStringProperty(batch_expiry);
    }

    public String getTax_per() {
        return tax_per.get();
    }

    public SimpleStringProperty tax_perProperty() {
        return tax_per;
    }

    public void setTax_per(String tax_per) {
        this.tax_per = new SimpleStringProperty(tax_per);
    }

    public String getIs_batch() {
        return is_batch.get();
    }

    public SimpleStringProperty is_batchProperty() {
        return is_batch;
    }

    public void setIs_batch(String is_batch) {
        this.is_batch = new SimpleStringProperty(is_batch);
    }

    public String getPurchaserate() {
        return purchaserate.get();
    }

    public SimpleStringProperty purchaserateProperty() {
        return purchaserate;
    }

    public void setPurchaserate(String purchaserate) {
        this.purchaserate = new SimpleStringProperty(purchaserate);
    }

    @Override
    public String toString() {
        return "TranxProductWindowDTO{" +
                "index=" + index +
                ", id=" + id +
                ", code=" + code +
                ", product_name=" + product_name +
                ", packing=" + packing +
                ", barcode=" + barcode +
                ", brand=" + brand +
                ", mrp=" + mrp +
                ", current_stock=" + current_stock +
                ", unit=" + unit +
                ", sales_rate=" + sales_rate +
                ", is_negative=" + is_negative +
                ", batch_expiry=" + batch_expiry +
                ", tax_per=" + tax_per +
                ", is_batch=" + is_batch +
                ", purchaserate=" + purchaserate +
                '}';
    }
}
