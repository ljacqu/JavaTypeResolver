package ch.jalu.typeresolver;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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

        // / when / then
        assertThat(FieldUtils.isRegularInstanceField(ArrayList.class.getDeclaredField("size")), equalTo(true));
        assertThat(FieldUtils.isRegularInstanceField(HashMap.class.getDeclaredField("DEFAULT_LOAD_FACTOR")), equalTo(false));
        if (syntheticField.isPresent()) {
            assertThat(FieldUtils.isRegularInstanceField(syntheticField.get()), equalTo(false));
        } else {
            throw new IllegalStateException("No field found in " + InnerClass.class);
        }
    }

    @Test
    void shouldGetFieldsIncludingParents() throws NoSuchFieldException {
        // given / when
        List<Field> allFields = FieldUtils.getFieldsIncludingParents(Class3.class);

        // then
        assertThat(allFields, hasSize(6));
        assertThat(allFields.get(0), equalTo(Class1.class.getDeclaredField("C1A")));
        assertThat(allFields.get(1), equalTo(Class1.class.getDeclaredField("c1b")));
        assertThat(allFields.get(2), equalTo(Class2.class.getDeclaredField("c2a")));
        assertThat(allFields.get(3), equalTo(Class2.class.getDeclaredField("c2b")));
        assertThat(allFields.get(4), equalTo(Class3.class.getDeclaredField("c3a")));
        assertThat(allFields.get(5), equalTo(Class3.class.getDeclaredField("c3b")));
    }

    @Test
    void shouldGetAllFieldsSatisfyingFilter() throws NoSuchFieldException {
        // given / when
        List<Field> allFields = FieldUtils.getFieldsIncludingParents(Class3.class,
            field -> field.getType().isPrimitive());

        // then
        assertThat(allFields, hasSize(4));
        assertThat(allFields.get(0), equalTo(Class1.class.getDeclaredField("C1A")));
        assertThat(allFields.get(1), equalTo(Class1.class.getDeclaredField("c1b")));
        assertThat(allFields.get(2), equalTo(Class2.class.getDeclaredField("c2a")));
        assertThat(allFields.get(3), equalTo(Class3.class.getDeclaredField("c3b")));
    }

    @Test
    void shouldGetAllFieldsWithFilterAndParentsLast() throws NoSuchFieldException {
        // given / when
        List<Field> allFields = FieldUtils.getFieldsIncludingParents(Class3.class,
            field -> !field.getName().endsWith("a"), false);

        // then
        assertThat(allFields, hasSize(4));
        assertThat(allFields.get(0), equalTo(Class3.class.getDeclaredField("c3b")));
        assertThat(allFields.get(1), equalTo(Class2.class.getDeclaredField("c2b")));
        assertThat(allFields.get(2), equalTo(Class1.class.getDeclaredField("C1A")));
        assertThat(allFields.get(3), equalTo(Class1.class.getDeclaredField("c1b")));
    }

    @Test
    void shouldGetAllRegularInstanceFieldsIncludingParents() throws NoSuchFieldException {
        // given / when
        List<Field> allFields1 = FieldUtils.getInstanceFieldsIncludingParents(Class3.class);
        List<Field> allFields2 = FieldUtils.getInstanceFieldsIncludingParents(InnerClass.class);

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
        assertThat(FieldUtils.tryFindFieldInClassOrParents(Class3.class, "c3a"), equalTo(Optional.of(Class3.class.getDeclaredField("c3a"))));
        assertThat(FieldUtils.tryFindFieldInClassOrParents(Class3.class, "c2a"), equalTo(Optional.of(Class2.class.getDeclaredField("c2a"))));
        assertThat(FieldUtils.tryFindFieldInClassOrParents(Class3.class, "C1A"), equalTo(Optional.of(Class1.class.getDeclaredField("C1A"))));

        assertThat(FieldUtils.tryFindFieldInClassOrParents(Class3.class, "bogus"), equalTo(Optional.empty()));
        assertThat(FieldUtils.tryFindFieldInClassOrParents(Class3.class, ""), equalTo(Optional.empty()));
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
}