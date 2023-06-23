package ch.jalu.typeresolver.array;

import ch.jalu.typeresolver.reference.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Test for {@link GenericArrayTypeProperties}.
 */
class GenericArrayTypePropertiesTest {

    @Test
    void shouldHaveWorkingExampleInJavadoc() {
        // given / when
        Type stringList = new TypeReference<List<String>>() { }.getType();
        GenericArrayType stringListArray = (GenericArrayType) new TypeReference<List<String>[][]>() { }.getType();
        GenericArrayTypeProperties arrayProps = new GenericArrayTypeProperties(stringListArray);
        GenericArrayTypeProperties expected = new GenericArrayTypeProperties(stringList, 2);

        // then
        assertThat(arrayProps, equalTo(expected));
    }

    @Test
    <T> void shouldUnwrapGenericArrayType() {
        // given
        TypeReference<List<String>[][][]> stringList3d = new TypeReference<List<String>[][][]>() { };
        TypeReference<Map<Double, String>[]> doubleMap1d = new TypeReference<Map<Double, String>[]>() { };
        TypeReference<T[][]> typeVar2d = new TypeReference<T[][]>() { };

        // when / then
        testGenericArrayDescription(stringList3d.getType(),
            new TypeReference<List<String>>() { }, 3);
        testGenericArrayDescription(doubleMap1d.getType(),
            new TypeReference<Map<Double, String>>() { }, 1);
        testGenericArrayDescription(typeVar2d.getType(),
            new TypeReference<T>() { }, 2);
    }

    @Test
    void shouldDefineProperEqualsAndHashCode() {
        // given
        Type stringList = new TypeReference<List<String>>() { }.getType();
        GenericArrayType stringList2d = (GenericArrayType) new TypeReference<List<String>[][]>() { }.getType();
        Type map = new TypeReference<Map<Double, Float>>() { }.getType();

        GenericArrayTypeProperties prop1 = new GenericArrayTypeProperties(stringList, 2);
        GenericArrayTypeProperties prop2 = new GenericArrayTypeProperties(stringList2d);
        GenericArrayTypeProperties prop3 = new GenericArrayTypeProperties(map, 2);

        // when / then
        assertThat(prop1, equalTo(prop2));
        assertThat(prop1.hashCode(), equalTo(prop2.hashCode()));
        assertThat(prop1, not(equalTo(prop3)));
        assertThat(prop1.hashCode(), not(equalTo(prop3.hashCode())));

        assertThat(prop1.equals(prop1), equalTo(true));
        assertThat(prop1.equals(new Object()), equalTo(false));
    }

    @Test
    void shouldDefineToString() {
        // given
        Type listType = new TypeReference<List<Integer>>() { }.getType();
        GenericArrayTypeProperties prop = new GenericArrayTypeProperties(listType, 3);

        // when / then
        assertThat(prop.toString(), equalTo(prop.getClass().getSimpleName() + "[componentType='java.util.List<java.lang.Integer>', dimension=3]"));
    }

    private static void testGenericArrayDescription(Type givenArrayClass,
                                                    TypeReference<?> expectedComponentType, int expectedDimension) {
        GenericArrayTypeProperties actualDescription = new GenericArrayTypeProperties((GenericArrayType) givenArrayClass);

        assertThat(actualDescription.getComponentType(), equalTo(expectedComponentType.getType()));
        assertThat(actualDescription.getDimension(), equalTo(expectedDimension));
    }
}