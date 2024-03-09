package ch.jalu.typeresolver.typeimpl;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Implementation of {@link GenericArrayType} with equals and hashCode mirroring the implementation
 * used by the JDK internally.
 */
public class GenericArrayTypeImpl implements GenericArrayType {

    private final Type genericComponentType;

    public GenericArrayTypeImpl(Type genericComponentType) {
        this.genericComponentType = genericComponentType;
    }

    /**
     * Wraps the given component type into an array type of the given dimension. If the dimension is 0,
     * the generic component type is returned as is.
     * <p>
     * Examples:<pre>{@code
     *   Type stringListType = new TypeReference<List<String>>() { }.getType();
     *   Type result1 = GenericArrayTypeImpl.create(stringListType, 1); // Result: List<String>[]
     *   Type result2 = GenericArrayTypeImpl.create(stringListType, 3); // Result: List<String>[][][]
     *   Type result3 = GenericArrayTypeImpl.create(stringListType, 0); // Result: List<String>
     * }</pre>
     *
     * @param genericComponentType the component type of the generic array
     * @param dimension the number of times to wrap the component type into a generic array
     * @return a generic array type with the specified component type and dimension (component type for dimension = 0)
     * @throws IllegalArgumentException if the dimension is negative
     */
    public static Type create(Type genericComponentType, int dimension) {
        if (dimension < 0) {
            throw new IllegalArgumentException("The dimension may not be negative");
        }

        Type type = genericComponentType;
        for (int i = 0; i < dimension; ++i) {
            type = new GenericArrayTypeImpl(type);
        }
        return type;
    }

    @Override
    public Type getGenericComponentType() {
        return genericComponentType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof GenericArrayType) {
            return Objects.equals(this.genericComponentType, ((GenericArrayType) obj).getGenericComponentType());
        }
        return false;
    }

    @Override // Same behavior as implementation in sun.reflect
    public int hashCode() {
        return Objects.hashCode(genericComponentType);
    }

    @Override
    public String toString() {
        String type;
        if (genericComponentType instanceof Class<?>) {
            type = ((Class<?>) genericComponentType).getName();
        } else {
            type = genericComponentType.toString();
        }
        return type + "[]";
    }
}
