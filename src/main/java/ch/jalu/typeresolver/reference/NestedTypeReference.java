package ch.jalu.typeresolver.reference;

import ch.jalu.typeresolver.TypeInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;

/**
 * A type reference class that retrieves the type argument of this class's List type argument, allowing the creation of
 * wildcard types, which cannot be created directly with {@link TypeReference}. This class is used as follows:
 * <pre>{@code
 *   TypeInfo typeInfo = new NestedTypeReference<List<?>>() { }; // TypeInfo of '?' wildcard
 * }</pre>
 * <p>
 * This class is specifically for creating wildcard types from a type declaration, and the resulting wildcard type
 * can be retrieved with {@link #wildcardType()}:<pre>{@code
 *  WildcardType wildcard = new NestedTypeReference<List<? extends Serializable>>() { }.wildcardType();
 * }</pre>
 *
 * Use {@link TypeReference} if it is not needed to nest the type parameter in a dummy {@code List} type.
 * Note that wildcard types can also be created programmatically with
 * {@link ch.jalu.typeresolver.typeimpl.WildcardTypeImpl}.
 *
 * @param <L> the list type whose type argument will be used to create the wildcard type. Bounded to List as a reminder
 *            that the actual type argument of the List is considered, and not the List itself.
 */
public abstract class NestedTypeReference<L extends List> extends TypeInfo {

    /**
     * Constructor. See Javadoc on class for proper usage.
     */
    public NestedTypeReference() {
    }

    /**
     * Convenience method to retrieve the inner class cast to a WildcardType.
     * An exception is thrown if the type is not a wildcard.
     *
     * @return the wrapped type cast as a wildcard type
     * @throws IllegalStateException if the wrapped type is not a wildcard type
     */
    public WildcardType wildcardType() {
        Type type = getType();
        if (type instanceof WildcardType) {
            return (WildcardType) type;
        }
        throw new IllegalStateException("The type is not a wildcard but is " + (type == null ? null : type.getClass()));
    }

    @Override
    protected Type inferTypeForNoArgsConstructor() {
        Type genericSuperClass = getClass().getGenericSuperclass();
        if (!(genericSuperClass instanceof ParameterizedType)) {
            throw new IllegalStateException("The parent class is not a parameterized type");
        }

        Type innerType = ((ParameterizedType) genericSuperClass).getActualTypeArguments()[0];
        if (!(innerType instanceof ParameterizedType)) {
            throw new IllegalStateException("The inner type of " + getClass().getSimpleName()
                + " should be a parameterized type (please refer to the Javadoc on the class)");
        }

        return ((ParameterizedType) innerType).getActualTypeArguments()[0];
    }
}
