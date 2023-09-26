package ch.jalu.typeresolver;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * Test for {@link FieldUtils}.
 */
class FieldUtilsTest {

    @Test
    void shouldFormatField() throws NoSuchFieldException {
        // given
        Field field1 = ArrayList.class.getDeclaredField("size");
        Field field2 = HashMap.class.getDeclaredField("DEFAULT_LOAD_FACTOR");

        // when / then
        assertThat(FieldUtils.formatField(field1), equalTo("ArrayList#size"));
        assertThat(FieldUtils.formatField(field2), equalTo("HashMap#DEFAULT_LOAD_FACTOR"));
    }

    @Test
    void shouldReturnIfIsNonSyntheticInstanceField() throws NoSuchFieldException {
        // given
        Optional<Field> syntheticField = Arrays.stream(InnerClass.class.getDeclaredFields())
            .findFirst();

        // when / then
        assertThat(FieldUtils.isRegularInstanceField(ArrayList.class.getDeclaredField("size")), equalTo(true));
        assertThat(FieldUtils.isRegularInstanceField(HashMap.class.getDeclaredField("DEFAULT_LOAD_FACTOR")), equalTo(false));
        if (syntheticField.isPresent()) {
            assertThat(FieldUtils.isRegularInstanceField(syntheticField.get()), equalTo(false));
        } else {
            throw new IllegalStateException("No field found in " + InnerClass.class);
        }
    }

    @Test
    void shouldReturnIfIsNonSyntheticStaticField() throws NoSuchFieldException {
        // given
        Optional<Field> syntheticField = Arrays.stream(InnerClass.class.getDeclaredFields())
            .findFirst();

        // when / then
        assertThat(FieldUtils.isRegularStaticField(ArrayList.class.getDeclaredField("size")), equalTo(false));
        assertThat(FieldUtils.isRegularStaticField(HashMap.class.getDeclaredField("DEFAULT_LOAD_FACTOR")), equalTo(true));
        if (syntheticField.isPresent()) {
            assertThat(FieldUtils.isRegularStaticField(syntheticField.get()), equalTo(false));
        } else {
            throw new IllegalStateException("No field found in " + InnerClass.class);
        }
    }

    @Test
    void shouldGetFieldsIncludingParents() throws NoSuchFieldException {
        // given / when
        List<Field> allFields = FieldUtils.getAllFields(Class3.class).collect(Collectors.toList());

        // then
        assertThat(allFields.size(), greaterThanOrEqualTo(6));
        List<Field> expectedFields = new ArrayList<>();
        expectedFields.add(Class1.class.getDeclaredField("C1A"));
        expectedFields.add(Class1.class.getDeclaredField("c1b"));
        expectedFields.add(Class2.class.getDeclaredField("c2a"));
        expectedFields.add(Class2.class.getDeclaredField("c2b"));
        expectedFields.add(Class3.class.getDeclaredField("c3a"));
        expectedFields.add(Class3.class.getDeclaredField("c3b"));

        // We expect the fields to appear in the given order, but allow other fields in between because
        // various plugins might add synthetic fields during test runs
        Iterator<Field> expectedFieldsIterator = expectedFields.iterator();
        Field expectedField = expectedFieldsIterator.next();
        for (Field field : allFields) {
            if (field.equals(expectedField)) {
                expectedField = expectedFieldsIterator.hasNext() ? expectedFieldsIterator.next() : null;
            }
        }

        if (expectedFieldsIterator.hasNext()) {
            throw new IllegalStateException("Could not match all fields!\nExpected: " + expectedFields
                + "\nActual: " + allFields);
        }
    }

