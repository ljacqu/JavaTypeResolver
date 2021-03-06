package ch.jalu.typeresolver;

import ch.jalu.typeresolver.array.AbstractArrayProperties;
import ch.jalu.typeresolver.reference.TypeReference;
import ch.jalu.typeresolver.typeimpl.GenericArrayTypeImpl;
import ch.jalu.typeresolver.typeimpl.WildcardTypeImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

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
        assertThat(CommonTypeUtil.getRawType(list), equalTo(List.class));
        assertThat(CommonTypeUtil.getRawType(map), equalTo(Map.class));
    }

    @Test
    void shouldCreateArrayClass() {
        // given / when
        Class<?> stringArr = CommonTypeUtil.createArrayClass(String.class);
        Class<?> byte3dArr = CommonTypeUtil.createArrayClass(byte[][].class);

        // then
        assertThat(stringArr, equalTo(String[].class));
        assertThat(byte3dArr, equalTo(byte[][][].class));
    }

    @Test
    void shouldCreateArrayClassWithDefinedDimension() {
        // given / when
        Class<?> stringArr = CommonTypeUtil.createArrayClass(String.class, 3);
        Class<?> charArr = CommonTypeUtil.createArrayClass(char.class, 0);
        Class<?> bigDecimalArr = CommonTypeUtil.createArrayClass(BigDecimal.class, 2);

        // then
        assertThat(stringArr, equalTo(String[][][].class));
        assertThat(charArr, equalTo(char.class));
        assertThat(bigDecimalArr, equalTo(BigDecimal[][].class));
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
        assertThat(CommonTypeUtil.hasExplicitUpperBound(wildcardWithObject), equalTo(false));
        assertThat(CommonTypeUtil.hasExplicitUpperBound(wildcardWithString), equalTo(true));

        assertThat(CommonTypeUtil.hasExplicitUpperBound(emptyWildcard), equalTo(false));
        assertThat(CommonTypeUtil.hasExplicitUpperBound(wildcardWithObjectAndString), equalTo(true));
    }

    @Test
    void shouldReturnDefinitiveClassAsType() {
        // given
        Type stringList = new TypeReference<List<String>>() { }.getType();
        Type stringListArr = new TypeReference<List<String>[][]>() { }.getType();

        // when / then
        assertThat(CommonTypeUtil.getDefinitiveClass(String.class), equalTo(String.class));
        assertThat(CommonTypeUtil.getDefinitiveClass(stringList), equalTo(List.class));
        assertThat(CommonTypeUtil.getDefinitiveClass(stringListArr), equalTo(List[][].class));
    }

    @Test
    void shouldReturnNullIfNoDefinitiveClassIsApplicable() {
        // given
        Type wildcard = WildcardTypeImpl.newWildcardExtends(String.class);
        Type wildcardArray = new GenericArrayTypeImpl(wildcard);

        // when / then
        assertThat(CommonTypeUtil.getDefinitiveClass(wildcard), nullValue());
        assertThat(CommonTypeUtil.getDefinitiveClass(wildcardArray), nullValue());
    }

    @Test
    void shouldCreateArrayTypeWithCorrectDimension() {
        // given
        Type integer = Integer.class;
        Type genericList = new TypeReference<List<Double>>() { }.getType();

        // when
        Type integerArray = CommonTypeUtil.createArrayType(integer, 2);
        Type genericListArray = CommonTypeUtil.createArrayType(genericList, 3);

        // then
        assertThat(integerArray, equalTo(Integer[][].class));
        assertThat(genericListArray, equalTo(new TypeReference<List<Double>[][][]>() { }.getType()));
    }

    @Test
    void shouldReturnSameTypeForZeroOrNegativeDimension() {
        // given
        Type string = String.class;
        Type genericSet = new TypeReference<Set<TimeUnit>>() { }.getType();

        // when / then
        assertThat(CommonTypeUtil.createArrayType(string, 0), equalTo(string));
        assertThat(CommonTypeUtil.createArrayType(string, -2), equalTo(string));
        assertThat(CommonTypeUtil.createArrayType(genericSet, 0), equalTo(genericSet));
        assertThat(CommonTypeUtil.createArrayType(genericSet, -1), equalTo(genericSet));
    }

    @Test
    void shouldReturnAppropriateArrayPropertiesObject() {
        // given
        Type doubleListArray = new TypeReference<List<Double>[]>() { }.getType();
        Class<?> charArray = char[][][].class;

        // when
        AbstractArrayProperties doubleArrayProperties = CommonTypeUtil.getArrayProperty(doubleListArray);
        AbstractArrayProperties charArrayProperties = CommonTypeUtil.getArrayProperty(charArray);

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
        AbstractArrayProperties stringClassInfo = CommonTypeUtil.getArrayProperty(stringClass);
        AbstractArrayProperties wildcardInfo = CommonTypeUtil.getArrayProperty(wildcard);

        // then
        assertThat(stringClassInfo.getComponentType(), equalTo(stringClass));
        assertThat(stringClassInfo.getDimension(), equalTo(0));
        assertThat(wildcardInfo.getComponentType(), equalTo(wildcard));
        assertThat(wildcardInfo.getDimension(), equalTo(0));
    }
}