package ch.jalu.typeresolver.classutil;

import ch.jalu.typeresolver.EnumUtils;
import ch.jalu.typeresolver.array.ArrayClassProperties;
import ch.jalu.typeresolver.primitives.PrimitiveType;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Optional;

/**
 * Various utilities to load and inspect {@link Class} objects.
 */
public final class ClassUtils {

    private ClassUtils() {
    }

    /**
     * Returns whether the class exists and can be loaded by the current class loader.
     *
     * @param name the name of the class to try to load
     * @return true if the class exists, false otherwise
     */
    public static boolean classExists(String name) {
        return tryLoadClass(name).isPresent();
    }

    /**
     * Wraps {@link Class#forName(String)} and swallows the {@link ClassNotFoundException} it potentially throws:
     * allows to load a class by name if possible, otherwise returns an empty optional.
     * <p>
     * Note that errors thrown by {@link Class#forName(String)} (e.g. {@link LinkageError}) are not caught as they
     * typically indicate more severe issues.
     *
     * @param name the class name to try to load
     * @return optional with the class if possible, otherwise an empty optional
     */
    public static Optional<Class<?>> tryLoadClass(String name) {
        try {
            return Optional.of(Class.forName(name));
        } catch (ClassNotFoundException ignored) {
            return Optional.empty();
        }
    }

