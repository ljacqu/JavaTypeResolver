package ch.jalu.typeresolver;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ParameterizedTypeTestUtil {

    private ParameterizedTypeTestUtil() {
    }

    public static Matcher<Type> isParameterizedType(Class<?> expectedRawType, Type... expectedArguments) {
        return new ParameterizedTypeMatcher<>(Function.identity(), expectedRawType, expectedArguments);
    }

    public static Matcher<TypeInfo> ofParameterizedType(Class<?> expectedRawType, Type... expectedArguments) {
        return new ParameterizedTypeMatcher<>(TypeInfo::getType, expectedRawType, expectedArguments);
    }

    private static class ParameterizedTypeMatcher<T> extends TypeSafeMatcher<T> {

        private final Function<T, Type> unwrapFunction;
        private final Class<?> expectedRawType;
        private final Type[] expectedArguments;

        ParameterizedTypeMatcher(Function<T, Type> unwrapFunction,
                                 Class<?> expectedRawType, Type... expectedArguments) {
            this.unwrapFunction = unwrapFunction;
            this.expectedRawType = expectedRawType;
            this.expectedArguments = expectedArguments;
        }

        @Override
        protected boolean matchesSafely(T item) {
            Type actualType = unwrapFunction.apply(item);
            if (actualType instanceof ParameterizedType) {
                ParameterizedType actual = (ParameterizedType) actualType;
                return expectedRawType.equals(actual.getRawType())
                    && allTypesMatch(expectedArguments, actual.getActualTypeArguments());
            }
            return false;
        }

        private boolean allTypesMatch(Type[] expected, Type[] actual) {
            return expected.length == actual.length
                && IntStream.range(0, expected.length).allMatch(i -> expected[i].equals(actual[i]));
        }

        @Override
        public void describeTo(Description description) {
            String arguments = Arrays.stream(expectedArguments)
                                   .map(Object::toString)
                                   .collect(Collectors.joining(", "));
            description.appendText("Parameterized type with rawType=" + expectedRawType
                + ", with type arguments=" + arguments);
        }
    }
}
