package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.composite.HumanNameDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.NameUseEnum;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Set;


public class Controller {

    private double circleSize = 100.0;
    public LineChart timelineChart;

    public void initialize() {
        XYChart.Series series = new XYChart.Series();
        series.setName("test");

        ObservableList<XYChart.Data> dataObservableList = FXCollections.observableArrayList();

        for (int i = 0; i < 10; i++) {
            XYChart.Data<String, Number> data = new XYChart.Data<>("" + i, 1, "" + i * 17);
            Region region = new Region();
            region.setShape(new Circle(circleSize));
            region.setPrefHeight(circleSize);
            region.setPrefWidth(circleSize);
            try {
                region.setBackground(new Background(new BackgroundImage(new Image(new FileInputStream(new File(getClass().getResource("/images/baseline_accessibility_black_18dp.png").toURI()))), null, null, null, null)));//new BackgroundFill(Paint.valueOf(circleColor),CornerRadii.EMPTY,Insets.EMPTY)));
            } catch (FileNotFoundException | URISyntaxException e) {
                e.printStackTrace();
            }
            data.setNode(region);
            dataObservableList.add(data);
        }
        series.getData().addAll(dataObservableList);
        timelineChart.getData().addAll(series);

        //hint after hover data point
        Set<Node> node = timelineChart.lookupAll(".default-color0.chart-line-symbol.series0.");
        node.forEach((element) -> element.setOnMouseEntered((MouseEvent event1) -> {
            for (int i = 0; i < dataObservableList.size(); i++) {
                if (event1.getSource().toString().contains("data" + i)) {
                    Tooltip t = new Tooltip(dataObservableList.get(i).getExtraValue().toString());
                    Tooltip.install(element, t);
                }
            }
        }));

        //test();

        timelineChart.setPrefWidth(timelineChart.getPrefWidth() * 10);
    }

    @FXML
    private void test() {
        System.out.println("click");

        Logger logger = LoggerFactory.getLogger(Controller.class);
        logger.info("This is how you configure Java Logging with SLF4J");


        FhirContext fhirContext = FhirContext.forDstu2();

        // The following is an example Patient resource
        String msgString = "<Patient xmlns=\"http://hl7.org/fhir\">"
                + "<text><status value=\"generated\" /><div xmlns=\"http://www.w3.org/1999/xhtml\">John Cardinal</div></text>"
                + "<identifier><system value=\"http://orionhealth.com/mrn\" /><value value=\"PRP1660\" /></identifier>"
                + "<name><use value=\"official\" /><family value=\"Cardinal\" /><given value=\"John\" /></name>"
                + "<gender value=\"M\"></gender>"
                + "<address><use value=\"home\" /><line value=\"2222 Home Street\" /></address><active value=\"true\" />"
                + "</Patient>";

// The hapi context object is used to create a new XML parser
// instance. The parser can then be used to parse (or unmarshall) the
// string message into a Patient object
        IParser parser = fhirContext.newXmlParser();
        Patient patient = parser.parseResource(Patient.class, msgString);

// The patient object has accessor methods to retrieve all of the
// data which has been parsed into the instance.
        String patientId = patient.getIdentifier().get(0).getValue();
        String familyName = patient.getName().get(0).getFamily().get(0).getValue();
        String gender = patient.getGender();

        System.out.println(patientId); // PRP1660
        System.out.println(familyName); // Cardinal
        System.out.println(gender); // M

        System.out.println(fhirContext.newXmlParser().encodeResourceToString(patient));

        /**
         * FHIR model types in HAPI are simple POJOs. To create a new
         * one, invoke the default constructor and then
         * start populating values.
         */
        patient = new Patient();

// Add an MRN (a patient identifier)
        IdentifierDt id = patient.addIdentifier();
        id.setSystem("http://example.com/fictitious-mrns");
        id.setValue("MRN001");

// Add a name
        HumanNameDt name = patient.addName();
        name.setUse(NameUseEnum.OFFICIAL);
        name.addFamily("Tester");
        name.addGiven("John");
        name.addGiven("Q");

        patient.setGender(AdministrativeGenderEnum.MALE);

// We can now use a parser to encode this resource into a string.
        String encoded = fhirContext.newXmlParser().encodeResourceToString(patient);
        System.out.println(encoded);


        patient = new Patient();
        patient.addIdentifier().setSystem("http://example.com/fictitious-mrns").setValue("MRN001");
        patient.addName().setUse(NameUseEnum.OFFICIAL).addFamily("Tester").addGiven("John").addGiven("Q");
        patient.setGender(AdministrativeGenderEnum.FEMALE);

        encoded = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        System.out.println(encoded);

        Patient p = fhirContext.newJsonParser().parseResource(Patient.class, encoded);
        System.out.println(p.getName().get(0).getFamily().get(0).getValue());
        System.out.println(p.getGender());
        System.out.println("************************");
        // We're connecting to a DSTU1 compliant server in this example

        IGenericClient client = fhirContext.newRestfulGenericClient("http://fhirtest.uhn.ca/baseDstu2");

// Perform a search
        Bundle results = client
                .search()
                .forResource(Patient.class)
                .where(Patient.FAMILY.matches().value("duck"))
                .returnBundle(ca.uhn.fhir.model.dstu2.resource.Bundle.class)
                .execute();

        System.out.println("Found " + results.getEntry().size() + " patients named 'duck'");
    }

}
