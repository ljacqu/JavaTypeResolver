package ch.jalu.typeresolver.array;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link ArrayComponentType}.
 */
class ArrayComponentTypeTest {

    @ParameterizedTest
    @MethodSource("argsForDetermineArrayTypeTest")
    void shouldDetermineArrayType(Object array, ArrayComponentType expectedType) {
        // given / when
        ArrayComponentType result = ArrayComponentType.getArrayComponentType(array);

        // then
        assertThat(result, equalTo(expectedType));
        if (result == ArrayComponentType.OBJECT) {
            assertThat(result.getComponentClass(), equalTo(Object.class));
        } else {
            assertThat(result.getComponentClass(), equalTo(array.getClass().getComponentType()));
        }
    }

    static Stream<Arguments> argsForDetermineArrayTypeTest() {
        return Stream.of(
            Arguments.of(new boolean[]{true}, ArrayComponentType.BOOLEAN),
            Arguments.of(new byte[]{}, ArrayComponentType.BYTE),
            Arguments.of(new char[]{'ç'}, ArrayComponentType.CHARACTER),
            Arguments.of(new short[]{}, ArrayComponentType.SHORT),
            Arguments.of(new int[]{22}, ArrayComponentType.INTEGER),
            Arguments.of(new long[]{420}, ArrayComponentType.LONG),
            Arguments.of(new float[]{}, ArrayComponentType.FLOAT),
            Arguments.of(new double[]{3.4}, ArrayComponentType.DOUBLE),

            Arguments.of(new Object[]{'5'}, ArrayComponentType.OBJECT),
            Arguments.of(new Boolean[]{true}, ArrayComponentType.OBJECT),
            Arguments.of(new Double[]{6.2}, ArrayComponentType.OBJECT),
            Arguments.of(new String[]{"t"}, ArrayComponentType.OBJECT),
            Arguments.of(new String[][]{}, ArrayComponentType.OBJECT),
            Arguments.of(new double[][]{{54.2}}, ArrayComponentType.OBJECT),
            Arguments.of(new char[][][]{{{'ê'}}}, ArrayComponentType.OBJECT)
        );
    }

    @Test
    void shouldThrowForNull() {
        // given / when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ArrayComponentType.getArrayComponentType(null));

        // then
        assertThat(ex.getMessage(), equalTo("Expected an array but got an object of type: null"));
    }

    @Test
    void shouldThrowForNonArrayValue() {
        // given / when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> ArrayComponentType.getArrayComponentType("test"));

        // then
        assertThat(ex.getMessage(), equalTo("Expected an array but got an object of type: java.lang.String"));
    }
}