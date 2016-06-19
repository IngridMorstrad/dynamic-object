package com.github.rschmitt.dynamicobject;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.github.rschmitt.dynamicobject.DynamicObject.deregisterTag;
import static com.github.rschmitt.dynamicobject.DynamicObject.deserialize;
import static com.github.rschmitt.dynamicobject.DynamicObject.newInstance;
import static com.github.rschmitt.dynamicobject.DynamicObject.registerTag;
import static com.github.rschmitt.dynamicobject.DynamicObject.serialize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ValidationTest {
    @BeforeClass
    public static void setup() {
        registerTag(RequiredFields.class, "R");
        registerTag(Mismatch.class, "M");
        registerTag(Inner.class, "I");
        registerTag(Outer.class, "O");
        registerTag(ListContainer.class, "LC");
    }

    @AfterClass
    public static void teardown() {
        deregisterTag(RequiredFields.class);
        deregisterTag(Mismatch.class);
        deregisterTag(Inner.class);
        deregisterTag(Outer.class);
        deregisterTag(ListContainer.class);
    }

    @Test
    public void nullValidation() {
        NoRequiredFields noRequiredFields = newInstance(NoRequiredFields.class);
        assertNull(noRequiredFields.s());
        assertNull(noRequiredFields.s(null).s());
        assertNull(noRequiredFields.validate().s());
        assertNull(noRequiredFields.s(null).validate().s());
    }

    @Test
    public void validationSuccessful() {
        validationSuccess("#R{:x 1, :y 2}", RequiredFields.class);
        validationSuccess("#R{:x 1, :y 2, :z 3}", RequiredFields.class);
        validationSuccess("#O{:inner #I{:x 4}}", Outer.class);
        validationSuccess("#LC{:inner [#I{:x 1} #I{:x 2}]}", ListContainer.class);
    }

    @Test
    public void requiredFieldsMissing() {
        validationFailure("#R{}", RequiredFields.class);
        validationFailure("#R{:z 19}", RequiredFields.class);
        validationFailure("#R{:x nil, :y 1}", RequiredFields.class);
        validationFailure("#R{:x 1, :y nil}", RequiredFields.class);
        validationFailure("#R{:x nil, :y nil}", RequiredFields.class);

        validationFailure("#O{:inner #I{}}", Outer.class);
        validationFailure("#LC{:inner [#I{:x nil}, #I{:x nil}]}", ListContainer.class);
        validationFailure("#LC{:inner [#I{}, #I{}]}", ListContainer.class);
    }

    @Test
    public void typeMismatches() {
        validationFailure("#M{:required-string \"str\", :optional-string 4}", Mismatch.class);
        validationFailure("#R{:x \"strings!\", :y \"moar strings!\"}", RequiredFields.class);
        validationFailure("#M{:required-string 4}", Mismatch.class);
        validationFailure("#O{:inner #I{:x \"strings!\"}}", Outer.class);
        validationFailure("#O{:inner 4}", Outer.class);
        validationFailure("#LC{:list [\"string!\" \"another string!\"]}", ListContainer.class);
        validationFailure("#LC{:list #{\"string!\" \"another string!\"}}", ListContainer.class);
        validationFailure("#LC{:inner [#I{:x 1}, #I{:x \"str\"}]}", ListContainer.class);
    }

    @Test
    public void nestedCollections() throws Exception {
        validationSuccess("{:rawList [\"str1\" \"str2\" 3]}", CompoundLists.class);
        validationSuccess("{:strings [\"str1\" \"str2\" nil \"str4\"]}", CompoundLists.class);

        validationSuccess("{:listOfListOfStrings [[\"str1\"] [\"str2\"]]}", CompoundLists.class);
        validationFailure("{:listOfListOfStrings #{[\"str1\"] [\"str2\"]}}", CompoundLists.class);
        validationFailure("{:listOfListOfStrings [#{\"str1\"} #{\"str2\"}]}", CompoundLists.class);
        validationFailure("{:listOfListOfStrings [[\"str1\"] [\"str2\"] 3]}", CompoundLists.class);
        validationFailure("{:listOfListOfStrings [[\"str1\"] [\"str2\"] [3]]}", CompoundLists.class);

        validationSuccess("{:longs #{1 4 3 2}}", CompoundSets.class);
        validationSuccess("{:rawSet #{\"str1\" \"str2\" 3}}", CompoundSets.class);
        validationSuccess("{:strings #{nil \"str1\" \"str2\" \"str4\"}}", CompoundSets.class);
        validationSuccess("{:setOfSetOfStrings nil}", CompoundSets.class);
        validationSuccess("{:setOfSetOfStrings #{nil}}", CompoundSets.class);
        validationSuccess("{:setOfSetOfStrings #{#{}}}", CompoundSets.class);
        validationSuccess("{:setOfSetOfStrings #{#{}}}", CompoundSets.class);
        validationSuccess("{:setOfSetOfStrings #{#{\"str2\"} #{\"str1\"}}}", CompoundSets.class);
        validationSuccess("{:setOfSetOfStrings #{#{\"str2\"} #{\"str1\"}}}", CompoundSets.class);
        validationFailure("{:setOfSetOfStrings #{[\"str1\"] [\"str2\"]}}", CompoundSets.class);
        validationFailure("{:setOfSetOfStrings #{#{\"str1\"} #{\"str2\"} 3}}", CompoundSets.class);
        validationFailure("{:setOfSetOfStrings #{#{\"str1\"} #{\"str2\"} #{3}}}", CompoundSets.class);

        validationFailure("{:strings {\"k\" \\newline}}", CompoundMaps.class);
        validationFailure("{:strings {1 nil}}", CompoundMaps.class);
        validationFailure("{:strings {1 {\"k\" \"v\"}}}", CompoundMaps.class);
        validationFailure("{:strings {\"str1\" \"str2\", \"str3\" 4}}", CompoundMaps.class);
        validationSuccess("{:strings nil}", CompoundMaps.class);
        validationSuccess("{:strings {}}", CompoundMaps.class);
        validationSuccess("{:strings {\"a\" \"b\", \"c\" \"d\", \"e\" nil}}", CompoundMaps.class);

        validationSuccess("{:rawMap nil}", CompoundMaps.class);
        validationSuccess("{:rawMap {}}", CompoundMaps.class);
        validationSuccess("{:rawMap {\\newline nil}}", CompoundMaps.class);
        validationSuccess("{:rawMap {\"str1\" \"str2\", 3 4}}", CompoundMaps.class);
        validationSuccess("{:nestedGenericMaps {\"str1\" {\"str2\" \"str3\"}}}", CompoundMaps.class);
        validationSuccess("{:nestedGenericMaps {\"str1\" {\"str2\" nil}}}", CompoundMaps.class);
        validationSuccess("{:nestedGenericMaps {\"str1\" {nil \"str3\"}}}", CompoundMaps.class);
        validationSuccess("{:nestedGenericMaps {\"str1\" {}}}", CompoundMaps.class);

        validationSuccess("{:nestedGenericMaps nil}", CompoundMaps.class);
        validationFailure("{:nestedGenericMaps {1 nil}}", CompoundMaps.class);
        validationFailure("{:nestedGenericMaps [\"not\" \"a\" \"map\"]}", CompoundMaps.class);
        validationFailure("{:nestedGenericMaps {\"key\" [\"not\" \"a\" \"map\"]}}", CompoundMaps.class);
        validationFailure("{:nestedGenericMaps {\"key\" {\"k\" 4}}}", CompoundMaps.class);
        validationFailure("{:nestedGenericMaps {\"key\" {4 \"v\"}}}", CompoundMaps.class);
        validationFailure("{:nestedGenericMaps {1 {\"k\" \"v\"}}}", CompoundMaps.class);
        validationFailure("{:nestedGenericMaps {\"key\" {\"k\" #{\"v\"}}}}", CompoundMaps.class);

        validationSuccess("{:nestedNumericMaps {1 nil}}", CompoundMaps.class);
        validationSuccess("{:nestedNumericMaps {1 {}}}", CompoundMaps.class);
        validationSuccess("{:nestedNumericMaps {1 {2 nil}}}", CompoundMaps.class);
        validationSuccess("{:nestedNumericMaps {1 {2 3.3}}}", CompoundMaps.class);
        validationFailure("{:nestedNumericMaps [1 2 3]}", CompoundMaps.class);
        validationFailure("{:nestedNumericMaps {1 [2 3 4]}}", CompoundMaps.class);
        validationFailure("{:nestedNumericMaps {\"k\" {2 3}}}", CompoundMaps.class);
        validationFailure("{:nestedNumericMaps {1 {\"k\" 3}}}", CompoundMaps.class);
        validationFailure("{:nestedNumericMaps {1 {2 \"v\"}}}", CompoundMaps.class);
        validationFailure("{:nestedNumericMaps {1 {\"k\" \"v\"}}}", CompoundMaps.class);
        validationFailure("{:nestedNumericMaps {1 {2 #{3}}}}", CompoundMaps.class);
        validationFailure("{:nestedNumericMaps {1 {#{2} 3}}}", CompoundMaps.class);
        validationFailure("{:nestedNumericMaps {#{1} {2 3}}}", CompoundMaps.class); // TODO better error message
    }

    @Test(expected = UnsupportedOperationException.class)
    public void wildcardList() throws Exception {
        validationFailure("{:wildcardList [\"str1\", \"str2\", 3]}", CompoundLists.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void wildcardSet() throws Exception {
        validationFailure("{:wildcardSet #{\"str1\", \"str2\", 3}}", CompoundSets.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void wildcardKey() throws Exception {
        validationFailure("{:wildcardKey {\"str1\" \"str2\"}}", CompoundMaps.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void wildcardValue() throws Exception {
        deserialize("{:wildcardValue {\"str1\" \"str2\"}}", CompoundMaps.class).validate();
    }

    @Test(expected = NullPointerException.class)
    public void nullRequiredFieldThrowsException() {
        RequiredBoxedFields instance = deserialize("{}", RequiredBoxedFields.class);
        instance.x();
    }

    @Test
    public void customValidation() {
        validationSuccess("{:oddsOnly 5, :required 0}", Custom.class);
        validationFailure("{:oddsOnly 4, :required 0}", Custom.class);
        validationFailure("{:oddsOnly 5}", Custom.class);

        try {
            DynamicObject.deserialize("{:oddsOnly 4, :required 0}", Custom.class).validate();
            Assert.fail();
        } catch (IllegalStateException expected) {}
    }

    @Test(expected = IllegalStateException.class)
    public void customValidationRunsLast() {
        newInstance(Custom2.class).validate();
    }

    @Test(expected = CustomException.class)
    public void customExceptionsArePropagated() {
        newInstance(Custom2.class).str("value").validate();
    }

    @Test(expected = CustomException.class)
    public void customExceptionsArePropagated2() {
        DynamicObject.deserialize("{:str \"value\"}", Custom2.class).validate();
    }

    @Test
    public void mutatingValidators() {
        MutatingValidator before = newInstance(MutatingValidator.class);
        MutatingValidator after = before.validate();
        assertEquals("value", after.str());
        assertEquals("value", newInstance(MutatingValidator.class).validate().str());
    }

    private static <D extends DynamicObject<D>> void validationFailure(String edn, Class<D> type) {
        try {
            deserialize(edn, type).validate();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            System.out.println(String.format("%s => %s", edn, ex.getMessage()));
        }
    }

    private static <D extends DynamicObject<D>> void validationSuccess(String edn, Class<D> type) {
        D instance = deserialize(edn, type).validate();
        assertEquals(edn, serialize(instance));
    }


    public interface NoRequiredFields extends DynamicObject<NoRequiredFields> {
        String s();
        NoRequiredFields s(String s);
    }

    public interface RequiredFields extends DynamicObject<RequiredFields> {
        @Required long x();
        @Required long y();

        long z();
    }

    public interface RequiredBoxedFields extends DynamicObject<RequiredBoxedFields> {
        @Required Long x();
    }

    public interface Mismatch extends DynamicObject<Mismatch> {
        @Key(":required-string") @Required String requiredString();
        @Key(":optional-string") String optionalString();
    }

    public interface Inner extends DynamicObject<Inner> {
        @Required long x();
    }

    public interface Outer extends DynamicObject<Outer> {
        @Required Inner inner();
    }

    public interface ListContainer extends DynamicObject<ListContainer> {
        List<BigInteger> list();
        List<Inner> inner();
    }

    public interface CompoundLists extends DynamicObject<CompoundLists> {
        List<String> strings();
        List<Long> longs();
        List rawList();
        List<?> wildcardList();
        List<List<String>> listOfListOfStrings();
    }

    public interface CompoundSets extends DynamicObject<CompoundSets> {
        Set<String> strings();
        Set<Long> longs();
        Set rawSet();
        Set<?> wildcardSet();
        Set<Set<String>> setOfSetOfStrings();
    }

    public interface CompoundMaps extends DynamicObject<CompoundMaps> {
        Map<String, String> strings();
        Map rawMap();
        Map<?, String> wildcardKey();
        Map<String, ?> wildcardValue();
        Map<String, Map<String, String>> nestedGenericMaps();
        Map<Long, Map<Long, Double>> nestedNumericMaps();
    }

    public interface Custom extends DynamicObject<Custom> {
        @Required long oddsOnly();
        @Required long required();

        default Custom validate() {
            if (oddsOnly() % 2 == 0)
                throw new IllegalStateException("Odd number expected");
            return this;
        }
    }

    public interface Custom2 extends DynamicObject<Custom2> {
        @Required String str();
        Custom2 str(String str);

        default Custom2 validate() {
            throw new CustomException();
        }
    }

    public interface MutatingValidator extends DynamicObject<MutatingValidator> {
        String str();
        MutatingValidator str(String str);

        default MutatingValidator validate() {
            if (size() == 0) {
                return str("value");
            } else return this;
        }
    }

    public static class CustomException extends RuntimeException {}
}
