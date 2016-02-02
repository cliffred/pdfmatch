package com.cliffred;

import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;


public class PdfComparerTest {

    @Test
    public void equals() throws Exception {
        File root = new File(this.getClass().getResource("/").toURI());

        File same1 = new File(root, "same1.pdf");
        File same2 = new File(root, "same2.pdf");
        assertThat(PdfComparer.equals(same1, same2), is(true));

        File diff = new File(root, "diff1.pdf");
        assertThat(PdfComparer.equals(same1, diff), is(not(true)));
    }
}