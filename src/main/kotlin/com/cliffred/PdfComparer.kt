package com.cliffred

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.rendering.PDFRenderer
import java.awt.Color
import java.awt.Point
import java.awt.Rectangle
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.util.*
import javax.imageio.ImageIO


@Throws(IOException::class)
fun samePdfFile(doc1: File, doc2: File): Boolean {
    PDDocument.load(doc1).use { pdDoc1 ->
        PDDocument.load(doc2).use { pdDoc2 ->
            val doc1Renderer = PDFRenderer(pdDoc1)
            val doc2Renderer = PDFRenderer(pdDoc2)

            if (pdDoc1.numberOfPages != pdDoc2.numberOfPages) {
                return false
            }

            for (page in 0..pdDoc1.numberOfPages - 1) {
                val bim1 = doc1Renderer.renderImage(page)
                val bim2 = doc2Renderer.renderImage(page)

                if (!equalImages(bim1, bim2)) {
                    createDiffImage(bim1, bim2)
                    return false
                }
            }
            return true
        }
    }
}

private fun equalImages(img1: BufferedImage, img2: BufferedImage): Boolean {
    if (img1.width == img2.width && img1.height == img2.height) {
        val width = img1.width
        val height = img1.height

        for (y in 0..height - 1) {
            for (x in 0..width - 1) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    return false
                }
            }
        }
        return true
    } else {
        return false
    }
}

@Throws(IOException::class)
private fun createDiffImage(img1: BufferedImage, img2: BufferedImage) {
    if (img1.width == img2.width && img1.height == img2.height) {
        val width = img1.width
        val height = img1.height
        val diffPoints = ArrayList<Point>()

        for (y in 0..height - 1) {
            for (x in 0..width - 1) {
                if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
                    diffPoints.add(Point(x, y))
                }
            }
        }

        val rectangles = ArrayList<ClusterRectangle>()

        diffPoints.forEach {
            if (!addToExisting(rectangles, it)) {
                rectangles.add(ClusterRectangle(it, 5.0))
            }
        }

        removeOverlapping(rectangles)

        renderRectangles(img1, rectangles)
        renderRectangles(img2, rectangles)
        val diffImage = BufferedImage(img1.width * 2 + 1, img1.height,
                img1.type)
        val g = diffImage.graphics
        g.drawImage(img1, 0, 0, null)
        g.drawImage(img2, img1.width + 1, 0, null)
        ImageIO.write(diffImage, "png", File("diff.png"))
    }
}

private fun removeOverlapping(rectangles: MutableList<ClusterRectangle>) {
    if (rectangles.size > 1) {
        val reverseOrder = Comparator<Int> { a, b -> -Integer.compare(a, b) }
        val removeIndices = TreeSet(reverseOrder)

        for (i in rectangles.indices) {
            val rec1 = rectangles[i]
            for (j in rectangles.indices) {
                if (i == j) {
                    continue
                }
                val rec2 = rectangles[j]
                if (rec1.contains(rec2)) {
                    removeIndices.add(j)
                }
            }
        }
        for (removeIndex in removeIndices) {
            rectangles.removeAt(removeIndex.toInt())
        }
    }
}

private fun renderRectangles(diffImage: BufferedImage,
                             rectangles: List<ClusterRectangle>) {
    for (rectangle in rectangles) {
        rectangle.setSize(rectangle.width + 1, rectangle.height + 1)
        val graphics = diffImage.createGraphics()
        graphics.color = Color(30, 30, 255, 70)
        graphics.fill(rectangle)
        graphics.dispose()
    }
}

private fun addToExisting(rectangles: List<ClusterRectangle>, point: Point): Boolean {
    for (rectangle in rectangles) {
        if (rectangle.addPoint(point)) {
            return true
        }
    }
    return false
}

private class ClusterRectangle(first: Point, val maxDistance: kotlin.Double) : Rectangle(first) {

    fun addPoint(point: Point): Boolean {
        if (contains(point)) {
            return true
        }

        for (line in lines) {
            if (line.ptSegDist(point) < maxDistance) {
                add(point)
                return true
            }
        }
        return false
    }

    private val lines: Collection<Line2D>
        get() {
            val lines = ArrayList<Line2D>(4)
            lines.add(Line2D.Double(minX, minY, maxX, minY))
            lines.add(Line2D.Double(minX, maxY, maxX, maxY))
            lines.add(Line2D.Double(minX, minY, minX, maxY))
            lines.add(Line2D.Double(maxX, minY, maxX, maxY))
            return lines
        }
}
