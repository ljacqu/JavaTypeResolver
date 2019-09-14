package ch.jalu.typeresolver;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;

public final class TypeToClassUtil {

    private TypeToClassUtil() {
    }

    /**
     * Converts the given Type to a Class that is safe for writing (e.g. for adding elements to a collection).
     * <b>See {@link TypeInfo#toClass()} for more details.</b>
     *
     * @param type the type to process
     * @return safe-to-write class of the type, or null if not known
     */
    @Nullable
    public static Class<?> getSafeToWriteClass(@Nullable Type type) {
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
     * Converts the given Type to the most specific class possible.
     * <b>See {@link TypeInfo#getSafeToReadClass()} for more details.</b>
     *
     * @param type the type to process
     * @return class which is safe for reading (e.g. when retrieving an element from a collection)
     */
    public static Class<?> getSafeToReadClass(@Nullable Type type) {
        Class<?> clazz = getSafeToReadClassOrNull(type);
        return clazz == null ? Object.class : clazz;
    }

    @Nullable
    private static Class<?> getSafeToReadClassOrNull(@Nullable Type type) {
        Class<?> safeToWriteClass = getSafeToWriteClass(type);
        if (safeToWriteClass != null) {
            return safeToWriteClass;
        }

        if (type instanceof WildcardType) {
            return getFirstResolvableSafeToReadClass(((WildcardType) type).getUpperBounds());
        } else if (type instanceof TypeVariable<?>) {
            return getFirstResolvableSafeToReadClass(((TypeVariable<?>) type).getBounds());
        }
        return null;
    }

    @Nullable
    private static Class<?> getFirstResolvableSafeToReadClass(Type[] types) {
        return Arrays.stream(types)
            .map(TypeToClassUtil::getSafeToReadClassOrNull)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }
}
