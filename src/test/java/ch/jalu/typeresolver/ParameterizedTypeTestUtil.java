package ch.jalu.typeresolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class ParameterizedTypeTestUtil {

    private ParameterizedTypeTestUtil() {
    }


    public static void assertIsParameterizedType(TypeInfo actualType,
                                                 Class<?> expectedRawType, Type... expectedTypeArguments) {
        assertIsParameterizedType(actualType.getType(), expectedRawType, expectedTypeArguments);
    }

    public static void assertIsParameterizedType(Type actualType,
                                                 Class<?> expectedRawType, Type... expectedTypeArguments) {
        assertTrue(actualType instanceof ParameterizedType);
        ParameterizedType pt = (ParameterizedType) actualType;
        assertEquals(expectedRawType, pt.getRawType());
        assertArrayEquals(expectedTypeArguments, pt.getActualTypeArguments());
    }
}
