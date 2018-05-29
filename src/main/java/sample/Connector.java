package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import org.hl7.fhir.dstu3.model.*;

import java.util.LinkedList;
import java.util.List;

public class Connector {

    private static final String SERVER_ADDRESS = "http://hapi.fhir.org/baseDstu3";

    private FhirContext fhirContext;
    private IGenericClient client;
    private List<Bundle.BundleEntryComponent> result = new LinkedList<>();

    public Connector() {
        fhirContext = FhirContext.forDstu3();
        fhirContext.getRestfulClientFactory().setSocketTimeout(60*1000);
        client = fhirContext.newRestfulGenericClient(SERVER_ADDRESS);
    }

    public List<Bundle.BundleEntryComponent> getAllAvailablePatient() {
        Bundle results = client
                .search()
                .forResource(Patient.class)
                //.where( Patient.FAMILY.matches().value("Smith"))
                .where(new StringClientParam("given").matches().value("Huong"))
                .returnBundle(Bundle.class)
                .count(1000)
                .execute();
        System.out.println(results.getTotal());
        List<Bundle.BundleEntryComponent> entries = new LinkedList<>();
        entries.addAll(results.getEntry());
        while (results.getLink(Bundle.LINK_NEXT) != null) {
            // load next page
            results = client.loadPage().next(results).execute();
            entries.addAll(results.getEntry());
            System.out.println(entries.size());
        }
        return entries;
    }

    public List<Bundle.BundleEntryComponent> getPatientByFamilyName(String familyName) {
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .where( Patient.FAMILY.matches().value(familyName))
                .returnBundle(Bundle.class)
                .count(1000)
                .execute();
        System.out.println(results.getTotal());
        List<Bundle.BundleEntryComponent> entries = new LinkedList<>();
        entries.addAll(results.getEntry());
        while (results.getLink(Bundle.LINK_NEXT) != null) {
            // load next page
            results = client.loadPage().next(results).execute();
            entries.addAll(results.getEntry());
            System.out.println(entries.size());
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
