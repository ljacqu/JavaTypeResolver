package ch.jalu.typeresolver.typeimpl;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Common parent class for tests of Type implementations to ensure that they
 * are in sync with the internal JDK implementation.
 */
abstract class AbstractTypeImplTest {

    final Type[] types;
    final Type[] jdkTypes;

    AbstractTypeImplTest(Type type1, Type type2, Type type3, Type jdkType1, Type jdkType2, Type jdkType3) {
        this.types = new Type[]{ type1, type2, type3 };
        this.jdkTypes = new Type[]{ jdkType1, jdkType2, jdkType3 };
    }

    @Test
    void shouldBeEqualToJdkTypes() {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                String description = i + ", " + j;
                boolean expectedEquals = (i == j);

                assertThat(description, types[i].equals(jdkTypes[j]), equalTo(expectedEquals));
                assertThat(description, jdkTypes[j].equals(types[i]), equalTo(expectedEquals));
            }
            assertThat(Integer.toString(i), types[i].equals(types[i]), equalTo(true));
            assertThat(Integer.toString(i), types[i].equals(Object.class), equalTo(false));
        }
    }

    @Test
    void shouldDefineSameToString() {
        testValueFromImplAndJdk(Object::toString);
    }

    @Test
    void shouldHaveSameHashCodeAsImplementation() {
        testValueFromImplAndJdk(Object::hashCode);
    }

    private void testValueFromImplAndJdk(Function<Type, Object> function) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                String description = i + ", " + j;
                if (i == j) {
                    assertThat(description, function.apply(types[i]), equalTo(function.apply(jdkTypes[j])));
                } else {
                    assertThat(description, function.apply(types[i]), not(function.apply(jdkTypes[j])));
                }
            }
        }
    }
}
