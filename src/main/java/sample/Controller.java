package sample;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;


public class Controller implements Observer {

    public Label actualChooser;
    public ListView listAll;
    public Button searchAllButton;
    public Button searchChoosePatientButton;
    public ListView medicationList;

    private DataProvider dataProvider = new DataProvider();


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

        DataProvider dataProvider = new DataProvider();
        dataProvider.setOrder(DataProvider.GET_ALL_PATIENT);
        dataProvider.addObserver(this);
        new Thread(dataProvider).start();


        Logger logger = LoggerFactory.getLogger(Controller.class);
        logger.info("This is how you configure Java Logging with SLF4J");


        FhirContext fhirContext = FhirContext.forDstu3();

        // The following is an example Patient resource
        String msgString = "<Patient xmlns=\"http://hl7.org/fhir\">"
                + "<text><status value=\"generated\" /><div xmlns=\"http://www.w3.org/1999/xhtml\">John Cardinal</div></text>"
                + "<identifier><system value=\"http://orionhealth.com/mrn\" /><value value=\"PRP1660\" /></identifier>"
                + "<name><use value=\"official\" /><family value=\"Cardinal\" /><given value=\"John\" /></name>"
                + "<gender value=\"unknown\"></gender>"
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
        String familyName = patient.getName().get(0).getFamily();
        String gender = patient.getGender().getDisplay();

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
        Identifier id = patient.addIdentifier();
        id.setSystem("http://example.com/fictitious-mrns");
        id.setValue("MRN001");

// Add a name
        HumanName name = patient.addName();
        name.setUse(HumanName.NameUse.OFFICIAL);
        name.setFamily("Tester");
        name.addGiven("John");
        name.addGiven("Q");

        patient.setGender(Enumerations.AdministrativeGender.MALE);

// We can now use a parser to encode this resource into a string.
        String encoded = fhirContext.newXmlParser().encodeResourceToString(patient);
        System.out.println(encoded);


        patient = new Patient();
        patient.addIdentifier().setSystem("http://example.com/fictitious-mrns").setValue("MRN001");
        patient.addName().setUse(HumanName.NameUse.OFFICIAL).setFamily("Tester").addGiven("John").addGiven("Q");
        patient.setGender(Enumerations.AdministrativeGender.FEMALE);

        encoded = fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(patient);
        System.out.println(encoded);

        Patient p = fhirContext.newJsonParser().parseResource(Patient.class, encoded);
        System.out.println(p.getName().get(0).getFamily());
        System.out.println(p.getGender());
        System.out.println("************************");
    }

    @Override
    public void update(Observable observable, Object arg) {
        DataProvider receiver = (DataProvider) observable;
        switch (receiver.getOrder()){
            case DataProvider.GET_ALL_PATIENT:{
                Platform.runLater(() -> {
                    listAll.setItems(FXCollections.observableArrayList(receiver.getResultPatients()));
                });
            }
            case DataProvider.GET_PATIENT:{
                Platform.runLater(() -> {
                    medicationList.setItems(FXCollections.observableArrayList(receiver.getMedicationList()));
                });
            }
        }
    }

    public void searchAll(ActionEvent actionEvent) {
        listAll.getItems().clear();
        medicationList.getItems().clear();
        dataProvider.clearPatientList();
        dataProvider.clearMedicalList();
        dataProvider.clearMedicationList();
        dataProvider.setOrder(DataProvider.GET_ALL_PATIENT);
        dataProvider.addObserver(this);
        new Thread(dataProvider).start();
    }

    public void searchPatient(ActionEvent actionEvent) {
        if (listAll.getSelectionModel().getSelectedItems() != null) {
            medicationList.getItems().clear();
            dataProvider.clearMedicalList();
            dataProvider.clearMedicationList();
            dataProvider.setOrder(DataProvider.GET_PATIENT);
            dataProvider.setPatientID(dataProvider.getResultPatients().get(listAll.getSelectionModel().getSelectedIndex()).getID());
            new Thread(dataProvider).start();
        }
    }
}
