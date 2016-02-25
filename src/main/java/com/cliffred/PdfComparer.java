package com.cliffred;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PdfComparer {

    private PdfComparer() {
    }

    public static boolean samePdfFile(File doc1, File doc2) throws IOException {
        try (
                PDDocument pdDoc1 = PDDocument.load(doc1);
                PDDocument pdDoc2 = PDDocument.load(doc2)
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
            int width = img1.getWidth();
            int height = img1.getHeight();
            List<Point> diffPoints = new ArrayList<>();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                        diffPoints.add(new Point(x, y));
                    }
                }
            }

            List<ClusterRectangle> rectangles = new ArrayList<>();

            diffPoints.stream().filter(point -> !addToExisting(rectangles, point))
                    .forEach(point -> rectangles.add(new ClusterRectangle(point, 5)));

            removeOverlapping(rectangles);

            renderRectangles(img1, rectangles);
            renderRectangles(img2, rectangles);
            BufferedImage diffImage = new BufferedImage(img1.getWidth() * 2 + 1, img1.getHeight(),
                    img1.getType());
            Graphics g = diffImage.getGraphics();
            g.drawImage(img1, 0, 0, null);
            g.drawImage(img2, img1.getWidth() + 1, 0, null);
            ImageIO.write(diffImage, "png", new File("diff.png"));
        }
    }

    private static void removeOverlapping(List<ClusterRectangle> rectangles) {
        if (rectangles.size() > 1) {
            Set<Integer> removeIndices = new TreeSet<>(
                    (Comparator<Integer>) (o1, o2) -> -Integer.compare(o1, o2));
            for (int i = 0; i < rectangles.size(); i++) {
                Rectangle rec1 = rectangles.get(i);
                for (int j = 0; j < rectangles.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    Rectangle rec2 = rectangles.get(j);
                    if (rec1.contains(rec2)) {
                        removeIndices.add(j);
                    }
                }
            }
            for (Integer removeIndex : removeIndices) {
                rectangles.remove(removeIndex.intValue());
            }
        }
    }

    private static void renderRectangles(final BufferedImage diffImage,
            final List<ClusterRectangle> rectangles) {
        for (ClusterRectangle rectangle : rectangles) {
            Graphics2D graphics = diffImage.createGraphics();
            graphics.setColor(Color.MAGENTA);
            graphics.setStroke(new BasicStroke(1));
            graphics.drawRect((int) rectangle.getMinX() - 1, (int) rectangle.getMinY() - 1,
                    (int) rectangle.getWidth() + 1, (int) rectangle.getHeight() + 1);
            graphics.dispose();
        }
    }

    private static boolean addToExisting(List<ClusterRectangle> rectangles, Point point) {
        for (ClusterRectangle rectangle : rectangles) {
            if (rectangle.addPoint(point)) {
                return true;
            }
        }
        return false;
    }

    private static class ClusterRectangle extends Rectangle {

        final double maxDistance;

        public ClusterRectangle(Point first, double maxDistance) {
            super(first);
            this.maxDistance = maxDistance;
        }

        public boolean addPoint(Point point) {
            if (contains(point)) {
                return true;
            }

            for (Line2D line : getLines()) {
                if (line.ptSegDist(point) < maxDistance) {
                    add(point);
                    return true;
                }
            }
            return false;
        }

        private Collection<Line2D> getLines() {
            Collection<Line2D> lines = new ArrayList<>(4);
            lines.add(new Line2D.Double(getMinX(), getMinY(), getMaxX(), getMinY())); //top
            lines.add(new Line2D.Double(getMinX(), getMaxY(), getMaxX(), getMaxY())); //bottom
            lines.add(new Line2D.Double(getMinX(), getMinY(), getMinX(), getMaxY())); //left
            lines.add(new Line2D.Double(getMaxX(), getMinY(), getMaxX(), getMaxY())); //right
            return lines;
        }
    }
}
