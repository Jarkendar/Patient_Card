package sample.datas_model;

import org.hl7.fhir.dstu3.model.Bundle;

import java.util.Date;

public class MedicalData {
    private String typeName;
    private String name;
    private String ID;
    private Date startDate;
    private Date endDate;
    private String measure;
    private Bundle.BundleEntryComponent source;

    public MedicalData(String typeName, String name, String ID, Date startDate, Date endDate, String measure, Bundle.BundleEntryComponent source) {
        this.typeName = typeName;
        this.name = name;
        this.ID = ID;
        this.startDate = startDate;
        this.endDate = endDate;
        this.measure = measure;
        this.source = source;
    }

    public String getHint(){
        String message = "Type: "+getTypeName()+"\nName: "+getName()+"\nID: "+getID();
        switch (typeName){
            case "Medication Statement":{
                message += "\nStart date: "+getStartDate()+"\nEnd date: "+getEndDate()+"\nDose: ";
                break;
            }
            case "Observation":{
                message += "\nIssued date: "+getStartDate()+"\nMeasure: ";
            }
        }
        return message +  getMeasure();
    }

    public String toString(){
        String message =  "Type: "+getTypeName()+", Name: "+getName()+", ID: "+getID();
        switch (typeName){
            case "Medication Statement":{
                message += ", Start date: "+getStartDate()+", End date: "+", Dose: ";
                break;
            }
            case "Observation":{
                message += ", Issued date: "+getStartDate()+", Measure: ";
            }
        }
        return message +  getMeasure();
    }

    public String getTypeName() {
        return typeName;
    }

    public String getName() {
        return name;
    }

    public String getID() {
        return ID;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getMeasure() {
        return measure;
    }

    public Bundle.BundleEntryComponent getSource() {
        return source;
    }

}
