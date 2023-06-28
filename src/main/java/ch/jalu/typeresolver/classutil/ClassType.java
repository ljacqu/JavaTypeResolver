package ch.jalu.typeresolver.classutil;

/**
 * Enum to categorize {@link Class} objects.
 */
public enum ClassType {

    /** The class represents an {@link Enum enum}, e.g. {@link java.util.concurrent.TimeUnit}. */
    ENUM,

    /**
     * The class is the synthetic class of an enum entry that anonymously extends the enum type. Although this class
     * returns {@code false} for the {@link Class#isEnum()} method, it is assignable to the {@link Enum} type.
     * <br>Example: {@code NumericShaper.Range.ETHIOPIC.getClass()}
     */
    ENUM_ENTRY,

    /**
     * The class is an annotation type, e.g. {@link Override}. Classes that manually extend
     * {@link java.lang.annotation.Annotation} do not fall into this category.
     * <p>
     * Note that objects of annotation types have a corresponding proxy class; their class is categorized as
     * {@link #PROXY_CLASS}. Example:<pre>{@code
     * @Test void demo() throws Exception {
     *   Test ann = getClass().getDeclaredMethod("demo").getAnnotation(Test.class);
     *   System.out.println(ann.getClass()); // Prints something like "$Proxy9"
     *   System.out.println(ClassUtil.getType(ann.getClass())); // Prints PROXY_CLASS
     * }}</pre>
     */
    ANNOTATION,

    /**
     * The class is a primitive type such as {@code double.class}, or {@code void.class}.
     */
    PRIMITIVE,

    /**
     * The class is an array, such as {@code String[].class} or {@code int[].class}.
     *
     * @see ch.jalu.typeresolver.array.ArrayClassProperties
     */
    ARRAY,

    /**
     * The class is an interface, such as {@link java.util.Collection}.
     */
    INTERFACE,

    /**
     * The class is a proxy class dynamically generated at runtime, such as those used in reflection or
     * dynamic proxy mechanisms.
     */
    PROXY_CLASS,

    /**
     * The class is a "regular" Java class, which typically means it was declared with the {@code class} or
     * {@code record} keyword. A class is considered a "regular class" by exclusion: it is a "regular class" if none of
     * the other types of this enum apply to it.
     * <br>Example: {@link String}
     * <p>
     * Note that nested classes and anonymous classes may also be considered "regular classes".
     */
    REGULAR_CLASS

}
