package ch.jalu.typeresolver;

import ch.jalu.typeresolver.typeimpl.GenericArrayTypeImpl;
import ch.jalu.typeresolver.typeimpl.ParameterizedTypeImpl;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Contains simple utility methods for Type objects.
 */
public final class CommonTypeUtil {

    private CommonTypeUtil() {
    }

    /**
     * Convenience method which returns the raw type of the given parameterized type cast as {@link Class}.
     * <p>
     * Background: in current JRE versions, the raw type can only be a Class, so the raw type can be cast
     * without checking it beforehand.
     *
     * @param parameterizedType the parameterized type whose raw type should be returned
     * @return the raw type of the parameterized type
     */
    public static Class<?> getRawType(ParameterizedType parameterizedType) {
        return (Class<?>) parameterizedType.getRawType();
    }

    /**
     * Returns an array class of the given component type, e.g. if the componentType is {@code String.class},
     * then {@code String[].class} is returned.
     *
     * @param componentType the component type of the array
     * @return array class of the given component
     */
    public static Class<?> createArrayClass(Class<?> componentType) {
        return Array.newInstance(componentType, 0).getClass();
    }

    /**
     * Returns an array class or a {@link java.lang.reflect.GenericArrayType} instance whose component type is
     * the {@code componentType} argument.
     *
     * @param componentType the component type to create an array type for
     * @return the appropriate array type
     */
    public static Type createArrayType(Type componentType) {
        if (componentType instanceof Class<?>) {
            return createArrayClass((Class<?>) componentType);
        }
        return new GenericArrayTypeImpl(componentType);
    }

    /**
     * Returns whether the given wildcard type has an explicitly defined upper bound.
     * <p>
     * Background: the JRE sets the upper bounds to an array with one element of Object.class if the wildcard has
     * lower bounds, or if the wildcard has no specified bounds (i.e. a simple "?").
     *
     * @param wildcardType the wildcard type to analyze
     * @return true if it has a specific upper bound
     */
    public static boolean hasExplicitUpperBound(WildcardType wildcardType) {
        Type[] upperBounds = wildcardType.getUpperBounds();
        if (upperBounds.length == 0) {
            return false;
        }
        return !Object.class.equals(upperBounds[0]) || upperBounds.length > 1;
    }

    @Nullable
    public static ParameterizedType createParameterizedType(Class<?> rawType) {
        if (rawType.getTypeParameters().length > 0) {
            return new ParameterizedTypeImpl(rawType, rawType.getEnclosingClass(), rawType.getTypeParameters());
        }
        return null;
    }
}
