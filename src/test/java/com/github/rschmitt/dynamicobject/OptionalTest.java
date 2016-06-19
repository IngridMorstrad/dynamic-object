package com.github.rschmitt.dynamicobject;

import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static com.github.rschmitt.dynamicobject.DynamicObject.deserialize;
import static com.github.rschmitt.dynamicobject.DynamicObject.newInstance;
import static com.github.rschmitt.dynamicobject.DynamicObject.serialize;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class OptionalTest {
    @Test
    public void valuePresent() {
        OptWrapper instance = deserialize("{:str \"value\"}", OptWrapper.class).validate();
        OptWrapper expected = newInstance(OptWrapper.class).str(Optional.of("value")).validate();

        assertEquals("value", instance.str().get());
        assertEquals(expected, instance);
    }

    @Test
    public void valueMissing() {
        OptWrapper instance = deserialize("{:str nil}", OptWrapper.class).validate();
        OptWrapper expected = newInstance(OptWrapper.class).str(Optional.empty()).validate();

        assertFalse(instance.str().isPresent());
        assertEquals(expected, instance);
    }

    @Test
    public void nonOptionalBuilder() {
        OptWrapper nothing = newInstance(OptWrapper.class).rawStr(null).validate();
        OptWrapper some = newInstance(OptWrapper.class).rawStr("some string").validate();

        assertEquals(some.rawStr(), Optional.of("some string"));
        assertEquals(nothing.rawStr(), Optional.empty());
    }

    @Test
    public void longPresent() {
        OptWrapper instance = deserialize("{:l 24601}", OptWrapper.class).validate();
        OptWrapper expected = newInstance(OptWrapper.class).l(Optional.of(24601L)).validate();

        assertEquals(Long.valueOf(24601), instance.l().get());
        assertEquals(expected, instance);
    }

    @Test
    public void listPresent() {
        OptWrapper instance = deserialize("{:longs [1 2 3]}", OptWrapper.class).validate();
        OptWrapper expected = newInstance(OptWrapper.class).longs(Optional.of(asList(1L, 2L, 3L))).validate();

        assertEquals(asList(1L, 2L, 3L), instance.longs().get());
        assertEquals(expected, instance);
    }

    @Test
    public void instantPresent() {
        String edn = "{:inst #inst \"1985-04-12T23:20:50.520-00:00\"}";
        Instant expected = Instant.parse("1985-04-12T23:20:50.52Z");

        OptWrapper instance = deserialize(edn, OptWrapper.class).validate();

        assertEquals(expected, instance.inst().get());
        assertEquals(edn, serialize(instance));
    }

    @Test
    public void dynamicObjectPresent() {
        DynamicObject.registerTag(OptWrapper.class, "OptWrapper");

        OptWrapper instance = deserialize("#OptWrapper{:wrapper #OptWrapper{:l 24}}", OptWrapper.class).validate();
        OptWrapper expected = newInstance(OptWrapper.class).wrapper(Optional.of(newInstance(OptWrapper.class).l(Optional.of(24L)))).validate();

        assertEquals(expected.wrapper().get(), instance.wrapper().get());
        assertEquals(expected, instance);

        DynamicObject.deregisterTag(OptWrapper.class);
    }

    @Test
    public void optionalValidation() {
        deserialize("{}", OptWrapper.class).validate();
        deserialize("{:str \"value\"}", OptWrapper.class).validate();
    }

    @Test(expected = IllegalStateException.class)
    public void optionalValidationFailure() {
        deserialize("{:str 4}", OptWrapper.class).validate();
    }

    public interface OptWrapper extends DynamicObject<OptWrapper> {
        Optional<String> str();
        Optional<Long> l();
        Optional<List<Long>> longs();
        Optional<Instant> inst();
        Optional<OptWrapper> wrapper();

        OptWrapper str(Optional<String> str);
        OptWrapper l(Optional<Long> i);
        OptWrapper longs(Optional<List<Long>> longs);
        OptWrapper inst(Optional<Instant> inst);
        OptWrapper wrapper(Optional<OptWrapper> wrapper);

        Optional<String> rawStr();
        OptWrapper rawStr(String str);
    }
}
