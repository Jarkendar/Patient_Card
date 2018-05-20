package sample;

import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import sample.datas_model.MedicalData;
import sample.datas_model.MedicationData;
import sample.datas_model.PatientData;

import java.util.*;

public class DataProvider extends Observable implements Runnable {

    private LinkedList<Observer> observers = new LinkedList<>();
    public static final String GET_ALL_PATIENT = "GET_ALL_PATIENT";
    public static final String GET_PATIENT = "GET_PATIENT";

    private Connector connector;
    private String order = "";
    private String patientID = "";
    private LinkedList<PatientData> resultPatients = new LinkedList<>();
    private LinkedList<MedicalData> resultMedicals = new LinkedList<>();
    private LinkedList<MedicationData> medicationList = new LinkedList<>();
    private String filterCondition = "";


    public DataProvider() {
        connector = new Connector();
    }

    @Override
    public void run() {
        switch (order) {
            case GET_ALL_PATIENT: {
                List<Bundle.BundleEntryComponent> entries = connector.getAllAvailablePatient();
                resultPatients = getPatientFromEntries(entries);
                break;
            }
            case GET_PATIENT: {
                List<Bundle.BundleEntryComponent> entries = connector.getEverythingFromPatient(patientID);
                resultMedicals = getMedicalFromEntries(entries);
                break;
            }
        }
        notifyObservers();
    }

    private LinkedList<PatientData> getPatientFromEntries(List<Bundle.BundleEntryComponent> entryComponents) {
        LinkedList<PatientData> patients = new LinkedList<>();
        for (Bundle.BundleEntryComponent entryComponent : entryComponents) {
            Patient patient = (Patient) entryComponent.getResource();
            String id = entryComponent.getFullUrl().split("/")[entryComponent.getFullUrl().split("/").length - 1];
            String name = patient.getName().isEmpty() ? "unknow" : patient.getName().get(0).getFamily();
            String gender = patient.getGender() == null ? "Unknow" : patient.getGender().getDisplay();
            Date birthDate = patient.getBirthDate();
            patients.addLast(new PatientData(id, name, birthDate, gender, entryComponent));
        }
        return patients;
    }

    private LinkedList<MedicalData> getMedicalFromEntries(List<Bundle.BundleEntryComponent> entries) {
        LinkedList<MedicalData> medicals = new LinkedList<>();
        for (Bundle.BundleEntryComponent entryComponent : entries) {
            if (entryComponent.getResource() instanceof MedicationStatement) {
                MedicationStatement medicationStatement = (MedicationStatement) entryComponent.getResource();
                String name = "";
                Date startDate = null;
                Date endDate = null;
                String measure = "";
                try {
                    name = medicationStatement.getMedicationCodeableConcept().getCodingFirstRep().getDisplay();
                    startDate = medicationStatement.getEffectivePeriod().getStart();
                    endDate = medicationStatement.getEffectivePeriod().getEnd();
                    measure = medicationStatement.getDosageFirstRep().getDoseSimpleQuantity().getValue().toString() + medicationStatement.getDosageFirstRep().getDoseSimpleQuantity().getUnit();
                } catch (FHIRException e) {
                    e.printStackTrace();
                }
                medicals.addLast(new MedicalData("Medication Statement", name, entryComponent.getFullUrl().split("/")[entryComponent.getFullUrl().split("/").length - 1], startDate, endDate, measure, entryComponent));
            } else if (entryComponent.getResource() instanceof Observation) {
                Observation observation = (Observation) entryComponent.getResource();
                String name = "";
                Date startDate = null;
                String measure = "";
                try {
                    name = observation.getCode().getText();
                    startDate = observation.getIssued();
                    if (observation.hasValueQuantity()) {
                        measure = observation.getValueQuantity().getValue().toString() + observation.getValueQuantity().getUnit();
                    } else if (observation.hasComponent()){
                        LinkedList<String> names = new LinkedList<>();
                        LinkedList<String> values = new LinkedList<>();
                        for (Observation.ObservationComponentComponent component : observation.getComponent()){
                            names.addLast(component.getCode().getText());
                            values.addLast(component.getValueQuantity().getValue().toString()+component.getValueQuantity().getUnit());
                        }
                        name = String.join("/", names);
                        measure = String.join("/",values);
                    }
                } catch (FHIRException e) {
                    e.printStackTrace();
                }
                medicals.addLast(new MedicalData("Observation", name, entryComponent.getFullUrl().split("/")[entryComponent.getFullUrl().split("/").length - 1], startDate, null, measure, entryComponent));
            } else if (entryComponent.getResource() instanceof Medication) {
                Medication medication = (Medication) entryComponent.getResource();
                medicationList.addLast(new MedicationData(medication.getCode().getCodingFirstRep().getDisplay(), medication.getCode().getCodingFirstRep().getCode(), entryComponent.getFullUrl().split("/")[entryComponent.getFullUrl().split("/").length - 1], entryComponent));
            } else {
                System.out.println("**************************" + entryComponent.getResource()+" "+entryComponent.getFullUrl());
            }
        }
        return medicals;
    }

    public void clearPatientList(){
        resultPatients.clear();
    }

    public void clearMedicalList(){
        resultMedicals.clear();
    }

    public void clearMedicationList(){
        medicationList.clear();
    }

    public LinkedList<PatientData> getResultPatients() {
        return resultPatients;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getOrder() {
        return order;
    }

    public void setPatientID(String patientID) {
        this.patientID = patientID;
    }

    public LinkedList<MedicationData> getMedicationList() {
        return medicationList;
    }

    public LinkedList<MedicalData> getResultMedicals() {
        return resultMedicals;
    }

    public void setFilterCondition(String filterCondition) {
        this.filterCondition = filterCondition;
    }

    @Override
    public synchronized void addObserver(Observer observer) {
        super.addObserver(observer);
        observers.addLast(observer);
    }

    @Override
    public synchronized void deleteObserver(Observer observer) {
        super.deleteObserver(observer);
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        super.notifyObservers();
        for (Observer observer : observers) {
            observer.update(this, order);
        }
    }

    @Override
    public synchronized void deleteObservers() {
        super.deleteObservers();
        observers.clear();
    }
}
