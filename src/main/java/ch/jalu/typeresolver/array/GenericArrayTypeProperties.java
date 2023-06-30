package ch.jalu.typeresolver.array;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Describes a generic array type: specifies the array dimension and the component type.
 * <p>
 * Examples:<pre>{@code
 * Type stringList = new TypeReference<List<String>>() { }.getType();
 * GenericArrayType stringListArray = (GenericArrayType) new TypeReference<List<String>[][]>() { }.getType();
 * GenericArrayTypeProperties arrayProps = new GenericArrayTypeProperties(stringListArray);
 * // arrayProps = new GenericArrayTypeProperties(stringList, 2)}</pre>
 *
 * @see ArrayTypeUtils#getArrayProperty
 */
public class GenericArrayTypeProperties implements ArrayTypeProperties {

    private final Type componentType;
    private final int dimension;

    /**
     * Constructor: creates an instance based on the properties of the given array type.
     *
     * @param genericArrayType the generic array type to describe
     */
    public GenericArrayTypeProperties(GenericArrayType genericArrayType) {
        Type componentType = genericArrayType;
        int dimension = 0;
        while (componentType instanceof GenericArrayType) {
            componentType = ((GenericArrayType) componentType).getGenericComponentType();
            ++dimension;
        }

        this.componentType = componentType;
        this.dimension = dimension;
    }

    /**
     * Constructor for manual creation.
     *
     * @param componentType the component type
     * @param dimension the dimension
     */
    public GenericArrayTypeProperties(Type componentType, int dimension) {
        this.componentType = componentType;
        this.dimension = dimension;
    }

    @Override
    public Type getComponentType() {
        return componentType;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof GenericArrayTypeProperties) {
            GenericArrayTypeProperties that = (GenericArrayTypeProperties) obj;
            return this.dimension == that.dimension && Objects.equals(this.componentType, that.componentType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimension, componentType);
    }

    @Override
    public String toString() {
        return "GenericArrayTypeProperties[componentType='" + componentType + "', dimension=" + dimension + "]";
    }
}
