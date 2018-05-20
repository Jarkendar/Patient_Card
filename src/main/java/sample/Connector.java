package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.Patient;

import java.util.LinkedList;
import java.util.List;

public class Connector {

    private static final String SERVER_ADDRESS = "http://hapi.fhir.org/baseDstu3";

    private FhirContext fhirContext;
    private IGenericClient client;
    private List<Bundle.BundleEntryComponent> result = new LinkedList<>();

    public Connector() {
        fhirContext = FhirContext.forDstu3();
        client = fhirContext.newRestfulGenericClient(SERVER_ADDRESS);
    }

    public List<Bundle.BundleEntryComponent> getAllAvailablePatient() {
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .where(new StringClientParam("given").matches().value("Huong"))
                .returnBundle(Bundle.class)
                .limitTo(500)
                .execute();
        List<Bundle.BundleEntryComponent> entries = new LinkedList<>();
        entries.addAll(results.getEntry());
        while (results.getLink(Bundle.LINK_NEXT) != null) {
            // load next page
            results = client.loadPage().next(results).execute();
            entries.addAll(results.getEntry());
        }
        return entries;
    }

    public List<Bundle.BundleEntryComponent> getEverythingFromPatient(String patientID) {
        Parameters parameters = client
                .operation()
                .onInstance(new IdType("Patient", patientID))
                .named("$everything")
                .withNoParameters(Parameters.class)
                .useHttpGet()
                .execute();
        List<Bundle.BundleEntryComponent> entries = new LinkedList<>();

        List<Parameters.ParametersParameterComponent> parameterComponents = parameters.getParameter();
        for (Parameters.ParametersParameterComponent parameterComponent : parameterComponents){
            Bundle bundle = (Bundle) parameterComponent.getResource();
            entries.addAll(bundle.getEntry());
            while (bundle.getLink(Bundle.LINK_NEXT) != null) {
                // load next page
                bundle = client.loadPage().next(bundle).execute();
                entries.addAll(bundle.getEntry());
            }
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

}
