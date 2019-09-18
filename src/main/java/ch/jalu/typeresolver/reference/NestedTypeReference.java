package ch.jalu.typeresolver.reference;

import ch.jalu.typeresolver.TypeInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Type reference class which returns the first type argument of the inner type argument. This is used
 * to create wildcard types, which cannot be created directly:
 * <br>{@code TypeInfo typeInfo = new NestedTypeReference<List<?>>() { }; // TypeInfo of '?' wildcard}
 *
 * @param <O> the "outer" type whose first type argument will be used
 */
public abstract class NestedTypeReference<O> extends TypeInfo {

    /**
     * Constructor. See javadoc on class for proper usage.
     */
    public NestedTypeReference() {
    }

    /**
     * Convenience method to retrieve the inner class casted to a Wildcard.
     * An exception is thrown if the type is not a wildcard.
     *
     * @return the wrapped type casted as wildcard
     */
    public WildcardType wildcardType() {
        Type type = getType();
        if (type instanceof WildcardType) {
            return (WildcardType) type;
        }
        throw new IllegalStateException("Type is not a wildcard but is "
                                            + (type == null ? null : type.getClass()));
    }

    @Override
    protected Type inferTypeForNoArgsConstructor() {
        Type genericSuperClass = getClass().getGenericSuperclass();
        if (!(genericSuperClass instanceof ParameterizedType)) {
            throw new IllegalStateException("Parent class is not a parameterized type");
        }

        Type innerType = ((ParameterizedType) genericSuperClass).getActualTypeArguments()[0];
        if (!(innerType instanceof ParameterizedType)) {
            throw new IllegalStateException("Inner type of " + getClass().getSimpleName()
                + " should be a parameterized type (read Class javadoc)");
        }

        return ((ParameterizedType) innerType).getActualTypeArguments()[0];
    }
}
