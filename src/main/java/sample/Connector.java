package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;

import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Connector extends Observable implements Runnable {

    private static final String SERVER_ADDRESS = "http://hapi.fhir.org/baseDstu3";
    public static final String GET_ALL_PATTIENT = "GET_ALL_PATTIENT";

    private LinkedList<Observer> observers = new LinkedList<>();

    private String option = "";
    private List<Bundle.BundleEntryComponent> result = new LinkedList<>();

    public Connector(String option) {
        this.option = option;
    }

    @Override
    public void run() {
        FhirContext fhirContext = FhirContext.forDstu3();
        IGenericClient client = fhirContext.newRestfulGenericClient(SERVER_ADDRESS);

        switch (option) {
            case GET_ALL_PATTIENT: {
                result = getAllAvailablePatient(client);
                break;
            }
        }
        notifyObservers();
    }

    private List<Bundle.BundleEntryComponent> getAllAvailablePatient(IGenericClient client) {
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .returnBundle(Bundle.class)
                .execute();
        System.out.println(results.getTotal());
        List<Bundle.BundleEntryComponent> entries = results.getEntry();
        System.out.println(entries.size());
        for (Bundle.BundleEntryComponent entry : entries) {
            displayEntry(entry);
        }
        return entries;
    }


    private void displayEntry(Bundle.BundleEntryComponent entry) {
        Patient p = (Patient) entry.getResource();//parser.parseResource(Patient.class, entry.toString());
        System.out.println(p.getIdentifier().get(0).getValue() + " " + p.getGender().getDisplay() + " " + p.getBirthDate());
    }

    public List<Bundle.BundleEntryComponent> getResult() {
        return result;
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
            observer.update(this, null);
        }
    }

    @Override
    public synchronized void deleteObservers() {
        super.deleteObservers();
        observers.clear();
    }
}
