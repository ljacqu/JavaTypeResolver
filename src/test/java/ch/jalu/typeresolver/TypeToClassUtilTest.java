package ch.jalu.typeresolver;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Test for {@link TypeToClassUtil}.
 */
class TypeToClassUtilTest {

    @Test
    void shouldReturnClassFromClass() {
        // given / when / then
        assertEquals(TypeToClassUtil.getSafeToWriteClass(String.class), String.class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(List.class), List.class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(int.class), int.class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(Double[].class), Double[].class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(char[].class), char[].class);
    }

    @Test
    void shouldReturnClassFromParameterizedClass() {
        // given
        Type stringList = new TypeToken<List<String>>(){ }.getType();
        Type questionMarkMap = new TypeToken<Map<?, ?>>(){ }.getType();
        Type stringListSetArray = new TypeToken<Set<List<String>>[]>(){ }.getType();

        // when / then
        assertEquals(TypeToClassUtil.getSafeToWriteClass(stringList), List.class);
        assertEquals(TypeToClassUtil.getSafeToWriteClass(questionMarkMap), Map.class);
        // TODO assertEquals(TypeToClassUtil.getSafeToWriteClass(stringListSetArray), Set[].class);
    }

    @Test
    void shouldReturnNullIfClassCannotBeDetermined() {
        // given
        Type questionMark = getFirstGenericType(new TypeToken<List<?>>(){ });
        Type superInt = getFirstGenericType(new TypeToken<List<? super Integer>>(){ });
        Type extComparable = getFirstGenericType(new TypeToken<List<? extends Comparable>>(){ });

        // when / then
        assertNull(TypeToClassUtil.getSafeToWriteClass(null));
        assertNull(TypeToClassUtil.getSafeToWriteClass(questionMark));
        assertNull(TypeToClassUtil.getSafeToWriteClass(superInt)); // TODO: Should be Integer.class
        assertNull(TypeToClassUtil.getSafeToWriteClass(extComparable));
    }

    @Test
    void shouldReturnSafeToReadClass() throws NoSuchMethodException {
        // given
        Type number = Number.class;
        Type questionMark = getFirstGenericType(new TypeToken<List<?>>(){ });
        Type superInt = getFirstGenericType(new TypeToken<List<? super Integer>>(){ });
        Type extComparable = getFirstGenericType(new TypeToken<List<? extends Comparable>>(){ });

        // when / then
        assertEquals(TypeToClassUtil.getSafeToReadClass(number), Number.class);
        assertEquals(TypeToClassUtil.getSafeToReadClass(questionMark), Object.class);
        assertEquals(TypeToClassUtil.getSafeToReadClass(superInt), Object.class);
        assertEquals(TypeToClassUtil.getSafeToReadClass(extComparable), Comparable.class);

        Type boundTypeVariable = getClass().getDeclaredMethod("bExtNumber").getGenericReturnType();
        assertEquals(TypeToClassUtil.getSafeToReadClass(boundTypeVariable), Number.class);
    }

    private static Type getFirstGenericType(TypeToken<?> typeToken) {
        return new TypeInfo(typeToken.getType())
            .getGenericTypeInfo(0)
            .getType();
    }

    private <B extends Number> B bExtNumber() {
        return null;
    }
}