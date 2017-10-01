package com.github.rschmitt.dynamicobject;


import static com.github.rschmitt.dynamicobject.DynamicObject.deserialize;
import static com.github.rschmitt.dynamicobject.DynamicObject.fromFressianByteArray;
import static com.github.rschmitt.dynamicobject.DynamicObject.newInstance;
import static com.github.rschmitt.dynamicobject.DynamicObject.serialize;
import static com.github.rschmitt.dynamicobject.DynamicObject.toFressianByteArray;
import static com.github.rschmitt.dynamicobject.TestUtils.assertEquivalent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RecursionTest {
    @BeforeEach
    public void setup() {
        try {
            DynamicObject.deregisterTag(LinkedList.class);
        } catch (NullPointerException ignore) { }
    }

    @Test
    public void recursion() {
        LinkedList tail = newInstance(LinkedList.class).value(3);
        LinkedList middle = newInstance(LinkedList.class).value(2).next(tail);
        LinkedList head = newInstance(LinkedList.class).value(1).next(middle);

        roundTrip(tail, false);
        roundTrip(middle, false);
        roundTrip(head, false);

        assertEquals(1, head.value());
        assertEquals(2, head.next().value());
        assertEquals(3, head.next().next().value());
        assertNull(head.next().next().next());

        assertEquivalent("{:next {:next {:value 3}, :value 2}, :value 1}", serialize(head));
        assertEquivalent("{:next {:value 3}, :value 2}", serialize(middle));
        assertEquivalent("{:value 3}", serialize(tail));
    }

    @Test
    public void taggedRecursion() {
        DynamicObject.registerTag(LinkedList.class, "LinkedList");

        LinkedList tail = newInstance(LinkedList.class).value(3);
        LinkedList middle = newInstance(LinkedList.class).value(2).next(tail);
        LinkedList head = newInstance(LinkedList.class).value(1).next(middle);

        roundTrip(tail, true);
        roundTrip(middle, true);
        roundTrip(head, true);
        assertEquivalent("#LinkedList{:value 3}", serialize(tail));
        assertEquivalent("#LinkedList{:next #LinkedList{:value 3}, :value 2}", serialize(middle));
        assertEquivalent("#LinkedList{:next #LinkedList{:next #LinkedList{:value 3}, :value 2}, :value 1}", serialize(head));
    }

    @Test
    public void registeringTheTagAddsItToSerializedOutput() {
        LinkedList tail = newInstance(LinkedList.class).value(3);
        LinkedList middle = newInstance(LinkedList.class).value(2).next(tail);
        LinkedList head = newInstance(LinkedList.class).value(1).next(middle);

        DynamicObject.registerTag(LinkedList.class, "LinkedList");

        roundTrip(tail, true);
        roundTrip(middle, true);
        roundTrip(head, true);
        assertEquivalent("#LinkedList{:value 3}", serialize(tail));
        assertEquivalent("#LinkedList{:next #LinkedList{:value 3}, :value 2}", serialize(middle));
        assertEquivalent("#LinkedList{:next #LinkedList{:next #LinkedList{:value 3}, :value 2}, :value 1}", serialize(head));
    }

    @Test
    public void deregisteringTheTagRemovesItFromSerializedOutput() {
        DynamicObject.registerTag(LinkedList.class, "LinkedList");

        LinkedList tail = newInstance(LinkedList.class).value(3);
        LinkedList middle = newInstance(LinkedList.class).value(2).next(tail);
        LinkedList head = newInstance(LinkedList.class).value(1).next(middle);

        DynamicObject.deregisterTag(LinkedList.class);

        roundTrip(tail, false);
        roundTrip(middle, false);
        roundTrip(head, false);

        assertEquivalent("{:next {:next {:value 3}, :value 2}, :value 1}", serialize(head));
        assertEquivalent("{:next {:value 3}, :value 2}", serialize(middle));
        assertEquivalent("{:value 3}", serialize(tail));
    }

    @Test
    public void registeringTheTagDoesNotAffectEqualityOfDeserializedInstances() {
        LinkedList obj1 = DynamicObject.deserialize("{:value 1, :next {:value 2, :next {:value 3}}}", LinkedList.class);
        DynamicObject.registerTag(LinkedList.class, "LinkedList");
        LinkedList obj2 = DynamicObject.deserialize("#LinkedList{:value 1, :next #LinkedList{:value 2, :next #LinkedList{:value 3}}}", LinkedList.class);
        DynamicObject.deregisterTag(LinkedList.class);

        LinkedList next = obj1.next().next();
        LinkedList next2 = obj1.next().next();
        assertEquals(next, next2);
        assertTrue(next.equals(next2));
        assertTrue(obj1.equals(obj2));
        assertEquals(obj1.next(), obj2.next());
        assertEquals(obj1, obj2);
        assertEquals(DynamicObject.serialize(obj1), DynamicObject.serialize(obj2));
    }

    private void roundTrip(LinkedList linkedList, boolean binary) {
        assertEquals(linkedList, deserialize(serialize(linkedList), LinkedList.class));
        if (binary)
            assertEquals(linkedList, fromFressianByteArray(toFressianByteArray(linkedList)));
    }

    public interface LinkedList extends DynamicObject<LinkedList> {
        long value();
        LinkedList next();

        LinkedList value(long value);
        LinkedList next(LinkedList linkedList);
    }
}
