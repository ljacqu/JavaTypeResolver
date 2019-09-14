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
    void shouldReturnTypeArgumentInfo() {
        assertEquals(getType("stringList").getTypeArgumentInfo(0),
            new TypeInfo(String.class));
        assertEquals(getType("numberIntegerMap").getTypeArgumentInfo(1),
            new TypeInfo(Integer.class));
        assertEquals(getType("stringListSet").getTypeArgumentInfo(0),
            getType("stringList"));
    }

    @Test
    void shouldReturnNullAsTypeArgumentIfNotApplicable() {
        assertNull(getType("stringList").getTypeArgumentInfo(1));
        assertNull(new TypeInfo(String.class).getTypeArgumentInfo(0));
        assertNull(new TypeInfo(null).getTypeArgumentInfo(1));
        assertNull(new TypeInfo(int.class).getTypeArgumentInfo(0));
    }

    @Test
    void shouldReturnTypeArgumentAsClass() {
        assertEquals(getType("stringList").getTypeArgumentAsClass(0), String.class);
        assertEquals(getType("stringListSet").getTypeArgumentAsClass(0), List.class);
        assertEquals(getType("numberIntegerMap").getTypeArgumentAsClass(1), Integer.class);
    }

    @Test
    void shouldReturnNullIfTypeArgumentAsClassIsNotApplicable() {
        assertNull(new TypeInfo(null).getTypeArgumentAsClass(0));
        assertNull(getType("questionMarkMap").getTypeArgumentAsClass(0));
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