package ch.jalu.typeresolver.array;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

/**
 * Describes a generic array type: specifies the array dimension and the component type.
 * <p>
 * Example: given generic array type {@code List<String>[][]},
 * <br>{@code getArrayPropertiesOfType(type); // description with dimension=2 and componentType=String.class}
 */
public class GenericArrayTypeProperties implements AbstractArrayProperties {

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
}
