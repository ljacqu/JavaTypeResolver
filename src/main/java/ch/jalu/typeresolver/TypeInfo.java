package ch.jalu.typeresolver;

import ch.jalu.typeresolver.array.ArrayTypeUtils;
import ch.jalu.typeresolver.typeimpl.ParameterizedTypeImpl;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Wraps a {@link Type} to offer easy retrieval of various type information.
 */
public class TypeInfo {

    private final Type type;
    private transient TypeVariableResolver resolverLazy;

    /**
     * Constructor. Alternatively, use {@link #of(Type)} or {@link #of(Field)}.
     *
     * @param type the type to wrap (e.g. coming from {@link Field#getGenericType()})
     */
    public TypeInfo(Type type) {
        this.type = type;
    }

    /**
     * No-args constructor for overriding classes which define the type to wrap by overriding
     * {@link #inferTypeForNoArgsConstructor()}.
     *
     * @see ch.jalu.typeresolver.reference.TypeReference
     */
    protected TypeInfo() {
        this.type = inferTypeForNoArgsConstructor();
    }

    /**
     * Creates a new instance wrapping the given type.
     *
     * @param type the type to wrap
     * @return type info for the given type
     */
    public static TypeInfo of(Type type) {
        return new TypeInfo(type);
    }

    /**
     * Creates a new instance wrapping the generic type of the given field.
     *
     * @param field the field whose generic type should be used
     * @return type info for the field's type
     */
    public static TypeInfo of(Field field) {
        return new TypeInfo(field.getGenericType());
    }

    /**
     * @return the type wrapped by this instance
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the type argument at the given index for the wrapped type, provided that the type is a parameterized type
     * and that the index is valid. Returns null if not applicable.
     * <p>
     * Examples for index = 0:<ul>
     * <li>{@code type = String -> result = null}</li>
     * <li>{@code type = List<String> -> result = String.class}</li>
     * <li>{@code type = Map<List<Integer>, String> -> result = List<Integer>}</li>
     * <li>{@code type = List -> result = null}</li>
     * </ul>
     *
     * @param index the index of the type parameter to get (0-based)
     * @return the type argument for the given index, null if not applicable
     */
    @Nullable
    public TypeInfo getTypeArgumentInfo(int index) {
        if (type instanceof ParameterizedType) {
            Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();
            if (index < typeArguments.length) {
                return new TypeInfo(typeArguments[index]);
            }
        }
        return null;
    }

    /**
     * Returns the type argument for the given index as Class object, provided that the type is a parameterized type
     * and that the index is valid. Returns null if not applicable, or if the generic
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
     * @param index the index of the type parameter to get (0-based)
     * @return the type argument as safe-to-write class, null if not applicable
     */
    @Nullable
    public Class<?> getTypeArgumentAsClass(int index) {
        TypeInfo genericTypeInfo = getTypeArgumentInfo(index);
        return genericTypeInfo == null ? null : genericTypeInfo.toClass();
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
        return TypeToClassUtils.getSafeToWriteClass(type);
    }

    /**
     * Returns a {@link Class} object of the wrapped type which is safe for reading. For example, if this instance
     * wraps the Type of a field, then the value on the field is guaranteed to be of the Class type returned by this
     * method (unless the value is null). The returned Class is as specific as possible.
     * <p>
     * Examples: <ul>
     * <li>{@code type = String -> result = String.class}</li>
     * <li>{@code type = List<String> -> result = List.class}</li>
     * <li>{@code type = ? super Integer -> result = Object.class}</li>
     * <li>{@code type = ? extends Comparable -> result = Comparable.class}</li>
     * </ul>
     *
     * @return the type as Class which is safe for reading (e.g. getting field value or reading from a collection)
     */
    public Class<?> getSafeToReadClass() {
        return TypeToClassUtils.getSafeToReadClass(type);
    }

    /**
     * Returns a type info of the enclosing type (i.e. the outer class if this type is an nested class).
     * Returns null if not applicable.
     *
     * @return the enclosing type, or null if this type is not a nested type
     */
    @Nullable
    public TypeInfo getEnclosingType() {
        Type enclosingType = null;
        if (type instanceof Class<?>) {
            enclosingType = ((Class<?>) type).getEnclosingClass();
        } else if (type instanceof ParameterizedType) {
            enclosingType = ((ParameterizedType) type).getOwnerType();
        }
        return enclosingType == null ? null : of(enclosingType);
    }

    /**
     * Returns the array component type of this type, if applicable. Returns null otherwise (if the type doesn't
     * represent an array).
     *
     * @return the component type of this array type, or null if this type is not an array
     */
    @Nullable
    public TypeInfo getComponentType() {
        Type componentType = null;
        if (type instanceof Class<?>) {
            componentType = ((Class<?>) type).getComponentType();
        } else if (type instanceof GenericArrayType) {
            componentType = ((GenericArrayType) type).getGenericComponentType();
        }
        return componentType == null ? null : of(componentType);
    }

