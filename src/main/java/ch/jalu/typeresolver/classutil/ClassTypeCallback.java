package ch.jalu.typeresolver.classutil;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * Extendable class with a method dedicated for each {@link ClassType}, with appropriately cast method parameters.
 * This class's methods are meant to be overridden. The methods can be overridden partially if only certain class types
 * are of interest.
 *
 * @param <R> the result type of the callback
 * @see ClassUtils#processClassByType
 */
public abstract class ClassTypeCallback<R> {

    /**
     * Called when the class to process represents an enum, such as {@link java.util.concurrent.TimeUnit}.
     *
     * @param enumClass the enum class
     * @return result or null
     */
    @Nullable
    public R forEnum(Class<? extends Enum<?>> enumClass) {
        return null;
    }

    /**
     * Called when the class to process is a synthetic class of an enum entry, which is created when an enum extends
     * the enum class anonymously (see {@link ClassType#ENUM_ENTRY}).
     * This method is called with the enum class (e.g. Range) and the concrete enum entry class that was given
     * to process.
     *
     * @param enumClass the enum class
     * @param enumEntryClass the synthetic class of the enum entry
     * @return result or null
     */
    @Nullable
    public R forEnumEntry(Class<? extends Enum<?>> enumClass, Class<? extends Enum<?>> enumEntryClass) {
        return null;
    }

    /**
     * Called when the class to process represents an annotation, such as {@link Override}.
     *
     * @param annotationClass the annotation class
     * @return result or null
     */
    @Nullable
    public R forAnnotation(Class<? extends Annotation> annotationClass) {
        return null;
    }

    /**
     * Called when the class to process represents a primitive type, such as {@code int.class}, or {@code void.class}.
     *
     * @param primitiveClass the primitive class (or void)
     * @return result or null
     */
    @Nullable
    public R forPrimitiveType(Class<?> primitiveClass) {
        return null;
    }

    /**
     * Called when the class to process is an array class, such as {@code int[].class} or {@code String[][].class}.
     * <p>
     * Note that the class cannot be cast to {@code Class<? extends Object[]>} without checking beforehand, due to
     * arrays of primitive types such as {@code int[].class}.
     *
     * @param arrayClass the array class
     * @return result or null
     */
    @Nullable
    public R forArrayType(Class<?> arrayClass) {
        return null;
    }

    /**
     * Called when the class to process is an interface, such as {@link java.util.Map}.
     *
     * @param interfaceType the interface
     * @return result or null
     */
    @Nullable
    public R forInterface(Class<?> interfaceType) {
        return null;
    }

    /**
     * Called when the class to process is a dynamic proxy, cf. {@link java.lang.reflect.Proxy}.
     *
     * @param proxyClass the proxy class
     * @return result or null
     */
    @Nullable
    public R forProxyClass(Class<?> proxyClass) {
        return null;
    }

    /**
     * Called when the class to process is a "regular class", as explained in {@link ClassUtils#isRegularJavaClass}.
     *
     * @param regularClass the Java class
     * @return result or null
     */
    @Nullable
    public R forRegularClass(Class<?> regularClass) {
        return null;
    }
}
