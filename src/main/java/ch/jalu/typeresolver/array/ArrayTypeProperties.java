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
     * Returns the dimension of the array type. For example, the dimension is 1 for a regular array type like
     * {@code String[]}; for {@code double[][][]}, the dimension is 3. If the processed type is not an array, the
     * method returns 0.
     *
     * @return the dimension, or 0 if it is not an array
     */
    int getDimension();

}