    @Test
    void shouldGetAllFieldsWithFilterAndParentsLast() throws NoSuchFieldException {
        // given / when
        List<Field> allFields = FieldUtils.getAllFields(Class3.class, false)
            .filter(f -> !f.isSynthetic())
            .collect(Collectors.toList());

        // then
        assertThat(allFields, hasSize(6));
        assertThat(allFields.get(0), equalTo(Class3.class.getDeclaredField("c3a")));
        assertThat(allFields.get(1), equalTo(Class3.class.getDeclaredField("c3b")));
        assertThat(allFields.get(2), equalTo(Class2.class.getDeclaredField("c2a")));
        assertThat(allFields.get(3), equalTo(Class2.class.getDeclaredField("c2b")));
        assertThat(allFields.get(4), equalTo(Class1.class.getDeclaredField("C1A")));
        assertThat(allFields.get(5), equalTo(Class1.class.getDeclaredField("c1b")));
    }

    @Test
    void shouldGetAllRegularInstanceFieldsIncludingParents() throws NoSuchFieldException {
        // given / when
        List<Field> allFields1 = FieldUtils.collectAllRegularInstanceFields(Class3.class);
        List<Field> allFields2 = FieldUtils.collectAllRegularInstanceFields(InnerClass.class);

        // then
        assertThat(allFields1, hasSize(4));
        assertThat(allFields1.get(0), equalTo(Class1.class.getDeclaredField("c1b")));
        assertThat(allFields1.get(1), equalTo(Class2.class.getDeclaredField("c2a")));
        assertThat(allFields1.get(2), equalTo(Class2.class.getDeclaredField("c2b")));
        assertThat(allFields1.get(3), equalTo(Class3.class.getDeclaredField("c3b")));

        assertThat(allFields2, empty());
    }

    @Test
    void shouldReturnFieldFromClassIfExists() throws NoSuchFieldException {
        // given / when / then
        assertThat(FieldUtils.tryFindField(Class2.class, "c2a"), equalTo(Optional.of(Class2.class.getDeclaredField("c2a"))));
        assertThat(FieldUtils.tryFindField(Class2.class, "c1b"), equalTo(Optional.empty())); // Make sure parent classes are not included
        assertThat(FieldUtils.tryFindField(Class2.class, "bogus"), equalTo(Optional.empty()));
    }

    @Test
    void shouldReturnFieldFromClassOrParentIfExists() throws NoSuchFieldException {
        // given / when / then
        assertThat(FieldUtils.tryFindFieldInClassOrParent(Class3.class, "c3a"), equalTo(Optional.of(Class3.class.getDeclaredField("c3a"))));
        assertThat(FieldUtils.tryFindFieldInClassOrParent(Class3.class, "c2a"), equalTo(Optional.of(Class2.class.getDeclaredField("c2a"))));
        assertThat(FieldUtils.tryFindFieldInClassOrParent(Class3.class, "C1A"), equalTo(Optional.of(Class1.class.getDeclaredField("C1A"))));

        // Ensure the field lowest in the hierarchy is matched, if multiple field names have the same name
        assertThat(FieldUtils.tryFindFieldInClassOrParent(ClassWithSameFieldNames.class, "c1b"), equalTo(Optional.of(ClassWithSameFieldNames.class.getDeclaredField("c1b"))));
        assertThat(FieldUtils.tryFindFieldInClassOrParent(ClassWithSameFieldNames.class, "c2b"), equalTo(Optional.of(ClassWithSameFieldNames.class.getDeclaredField("c2b"))));
        assertThat(FieldUtils.tryFindFieldInClassOrParent(ClassWithSameFieldNames.class, "c3b"), equalTo(Optional.of(Class3.class.getDeclaredField("c3b"))));
        assertThat(FieldUtils.tryFindFieldInClassOrParent(ClassWithSameFieldNames.class, "c2a"), equalTo(Optional.of(Class2.class.getDeclaredField("c2a"))));

        assertThat(FieldUtils.tryFindFieldInClassOrParent(Class3.class, "bogus"), equalTo(Optional.empty()));
        assertThat(FieldUtils.tryFindFieldInClassOrParent(Class3.class, ""), equalTo(Optional.empty()));
    }

