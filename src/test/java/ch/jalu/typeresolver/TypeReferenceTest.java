package ch.jalu.typeresolver;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static ch.jalu.typeresolver.ParameterizedTypeTestUtil.assertIsParameterizedType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link TypeReference}.
 */
class TypeReferenceTest {

    @Test
    void shouldHaveClassAsTypeArgument() {
        // given / when
        TypeInfo typeInfo = new TypeReference<String>() {};

        // then
        assertEquals(typeInfo.getType(), String.class);
    }

    @Test
    void shouldHaveParameterizedTypeAsTypeArgument() {
        // given / when
        TypeInfo typeInfo = new TypeReference<Map<String, Double>>() {};

        // then
        assertIsParameterizedType(typeInfo, Map.class, String.class, Double.class);
    }

    @Test
    void shouldHaveArrayAsTypeArgument() {
        // given / when / then
        assertEquals(new TypeReference<Double[][]>() { }.getType(), Double[][].class);
        assertEquals(new TypeReference<char[]>() { }.getType(), char[].class);
    }

    @Test
    void shouldThrowForMissingTypeArgument() {
        // given / when / then
        assertThrows(IllegalStateException.class, () -> new TypeReference() {});
    }
}