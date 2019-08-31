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

    /**
     * Constructor.
     *
     * @param type the type to wrap (e.g. coming from {@link Field#getGenericType()})
     */
    public TypeInfo(@Nullable Type type) {
        this.type = type;
    }

    /**
     * @return the type wrapped by this instance
     */
    @Nullable
    public Type getType() {
        return type;
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
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            // Current implementations can only have Class<?> as raw type, so cast without checking
            return (Class<?>) pt.getRawType();
        }
        return null;
    }

    /**
     * "Unwraps" the type and returns the generic type information for the given index, provided the wrapped type
     * contains generic information. Returns null if not applicable.
     * <p>
     * Examples for index = 0:<ul>
     * <li>{@code type = String -> result = null}</li>
     * <li>{@code type = List<String> -> result = String.class}</li>
     * <li>{@code type = Map<List<Integer>, String> -> result = List<Integer>}</li>
     * <li>{@code type = List -> result = null}</li>
     * </ul>
     *
     * @param index the index of the generic type to get (0-based)
     * @return type information representing the generic type info for the given index, null if not applicable
     */
    @Nullable
    public TypeInfo getGenericTypeInfo(int index) {
        if (type instanceof ParameterizedType) {
            Type[] genericTypes = ((ParameterizedType) type).getActualTypeArguments();
            if (index < genericTypes.length) {
                return new TypeInfo(genericTypes[index]);
            }
        }
        return null;
    }

    /**
     * "Unwraps" the type and returns the generic type information for the given index as Class object,
     * provided the wrapped type contains generic information. Returns null if not applicable, or if the generic
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
     * @param index the index of the generic type to get (0-based)
     * @return type information representing the generic type info for the given index, null if not applicable
     */
    @Nullable
    public Class<?> getGenericTypeAsClass(int index) {
        TypeInfo genericTypeInfo = getGenericTypeInfo(index);
        return genericTypeInfo == null ? null : genericTypeInfo.toClass();
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
}
