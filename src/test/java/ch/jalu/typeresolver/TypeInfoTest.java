package ch.jalu.typeresolver;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test for {@link TypeInfo}.
 */
class TypeInfoTest {

    @Test
    void shouldReturnClassFromClass() {
        assertEquals(new TypeInfo(String.class).toClass(), String.class);
        assertEquals(new TypeInfo(List.class).toClass(), List.class);
        assertEquals(new TypeInfo(int.class).toClass(), int.class);
        assertEquals(new TypeInfo(Double[].class).toClass(), Double[].class);
        assertEquals(new TypeInfo(char[].class).toClass(), char[].class);
    }

    @Test
    void shouldReturnClassFromParameterizedClass() {
        assertEquals(new TypeInfo(getType("stringList")).toClass(), List.class);
        assertEquals(new TypeInfo(getType("stringListSet")).toClass(), Set.class);
        assertEquals(new TypeInfo(getType("questionMarkMap")).toClass(), Map.class);
    }

    @Test
    void shouldReturnNullIfClassCannotBeFound() {
        assertNull(new TypeInfo(null).toClass());
        Type quesionMarkType = new TypeInfo(getType("questionMarkMap")).getGenericTypeInfo(0).getType();
        assertNull(new TypeInfo(quesionMarkType).toClass());
    }

    @Test
    void shouldReturnGenericTypeInfo() {
        assertEquals(new TypeInfo(getType("stringList")).getGenericTypeInfo(0),
            new TypeInfo(String.class));
        assertEquals(new TypeInfo(getType("numberIntegerMap")).getGenericTypeInfo(1),
            new TypeInfo(Integer.class));
        assertEquals(new TypeInfo(getType("stringListSet")).getGenericTypeInfo(0),
            new TypeInfo(getType("stringList")));
    }

    @Test
    void shouldReturnNullAsGenericTypeInfoIfNotApplicable() {
        assertNull(new TypeInfo(getType("stringList")).getGenericTypeInfo(1));
        assertNull(new TypeInfo(String.class).getGenericTypeInfo(0));
        assertNull(new TypeInfo(null).getGenericTypeInfo(1));
        assertNull(new TypeInfo(int.class).getGenericTypeInfo(0));
    }

    @Test
    void shouldReturnGenericTypeAsClass() {
        assertEquals(new TypeInfo(getType("stringList")).getGenericTypeAsClass(0), String.class);
        assertEquals(new TypeInfo(getType("stringListSet")).getGenericTypeAsClass(0), List.class);
        assertEquals(new TypeInfo(getType("numberIntegerMap")).getGenericTypeAsClass(1), Integer.class);
    }

    @Test
    void shouldReturnNullIfGenericTypeClassIsNotApplicable() {
        assertNull(new TypeInfo(null).getGenericTypeAsClass(0));
        assertNull(new TypeInfo(getType("questionMarkMap")).getGenericTypeAsClass(0));
    }

    private static Type getType(String fieldName) {
        try {
            return ParameterizedTypes.class.getDeclaredField(fieldName).getGenericType();
        } catch (Exception e) {
            throw new IllegalStateException(fieldName, e);
        }
    }

    private static final class ParameterizedTypes {
        private List<String> stringList;
        private Map<?, ?> questionMarkMap;
        private Set<List<String>> stringListSet;
        private Map<Number, Integer> numberIntegerMap;
    }
}