package ch.jalu.typeresolver.array;

import java.lang.reflect.Type;

/**
 * Describes an array type.
 */
public interface ArrayTypeProperties {

    /**
     * Returns the component of the array. Never an array itself.
     *
     * @return the component
     */
    Type getComponentType();

    /**
     * Returns the dimension of the array type, i.e. 1 for a regular array type like {@code String[]},
     * or, for instance, 3 for a type like {@code double[][][]}. May return 0 if the processed type
     * is not an array.
     *
     * @return the dimension, or 0 if is not an array
     */
    int getDimension();
}
