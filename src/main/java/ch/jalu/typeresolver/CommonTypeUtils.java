package ch.jalu.typeresolver;

import ch.jalu.typeresolver.array.ArrayTypeUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;

/**
 * Contains simple utility methods for Type objects.
 */
public final class CommonTypeUtils {

    private CommonTypeUtils() {
    }

    /**
     * Convenience method which returns the raw type of the given parameterized type cast as {@link Class}.
     * <p>
     * Background: in current JDK versions, the raw type can only be a Class, so the raw type can be cast
     * without checking it beforehand.
     *
     * @param parameterizedType the parameterized type whose raw type should be returned
     * @return the raw type of the parameterized type
     */
    public static Class<?> getRawType(ParameterizedType parameterizedType) {
        return (Class<?>) parameterizedType.getRawType();
    }

    /**
     * Returns whether the given wildcard type has an explicitly defined upper bound.
     * <p>
     * Background: the JDK sets the upper bounds to an array with one element of Object.class if the wildcard has
     * lower bounds, or if the wildcard has no specified bounds (i.e. a simple "?").
     *
     * @param wildcardType the wildcard type to analyze
     * @return true if it has a specific upper bound
     */
    public static boolean hasExplicitUpperBound(WildcardType wildcardType) {
        Type[] upperBounds = wildcardType.getUpperBounds();
        if (upperBounds.length == 0) {
            return false;
        }
        return !Object.class.equals(upperBounds[0]) || upperBounds.length > 1;
    }

    /**
     * Returns the Class equivalent of the type for which there is an absolute class equivalent,
     * i.e. for all Class, ParameterizedType and for some GenericArrayType instances.
     *
     * @param type the type to return as class if possible
     * @return the type as class, or null if not applicable
     */
    public static @Nullable Class<?> getDefinitiveClass(Type type) {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getRawType((ParameterizedType) type);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            Class<?> componentAsClass = getDefinitiveClass(gat.getGenericComponentType());
            if (componentAsClass != null) {
                return ArrayTypeUtils.createArrayClass(componentAsClass);
            }
        }
        return null;
    }
}
