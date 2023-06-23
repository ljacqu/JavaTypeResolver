package ch.jalu.typeresolver.array;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Test for {@link ArrayClassProperties}.
 */
class ArrayClassPropertiesTest {

    @Test
    void shouldHaveValidJavadocExample() {
        // given / when
        ArrayClassProperties arrayProps1 = new ArrayClassProperties(String[][].class);
        ArrayClassProperties expected1 = new ArrayClassProperties(String.class, 2);
        ArrayClassProperties arrayProps2 = new ArrayClassProperties(String.class);
        ArrayClassProperties expected2 = new ArrayClassProperties(String.class, 0);

        // then
        assertThat(arrayProps1, equalTo(expected1));
        assertThat(arrayProps2, equalTo(expected2));
    }

    @Test
    void shouldUnwrapArrayClass() {
        // given / when / then
        testArrayDescription(String[].class, String.class, 1);
        testArrayDescription(short[][][][].class, short.class, 4);
        testArrayDescription(Integer.class, Integer.class, 0);
    }

    @Test
    void shouldDefineProperEqualsAndHashCode() {
        // given
        ArrayClassProperties prop1 = new ArrayClassProperties(String.class, 2);
        ArrayClassProperties prop2 = new ArrayClassProperties(String[][].class);
        ArrayClassProperties prop3 = new ArrayClassProperties(double.class, 2);

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
        ArrayClassProperties prop = new ArrayClassProperties(Integer.class, 2);

        // when / then
        assertThat(prop.toString(), equalTo(prop.getClass().getSimpleName() + "[componentType='class java.lang.Integer', dimension=2]"));
    }

    private static void testArrayDescription(Class<?> givenArrayClass,
                                             Class<?> expectedComponentType, int expectedDimension) {
        ArrayClassProperties actualDescription = new ArrayClassProperties(givenArrayClass);

        assertThat(actualDescription.getComponentType(), equalTo(expectedComponentType));
        assertThat(actualDescription.getDimension(), equalTo(expectedDimension));
    }
}