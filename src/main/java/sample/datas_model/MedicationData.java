package sample.datas_model;

import org.hl7.fhir.dstu3.model.Bundle;

public class MedicationData {
    private String medicationName;
    private String medicationCode;
    private String ID;
    private Bundle.BundleEntryComponent source;

    public MedicationData(String medicationName, String medicationCode, String ID, Bundle.BundleEntryComponent source) {
        this.medicationName = medicationName;
        this.medicationCode = medicationCode;
        this.ID = ID;
        this.source = source;
    }

    public String getHint() {
        return "Name: " + getMedicationName() + "\nCode: " + medicationCode + "\nID: " + getID();
    }

    public String toString() {
        return "Name: " + getMedicationName() + ", Code: " + medicationCode;
    }

    public String getMedicationName() {
        return medicationName;
    }

    public String getMedicationCode() {
        return medicationCode;
    }

    public String getID() {
        return ID;
    }

    public Bundle.BundleEntryComponent getSource() {
        return source;
    }
}
