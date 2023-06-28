package ch.jalu.typeresolver.array;

import java.util.Objects;

/**
 * Describes a class' array type properties by specifying the array dimension of this class and its component type.
 * <p>
 * Examples:<pre>{@code
 * ArrayClassProperties arrayProps1 = new ArrayClassProperties(String[][].class);
 * // arrayProps1 = new ArrayClassProperties(String.class, 2)
 * ArrayClassProperties arrayProps1 = new ArrayClassProperties(String.class);
 * // arrayProps2 = new ArrayClassProperties(String.class, 0)}</pre>
 *
 * @see ArrayTypeUtil#getArrayProperty
 */
public class ArrayClassProperties implements ArrayTypeProperties {

    private final Class<?> componentType;
    private final int dimension;

    /**
     * Constructor.
     *
     * @param clazz the class to describe
     */
    public ArrayClassProperties(Class<?> clazz) {
        Class<?> component = clazz;
        int dimension = 0;
        while (component.isArray()) {
            component = component.getComponentType();
            ++dimension;
        }

        this.componentType = component;
        this.dimension = dimension;
    }

    /**
     * Constructor for manual creation.
     *
     * @param componentType the component type
     * @param dimension the dimension
     */
    public ArrayClassProperties(Class<?> componentType, int dimension) {
        this.componentType = componentType;
        this.dimension = dimension;
    }

    @Override
    public Class<?> getComponentType() {
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
        } else if (obj instanceof ArrayClassProperties) {
            ArrayClassProperties that = (ArrayClassProperties) obj;
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
        return "ArrayClassProperties[componentType='" + componentType + "', dimension=" + dimension + "]";
    }
}
