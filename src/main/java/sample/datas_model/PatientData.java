package sample.datas_model;

import org.hl7.fhir.dstu3.model.Bundle;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PatientData {
    private String ID;
    private String name;
    private Date birthDate;
    private String gender;
    private Bundle.BundleEntryComponent source;

    public PatientData(String ID, String name, Date birthDate, String gender, Bundle.BundleEntryComponent source) {
        this.ID = ID;
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.source = source;
    }

    public String getName() {
        return name;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public String getGender() {
        return gender;
    }

    public String getID() {
        return ID;
    }

    public Bundle.BundleEntryComponent getSource() {
        return source;
    }

    private String getPrettyDate() {
        if (getBirthDate() != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm dd-MMM-yyyy");
            return simpleDateFormat.format(getBirthDate());
        } else {
            return null;
        }
    }

    public String getHint() {
        return "Patient: " + getID() + "\nName: " + getName() + "\nGender: " + getGender() + "BirthDate: " + getPrettyDate();
    }

    @Override
    public String toString() {
        return "Patient: " + getID() + ", " + getName() + ", " + getGender() + ", " + getPrettyDate();
    }
}
