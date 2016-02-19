package com.cliffred;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import javafx.application.Application;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PdfComparer {

    private PdfComparer() {
    }

    public static boolean samePdfFile(File doc1, File doc2) throws IOException {
        try (
                PDDocument pdDoc1 = PDDocument.load(doc1);
                PDDocument pdDoc2 = PDDocument.load(doc2);
        ) {
            PDFRenderer doc1Renderer = new PDFRenderer(pdDoc1);
            PDFRenderer doc2Renderer = new PDFRenderer(pdDoc2);

            if (pdDoc1.getNumberOfPages() != pdDoc2.getNumberOfPages()) {
                return false;
            }

            for (int page = 0; page < pdDoc1.getNumberOfPages(); ++page) {
                BufferedImage bim1 = doc1Renderer.renderImage(page);
                BufferedImage bim2 = doc2Renderer.renderImage(page);

                if (!equalImages(bim1, bim2)) {
                    createDiffImage(bim1, bim2);
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean equalImages(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            int width = img1.getWidth();
            int height = img1.getHeight();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private static void createDiffImage(BufferedImage img1, BufferedImage img2) throws IOException {

        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            BufferedImage diffImage = new BufferedImage(img1.getWidth(), img1.getHeight(), img1.getType());
            int width = img1.getWidth();
            int height = img1.getHeight();

            List<Point> diffPoints = new ArrayList<>();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (img1.getRGB(x, y) == img2.getRGB(x, y)) {
                        diffImage.setRGB(x, y, img1.getRGB(x, y));
                    } else {
                        diffImage.setRGB(x, y, Color.CYAN.getRGB());
                        diffPoints.add(new Point(x, y));
                    }
                }
            }

            List<ClusterSquare> squares = new ArrayList<>();
            for (Point point : diffPoints) {
                if (!addToExisting(squares, point)) {
                    squares.add(new ClusterSquare(point, 5));
                }
            }

            for (ClusterSquare square : squares) {
                for (int x = square.topLeft.x; x <= square.topRight.x; x++) {
                    diffImage.setRGB(x, square.topLeft.y, Color.MAGENTA.getRGB());
                }
                for (int y = square.topRight.y; y <= square.bottomRight.y; y++) {
                    diffImage.setRGB(square.topRight.x, y, Color.MAGENTA.getRGB());
                }
                for (int x = square.bottomLeft.x; x <= square.bottomRight.x; x++) {
                    diffImage.setRGB(x, square.bottomLeft.y, Color.MAGENTA.getRGB());
                }
                for (int y = square.topLeft.y; y <= square.bottomLeft.y; y++) {
                    diffImage.setRGB(square.topLeft.x, y, Color.MAGENTA.getRGB());
                }
            }
            new ImageCanvas(diffImage);
            ImageIO.write(diffImage, "png", new File("diff.png"));
        }
    }

    private static boolean addToExisting(List<ClusterSquare> squares, Point point) {
        for (ClusterSquare square : squares) {
            if (square.add(point)) {
                return true;
            }
        }
        return false;
    }

    private static class ClusterSquare {

        final double maxDistance;

        Point topLeft;

        Point topRight;

        Point bottomLeft;

        Point bottomRight;

        final Point[] corners;

        public ClusterSquare(Point first, double maxDistance) {
            this.topLeft = new Point(first);
            this.topRight = new Point(first);
            this.bottomLeft = new Point(first);
            this.bottomRight = new Point(first);
            this.corners = new Point[] { topLeft, topRight, bottomLeft, bottomRight };
            this.maxDistance = maxDistance;
        }

        public boolean add(Point point) {
            if (point.getY() >= topLeft.getY() && point.getY() <= bottomLeft.getY() && point.getX() >= topLeft
                    .getX() && point.getX() <= topRight.getX()) {
                //point is within square
                return true;
            }

            for (int i = 0; i < 4; i++) {
                int j = (i == 3) ? 0 : i + 1;
                double distance;
                if (corners[i].equals(corners[j])) {
                    distance = corners[i].distance(point);
                } else {
                    distance = pointToLineDistance(corners[i], corners[j], point);
                }
                if (distance < maxDistance) {
                    redefine(point);
                    return true;
                }
            }
            return false;
        }

        private void redefine(Point point) {
            if (point.getY() < topLeft.getY()) {
                topLeft.y = point.y;
                topRight.y = point.y;
            } else if (point.getY() > bottomLeft.getY()){
                bottomLeft.y = point.y;
                bottomRight.y = point.y;
            }
            if (point.getX() < topLeft.getX()) {
                topLeft.x = point.x;
                bottomLeft.x = point.x;
            } else if (point.getX() > topRight.getX()){
                topRight.x = point.x;
                bottomRight.x = point.x;
            }
        }

        private static double pointToLineDistance(Point A, Point B, Point P) {
            double normalLength = Math.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y));
            return Math.abs((P.x - A.x) * (B.y - A.y) - (P.y - A.y) * (B.x - A.x)) / normalLength;
        }

        private static class MyPanel extends JPanel {

            public void paint(Graphics g){
                super.paint(g);
                //use g to draw your image
            }
        }

    }
}
