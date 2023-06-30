package ch.jalu.typeresolver;

import ch.jalu.typeresolver.array.ArrayTypeUtils;

import javax.annotation.Nullable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

final class TypeToClassUtils {

    private TypeToClassUtils() {
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
            return CommonTypeUtils.getRawType(pt);
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            return getFirstNonNull(TypeToClassUtils::getSafeToWriteClass, wt.getLowerBounds());
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            Class<?> componentAsClass = getSafeToWriteClass(gat.getGenericComponentType());
            if (componentAsClass != null) {
                return ArrayTypeUtils.createArrayClass(componentAsClass);
            }
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
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            return CommonTypeUtils.getRawType(pt);
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            if (CommonTypeUtils.hasExplicitUpperBound(wt)) {
                return getFirstNonNull(TypeToClassUtils::getSafeToReadClassOrNull, wt.getUpperBounds());
            }
        } else if (type instanceof TypeVariable<?>) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            return getFirstNonNull(TypeToClassUtils::getSafeToReadClassOrNull, tv.getBounds());
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            Class<?> componentAsClass = getSafeToReadClassOrNull(gat.getGenericComponentType());
            // componentAsClass usually isn't null because the component type of GenericArrayType is normally either a
            // ParameterizedType or a TypeVariable. If a type variable is unbounded, the JRE sets Object as the bound.
            // However, if the type was somehow resolved we might have an array of wildcard, for example.
            if (componentAsClass != null) {
                return ArrayTypeUtils.createArrayClass(componentAsClass);
            }
            return Object[].class;
        }
        return null;
    }

    @Nullable
    private static Class<?> getFirstNonNull(Function<Type, Class<?>> converter, Type[] inputs) {
        return Arrays.stream(inputs)
            .map(converter)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);
    }
}
