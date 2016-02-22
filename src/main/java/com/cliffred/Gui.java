package com.cliffred;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class Gui extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException, URISyntaxException {
//        Canvas layer2 = new Canvas(700, 300);
//        GraphicsContext gc2 = layer2.getGraphicsContext2D();
//
//        gc2.setFill(Color.BLUE);

//        Image image = SwingFXUtils.toFXImage(bim, null);
        final ImageView imageView = new ImageView();
        StackPane root = new StackPane();
        root.getChildren().add(imageView);
        root.setPrefSize(300, 300);
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.show();

        File path = new File(this.getClass().getResource("/").toURI());
        File v1 = new File(path, "v1.pdf");
        File v2 = new File(path, "v2.pdf");

        PdfComparer.samePdfFile(v1, v2, imageView);
//
//        Platform.runLater(() -> {
//            try {
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });

//        scene.setOnKeyPressed((evt) -> {
//            gc2.clearRect(0, 0, layer2.getWidth(), layer2.getHeight());
//            gc2.fillOval(Math.random() * layer2.getWidth(), Math.random() * layer2.getHeight(), 20, 30);
//        });
    }
}
