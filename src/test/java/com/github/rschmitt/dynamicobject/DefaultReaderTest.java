package com.github.rschmitt.dynamicobject;

import clojure.java.api.Clojure;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultReaderTest {
    @Test
    public void testUnknownReader() {
        String edn = "#some-namespace/some-record-name{:key :value}";
        Object obj = DynamicObject.deserialize(edn, Object.class);
        Unknown unknown = (Unknown) obj;

        assertEquals("some-namespace/some-record-name", unknown.getTag());
        assertEquals(Clojure.read("{:key :value}"), unknown.getElement());
        assertEquals(DynamicObjects.serialize(unknown), edn);
    }

    @Test(expected = RuntimeException.class)
    public void disableUnknownReader() {
        try {
            DynamicObject.setDefaultReader(null);
            DynamicObject.deserialize("#unknown{}", Object.class);
        } finally {
            DynamicObject.setDefaultReader(Unknown::new);
        }
    }
}