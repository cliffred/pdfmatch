package com.cliffred;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ImageCanvas extends Application {

    BufferedImage image;

    public ImageCanvas(BufferedImage image) {
        super();
        this.image = image;
        launch();
    }

    @Override
    public void start(Stage stage) {
        final SwingNode swingNode = new SwingNode();

        createSwingContent(swingNode, image);

        StackPane pane = new StackPane();
        pane.getChildren().add(swingNode);

        stage.setTitle("Image");
        stage.setScene(new Scene(pane, 288, 288));
        stage.show();
    }

    private void createSwingContent(final SwingNode swingNode, final BufferedImage image) {
        SwingUtilities.invokeLater(() -> swingNode.setContent(new ImagePanel(image)));
    }

    private static class ImagePanel extends JPanel {

        private BufferedImage image;

        public ImagePanel(BufferedImage image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null);
        }

    }
}
