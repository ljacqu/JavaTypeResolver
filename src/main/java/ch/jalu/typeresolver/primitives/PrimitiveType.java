package ch.jalu.typeresolver.primitives;

import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines primitive-reference class pairs and holds other utilities relating to primitive types.
 */
public enum PrimitiveType {

    /** Boolean type pair. */
    BOOLEAN(boolean.class, Boolean.class, false),

    /** Byte type pair. */
    BYTE(byte.class, Byte.class, (byte) 0),

    /** Character type pair. */
    CHARACTER(char.class, Character.class, (char) 0),

    /** Short type pair. */
    SHORT(short.class, Short.class, (short) 0),

    /** Integer type pair. */
    INTEGER(int.class, Integer.class, 0),

    /** Long type pair. */
    LONG(long.class, Long.class, 0L),

    /** Float type pair. */
    FLOAT(float.class, Float.class, 0f),

    /** Double type pair. */
    DOUBLE(double.class, Double.class, 0d);

    private static final Map<Class<?>, PrimitiveType> PRIMITIVES_BY_CLASS = initClassToPrimitiveTypes();

    private final Class<?> primitiveType;
    private final Class<?> referenceType;
    private final Object defaultValue;

    /**
     * Constructor.
     *
     * @param primitiveType the primitive class of this pair
     * @param referenceType the reference class of this pair
     * @param defaultValue the default value corresponding to the primitive class
     */
    PrimitiveType(Class<?> primitiveType, Class<?> referenceType, Object defaultValue) {
        this.primitiveType = primitiveType;
        this.referenceType = referenceType;
        this.defaultValue = defaultValue;
    }

    public Class<?> getPrimitiveType() {
        return primitiveType;
    }

    public Class<?> getReferenceType() {
        return referenceType;
    }

    /**
     * @return default value of the primitive type (e.g. for an uninitialized field); never null
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns whether the given class matches this primitive-reference class pair.
     *
     * @param clazz the class to process
     * @return true if this entry is associated with the given class, false otherwise
     */
    public boolean matches(@Nullable Class<?> clazz) {
        return primitiveType.equals(clazz) || referenceType.equals(clazz);
    }

    /**
     * Returns the {@link PrimitiveType} entry that corresponds to the given class, or null if not available.
     *
     * @param clazz the class to search for (primitive or reference type of a primitive-reference class pair)
     * @return the corresponding entry, or null if not applicable
     */
    @Nullable
    public static PrimitiveType from(Class<?> clazz) {
        return PRIMITIVES_BY_CLASS.get(clazz);
    }

    /**
     * Returns the given class's primitive class equivalent, if applicable. Returns the argument
     * if not applicable.
     * <p>
     * Note that {@link Void} is not converted to {@link Void#TYPE void.class}.
     *
     * @param clazz the class to process
     * @param <T> the class's type
     * @return the class's primitive type equivalent, or the provided argument if not applicable
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> toPrimitiveType(Class<T> clazz) {
        PrimitiveType primitiveTypePair = PRIMITIVES_BY_CLASS.get(clazz);
        return primitiveTypePair == null ? clazz : (Class<T>) primitiveTypePair.getPrimitiveType();
    }

    /**
     * Returns the given class's reference class equivalent, if applicable. Returns the argument
     * if not applicable.
     * <p>
     * Note that {@link Void#TYPE void.class} is not converted to {@link Void}.
     *
     * @param clazz the class to process
     * @param <T> the class's type
     * @return the class's reference type equivalent, or the provided argument if not applicable
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> toReferenceType(Class<T> clazz) {
        PrimitiveType primitiveTypePair = PRIMITIVES_BY_CLASS.get(clazz);
        return primitiveTypePair == null ? clazz : (Class<T>) primitiveTypePair.getReferenceType();
    }

    /**
     * Returns whether the class is a primitive type. Unlike {@link Class#isPrimitive()}, false is returned
     * for {@link Void#TYPE void.class}.
     *
     * @param clazz the class to process
     * @return true if the class is primitive, false otherwise
     */
    public static boolean isRealPrimitive(@Nullable Class<?> clazz) {
        return clazz != null && clazz.isPrimitive() && !void.class.equals(clazz);
    }

    // -----------
    // Initialization methods
    // -----------

    private static Map<Class<?>, PrimitiveType> initClassToPrimitiveTypes() {
        Map<Class<?>, PrimitiveType> classToPrimRefType = new HashMap<>();
        for (PrimitiveType type : values()) {
            classToPrimRefType.put(type.getPrimitiveType(), type);
            classToPrimRefType.put(type.getReferenceType(), type);
        }
        return Collections.unmodifiableMap(classToPrimRefType);
    }
}
