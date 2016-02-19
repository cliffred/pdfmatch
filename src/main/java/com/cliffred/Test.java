package com.cliffred;

import java.awt.image.BufferedImage;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Test extends Application {

    Image image;

    public Test(BufferedImage bim) {
        image = SwingFXUtils.toFXImage(bim, null);
    }

    @Override
    public void start(Stage primaryStage) {
        Canvas layer2 = new Canvas(700, 300);
        GraphicsContext gc2 = layer2.getGraphicsContext2D();

        gc2.setFill(Color.BLUE);

        Pane root = new Pane(new ImageView(image), layer2);
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.show();

        scene.setOnKeyPressed((evt) -> {
            gc2.clearRect(0, 0, layer2.getWidth(), layer2.getHeight());
            gc2.fillOval(Math.random() * layer2.getWidth(), Math.random() * layer2.getHeight(), 20, 30);
        });
    }
}
