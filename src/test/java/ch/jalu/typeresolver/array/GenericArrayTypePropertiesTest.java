package ch.jalu.typeresolver.array;

import ch.jalu.typeresolver.reference.TypeReference;
import org.junit.jupiter.api.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test for {@link GenericArrayTypeProperties}.
 */
class GenericArrayTypePropertiesTest {

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

    private static void testGenericArrayDescription(Type givenArrayClass,
                                                    TypeReference<?> expectedComponentType, int expectedDimension) {
        GenericArrayTypeProperties actualDescription = GenericArrayTypeProperties.getArrayPropertiesOfType((GenericArrayType) givenArrayClass);

        assertEquals(actualDescription.getComponentType(), expectedComponentType.getType());
        assertEquals(actualDescription.getDimension(), expectedDimension);
    }
}