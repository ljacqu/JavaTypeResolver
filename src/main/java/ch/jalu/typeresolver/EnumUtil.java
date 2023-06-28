package ch.jalu.typeresolver;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Utilities for enum classes.
 */
public final class EnumUtil {

    private EnumUtil() {
    }

    /**
     * Returns the enum entry with the given name if the provided class is an enum and has an entry that matches
     * exactly the given name. Null is returned otherwise.
     *
     * @param clazz the class to check for enum entries, or null
     * @param name the name to match, or null
     * @param <T> the class type
     * @return the enum entry if the name was non-null, the class was an enum and had such an enum entry; null otherwise
     */
    @Nullable
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T valueOfOrNull(@Nullable Class<T> clazz, @Nullable String name) {
        if (clazz == null || !clazz.isEnum() || name == null) {
            return null;
        }
        try {
            return (T) Enum.valueOf((Class) clazz, name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * Specifies whether the given class is an enum or a synthetic class of an enum entry.
     * <p>
     * Examples:<pre>{@code
     *   Class<?> class1 = NumericShaper.Range.class;
     *   Class<?> class2 = NumericShaper.Range.ETHIOPIC.getClass(); // NumericShaper$Range$1.class
     *
     *   EnumUtil.isEnumOrEnumEntryType(class1)    = true
     *   EnumUtil.isEnumOrEnumEntryType(class2)    = true
     *   EnumUtil.isEnumOrEnumEntryType(null)      = false
     *   EnumUtil.isEnumOrEnumEntryType(int.class) = false
     * }</pre>
     *
     * @param clazz the class to inspect, or null
     * @return true if the class is an enum or the class of an enum entry
     */
    public static boolean isEnumOrEnumEntryType(@Nullable Class<?> clazz) {
        return asEnumType(clazz).isPresent();
    }

    /**
     * Returns an optional with the class if it is an enum, or with the actual enum class if the given class is
     * a synthetic class of an enum entry.
     * <p>
     * Examples:<pre>{@code
     *   Class<?> class1 = NumericShaper.Range.class;
     *   Class<?> class2 = NumericShaper.Range.ETHIOPIC.getClass(); // NumericShaper$Range$1.class
     *
     *   EnumUtil.asEnumType(class1)    = Optional.of(NumericShaper.Range.class)
     *   EnumUtil.asEnumType(class2)    = Optional.of(NumericShaper.Range.class)
     *   EnumUtil.asEnumType(null)      = Optional.empty()
     *   EnumUtil.asEnumType(int.class) = Optional.empty()
     * }</pre>
     * <p>
     * Note: to always get the actual enum class of an enum entry, use {@link Enum#getDeclaringClass}.
     *
     * @param clazz the class to inspect, or null
     * @return optional with the enum type if applicable, otherwise empty optional
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static Optional<Class<? extends Enum<?>>> asEnumType(@Nullable Class<?> clazz) {
        if (clazz == null || !Enum.class.isAssignableFrom(clazz)) {
            return Optional.empty();
        }

        Class enumClass = clazz.isEnum() ? clazz : clazz.getEnclosingClass();
        return Optional.of(enumClass);
    }

    /**
     * Returns a collector that collects the results of a stream into an {@link EnumSet}. Convenience method for
     * brevity.
     *
     * @param enumType the class of the enum the stream consists of
     * @param <E> the enum type
     * @return collector to EnumSet
     */
    public static <E extends Enum<E>> Collector<E, ?, EnumSet<E>> toEnumSet(Class<E> enumType) {
        return Collectors.toCollection(() -> EnumSet.noneOf(enumType));
    }
}
