package com.juanlipuma;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Unit test for simple App.
 */
public class ObjectUtilsTest
{
    private static class POJO {
        public Boolean boolField = true;
        public String stringField = "hiMom";

        public POJO() {}

        public POJO(Boolean boolField, String stringField) {
            this.boolField = boolField;
            this.stringField = stringField;
        }
    }

    private static class POJO2 {
        public boolean boolField = false;
        public String stringField = "pojo2";
        public int answerToLifeTheUniverseAndEverything = 42;
    }

    private static class POJO3 {
        private int x;
        private int y;
    }


    @Test
    public void testFullMerge() {
        POJO src = new POJO(false, "sarasa");
        POJO dest = new POJO();

        ObjectUtils.merge(src, dest);
        assertEquals("boolField not copied", src.boolField, dest.boolField);
        assertEquals("stringField not copied", src.stringField, dest.stringField);
    }

    @Test
    public void testRestrictedMerge() {
        POJO src = new POJO(false, "sarasa");
        POJO dest = new POJO();

        ObjectUtils.merge(src, dest, Collections.singletonList("boolField"));
        assertEquals("boolField not copied", src.boolField, dest.boolField);
        assertNotEquals("stringField was copied when it was not included in field list", src.stringField, dest.stringField);
    }

    @Test
    public void testNoMergeNulls() {
        POJO src = new POJO(false, null);
        POJO dest = new POJO();

        ObjectUtils.merge(src, dest);
        assertEquals("boolField not copied", src.boolField, dest.boolField);
        assertNotNull("Null stringField was copied", dest.stringField);

    }

    @Test
    public void mergeDifferentTypes() {
        POJO src = new POJO();
        POJO2 dest = new POJO2();

        ObjectUtils.merge(src, dest);
        assertEquals("boolField not copied", src.boolField, dest.boolField);
        assertEquals("stringField not copied", src.stringField, dest.stringField);
        assertEquals("Non-common field was modified after copy", dest.answerToLifeTheUniverseAndEverything, 42);

    }
}
