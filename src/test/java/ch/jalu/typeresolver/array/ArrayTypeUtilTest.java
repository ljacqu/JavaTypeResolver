package ch.jalu.typeresolver.array;

import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.typeimpl.WildcardTypeImpl;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link ArrayTypeUtil}.
 */
class ArrayTypeUtilTest {

    @Test
    void shouldCreateArrayClass() {
        // given / when
        Class<?> stringArr = ArrayTypeUtil.createArrayClass(String.class);
        Class<?> byte3dArr = ArrayTypeUtil.createArrayClass(byte[][].class);

        // then
        assertThat(stringArr, equalTo(String[].class));
        assertThat(byte3dArr, equalTo(byte[][][].class));
    }

    @Test
    void shouldCreateArrayClassWithDefinedDimension() {
        // given / when
        Class<?> stringArr = ArrayTypeUtil.createArrayClass(String.class, 3);
        Class<?> charArr = ArrayTypeUtil.createArrayClass(char.class, 0);
        Class<?> bigDecimalArr = ArrayTypeUtil.createArrayClass(BigDecimal.class, 2);

        // then
        assertThat(stringArr, equalTo(String[][][].class));
        assertThat(charArr, equalTo(char.class));
        assertThat(bigDecimalArr, equalTo(BigDecimal[][].class));
    }

    @Test
    void shouldCreateClassWithAdditionalArrayDimension() {
        // given / when / then
        assertThat(ArrayTypeUtil.createArrayClass(String.class), equalTo(String[].class));
        assertThat(ArrayTypeUtil.createArrayClass(byte[].class), equalTo(byte[][].class));
        assertThat(ArrayTypeUtil.createArrayClass(Serializable[][][].class), equalTo(Serializable[][][][].class));
    }

    @Test
    void shouldThrowForVoidAsComponent() {
        // given / when / then
        assertThrows(IllegalArgumentException.class, () -> ArrayTypeUtil.createArrayClass(void.class));
    }

    @Test
    void shouldCreateArrayTypeWithCorrectDimension() {
        // given
        Type integer = Integer.class;
        Type genericList = new TypeReference<List<Double>>() { }.getType();

        // when
        Type integerArray = ArrayTypeUtil.createArrayType(integer, 2);
        Type genericListArray = ArrayTypeUtil.createArrayType(genericList, 3);

        // then
        assertThat(integerArray, equalTo(Integer[][].class));
        assertThat(genericListArray, equalTo(new TypeReference<List<Double>[][][]>() { }.getType()));
    }

    @Test
    void shouldReturnSameTypeForZeroDimension() {
        // given
        Type string = String.class;
        Type genericSet = new TypeReference<Set<TimeUnit>>() { }.getType();

        // when / then
        assertThat(ArrayTypeUtil.createArrayType(string, 0), equalTo(string));
        assertThat(ArrayTypeUtil.createArrayType(genericSet, 0), equalTo(genericSet));
    }

    @Test
    void shouldThrowForNegativeDimensions() {
        // given
        Type string = String.class;
        Type genericSet = new TypeReference<Set<TimeUnit>>() { }.getType();

        // when / then
        assertThrows(IllegalArgumentException.class, () -> ArrayTypeUtil.createArrayType(string, -2));
        assertThrows(IllegalArgumentException.class, () -> ArrayTypeUtil.createArrayType(genericSet, -1));
    }

    @Test
    void shouldReturnAppropriateArrayPropertiesObject() {
        // given
        Type doubleListArray = new TypeReference<List<Double>[]>() { }.getType();
        Class<?> charArray = char[][][].class;

        // when
        ArrayTypeProperties doubleArrayProperties = ArrayTypeUtil.getArrayProperty(doubleListArray);
        ArrayTypeProperties charArrayProperties = ArrayTypeUtil.getArrayProperty(charArray);

        // then
        assertThat(doubleArrayProperties.getComponentType(), equalTo(new TypeReference<List<Double>>() { }.getType()));
        assertThat(doubleArrayProperties.getDimension(), equalTo(1));
        assertThat(charArrayProperties.getComponentType(), equalTo(char.class));
        assertThat(charArrayProperties.getDimension(), equalTo(3));
    }

    @Test
    void shouldReturnArrayPropertyWithDimensionZeroForNonArrayTypes() {
        // given
        Class<?> stringClass = String.class;
        Type wildcard = WildcardTypeImpl.newUnboundedWildcard();

        // when
        ArrayTypeProperties stringClassInfo = ArrayTypeUtil.getArrayProperty(stringClass);
        ArrayTypeProperties wildcardInfo = ArrayTypeUtil.getArrayProperty(wildcard);

        // then
        assertThat(stringClassInfo.getComponentType(), equalTo(stringClass));
        assertThat(stringClassInfo.getDimension(), equalTo(0));
        assertThat(wildcardInfo.getComponentType(), equalTo(wildcard));
        assertThat(wildcardInfo.getDimension(), equalTo(0));
    }
}