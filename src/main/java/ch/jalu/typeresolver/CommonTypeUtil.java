package ch.jalu.typeresolver;

import ch.jalu.typeresolver.array.AbstractArrayProperties;
import ch.jalu.typeresolver.array.ArrayClassProperties;
import ch.jalu.typeresolver.array.GenericArrayTypeProperties;
import ch.jalu.typeresolver.typeimpl.GenericArrayTypeImpl;

import javax.annotation.Nullable;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
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
     * Returns an array class of the given component type and with the given dimensions, e.g. if the componentType is
     * {@code double.class} and the dimension is 3, then {@code double[][][].class} is returned.
     *
     * @param componentType the component type of the array
     * @param dimension the dimension of the class (may not be negative)
     * @return array class of the provided dimension with the given component
     */
    public static Class<?> createArrayClass(Class<?> componentType, int dimension) {
        if (dimension < 0) {
            throw new IllegalArgumentException("Dimension may not be negative");
        }

        Class<?> clazz = componentType;
        for (int i = 0; i < dimension; ++i) {
            clazz = createArrayClass(clazz);
        }
        return clazz;
    }

    /**
     * Returns an array class or a {@link java.lang.reflect.GenericArrayType} instance whose component type is
     * the {@code componentType} argument.
     *
     * @param componentType the component type to create an array type for
     * @return the appropriate array type
     */
    public static Type createArrayType(Type componentType) {
        return createArrayType(componentType, 1);
    }

    /**
     * Returns an array class or a generic array type whose component is the provided component type and
     * with the given dimension.
     *
     * @param componentType the component type
     * @param dimension the dimension of the array (may not be negative)
     * @return the appropriate array type of the given dimension
     */
    public static Type createArrayType(Type componentType, int dimension) {
        if (componentType instanceof Class<?>) {
            return createArrayClass((Class<?>) componentType, dimension);
        }
        return GenericArrayTypeImpl.create(componentType, dimension);
    }

    /**
     * Returns a description of the given array type, or an array property with dimension 0
     * if it is not an array. Never returns null.
     *
     * @param type the type to inspect
     * @return array description of the given type
     */
    public static AbstractArrayProperties getArrayProperty(Type type) {
        if (type instanceof Class<?>) {
            return ArrayClassProperties.getArrayPropertiesOfClass((Class<?>) type);
        } else if (type instanceof GenericArrayType) {
            return GenericArrayTypeProperties.getArrayPropertiesOfType((GenericArrayType) type);
        }
        return new GenericArrayTypeProperties(type, 0);
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

    /**
     * Returns the Class equivalent of the type for which there is an absolute class equivalent,
     * i.e. for all Class, ParameterizedType and for some GenericArrayType instances.
     *
     * @param type the type to return as class if possible
     * @return the type as class, or null if not applicable
     */
    @Nullable
    public static Class<?> getDefinitiveClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getRawType((ParameterizedType) type);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            Class<?> componentAsClass = getDefinitiveClass(gat.getGenericComponentType());
            if (componentAsClass != null) {
                return createArrayClass(componentAsClass);
            }
        }
        return null;
    }
}