    /**
     * Resolves the given type if possible, based on information from the wrapped Type of this instance. For example,
     * if this type info's type is {@code Optional<String>} and the input type is the Optional's {@code T} type,
     * {@code String} will be returned.
     * <p>
     * Supported actions:<ul>
     *  <li>Resolution of type variables, e.g. {@code T} to {@code String}</li>
     *  <li>Resolution of type arguments, e.g. {@code Map<K, V>} to {@code Map<String, Long>}.</li>
     *  <li>Resolution of generic array type, e.g. {@code T[]} to {@code List<String>[]}</li>
     *  <li>Resolution within wildcard bounds, e.g. {@code ? super T} to {@code ? super Serializable}
     * </ul>
     *
     * @param type the type to resolve
     * @return the resolved type, or the original type if no resolution was possible, never null
     */
    public TypeInfo resolve(Type type) {
        // Avoid creating/calling resolver if type is a class -> nothing to resolve
        Type resolvedType = (type instanceof Class<?>) ? type : getOrInitResolver().resolve(type);
        return new TypeInfo(resolvedType);
    }

    /**
     * Returns the given class' including its type arguments if it is a superclass of this wrapped type.
     * Returns null if the given class is not a superclass of this wrapped type.
     *
     * @param clazz the desired superclass to get the info for
     * @return the type info of the superclass of the given class, or null if this wrapped type is not a supertype
     */
    @Nullable
    public TypeInfo resolveSuperclass(Class<?> clazz) {
        final Class<?> thisClass = toClass();
        if (thisClass == null || !clazz.isAssignableFrom(thisClass)) {
            return null;
        }

        if (clazz.isArray()) {
            TypeInfo resolvedComponent = getComponentType().resolveSuperclass(clazz.getComponentType());
            return of(ArrayTypeUtils.createArrayType(resolvedComponent.getType()));
        } else if (clazz.getTypeParameters().length > 0) {
            TypeVariableResolver resolver = getOrInitResolver();
            return new TypeInfo(new ParameterizedTypeImpl(clazz,
                getOwnerTypeForResolvedParameterizedType(clazz),
                resolver.resolveTypes(clazz.getTypeParameters())));
        }
        return new TypeInfo(clazz);
    }

    /**
     * Returns all types that this wrapped type is an instance of, i.e. returns all superclasses and interfaces
     * that the class can be assigned to.
     *
     * @return all types that this wrapped type can be assigned to
     */
    public Set<Type> getAllTypes() {
        return TypeVisitor.gatherAllTypes(type, getOrInitResolver());
    }

    /**
     * Returns all types that this wrapped type is an instance of, as TypeInfo instances.
     *
     * @return all types this wrapped type can be assigned to
     */
    public Set<TypeInfo> getAllTypeInfos() {
        return TypeVisitor.gatherAllTypes(type, getOrInitResolver(), new HashSet<>(), TypeInfo::new);
    }

    /**
     * Invokes the given consumer for each type that this wrapped type can be assigned to (i.e. all parents
     * and interfaces).
     *
     * @param typeVisitor callback run for each type that this wrapped type can be assigned to
     */
    public void visitAllTypes(Consumer<Type> typeVisitor) {
        TypeVisitor.visitAllTypes(type, getOrInitResolver(), typeVisitor);
    }

    @Nullable
    private Type getOwnerTypeForResolvedParameterizedType(Class<?> superclass) {
        Class<?> enclosingClass = superclass.getEnclosingClass();
        // Return enclosing class without type arguments if the nested class is static (in line with Java behavior)
        if (enclosingClass == null || Modifier.isStatic(superclass.getModifiers())) {
            return enclosingClass;
        }

        TypeInfo enclosingInfo = getEnclosingType();
        while (enclosingInfo != null) {
            TypeInfo resolvedEnclosingType = enclosingInfo.resolveSuperclass(enclosingClass);
            if (resolvedEnclosingType != null) {
                return resolvedEnclosingType.getType();
            }
            enclosingInfo = enclosingInfo.getEnclosingType();
        }
        throw new IllegalStateException("Could not resolve enclosing class '" + enclosingClass + "' from " + type);
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

    /**
     * Called in the no-args constructor, allowing to infer the type through some other way.
     *
     * @return the type this instance will wrap
     */
    protected Type inferTypeForNoArgsConstructor() {
        throw new UnsupportedOperationException(); // meant to be overridden
    }

    private TypeVariableResolver getOrInitResolver() {
        if (resolverLazy == null) {
            resolverLazy = new TypeVariableResolver(type);
        }
        return resolverLazy;
    }
}
