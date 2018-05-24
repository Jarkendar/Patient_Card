package sample;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;
import sample.datas_model.MedicalData;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.*;


public class Controller implements Observer {

    public Label actualChooser;
    public ListView listAll;
    public Button searchAllButton;
    public Button searchChoosePatientButton;
    public ListView medicationList;
    public Button searchByFamilyNameButton;
    public TextField familyNameTextField;

    private DataProvider dataProvider = new DataProvider();


    private double circleSize = 100.0;
    public LineChart timelineChart;


    @Override
    public void update(Observable observable, Object arg) {
        DataProvider receiver = (DataProvider) observable;
        switch (receiver.getOrder()){
            case DataProvider.GET_ALL_PATIENT:{
                Platform.runLater(() -> {
                    listAll.setItems(FXCollections.observableArrayList(receiver.getResultPatients()));
                });
                break;
            }
            case DataProvider.GET_PATIENT:{
                Platform.runLater(() -> {
                    medicationList.setItems(FXCollections.observableArrayList(receiver.getMedicationList()));
                    buildTimeline(receiver.getResultMedicals());
                });
                break;
            }
            case DataProvider.GET_PATIENT_BY_FAMILY_NAME:{
                Platform.runLater(() -> {
                    listAll.setItems(FXCollections.observableArrayList(receiver.getResultPatients()));
                });
                break;
            }
        }
    }

    private void buildTimeline(LinkedList<MedicalData> medicalData){
        Platform.runLater(() -> {
            System.out.println(timelineChart.getPrefWidth()+"--");
            setDefaultTimeline();
            System.out.println(medicalData.size());
            System.out.println(timelineChart.getPrefWidth()*medicalData.size());
            createSeriesFromMedicalData(medicalData);
            timelineChart.setPrefWidth(timelineChart.getPrefWidth()*medicalData.size());
        });
    }

    private void createSeriesFromMedicalData(LinkedList<MedicalData> medicalData){
        XYChart.Series series = new XYChart.Series();
        series.setName("Patient data");

        medicalData.sort(Comparator.comparing(MedicalData::getStartDate));

        ObservableList<XYChart.Data<String, Number>> dataObservableList = FXCollections.observableArrayList();
        System.out.println("medical data size = "+medicalData.size());
        int i = 1;
        for (MedicalData medical : medicalData) {
            XYChart.Data<String, Number> data = new XYChart.Data<>((i++)+") "+medical.getPrettyDate(), 1, medical);
            Region region = new Region();
            region.setShape(new Circle(circleSize));
            region.setPrefHeight(circleSize);
            region.setPrefWidth(circleSize);
            try {
                switch (medical.getTypeName()){
                    case "Medication Statement":{
                        region.setBackground(new Background(new BackgroundImage(new Image(new FileInputStream(new File(getClass().getResource("/images/syringe.png").toURI()))), null, null, null, null)));
                        break;
                    }
                    case "Observation":{
                        region.setBackground(new Background(new BackgroundImage(new Image(new FileInputStream(new File(getClass().getResource("/images/lupe.png").toURI()))), null, null, null, null)));
                        break;
                    }
                }
            } catch (FileNotFoundException | URISyntaxException e) {
                e.printStackTrace();
            }
            data.setNode(region);
            dataObservableList.add(data);
        }
        System.out.println("observable = "+dataObservableList.size());
        series.getData().addAll(dataObservableList);
        System.out.println(series.getData().size());
        timelineChart.getData().addAll(series);
        setListenersToNodeChart(timelineChart.lookupAll(".default-color0.chart-line-symbol.series0."), dataObservableList);

    }

    private void setListenersToNodeChart(Set<Node> nodes, ObservableList<XYChart.Data<String, Number>> dataObservableList){
        nodes.forEach((element) -> element.setOnMouseEntered((MouseEvent event) -> {
            for (int i = 0; i< dataObservableList.size(); i++){
                if (event.getSource().toString().contains("data"+i)){
                    Tooltip tooltip = new Tooltip(((MedicalData)dataObservableList.get(i).getExtraValue()).getHint());
                    Tooltip.install(element, tooltip);
                }
            }
        }));
    }

    private void setDefaultTimeline(){
        timelineChart.setPrefWidth(250.0);
        for (int i =0; i<timelineChart.getData().size(); i++){
            timelineChart.getData().remove(i);
        }
    }

    public void searchAll(ActionEvent actionEvent) {
        dataProvider.deleteObservers();
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
            dataProvider.deleteObservers();
            medicationList.getItems().clear();
            dataProvider.clearMedicalList();
            dataProvider.clearMedicationList();
            dataProvider.setOrder(DataProvider.GET_PATIENT);
            dataProvider.setPatientInfo(dataProvider.getResultPatients().get(listAll.getSelectionModel().getSelectedIndex()).getID());
            dataProvider.addObserver(this);
            new Thread(dataProvider).start();
        }
    }

    public void searchPatientByFamilyName(ActionEvent actionEvent) {
        if (!familyNameTextField.getText().equals("")){
            dataProvider.deleteObservers();
            listAll.getItems().clear();
            medicationList.getItems().clear();
            dataProvider.clearPatientList();
            dataProvider.clearMedicalList();
            dataProvider.clearMedicationList();
            dataProvider.setOrder(DataProvider.GET_PATIENT_BY_FAMILY_NAME);
            dataProvider.setPatientInfo(familyNameTextField.getText());
            dataProvider.addObserver(this);
            new Thread(dataProvider).start();
        }
    }
}
