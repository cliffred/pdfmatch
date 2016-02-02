package com.cliffred;


import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PdfComparer {

    private PdfComparer() {
    }

    public static boolean equals(File doc1, File doc2) throws IOException {
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
                BufferedImage bim1 = doc1Renderer.renderImageWithDPI(page, 300, ImageType.RGB);
                BufferedImage bim2 = doc2Renderer.renderImageWithDPI(page, 300, ImageType.RGB);

                if (!equalImages(bim1, bim2)) {
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean equalImages(BufferedImage img1, BufferedImage img2) {
        if (img1.getWidth() == img2.getWidth() && img1.getHeight() == img2.getHeight()) {
            BufferedImage diffImage = new BufferedImage(img1.getWidth(), img1.getHeight(), img1.getType());
            int width = img1.getWidth();
            int height = img1.getHeight();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (img1.getRGB(x, y) == img2.getRGB(x, y)) {
                        diffImage.setRGB(x, y, img1.getRGB(x, y));
                    } else {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
