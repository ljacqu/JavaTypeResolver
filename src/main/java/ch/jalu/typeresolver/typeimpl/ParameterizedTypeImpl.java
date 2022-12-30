package ch.jalu.typeresolver.typeimpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Implementation of {@link ParameterizedType}, with same {@link Object#equals} and {@link Object#hashCode()}
 * as the JRE implementation.
 */
public class ParameterizedTypeImpl implements ParameterizedType {

    private final Class<?> rawType;
    private final Type ownerType;
    private final Type[] actualTypeArguments;

    public ParameterizedTypeImpl(Class<?> rawType, Type ownerType, Type... actualTypeArguments) {
        this.rawType = rawType;
        this.ownerType = ownerType;
        this.actualTypeArguments = actualTypeArguments;
    }

    /**
     * Creates a new {@link ParameterizedTypeImpl} instance which represents the given class as a
     * parameterized type with the original type arguments. Throws an exception if the given raw type
     * has no type parameters.
     *
     * @param rawType the type to create a parameterized type object for
     * @return parameterized type of the class and its type arguments
     */
    public static ParameterizedTypeImpl newTypeWithTypeParameters(Class<?> rawType) {
        TypeVariable<? extends Class<?>>[] typeParams = rawType.getTypeParameters();
        if (typeParams.length == 0) {
            throw new IllegalArgumentException("Class '" + rawType + "' has no type arguments");
        }
        return new ParameterizedTypeImpl(rawType, rawType.getDeclaringClass(), typeParams);
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    @Override
    public Class<?> getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ParameterizedType) {
            ParameterizedType that = (ParameterizedType) obj;
            return Objects.equals(this.ownerType, that.getOwnerType())
                && Objects.equals(this.rawType, that.getRawType())
                && Arrays.equals(this.actualTypeArguments, that.getActualTypeArguments());
        }
        return false;
    }

    @Override // Same behavior as implementation in sun.reflect
    public int hashCode() {
        return Arrays.hashCode(this.actualTypeArguments)
            ^ Objects.hashCode(this.ownerType)
            ^ Objects.hashCode(this.rawType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (ownerType != null) {
            sb.append(ownerType.getTypeName())
                .append("$")
                .append(rawType.getSimpleName());
        } else {
            sb.append(rawType.getName());
        }

        if (actualTypeArguments.length > 0) {
            sb.append("<");
            for (int i = 0; i < actualTypeArguments.length; ++i) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(actualTypeArguments[i].getTypeName());
            }
            sb.append(">");
        }
        return sb.toString();
    }
}
