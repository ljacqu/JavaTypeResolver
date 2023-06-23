package ch.jalu.typeresolver.array;

import ch.jalu.typeresolver.typeimpl.GenericArrayTypeImpl;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * Utilities for array types.
 */
public final class ArrayTypeUtils {

    private ArrayTypeUtils() {
    }

    /**
     * Returns an array class of the given component type, e.g. if the componentType is {@code String.class},
     * then {@code String[].class} is returned.
     *
     * @param componentType the component type of the array
     * @return array class of the given component
     * @throws IllegalArgumentException if the component type is {@code void.class}
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
     * @throws IllegalArgumentException if the dimension is negative, or if the component type is {@code void.class}
     */
    public static Class<?> createArrayClass(Class<?> componentType, int dimension) {
        if (dimension < 0) {
            throw new IllegalArgumentException("Dimension may not be negative");
        } else if (dimension == 0) {
            return componentType;
        }

        int[] dimensions = new int[dimension];
        return Array.newInstance(componentType, dimensions).getClass();
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
     * @throws IllegalArgumentException if the dimension is negative, or if the component type is {@code void.class}
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
    public static ArrayTypeProperties getArrayProperty(Type type) {
        if (type instanceof Class<?>) {
            return ArrayClassProperties.getArrayPropertiesOfClass((Class<?>) type);
        } else if (type instanceof GenericArrayType) {
            return GenericArrayTypeProperties.getArrayPropertiesOfType((GenericArrayType) type);
        }
        return new GenericArrayTypeProperties(type, 0);
    }
}
