package ch.jalu.typeresolver.typeimpl;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Implementation of {@link GenericArrayType}.
 */
public class GenericArrayTypeImpl implements GenericArrayType {

    private final Type genericComponentType;

    public GenericArrayTypeImpl(Type genericComponentType) {
        this.genericComponentType = genericComponentType;
    }

    public static Type create(Type genericComponentType, int dimension) {
        if (dimension < 0) {
            throw new IllegalArgumentException("Dimension may not be negative");
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
