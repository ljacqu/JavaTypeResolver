package ch.jalu.typeresolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

public final class ParameterizedTypeTestUtil {

    private ParameterizedTypeTestUtil() {
    }


    public static void assertIsParameterizedType(TypeInfo actualType,
                                                 Class<?> expectedRawType, Type... expectedTypeArguments) {
        assertIsParameterizedType(actualType.getType(), expectedRawType, expectedTypeArguments);
    }

    public static void assertIsParameterizedType(Type actualType,
                                                 Class<?> expectedRawType, Type... expectedTypeArguments) {
        assertThat(actualType, instanceOf(ParameterizedType.class));
        ParameterizedType pt = (ParameterizedType) actualType;
        assertThat(expectedRawType, equalTo(pt.getRawType()));
        assertThat(expectedTypeArguments, equalTo(pt.getActualTypeArguments()));
    }
}
