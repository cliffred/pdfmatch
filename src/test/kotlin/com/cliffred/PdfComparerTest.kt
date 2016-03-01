package com.cliffred

import org.junit.Test
import java.io.File

class PdfComparerTest {

    @Test
    fun testSamePdfFile() {
        val v1 = File(this.javaClass.getResource("/v1.pdf").toURI())
        val v2 = File(this.javaClass.getResource("/v2.pdf").toURI())
        samePdfFile(v1, v2)
    }
}
