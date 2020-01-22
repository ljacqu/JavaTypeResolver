package ch.jalu.typeresolver.array;

import java.util.Objects;

/**
 * Describes a class' array details by specifying the array dimension of this class and its component type.
 * <p>
 * Example: {@code getArrayPropertiesOfClass(String[][].class);
 *   // description with componentType=String.class and dimension=2}
 * <p>
 * {@code getArrayPropertiesOfClass(String.class); // description with componentType=String.class and dimension=0}
 */
public class ArrayClassProperties implements AbstractArrayProperties {

    private final Class<?> componentType;
    private final int dimension;

    /**
     * Constructor. Use {@link #getArrayPropertiesOfClass(Class)} for static imports.
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

    /**
     * Returns the array properties of the given class.
     *
     * @param clazz the class to process
     * @return the array properties of the given class
     */
    public static ArrayClassProperties getArrayPropertiesOfClass(Class<?> clazz) {
        return new ArrayClassProperties(clazz);
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
        return "ArrayClassProperties[dimension=" + dimension + ",componentType='" + componentType + "']";
    }
}
