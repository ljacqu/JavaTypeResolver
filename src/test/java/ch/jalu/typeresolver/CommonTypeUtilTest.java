package ch.jalu.typeresolver;

import ch.jalu.typeresolver.typeimpl.WildcardTypeImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test for {@link CommonTypeUtil}.
 */
class CommonTypeUtilTest {

    @Test
    void shouldReturnRawTypeAsClass() {
        // given
        ParameterizedType list = (ParameterizedType) new TypeReference<List<String>>() { }.getType();
        ParameterizedType map = (ParameterizedType) new TypeReference<Map<Integer, ?>>() { }.getType();

        // when / then
        assertEquals(CommonTypeUtil.getRawType(list), List.class);
        assertEquals(CommonTypeUtil.getRawType(map), Map.class);
    }

    @Test
    void shouldCreateArrayClass() {
        // given / when
        Class<?> stringArr = CommonTypeUtil.createArrayClass(String.class);
        Class<?> byte3dArr = CommonTypeUtil.createArrayClass(byte[][].class);

        // then
        assertEquals(stringArr, String[].class);
        assertEquals(byte3dArr, byte[][][].class);
    }

    @Test
    void shouldSpecifyIfHasUpperBounds() {
        // given
        WildcardTypeImpl wildcardWithObject = new WildcardTypeImpl(new Type[]{ Object.class }, new Type[0]);
        WildcardTypeImpl wildcardWithString = new WildcardTypeImpl(new Type[]{ String.class }, new Type[0]);

        // The following are never returned by the JRE (upperBounds always has 1 entry)
        WildcardTypeImpl emptyWildcard = new WildcardTypeImpl(new Type[0], new Type[0]);
        WildcardTypeImpl wildcardWithObjectAndString = new WildcardTypeImpl(new Type[]{ Object.class, String.class }, new Type[0]);

        // when / then
        assertFalse(CommonTypeUtil.hasExplicitUpperBound(wildcardWithObject));
        assertTrue(CommonTypeUtil.hasExplicitUpperBound(wildcardWithString));

        assertFalse(CommonTypeUtil.hasExplicitUpperBound(emptyWildcard));
        assertTrue(CommonTypeUtil.hasExplicitUpperBound(wildcardWithObjectAndString));
    }
}