package ch.jalu.typeresolver.array;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Describes a generic array type: specifies the array dimension and the component type.
 * <p>
 * Example: given generic array type {@code List<String>[][]},
 * <br>{@code getArrayPropertiesOfType(type); // description with dimension=2 and componentType=String.class}
 */
public class GenericArrayTypeProperties implements ArrayTypeProperties {

    private final Type componentType;
    private final int dimension;

    /**
     * Constructor. Use {@link #getArrayPropertiesOfType(GenericArrayType)} for static imports.
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

    /**
     * Returns the array properties of the given generic array type.
     *
     * @param genericArrayType the type to process
     * @return the array properties of the given type
     */
    public static GenericArrayTypeProperties getArrayPropertiesOfType(GenericArrayType genericArrayType) {
        return new GenericArrayTypeProperties(genericArrayType);
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
        return "GenericArrayTypeProperties[dimension=" + dimension + ",componentType='" + componentType + "']";
    }
}
