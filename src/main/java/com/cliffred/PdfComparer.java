package com.cliffred;

import javafx.scene.image.ImageView;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfComparer {

    private PdfComparer() {
    }

    public static boolean samePdfFile(File doc1, File doc2, final ImageView imageView) throws IOException {
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
                    createDiffImage(bim1, bim2, imageView);
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

    private static void createDiffImage(BufferedImage img1, BufferedImage img2, ImageView iv) throws IOException {

        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            int width = img1.getWidth();
            int height = img1.getHeight();
//            BufferedImage diffImage = new BufferedImage(img1.getWidth(), img1.getHeight(), img1.getType());
            List<Point> diffPoints = new ArrayList<>();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (img1.getRGB(x, y) == img2.getRGB(x, y)) {
//                        diffImage.setRGB(x, y, img1.getRGB(x, y));
                    } else {
//                        diffImage.setRGB(x, y, Color.CYAN.getRGB());
                        diffPoints.add(new Point(x, y));
                    }
                }
            }

            List<ClusterSquare> squares = new ArrayList<>();
//            int i = 0, j = 0;
//            Timeline timeline = new Timeline(
//                    new KeyFrame(Duration.ZERO, new KeyValue(iv.imageProperty(), SwingFXUtils.toFXImage(diffImage, null)))
//            );

            for (Point point : diffPoints) {
                if (!addToExisting(squares, point)) {
                    squares.add(new ClusterSquare(point, 5));
                }
//                if (i == 50) {
//                    final BufferedImage image = deepCopy(diffImage);
//                    renderSquares(image, squares);
//                    i = 0;
//                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(j), new KeyValue(iv.imageProperty(), SwingFXUtils.toFXImage(image, null))));
//                    j++;
//                }
//                i++;

            }
//            timeline.play();

            renderSquares(img1, squares);
            renderSquares(img2, squares);
            BufferedImage diffImage = new BufferedImage(img1.getWidth() * 2 + 1, img1.getHeight(), img1.getType());
            Graphics g = diffImage.getGraphics();
            g.drawImage(img1, 0, 0, null);
            g.drawImage(img2, img1.getWidth() + 1, 0, null);
            ImageIO.write(diffImage, "png", new File("diff.png"));
        }
    }

    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    private static void renderSquares(final BufferedImage diffImage, final List<ClusterSquare> squares) {
        for (int i = 0; i < squares.size(); i++) {
            ClusterSquare square = squares.get(i);
            int color = (i == 0) ? Color.RED.getRGB() : Color.MAGENTA.getRGB();
            for (int x = square.topLeft.x - 1; x <= square.topRight.x + 1; x++) {
                diffImage.setRGB(x, square.topLeft.y - 1, color);
            }
            for (int y = square.topRight.y - 1; y <= square.bottomRight.y + 1; y++) {
                diffImage.setRGB(square.topRight.x + 1, y, color);
            }
            for (int x = square.bottomLeft.x - 1; x <= square.bottomRight.x + 1; x++) {
                diffImage.setRGB(x, square.bottomLeft.y + 1, color);
            }
            for (int y = square.topLeft.y - 1; y <= square.bottomLeft.y + 1; y++) {
                diffImage.setRGB(square.topLeft.x - 1, y, color);
            }
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
            this.corners = new Point[]{topLeft, topRight, bottomRight, bottomLeft};
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
                    distance = distToSegment(point, corners[i], corners[j]);
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
            } else if (point.getY() > bottomLeft.getY()) {
                bottomLeft.y = point.y;
                bottomRight.y = point.y;
            }
            if (point.getX() < topLeft.getX()) {
                topLeft.x = point.x;
                bottomLeft.x = point.x;
            } else if (point.getX() > topRight.getX()) {
                topRight.x = point.x;
                bottomRight.x = point.x;
            }
        }

//        private static double pointToLineDistance(Point A, Point B, Point P) {
//            double normalLength = Math.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y));
//            return Math.abs((P.x - A.x) * (B.y - A.y) - (P.y - A.y) * (B.x - A.x)) / normalLength;
//        }

        static double sqr(double x) {
            return x * x;
        }

        static double dist2(DoublePoint v, DoublePoint w) {
            return sqr(v.x - w.x) + sqr(v.y - w.y);
        }

        static double distToSegmentSquared(DoublePoint p, DoublePoint v, DoublePoint w) {
            double l2 = dist2(v, w);
            if (l2 == 0) return dist2(p, v);
            double t = ((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2;
            if (t < 0) return dist2(p, v);
            if (t > 1) return dist2(p, w);
            return dist2(p, new DoublePoint(
                    v.x + t * (w.x - v.x),
                    v.y + t * (w.y - v.y)
            ));
        }

        static double distToSegment(Point p, Point v, Point w) {
            return Math.sqrt(distToSegmentSquared(new DoublePoint(p.x, p.y), new DoublePoint(v.x, v.y), new DoublePoint(w.x, w.y)));
        }

        static class DoublePoint {
            public double x;
            public double y;

            public DoublePoint(double x, double y) {
                this.x = x;
                this.y = y;
            }
        }
    }
}
