package ch.jalu.typeresolver.array;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test for {@link ArrayClassProperties}.
 */
class ArrayClassPropertiesTest {

    @Test
    void shouldUnwrapArrayClass() {
        // given / when / then
        testArrayDescription(String[].class, String.class, 1);
        testArrayDescription(short[][][][].class, short.class, 4);
        testArrayDescription(Integer.class, Integer.class, 0);
    }

    private static void testArrayDescription(Class<?> givenArrayClass,
                                             Class<?> expectedComponentType, int expectedDimension) {
        ArrayClassProperties actualDescription = ArrayClassProperties.getArrayPropertiesOfClass(givenArrayClass);

        assertThat(actualDescription.getComponentType(), equalTo(expectedComponentType));
        assertThat(actualDescription.getDimension(), equalTo(expectedDimension));
    }
}