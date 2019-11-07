package ch.jalu.typeresolver.typeimpl;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/**
 * Common parent class for tests of Type implementations to ensure that they
 * are in sync with the JRE implementation.
 */
abstract class AbstractTypeImplTest {

    final Type[] types;
    final Type[] jreTypes;

    AbstractTypeImplTest(Type type1, Type type2, Type type3, Type jreType1, Type jreType2, Type jreType3) {
        this.types = new Type[]{ type1, type2, type3 };
        this.jreTypes = new Type[]{ jreType1, jreType2, jreType3 };
    }

    @Test
    void shouldBeEqualToJreTypes() {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                String description = i + ", " + j;
                boolean expectedEquals = (i == j);

                assertThat(description, types[i].equals(jreTypes[j]), equalTo(expectedEquals));
                assertThat(description, jreTypes[j].equals(types[i]), equalTo(expectedEquals));
            }
            assertThat(Integer.toString(i), types[i].equals(types[i]), equalTo(true));
            assertThat(Integer.toString(i), types[i].equals(Object.class), equalTo(false));
        }
    }

    @Test
    void shouldDefineSameToString() {
        testValueFromImplAndJre(Object::toString);
    }

    @Test
    void shouldHaveSameHashCodeAsImplementation() {
        testValueFromImplAndJre(Object::hashCode);
    }

    private void testValueFromImplAndJre(Function<Type, Object> function) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                String description = i + ", " + j;
                if (i == j) {
                    assertThat(description, function.apply(types[i]), equalTo(function.apply(jreTypes[j])));
                } else {
                    assertThat(description, function.apply(types[i]), not(function.apply(jreTypes[j])));
                }
            }
        }
    }
}
