package com.cliffred;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;


public class PdfComparerTest {

    @Test
    public void testSamePdfFile() throws Exception {
        File root = new File(this.getClass().getResource("/").toURI());

        File same1 = new File(root, "same1.pdf");
        File same2 = new File(root, "same2.pdf");
        assertThat(PdfComparer.samePdfFile(same1, same2, null), is(true));

        File diff = new File(root, "diff1.pdf");
        assertThat(PdfComparer.samePdfFile(same1, diff, null), is(not(true)));
    }

    @Test
    public void testDiff() throws Exception {
        File root = new File(this.getClass().getResource("/").toURI());

        File v1 = new File(root, "v1.pdf");
        File v2 = new File(root, "v2.pdf");
        PdfComparer.samePdfFile(v1, v2, null);
    }
}
