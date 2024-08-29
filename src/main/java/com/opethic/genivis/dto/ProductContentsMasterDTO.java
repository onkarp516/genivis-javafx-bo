package com.opethic.genivis.dto;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Objects;

public class ProductContentsMasterDTO {
    private SimpleIntegerProperty id;
    private SimpleStringProperty contentName;
    private SimpleStringProperty power;
    private SimpleStringProperty packing;
    private SimpleStringProperty contentType;

    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setSerialNumber(int id) {
        this.id.set(id);
    }

    public String getContentName() {
        return contentName.get();
    }

    public SimpleStringProperty contentNameProperty() {
        return contentName;
    }

    public void setContentName(String contentName) {
        this.contentName.set(contentName);
    }

    public String getPower() {
        return power.get();
    }

    public SimpleStringProperty powerProperty() {
        return power;
    }

    public void setPower(String power) {
        this.power.set(power);
    }

    public String getPacking() {
        return packing.get();
    }

    public SimpleStringProperty packingProperty() {
        return packing;
    }

    public void setPacking(String packing) {
        this.packing.set(packing);
    }

    public String getContentType() {
        return contentType.get();
    }

    public SimpleStringProperty contentTypeProperty() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType.set(contentType);
    }

    public ProductContentsMasterDTO(Integer id, String contentName,
                                    String power, String packing, String contentType) {
        this.id =  new SimpleIntegerProperty(id);
        this.contentName = new SimpleStringProperty(contentName);
        this.power = new SimpleStringProperty(power);
        this.packing =new SimpleStringProperty(packing);
        this.contentType = new SimpleStringProperty(contentType);
    }

    @Override
    public String toString() {
        return String.format("{ \"id\": \"%d\", \"contentType\": \"%s\", \"content_power\": \"%s\", \"content_package\": \"%s\", \"contentTypeDose\": \"%s\"}",
                id.getValue(),
                contentName.getValue(),
                power.getValue(),
                packing.getValue(),
                contentType.getValue());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductContentsMasterDTO that = (ProductContentsMasterDTO) o;

        if (!id.getValue().equals(that.id.getValue())) return false;
        if (!contentName.getValue().equals(that.contentName.getValue())) return false;
        if (!power.getValue().equals(that.power.getValue())) return false;
        if (!packing.getValue().equals(that.packing.getValue())) return false;
        return contentType.getValue().equals(that.contentType.getValue());
    }

    @Override
    public int hashCode() {
        int result = id.getValue().hashCode();
        result = 31 * result + contentName.getValue().hashCode();
        result = 31 * result + power.getValue().hashCode();
        result = 31 * result + packing.getValue().hashCode();
        result = 31 * result + contentType.getValue().hashCode();
        return result;
    }
}


