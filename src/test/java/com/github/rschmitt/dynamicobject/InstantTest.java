package com.github.rschmitt.dynamicobject;

import static com.github.rschmitt.dynamicobject.DynamicObject.deserialize;
import static com.github.rschmitt.dynamicobject.DynamicObject.newInstance;
import static com.github.rschmitt.dynamicobject.DynamicObject.serialize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;

public class InstantTest {
    @Test
    public void dateBuilder() {
        Date expected = Date.from(Instant.parse("1985-04-12T23:20:50.52Z"));

        TimeWrapper timeWrapper = newInstance(TimeWrapper.class).date(expected);

        assertEquals(expected, timeWrapper.date());
        assertEquals("{:date #inst \"1985-04-12T23:20:50.520-00:00\"}", serialize(timeWrapper));
    }

    @Test
    public void instantBuilder() {
        Instant expected = Instant.parse("1985-04-12T23:20:50.52Z");

        TimeWrapper timeWrapper = newInstance(TimeWrapper.class).instant(expected);

        assertEquals(expected, timeWrapper.instant());
        assertEquals("{:instant #inst \"1985-04-12T23:20:50.520-00:00\"}", serialize(timeWrapper));
    }

    @Test
    public void dateParser() {
        String edn = "{:date #inst \"1985-04-12T23:20:50.520-00:00\"}";
        Date expected = Date.from(Instant.parse("1985-04-12T23:20:50.52Z"));

        TimeWrapper timeWrapper = deserialize(edn, TimeWrapper.class);

        assertEquals(expected, timeWrapper.date());
    }

    @Test
    public void instantParser() {
        String edn = "{:instant #inst \"1985-04-12T23:20:50.520-00:00\"}";
        Instant expected = Instant.parse("1985-04-12T23:20:50.52Z");

        TimeWrapper timeWrapper = deserialize(edn, TimeWrapper.class);

        assertEquals(expected, timeWrapper.instant());
        assertEquals(edn, serialize(timeWrapper));
    }

    public interface TimeWrapper extends DynamicObject<TimeWrapper> {
        Date date();
        Instant instant();

        TimeWrapper date(Date date);
        TimeWrapper instant(Instant instant);
    }
}