    /**
     * Loads a class or throws an {@link IllegalArgumentException}. This method wraps {@link Class#forName(String)}
     * to throw a runtime exception for convenience.
     *
     * @param name the class name to load
     * @return the loaded class, never null
     */
    public static Class<?> loadClassOrThrow(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Class '" + name + "' could not be loaded", e);
        }
    }

    /**
     * Returns whether the given object is an instance of the given target type. This method, unlike
     * {@link Class#isInstance}, boxes primitive classes, such that {@code isInstance(5, int.class)}
     * returns true. See also {@link ClassUtils#tryCast} if you want to cast the object safely.
     *
     * @param object the object to process
     * @param targetType the target type to check for
     * @return true if the object is not null and an instance of the given target type, false otherwise
     */
    public static boolean isInstance(@Nullable Object object, Class<?> targetType) {
        return PrimitiveType.toReferenceType(targetType).isInstance(object);
    }

    /**
     * Returns an optional with the given object cast to the target type, or an empty optional if the given object
     * cannot be cast to the specified type. See {@link #tryCast(Object, Class, boolean)} for details.
     *
     * @param object the object to cast
     * @param targetType the class to cast to
     * @param <T> target type
     * @return optional with the cast object, or empty optional if not possible
     */
    public static <T> Optional<T> tryCast(@Nullable Object object, Class<T> targetType) {
        return tryCast(object, targetType, true);
    }

    /**
     * Returns an optional with the given object cast to the target type, or an empty optional if the given object
     * cannot be cast to the specified type. If {@code autobox} is true, primitive types are cast to their wrapper
     * types.
     * <p>
     * Examples:<pre>{@code
     *   tryCast("a", String.class, false) = Optional.of("a")
     *   tryCast("a", String.class, true) = Optional.of("a")
     *   tryCast(3, int.class, false) = Optional.empty()
     *   tryCast(3, int.class, true) = Optional.of(3)
     *   tryCast(3, Integer.class, false) = Optional.of(3)
     * }</pre>
     * <p>
     * This method returns an empty Optional even if a widening primitive conversion (see JLS 5.1.2) exists:
     * {@code tryCast(3, long.class, autobox)} returns an empty optional (regardless of the value of {@code autobox}).
     *
     * @param object the object to cast
     * @param targetType the class to cast to
     * @param autobox whether the target type should be autoboxed, e.g. to turn int.class to Integer.class
     * @param <T> target type
     * @return optional with the cast object, or empty optional if not possible
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<T> tryCast(@Nullable Object object, Class<T> targetType, boolean autobox) {
        Class<T> target = autobox ? PrimitiveType.toReferenceType(targetType) : targetType;
        if (target.isInstance(object)) {
            return Optional.of((T) object);
        }
        return Optional.empty();
    }

    /**
     * Returns an Optional with the same class cast as subtype of the given {@code parent} if possible,
     * otherwise returns an empty optional. Auto-boxes classes. See {@link #asSubclassIfPossible(Class, Class, boolean)}
     * for more details.
     *
     * @param classToInspect the class to process
     * @param parent the parent type to check if the class is an extension of
     * @param <T> the parent type
     * @return optional with the class as extension of the parent, or empty optional if not possible
     * @see Class#asSubclass
     */
    public static <T> Optional<Class<? extends T>> asSubclassIfPossible(Class<?> classToInspect,
                                                                        Class<T> parent) {
        return asSubclassIfPossible(classToInspect, parent, true);
    }

    /**
     * Returns an Optional with the same class cast as subtype of the given {@code parent} if possible,
     * otherwise returns an empty optional. If {@code autobox} is true, primitive classes are cast to their wrapper
     * types.
     * <p>
     * Examples:<pre>{@code
     *  asSubclassIfPossible(int.class, Integer.class, false) = Optional.empty()
     *  asSubclassIfPossible(int.class, Integer.class, true) = Optional.of(int.class)
     *  asSubclassIfPossible(int.class, Number.class, false) = Optional.empty()
     *  asSubclassIfPossible(int.class, Number.class, true) = Optional.of(int.class)
     *  asSubclassIfPossible(int.class, long.class, false) = Optional.empty()
     *  asSubclassIfPossible(int.class, long.class, true) = Optional.empty()
     * }</pre>
     *
     * @param classToInspect the class to process
     * @param parent the parent type to check if the class is an extension of
     * @param autobox whether the target type should be autoboxed, e.g. to turn int.class to Integer.class
     * @param <T> the parent type
     * @return optional with the class as extension of the parent, or empty optional if not possible
     * @see Class#asSubclass
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<Class<? extends T>> asSubclassIfPossible(Class<?> classToInspect,
                                                                        Class<T> parent,
                                                                        boolean autobox) {
        boolean isSubclass;
        if (autobox) {
            isSubclass = PrimitiveType.toReferenceType(parent).isAssignableFrom(
                PrimitiveType.toReferenceType(classToInspect));
        } else {
            isSubclass = parent.isAssignableFrom(classToInspect);
        }

        return isSubclass ? Optional.of((Class<? extends T>) classToInspect) : Optional.empty();
    }

    /**
     * Returns the {@link Class#getName() class name} of the object null-safely, returning "null" if the object is null.
     * Use {@link #getSemanticName(Object)} to generate a null-safe, more user-appropriate name of the object's type.
     *
     * @param object the object whose type should be returned as string
     * @return the object's class name, or "null" if the object was null
     */
    public static String getClassName(@Nullable Object object) {
        return object == null ? "null" : object.getClass().getName();
    }

    /**
     * Returns the "semantic type" of the given object. The semantic type is the type that identifies the object with
     * the most semantic meaning; concretely, it ensures that for enum entries, the enum class is always returned,
     * and that the annotation <b>type</b> is returned for objects representing annotations.
     * <p>
     * Examples:<pre>{@code
     *   getSemanticType(null) = null
     *
     *   NumericShaper.Range.ETHIOPIC.getClass()       = NumericShaper$Range$1.class // or similar
     *   getSemanticType(NumericShaper.Range.ETHIOPIC) = NumericShaper$Range.class
     *
     *   FunctionalInterface fiAnnotation = Runnable.class.getAnnotation(FunctionalInterface.class);
     *   fiAnnotation.getClass()          = $Proxy12.class // or similar
     *   getSemanticType(fiAnnotation)    = FunctionalInterface.class
     * }</pre>
     *
     * @param object the object to inspect, or null
     * @return the semantic type (only null if the object was null)
     */
    public static @Nullable Class<?> getSemanticType(@Nullable Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Enum<?>) {
            return ((Enum<?>) object).getDeclaringClass();
        } else if (object instanceof Annotation && Proxy.isProxyClass(object.getClass())) {
            return ((Annotation) object).annotationType();
        }
        return object.getClass();
    }

    /**
     * Returns a human-friendly string representation of the given object, based on the
     * {@link #getSemanticType semantic type} of the given object. Returns the string "null" if the object is null.
     * The returned class names do not include the package. Annotations are prefixed by "{@code @}". Strings of array
     * classes are constructed by adding {@code []} at the end of the component type (e.g. "String[]").
     * <p>
     * Inner classes and anonymous classes contain the name as given by {@link Class#getName} with the package removed.
     * For example, a {@link java.util.Map.Entry} object is returned as "Map$Entry".
     *
     * @param object the object to provide a user-friendly string for of its type
     * @return most relevant type of the object as class name, or "null" as string (never null itself)
     */
    public static String getSemanticName(@Nullable Object object) {
        Class<?> semanticType = getSemanticType(object);
        return createNameOfSemanticType(semanticType);
    }

    /**
     * Returns a human-friendly string representation of the given class, or of the most meaningful associated type.
     * Concretely, this means any synthetic classes for enum entries are replaced by the enum type itself, as returned
     * by {@link EnumUtils#getAssociatedEnumType}.
     * <p>
     * Prefer using {@link #getSemanticName(Object)} whenever possible, as more semantic types can be inferred based
     * on an object.
     *
     * @param clazz the class to provide a user-friendly string for of its type
     * @return semantic type as class name, or "null" as string (never null itself)
     */
    public static String getSemanticName(@Nullable Class<?> clazz) {
        return EnumUtils.getAssociatedEnumType(clazz)
            .map(ClassUtils::createNameOfSemanticType)
            .orElseGet(() -> createNameOfSemanticType(clazz));
    }

    /**
     * Returns whether the given class is a "regular" Java class. The intention of this method is to match only classes
     * that are declared with the {@code class} or {@code record} keyword. This is done by exclusion: the class is
     * considered as a "regular class" if it is not: an interface, an enum, an array, an annotation, a proxy class,
     * a primitive type or {@code void.class}.
     * <p>
     * To handle more types, use {@link #getType(Class)}.
     *
     * @param clazz the class to inspect, or null
     * @return true if the class is a "regular Java class"
     */
    public static boolean isRegularJavaClass(@Nullable Class<?> clazz) {
        return getType(clazz) == ClassType.REGULAR_CLASS;
    }

    /**
     * Returns the type of the given class (see {@link ClassType}). A class is considered as a
     * {@link ClassType#REGULAR_CLASS regular class} if no other {@link ClassType} entry applies to it.
     *
     * @param clazz the class to inspect, or null
     * @return the type of the class, or null if the class was null
     */
    public static ClassType getType(Class<?> clazz) {
        if (clazz == null) {
            return null;
        } else if (Enum.class.isAssignableFrom(clazz)) {
            return clazz.isEnum() ? ClassType.ENUM : ClassType.ENUM_ENTRY;
        } else if (clazz.isPrimitive()) {
            return ClassType.PRIMITIVE;
        } else if (Proxy.isProxyClass(clazz)) {
            return ClassType.PROXY_CLASS;
        } else if (clazz.isAnnotation()) {
            return ClassType.ANNOTATION;
        } else if (clazz.isInterface()) {
            return ClassType.INTERFACE;
        } else if (clazz.isArray()) {
            return ClassType.ARRAY;
        }

        return ClassType.REGULAR_CLASS;
    }

    /**
     * Calls the method on the given {@link ClassTypeCallback} that corresponds to the given class's type and
     * returns the result.
     *
     * @param clazz the class to inspect and process
     * @param typeCallback the type callback to generate a result with
     * @param <R> the callback's result type
     * @return the result from the callback (may be null)
     */
    public static <R> @Nullable R processClassByType(@Nullable Class<?> clazz,
                                                     ClassTypeCallback<? extends R> typeCallback) {
        ClassType type = getType(clazz);
        if (type == null) {
            return null;
        }

        switch (type) {
            case ENUM:
                return typeCallback.forEnum((Class<? extends Enum<?>>) clazz);
            case ENUM_ENTRY:
                return typeCallback.forEnumEntry(
                    (Class<? extends Enum<?>>) clazz.getEnclosingClass(), (Class<? extends Enum<?>>) clazz);
            case ANNOTATION:
                return typeCallback.forAnnotation((Class<? extends Annotation>) clazz);
            case PRIMITIVE:
                return typeCallback.forPrimitiveType(clazz);
            case ARRAY:
                return typeCallback.forArrayType(clazz);
            case INTERFACE:
                return typeCallback.forInterface(clazz);
            case PROXY_CLASS:
                return typeCallback.forProxyClass(clazz);
            case REGULAR_CLASS:
                return typeCallback.forRegularClass(clazz);
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    private static String createNameOfSemanticType(@Nullable Class<?> semanticType) {
        if (semanticType == null) {
            return "null";
        } else if (semanticType.isAnnotation()) {
            return "@" + getNameWithoutPackage(semanticType);
        } else if (semanticType.isArray()) {
            ArrayClassProperties arrayProperties = new ArrayClassProperties(semanticType);
            StringBuilder stringBuilder = new StringBuilder(getNameWithoutPackage(arrayProperties.getComponentType()));
            appendArraySuffix(arrayProperties.getDimension(), stringBuilder);
            return stringBuilder.toString();
        }
        return getNameWithoutPackage(semanticType);
    }

    private static String getNameWithoutPackage(Class<?> clazz) {
        String name = clazz.getName();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private static void appendArraySuffix(int dimension, StringBuilder stringBuilder) {
        for (int i = 0; i < dimension; ++i) {
            stringBuilder.append("[]");
        }
    }
}
