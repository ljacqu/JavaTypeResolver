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
        assertEquals(getType("stringList").toClass(), List.class);
        assertEquals(getType("stringListSet").toClass(), Set.class);
        assertEquals(getType("questionMarkMap").toClass(), Map.class);
    }

    @Test
    void shouldReturnNullIfClassCannotBeDetermined() {
        assertNull(new TypeInfo(null).toClass());
        assertNull(getFirstGenericTypeFromField("questionMarkMap").toClass());
        assertNull(getFirstGenericTypeFromField("superIntList").toClass());
        assertNull(getFirstGenericTypeFromField("extComparableList").toClass());
    }

    @Test
    void shouldReturnGenericTypeInfo() {
        assertEquals(getType("stringList").getGenericTypeInfo(0),
            new TypeInfo(String.class));
        assertEquals(getType("numberIntegerMap").getGenericTypeInfo(1),
            new TypeInfo(Integer.class));
        assertEquals(getType("stringListSet").getGenericTypeInfo(0),
            getType("stringList"));
    }

    @Test
    void shouldReturnNullAsGenericTypeInfoIfNotApplicable() {
        assertNull(getType("stringList").getGenericTypeInfo(1));
        assertNull(new TypeInfo(String.class).getGenericTypeInfo(0));
        assertNull(new TypeInfo(null).getGenericTypeInfo(1));
        assertNull(new TypeInfo(int.class).getGenericTypeInfo(0));
    }

    @Test
    void shouldReturnGenericTypeAsClass() {
        assertEquals(getType("stringList").getGenericTypeAsClass(0), String.class);
        assertEquals(getType("stringListSet").getGenericTypeAsClass(0), List.class);
        assertEquals(getType("numberIntegerMap").getGenericTypeAsClass(1), Integer.class);
    }

    @Test
    void shouldReturnNullIfGenericTypeClassIsNotApplicable() {
        assertNull(new TypeInfo(null).getGenericTypeAsClass(0));
        assertNull(getType("questionMarkMap").getGenericTypeAsClass(0));
    }

    @Test
    void shouldReturnSafeToReadClass() throws NoSuchMethodException {
        assertEquals(getFirstGenericTypeFromField("numberIntegerMap").getSafeToReadClass(), Number.class);
        assertEquals(getFirstGenericTypeFromField("questionMarkMap").getSafeToReadClass(), Object.class);
        assertEquals(getFirstGenericTypeFromField("superIntList").getSafeToReadClass(), Object.class);
        assertEquals(getFirstGenericTypeFromField("extComparableList").getSafeToReadClass(), Comparable.class);

        Type boundTypeVariable = ParameterizedTypes.class.getDeclaredMethod("bExtSerializable").getGenericReturnType();
        assertEquals(new TypeInfo(boundTypeVariable).getSafeToReadClass(), Number.class);
    }

    private static TypeInfo getType(String fieldName) {
        try {
            return new TypeInfo(ParameterizedTypes.class.getDeclaredField(fieldName).getGenericType());
        } catch (Exception e) {
            throw new IllegalStateException(fieldName, e);
        }
    }

    private static TypeInfo getFirstGenericTypeFromField(String fieldName) {
        return getType(fieldName).getGenericTypeInfo(0);
    }

    private static final class ParameterizedTypes<V> {
        private List<String> stringList;
        private Map<?, ?> questionMarkMap;
        private Set<List<String>> stringListSet;
        private Map<Number, Integer> numberIntegerMap;
        private Set<V> typeVariableSet;
        private List<? super Integer> superIntList;
        private List<? extends Comparable> extComparableList;

        private <B extends Number> B bExtSerializable() {
            return null;
        }
    }
}