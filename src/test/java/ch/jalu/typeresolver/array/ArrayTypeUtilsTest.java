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
 * Test for {@link ArrayTypeUtils}.
 */
class ArrayTypeUtilsTest {

    @Test
    void shouldCreateArrayClass() {
        // given / when
        Class<?> stringArr = ArrayTypeUtils.createArrayClass(String.class);
        Class<?> byte3dArr = ArrayTypeUtils.createArrayClass(byte[][].class);

        // then
        assertThat(stringArr, equalTo(String[].class));
        assertThat(byte3dArr, equalTo(byte[][][].class));
    }

    @Test
    void shouldCreateArrayClassWithDefinedDimension() {
        // given / when
        Class<?> stringArr = ArrayTypeUtils.createArrayClass(String.class, 3);
        Class<?> charArr = ArrayTypeUtils.createArrayClass(char.class, 0);
        Class<?> bigDecimalArr = ArrayTypeUtils.createArrayClass(BigDecimal.class, 2);
        Class<?> bigDecimalArr4 = ArrayTypeUtils.createArrayClass(BigDecimal[][].class, 2);

        // then
        assertThat(stringArr, equalTo(String[][][].class));
        assertThat(charArr, equalTo(char.class));
        assertThat(bigDecimalArr, equalTo(BigDecimal[][].class));
        assertThat(bigDecimalArr4, equalTo(BigDecimal[][][][].class));
    }

    @Test
    void shouldCreateClassWithAdditionalArrayDimension() {
        // given / when / then
        assertThat(ArrayTypeUtils.createArrayClass(String.class), equalTo(String[].class));
        assertThat(ArrayTypeUtils.createArrayClass(byte[].class), equalTo(byte[][].class));
        assertThat(ArrayTypeUtils.createArrayClass(Serializable[][][].class), equalTo(Serializable[][][][].class));
    }

    @Test
    void shouldThrowForVoidAsComponent() {
        // given / when / then
        assertThrows(IllegalArgumentException.class, () -> ArrayTypeUtils.createArrayClass(void.class));
    }

    @Test
    void shouldCreateArrayTypeWithCorrectDimension() {
        // given
        Type integer = Integer.class;
        Type genericList = new TypeReference<List<Double>>() { }.getType();

        // when
        Type integerArray = ArrayTypeUtils.createArrayType(integer, 2);
        Type genericListArray = ArrayTypeUtils.createArrayType(genericList, 3);

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
        assertThat(ArrayTypeUtils.createArrayType(string, 0), equalTo(string));
        assertThat(ArrayTypeUtils.createArrayType(genericSet, 0), equalTo(genericSet));
    }

    @Test
    void shouldThrowForNegativeDimensions() {
        // given
        Type string = String.class;
        Type genericSet = new TypeReference<Set<TimeUnit>>() { }.getType();

        // when / then
        assertThrows(IllegalArgumentException.class, () -> ArrayTypeUtils.createArrayType(string, -2));
        assertThrows(IllegalArgumentException.class, () -> ArrayTypeUtils.createArrayType(genericSet, -1));
    }

    @Test
    void shouldReturnAppropriateArrayPropertiesObject() {
        // given
        Type doubleListArray = new TypeReference<List<Double>[]>() { }.getType();
        Class<?> charArray = char[][][].class;

        // when
        ArrayTypeProperties doubleArrayProperties = ArrayTypeUtils.getArrayProperty(doubleListArray);
        ArrayTypeProperties charArrayProperties = ArrayTypeUtils.getArrayProperty(charArray);

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
        ArrayTypeProperties stringClassInfo = ArrayTypeUtils.getArrayProperty(stringClass);
        ArrayTypeProperties wildcardInfo = ArrayTypeUtils.getArrayProperty(wildcard);

        // then
        assertThat(stringClassInfo.getComponentType(), equalTo(stringClass));
        assertThat(stringClassInfo.getDimension(), equalTo(0));
        assertThat(wildcardInfo.getComponentType(), equalTo(wildcard));
        assertThat(wildcardInfo.getDimension(), equalTo(0));
    }
}