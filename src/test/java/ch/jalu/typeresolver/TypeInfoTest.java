package ch.jalu.typeresolver;

import org.junit.jupiter.api.Test;

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

    private static TypeInfo getType(String fieldName) {
        try {
            return new TypeInfo(ParameterizedTypes.class.getDeclaredField(fieldName).getGenericType());
        } catch (Exception e) {
            throw new IllegalStateException(fieldName, e);
        }
    }

    private static final class ParameterizedTypes<V> {
        private List<String> stringList;
        private Map<?, ?> questionMarkMap;
        private Set<List<String>> stringListSet;
        private Map<Number, Integer> numberIntegerMap;
    }
}