package ch.jalu.typeresolver.reflect;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Utils class for constructors.
 */
public final class ConstructorUtils {

    private ConstructorUtils() {
    }

    /**
     * Returns the specified constructor if it exists, otherwise null.
     *
     * @param clazz the class to search in
     * @param parameterTypes the parameter types the constructor must match
     * @param <T> class type
     * @return the matching constructor, or null
     */
    // Optional<Constructor<T>> would be a nicer return type, but generics don't play well when the incoming `clazz`
    // parameter is typed as Class<?>
    public static <T> @Nullable Constructor<T> getConstructorOrNull(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException ignore) {
            return null;
        }
    }

    /**
     * Returns the specified constructor, throwing an exception if it does not exist.
     *
     * @param clazz the class to search in
     * @param parameterTypes the parameter types the constructor must match
     * @param <T> class type
     * @return the matching constructor
     */
    public static <T> Constructor<T> getConstructorOrThrow(Class<T> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No constructor on '" + clazz
                + "' matches the parameter types: [" + createListOfParamTypes(parameterTypes) + "]", e);
        }
    }

    /**
     * Calls the given constructor with the provided values. Throws a runtime exception if any exception occurs.
     *
     * @param constructor the constructor to invoke
     * @param values the values to invoke the constructor with
     * @param <T> class type of the constructor
     * @return the constructed object
     */
    public static <T> T invokeConstructor(Constructor<T> constructor, Object... values) {
        try {
            return constructor.newInstance(values);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to call constructor for '"
                + constructor.getDeclaringClass() + "'", e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to call constructor for '"
                + constructor.getDeclaringClass() + "'", e);
        }
    }

    /**
     * Creates a new object from the specified class's zero-args constructor. The constructor may be private.
     * An exception is thrown if the class does not have a zero-args constructor.
     *
     * @param clazz the class to instantiate
     * @param <T> the class type
     * @return new instance of the class
     */
    public static <T> T newInstanceFromZeroArgsConstructor(Class<T> clazz) {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return invokeConstructor(constructor);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                "Expected class '" + clazz.getName() + "' to have a zero-args constructor", e);
        }
    }

    /**
     * Creates a textual representation of the given constructor, using the involved type's simple name,
     * {@link Class#getSimpleName()}.
     * <p>
     * Examples: "Integer(int)", "HashMap(int, float)"
     *
     * @param constructor the constructor
     * @return string representation of the constructor
     */
    public static String simpleToString(Constructor<?> constructor) {
        String className = constructor.getDeclaringClass().getSimpleName();
        return className + "(" + createListOfParamTypes(constructor.getParameterTypes()) + ")";
    }

    private static String createListOfParamTypes(Class<?>[] paramTypes) {
        return Arrays.stream(paramTypes)
            .map(Class::getSimpleName)
            .collect(Collectors.joining(", "));
    }
}
