package ch.jalu.typeresolver.array;

import ch.jalu.typeresolver.classutil.ClassUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the component <i>type</i> an array can have: this enum has all primitive types, and Object.
 */
public enum ArrayComponentType {

    BOOLEAN(boolean.class),
    BYTE(byte.class),
    CHARACTER(char.class),
    SHORT(short.class),
    INTEGER(int.class),
    LONG(long.class),
    FLOAT(float.class),
    DOUBLE(double.class),
    /** Means any extension of Object, such as the component of {@code String[]}, or even {@code int[][]}. */
    OBJECT(Object.class);

    private static final Map<Class<?>, ArrayComponentType> PRIMITIVE_ARRAY_TO_COMPONENT =
        createPrimitiveArrayTypesToComponentMap();
    private final Class<?> componentClass;

    /**
     * Constructor.
     *
     * @param componentClass the base component type (primitive class, or Object.class)
     */
    ArrayComponentType(Class<?> componentClass) {
        this.componentClass = componentClass;
    }

    /**
     * @return The class that is the component type of the array. This is a primitive class like {@code int.class},
     *         or {@code Object.class} if the array's component type is a reference type.
     */
    public Class<?> getComponentClass() {
        return componentClass;
    }

    /**
     * Returns the appropriate {@link ArrayComponentType} entry for the given array. Throws an exception if
     * the argument is not an array.
     *
     * @param array the array to inspect
     * @return the array component type representing the enum's component type
     */
    public static ArrayComponentType getArrayComponentType(@Nullable Object array) {
        if (array != null) {
            Class<?> clazz = array.getClass();
            ArrayComponentType componentType = PRIMITIVE_ARRAY_TO_COMPONENT.get(clazz);
            if (componentType != null) {
                return componentType;
            } else if (Object[].class.isAssignableFrom(clazz)) {
                return ArrayComponentType.OBJECT;
            }
        }

        throw new IllegalArgumentException("Expected an array but got an object of type: "
            + ClassUtils.getClassName(array));
    }

    private static Map<Class<?>, ArrayComponentType> createPrimitiveArrayTypesToComponentMap() {
        Map<Class<?>, ArrayComponentType> map = new HashMap<>();
        map.put(boolean[].class, BOOLEAN);
        map.put(byte[].class, BYTE);
        map.put(char[].class, CHARACTER);
        map.put(short[].class, SHORT);
        map.put(int[].class, INTEGER);
        map.put(long[].class, LONG);
        map.put(float[].class, FLOAT);
        map.put(double[].class, DOUBLE);
        return Collections.unmodifiableMap(map);
    }
}