    @Test
    void shouldCollectFieldsByName() throws NoSuchFieldException {
        // given
        Field c1a = Class1.class.getDeclaredField("C1A");
        Field c1b = Class1.class.getDeclaredField("c1b");
        Field c2a = Class2.class.getDeclaredField("c2a");
        Field c2b = Class2.class.getDeclaredField("c2b");
        Field c3a = Class3.class.getDeclaredField("c3a");
        Field c3b = Class3.class.getDeclaredField("c3b");

        Field x1b = ClassWithSameFieldNames.class.getDeclaredField("c1b");
        Field x2b = ClassWithSameFieldNames.class.getDeclaredField("c2b");

        // when
        LinkedHashMap<String, Field> firstFieldsParentFirst = FieldUtils.getAllFields(ClassWithSameFieldNames.class, true)
            .filter(field -> !field.isSynthetic())
            .collect(FieldUtils.collectByName(true));
        LinkedHashMap<String, Field> firstFieldsParentLast = FieldUtils.getAllFields(ClassWithSameFieldNames.class, false)
            .filter(field -> !field.isSynthetic())
            .collect(FieldUtils.collectByName(true));
        LinkedHashMap<String, Field> lastFieldsParentFirst = FieldUtils.getAllFields(ClassWithSameFieldNames.class, true)
            .filter(field -> !field.isSynthetic())
            .collect(FieldUtils.collectByName(false));
        LinkedHashMap<String, Field> lastFieldsParentLast = FieldUtils.getAllFields(ClassWithSameFieldNames.class, false)
            .filter(field -> !field.isSynthetic())
            .collect(FieldUtils.collectByName(false));

        // then
        assertThat(firstFieldsParentFirst.keySet(), contains("C1A", "c1b", "c2a", "c2b", "c3a", "c3b"));
        assertThat(firstFieldsParentFirst.values(), contains( c1a,   c1b,   c2a,   c2b,   c3a,   c3b));
        assertThat(firstFieldsParentLast.keySet(),  contains("c1b", "c2b", "c3a", "c3b", "c2a", "C1A"));
        assertThat(firstFieldsParentLast.values(),  contains( x1b,   x2b,   c3a,   c3b,   c2a,   c1a ));
        assertThat(lastFieldsParentFirst.keySet(),  contains("C1A", "c1b", "c2a", "c2b", "c3a", "c3b"));
        assertThat(lastFieldsParentFirst.values(),  contains( c1a,   x1b,   c2a,   x2b,   c3a,   c3b ));
        assertThat(lastFieldsParentLast.keySet(),   contains("c1b", "c2b", "c3a", "c3b", "c2a", "C1A"));
        assertThat(lastFieldsParentLast.values(),   contains( c1b,   c2b,   c3a,   c3b,   c2a,   c1a ));
    }

    @Test
    void shouldHaveValidJavadoc_collectByName() throws NoSuchFieldException {
        // given
        Field x2b = ClassWithSameFieldNames.class.getDeclaredField("c2b");
        Field c2a = Class2.class.getDeclaredField("c2a");
        Field c2b = Class2.class.getDeclaredField("c2b");


        Stream<Field> stream = Stream.of(x2b, c2a, c2b);

        // when
        Map<String, List<Field>> result = stream.collect(Collectors.groupingBy(Field::getName));

        // then
        assertThat(result.keySet(), containsInAnyOrder("c2a", "c2b"));
        assertThat(result.get("c2a"), contains(c2a));
        assertThat(result.get("c2b"), contains(x2b, c2b));
    }

    private class InnerClass {
    }

    private static class Class1 {

        private static final char C1A = 'ǫ';
        private int c1b = 2;

    }

    private static class Class2 extends Class1 {

        private double c2a;
        private String c2b;

    }

    private static class Class3 extends Class2 {

        private static TimeUnit c3a;
        private int c3b;

    }

    private static class ClassWithSameFieldNames extends Class3 {

        private double c1b;
        private String c2b;

    }
}