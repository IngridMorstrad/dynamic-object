package com.github.rschmitt.dynamicobject;

import static com.github.rschmitt.dynamicobject.DynamicObject.deserialize;
import static com.github.rschmitt.dynamicobject.DynamicObject.newInstance;
import static com.github.rschmitt.dynamicobject.DynamicObject.serialize;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SchemaCollectionTest {
    @BeforeEach
    public void setup() {
        DynamicObject.registerTag(X.class, "X");
        DynamicObject.registerTag(Coll.class, "Coll");
    }

    @AfterEach
    public void teardown() {
        DynamicObject.deregisterTag(Coll.class);
        DynamicObject.deregisterTag(X.class);
    }

    @Test
    public void list() {
        Coll expected = deserialize("#Coll{:list [#X{:y 1}, #X{:y 2}, #X{:y 3}]}", Coll.class);
        List<X> list = new ArrayList<>();
        list.add(newInstance(X.class).y(1));
        list.add(newInstance(X.class).y(2));
        list.add(newInstance(X.class).y(3));

        Coll actual = newInstance(Coll.class).list(list);

        roundTripTest(expected, Coll.class);
        roundTripTest(actual, Coll.class);
        assertEquals(expected, actual);
        assertEquals(list, expected.list());
        assertEquals(list, actual.list());
        assertEquals(actual.list(), list);
        assertEquals(expected.list(), list);
    }

    @Test
    public void set() {
        Coll expected = deserialize("#Coll{:set #{#X{:y 1} #X{:y 2} #X{:y 3}}}", Coll.class);
        Set<X> set = new HashSet<>();
        set.add(newInstance(X.class).y(1));
        set.add(newInstance(X.class).y(3));
        set.add(newInstance(X.class).y(2));

        Coll actual = newInstance(Coll.class).set(set);

        roundTripTest(expected, Coll.class);
        roundTripTest(actual, Coll.class);
        assertEquals(expected, actual);
        assertEquals(set, expected.set());
        assertEquals(set, actual.set());
        assertEquals(actual.set(), set);
        assertEquals(expected.set(), set);
    }

    @Test
    public void map() {
        Coll expected = deserialize("#Coll{:map {#X{:y 1}, #X{:y 2}}}", Coll.class);
        Map<X, X> map = new HashMap<>();
        map.put(newInstance(X.class).y(1), newInstance(X.class).y(2));

        Coll actual = newInstance(Coll.class).map(map);

        roundTripTest(expected, Coll.class);
        roundTripTest(actual, Coll.class);
        assertEquals(expected, actual);
        assertEquals(map, expected.map());
        assertEquals(map, actual.map());
        assertEquals(actual.map(), map);
        assertEquals(expected.map(), map);
    }

    private <D extends DynamicObject<D>> void roundTripTest(D obj, Class<D> type) {
        String edn = serialize(obj);
        D actual = deserialize(edn, type);
        assertEquals(obj, actual);
    }

    public interface Coll extends DynamicObject<Coll> {
        List<X> list();
        Set<X> set();
        Map<X, X> map();

        Coll list(List<X> list);
        Coll set(Set<X> set);
        Coll map(Map<X, X> map);
    }

    public interface X extends DynamicObject<X> {
        int y();

        X y(int y);
    }
}
