package sample;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Region;
import javafx.scene.shape.Circle;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;


public class Controller {

    private double circleSize = 100.0;
    private String circleColor = "ff0000";
    public LineChart timelineChart;

    public void initialize() {
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Portfolio 1");
        timelineChart.setId("asd");

        for (int i = 0; i < 10; i++) {
            XYChart.Data<String, Number> data = new XYChart.Data<>("" + i, 1);
            Region region = new Region();
            region.setShape(new Circle(circleSize));
            region.setPrefHeight(circleSize);
            region.setPrefWidth(circleSize);
            try {
                region.setBackground(new Background(new BackgroundImage(new Image(new FileInputStream(new File(getClass().getResource("/images/baseline_accessibility_black_18dp.png").toURI()))), null, null, null, null)));//new BackgroundFill(Paint.valueOf(circleColor),CornerRadii.EMPTY,Insets.EMPTY)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            data.setNode(region);
            series1.getData().add(data);
        }

        timelineChart.getData().addAll(series1);


        timelineChart.setPrefWidth(timelineChart.getPrefWidth() * 10);
    }

}
