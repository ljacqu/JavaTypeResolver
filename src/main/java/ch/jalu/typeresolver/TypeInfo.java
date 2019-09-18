package ch.jalu.typeresolver;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Wraps a {@link Type} to offer easy retrieval of generic type information.
 */
public class TypeInfo {

    private final Type type;

    private TypeVariableResolver resolver;

    /**
     * Constructor.
     *
     * @param type the type to wrap (e.g. coming from {@link Field#getGenericType()})
     */
    public TypeInfo(Type type) {
        this.type = type;
    }

    protected TypeInfo() {
        this.type = inferTypeForNoArgsConstructor();
    }

    /**
     * @return the type wrapped by this instance
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the type argument at the given index for the wrapped type, provided that the type is a parameterized type
     * and that the index is valid. Returns null if not applicable.
     * <p>
     * Examples for index = 0:<ul>
     * <li>{@code type = String -> result = null}</li>
     * <li>{@code type = List<String> -> result = String.class}</li>
     * <li>{@code type = Map<List<Integer>, String> -> result = List<Integer>}</li>
     * <li>{@code type = List -> result = null}</li>
     * </ul>
     *
     * @param index the index of the type parameter to get (0-based)
     * @return the type argument for the given index, null if not applicable
     */
    @Nullable
    public TypeInfo getTypeArgumentInfo(int index) {
        if (type instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (index < typeArguments.length) {
                return new TypeInfo(typeArguments[index]);
            }
        }
        return null;
    }

    /**
     * Returns the type argument for the given index as Class object, provided that the type is a parameterized type
     * and that the index is valid. Returns null if not applicable, or if the generic
     * type info cannot be converted to a {@link #toClass() safe-to-write} Class.
     * <p>
     * Examples for index = 0:<ul>
     * <li>{@code type = String -> result = null}</li>
     * <li>{@code type = List<String> -> result = String.class}</li>
     * <li>{@code type = Map<List<Integer>, String> -> result = List.class}</li>
     * <li>{@code type = List<T> -> result = null}</li>
     * <li>{@code type = List -> result = null}</li>
     * </ul>
     *
     * @param index the index of the type parameter to get (0-based)
     * @return the type argument as safe-to-write class, null if not applicable
     */
    @Nullable
    public Class<?> getTypeArgumentAsClass(int index) {
        TypeInfo genericTypeInfo = getTypeArgumentInfo(index);
        return genericTypeInfo == null ? null : genericTypeInfo.toClass();
    }

    /**
     * Returns a {@link Class} object of the wrapped type which is safe for writing to. In other words, if
     * this instance wraps the Type of a field, an object of the same Class as returned by this method can
     * be set to the field.
     * <p>
     * Examples: <ul>
     * <li>{@code type = String -> result = String.class}</li>
     * <li>{@code type = List<String> -> result = List.class}</li>
     * <li>{@code type = ? super Integer -> result = Integer.class}</li>
     * <li>{@code type = ? extends Comparable -> result = null}</li>
     * </ul>
     *
     * @return the type as a Class which is safe to use for writing
     *         (e.g. setting a value to a field or adding to a collection);
     *         null if not applicable
     */
    @Nullable
    public Class<?> toClass() {
        return TypeToClassUtil.getSafeToWriteClass(type);
    }

    /**
     * Returns a {@link Class} object of the wrapped type which is safe for reading. For example, if this instance
     * wraps the Type of a field, then the value on the field is guaranteed to be of the Class type returned by this
     * method (unless the value is null). The returned Class is as specific as possible.
     * <p>
     * Examples: <ul>
     * <li>{@code type = String -> result = String.class}</li>
     * <li>{@code type = List<String> -> result = List.class}</li>
     * <li>{@code type = ? super Integer -> result = Object.class}</li>
     * <li>{@code type = ? extends Comparable -> result = Comparable.class}</li>
     * </ul>
     *
     * @return the type as Class which is safe for reading (e.g. getting field value or reading from a collection)
     */
    public Class<?> getSafeToReadClass() {
        return TypeToClassUtil.getSafeToReadClass(type);
    }

    /**
     * Resolves the given type if possible, based on information from the wrapped Type of this instance. For example,
     * if this type info's type is {@code Optional<String>} and the input type is the Optional's {@code T} type,
     * {@code String} will be returned.
     * <p>
     * Supported actions:<ul>
     *  <li>Resolution of type variables, e.g. {@code T} to {@code String}</li>
     *  <li>Resolution of type arguments, e.g. {@code Map<K, V>} to {@code Map<String, Long>}.</li>
     *  <li>Resolution of generic array type, e.g. {@code T[]} to {@code List<String>[]}</li>
     *  <li>Resolution within wildcard bounds, e.g. {@code ? super T} to {@code ? super Serializable}
     * </ul>
     *
     * @param type the type to resolve
     * @return the resolved type, or the original type if no resolution was possible, never null
     */
    public TypeInfo resolve(Type type) {
        // Avoid creating/calling resolver if type is a class -> nothing to resolve
        Type resolvedType = (type instanceof Class<?>) ? type : getOrInitResolver().resolve(type);
        return new TypeInfo(resolvedType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof TypeInfo) {
            return Objects.equals(this.type, ((TypeInfo) obj).type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    @Override
    public String toString() {
        return "TypeInfo[type=" + type + "]";
    }

    private TypeVariableResolver getOrInitResolver() {
        if (resolver == null) {
            resolver = new TypeVariableResolver(type);
        }
        return resolver;
    }

    /**
     * Called in the no-args constructor, allowing to infer the type through some other way.
     *
     * @return the type this instance will wrap
     */
    protected Type inferTypeForNoArgsConstructor() {
        throw new UnsupportedOperationException(); // meant to be overridden
    }
}
