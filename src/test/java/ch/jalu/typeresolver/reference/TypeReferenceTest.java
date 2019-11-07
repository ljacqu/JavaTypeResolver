package ch.jalu.typeresolver.reference;

import ch.jalu.typeresolver.TypeInfo;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static ch.jalu.typeresolver.ParameterizedTypeTestUtil.ofParameterizedType;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
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
        assertThat(typeInfo.getType(), equalTo(String.class));
    }

    @Test
    void shouldHaveParameterizedTypeAsTypeArgument() {
        // given / when
        TypeInfo typeInfo = new TypeReference<Map<String, Double>>() {};

        // then
        assertThat(typeInfo, ofParameterizedType(Map.class, String.class, Double.class));
    }

    @Test
    void shouldHaveArrayAsTypeArgument() {
        // given / when / then
        assertThat(new TypeReference<Double[][]>() { }.getType(), equalTo(Double[][].class));
        assertThat(new TypeReference<char[]>() { }.getType(), equalTo(char[].class));
    }

    @Test
    void shouldThrowForMissingTypeArgument() {
        // given / when / then
        assertThrows(IllegalStateException.class, () -> new TypeReference() {});
    }
}