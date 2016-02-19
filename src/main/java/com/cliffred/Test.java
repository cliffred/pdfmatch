package com.cliffred;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Test extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        Canvas layer1 = new Canvas(700, 300);
        Canvas layer2 = new Canvas(700, 300);
        GraphicsContext gc1 = layer1.getGraphicsContext2D();
        GraphicsContext gc2 = layer2.getGraphicsContext2D();

        gc1.setFill(Color.GREEN);
        gc1.setFont(new Font("Comic sans MS", 100));
        gc1.fillText("BACKGROUND", 0, 100);
        gc1.fillText("LAYER", 0, 200);
        gc1.setFont(new Font(30));
        gc1.setFill(Color.RED);
        gc1.fillText("Hold a key", 0, 270);

        gc2.setFill(Color.BLUE);
        Pane root = new Pane(layer1, layer2);
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setOnKeyPressed((evt) -> {
            gc2.clearRect(0, 0, layer2.getWidth(), layer2.getHeight());
            gc2.fillOval(Math.random() * layer2.getWidth(), Math.random() * layer2.getHeight(), 20, 30);
        });
    }

    private void drawShapes(GraphicsContext gc) {

        gc.setFill(Color.GREEN);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(5);
        gc.strokeLine(40, 10, 10, 40);
        gc.fillOval(10, 60, 30, 30);
        gc.strokeOval(60, 60, 30, 30);
        gc.fillRoundRect(110, 60, 30, 30, 10, 10);
        gc.strokeRoundRect(160, 60, 30, 30, 10, 10);
        gc.fillArc(10, 110, 30, 30, 45, 240, ArcType.OPEN);
        gc.fillArc(60, 110, 30, 30, 45, 240, ArcType.CHORD);
        gc.fillArc(110, 110, 30, 30, 45, 240, ArcType.ROUND);
        gc.strokeArc(10, 160, 30, 30, 45, 240, ArcType.OPEN);
        gc.strokeArc(60, 160, 30, 30, 45, 240, ArcType.CHORD);
        gc.strokeArc(110, 160, 30, 30, 45, 240, ArcType.ROUND);
        gc.fillPolygon(new double[] { 10, 40, 10, 40 }, new double[] { 210, 210, 240, 240 }, 4);
        gc.strokePolygon(new double[] { 60, 90, 60, 90 }, new double[] { 210, 210, 240, 240 }, 4);
        gc.strokePolyline(new double[] { 110, 140, 110, 140 }, new double[] { 210, 210, 240, 240 }, 4);
    }
}
