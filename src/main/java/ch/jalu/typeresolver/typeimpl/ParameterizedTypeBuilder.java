package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.CommonTypeUtils;
import ch.jalu.typeresolver.TypeInfo;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Builder to create parameterized types programmatically.
 * <p>
 * Example:<pre>{@code
 *   ParameterizedType pt = parameterizedTypeBuilder(Optional.class)
 *     .withTypeArg(0, String.class)
 *     .build();
 *  // pt = Optional<String>
 * }</pre>
 * You can also start with the existing values of a parameterized type using the constructor
 * {@link ParameterizedTypeBuilder#ParameterizedTypeBuilder(ParameterizedType)}.
 */
public class ParameterizedTypeBuilder {

    private final Class<?> rawType;
    @Nullable
    private final Type ownerType;
    private final TypeVariable<?>[] typeParameters;
    private final Type[] newTypeArguments;

    /**
     * Creates a builder with the given parameterized type as initial values.
     *
     * @param parameterizedType the parameterized type to start the builder off with
     */
    public ParameterizedTypeBuilder(ParameterizedType parameterizedType) {
        this.rawType = CommonTypeUtils.getRawType(parameterizedType);
        this.ownerType = parameterizedType.getOwnerType();
        this.typeParameters = rawType.getTypeParameters();
        this.newTypeArguments = parameterizedType.getActualTypeArguments().clone();
    }

    private ParameterizedTypeBuilder(Class<?> rawType) {
        TypeVariable<?>[] typeParams = rawType.getTypeParameters();
        if (typeParams.length == 0) {
            throw new IllegalArgumentException("Class '" + rawType + "' has no type arguments");
        }

        this.rawType = rawType;
        this.ownerType = createOwnerType(rawType);
        this.typeParameters = typeParams;
        this.newTypeArguments = new Type[typeParameters.length];
    }

    /**
     * Creates a builder with the given Class as its base type.
     *
     * @param clazz the raw type of the parameterized type that will be created
     * @return new builder with the given class as the raw type
     * @throws IllegalArgumentException if the class has no type parameters
     */
    public static ParameterizedTypeBuilder parameterizedTypeBuilder(Class<?> clazz) {
        return new ParameterizedTypeBuilder(clazz);
    }

    /**
     * Creates a new parameterized type representing the given Collection type and the provided type argument.
     * Throws an exception if the given base type does not have at least one type parameter.
     * <p>
     *   Example:
     *   <pre>{@code newCollectionType(ArrayList.class, Double.class) // ArrayList<Double>}</pre>
     * <p>
     * If you know the base type and argument at compile time, consider using
     * {@link ch.jalu.typeresolver.reference.TypeReference TypeReference} instead:<br>
     * {@code Type type = new TypeReference<ArrayList<String>>(){ }.getType()}.
     *
     * @param baseType the collection type to use as raw type
     * @param typeArgument the type argument of the collection
     * @return parameterized type
     * @throws IllegalArgumentException if the base type does not have at least one type parameter
     */
    public static ParameterizedTypeImpl newCollectionType(Class<? extends Collection> baseType,
                                                          @Nullable Type typeArgument) {
        return new ParameterizedTypeBuilder(baseType)
            .withTypeArg(0, typeArgument)
            .build();
    }

    /**
     * Creates a new parameterized type representing the given Map type and the provided key and value arguments.
     * Throws an exception if the given map type does not have at least two type parameters.
     * <p>
     *   Example:
     *   <pre>{@code newMapType(HashMap.class, String.class, Double.class) // Map<String, Double>}</pre>
     * <p>
     * If you know the base type and arguments at compile time, consider using
     * {@link ch.jalu.typeresolver.reference.TypeReference TypeReference} instead:<br>
     * {@code Type type = new TypeReference<HashMap<String, Double>>(){ }.getType()}.
     *
     * @param baseType the map implementation type to use as raw type
     * @param keyType the key type argument
     * @param valueType the value type argument
     * @return parameterized type
     * @throws IllegalArgumentException if the base type does not have at least two type parameters
     */
    public static ParameterizedTypeImpl newMapType(Class<? extends Map> baseType,
                                                   @Nullable Type keyType, @Nullable Type valueType) {
        return new ParameterizedTypeBuilder(baseType)
            .withTypeArg(0, keyType)
            .withTypeArg(1, valueType)
            .build();
    }

    /**
     * Sets the type argument to the given index. Equivalent to {@link #withTypeArg(int, Type)}.
     *
     * @param typeParameterIndex the index of the parameter to set (0-based)
     * @param typeInfo type info whose type should be set as argument
     * @return this builder
     */
    public ParameterizedTypeBuilder withTypeArg(int typeParameterIndex, TypeInfo typeInfo) {
        return withTypeArg(typeParameterIndex, typeInfo.getType());
    }

    /**
     * Sets the type parameter at the given index to the provided type. Null can be provided to reset the
     * type parameter at the given index back to the class's type variable. Throws an exception if the parameter
     * index is invalid.
     * <p>
     *   Example: <pre>{@code newTypeFromClass(Optional.class).withTypeArg(0, Double.class).build();}</pre>
     *
     * @param typeParameterIndex the index of the parameter to set (0-based)
     * @param type the type to set (or null to reset to the type variable)
     * @return this builder
     * @throws IllegalArgumentException if the type parameter index is out of bounds
     */
    public ParameterizedTypeBuilder withTypeArg(int typeParameterIndex, @Nullable Type type) {
        if (typeParameterIndex < 0 || typeParameterIndex >= typeParameters.length) {
            throw new IllegalArgumentException("Type parameter index " + typeParameterIndex
                + " is out of bounds for " + rawType);
        }

        updateNewTypeArgumentEntry(typeParameterIndex, type);
        return this;
    }

    /**
     * Sets the type argument for the parameter with the given name. Equivalent to {@link #withTypeArg(int, Type)}.
     *
     * @param typeParameterName the name of the type parameter to set
     * @param typeInfo type info whose type should be set as argument
     * @return this builder
     * @throws IllegalArgumentException if the raw type does not have a type parameter matching the name
     */
    public ParameterizedTypeBuilder withTypeArg(String typeParameterName, TypeInfo typeInfo) {
        return withTypeArg(typeParameterName, typeInfo.getType());
    }

    /**
     * Sets the type parameter with the given name to the provided type. Null can be provided to reset the
     * type parameter back to the class's type variable. Throws an exception if the parameter name could not be found.
     * <p>
     *  Example: <pre>{@code newTypeFromClass(Optional.class).withTypeArg("T", Double.class).build();}</pre>
     *
     * @param typeParameterName the name of the type parameter to set
     * @param type the type to set (or null to reset to the type variable)
     * @return this builder
     * @throws IllegalArgumentException if the raw type does not have a type parameter matching the name
     */
    public ParameterizedTypeBuilder withTypeArg(String typeParameterName, @Nullable Type type) {
        int index = findIndexOfMatchingTypeParam(p -> p.getName().equals(typeParameterName),
            () -> "No type parameter '" + typeParameterName + "' on " + rawType);
        updateNewTypeArgumentEntry(index, type);
        return this;
    }

    /**
     * Sets the given type variable to the provided type. Equivalent to {@link #withTypeArg(TypeVariable, Type)}.
     *
     * @param typeVariable the type variable to match
     * @param typeInfo type info whose type should be set as argument
     * @return this builder
     * @throws IllegalArgumentException if the type variable does not belong to the raw type
     */
    public ParameterizedTypeBuilder withTypeArg(TypeVariable<?> typeVariable, TypeInfo typeInfo) {
        return withTypeArg(typeVariable, typeInfo.getType());
    }

    /**
     * Sets the type parameter identified by the given type variable to the provided type. Null can be provided to
     * reset the type parameter back to the class's type variable. Throws an exception if the type variable could not
     * be matched.
     * <p>
     *  Example:<pre>{@code
     *    TypeVariable<?> tv = Optional.class.getTypeParameters()[0];
     *    newTypeFromClass(Optional.class).withTypeArg(tv, Integer.class);
     * }</pre>
     *
     * @param typeVariable the type variable to match
     * @param type the type to set (or null to reset to the type variable)
     * @return this builder
     * @throws IllegalArgumentException if the type variable does not belong to the raw type
     */
    public ParameterizedTypeBuilder withTypeArg(TypeVariable<?> typeVariable, @Nullable Type type) {
        Predicate<TypeVariable<?>> filter = curVar -> curVar.getName().equals(typeVariable.getName())
            && curVar.getGenericDeclaration().equals(typeVariable.getGenericDeclaration());
        int index = findIndexOfMatchingTypeParam(filter,
            () -> "No type parameter matched '" + typeVariable + "' on " + rawType);
        updateNewTypeArgumentEntry(index, type);
        return this;
    }

    /**
     * Resets all type parameters to the type variables of the raw type.
     *
     * @return this builder
     */
    public ParameterizedTypeBuilder withTypeVariables() {
        TypeVariable<? extends Class<?>>[] typeVariables = rawType.getTypeParameters();
        System.arraycopy(typeVariables, 0, this.newTypeArguments, 0, typeVariables.length);
        return this;
    }

    /**
     * Creates a parameterized type with the configured raw type and type arguments.
     * An exception is thrown if any type argument is missing.
     *
     * @return new parameterized type
     * @throws IllegalStateException if a type parameter has not been associated with a value
     */
    public ParameterizedTypeImpl build() {
        for (int i = 0; i < typeParameters.length; ++i) {
            if (newTypeArguments[i] == null) {
                String typeVariableName = typeParameters[i].getName();
                throw new IllegalStateException(
                    "Type parameter '" + typeVariableName + "' at index " + i + " has not been set");
            }
        }
        return new ParameterizedTypeImpl(rawType, ownerType, newTypeArguments);
    }

    /**
     * Creates the appropriate Type with generic information (if needed) to be used as
     * {@link ParameterizedType#getOwnerType() owner type} of a parameterized type impl with the given raw type.
     *
     * @param rawType the raw type whose owner type should be created with generic information
     * @return owner type to use for the raw type
     */
    public static @Nullable Type createOwnerType(Class<?> rawType) {
        Class<?> directDeclaringClass = rawType.getDeclaringClass();
        if (directDeclaringClass == null || Modifier.isStatic(rawType.getModifiers())) {
            return directDeclaringClass;
        }
        return createOwnerTypeHierarchyForDeclaredNonStaticClass(rawType);
    }

    /**
     * Creates the appropriate owner type with relevant generic info for use in a ParameterizedType
     * with the given raw type, which may not be static and which must have a non-null declaring class.
     * <p>
     * The returned type is either the class's declaring class as Class object if there are no type parameters in
     * the declaring classes that are available in the class's scope. Otherwise, a hierarchy of ParameterizedType
     * is returned up to the last class with type parameters available in the class's scope.
     *
     * @param rawType the non-static declared class to inspect
     * @return the appropriate owner type for a parameterized type with the given raw type
     */
    private static Type createOwnerTypeHierarchyForDeclaredNonStaticClass(Class<?> rawType) {
        List<Class<?>> declaringClasses = collectRelevantDeclaringClasses(rawType);

        Type lastOwnerType = null;
        for (int i = declaringClasses.size() - 1; i >= 0; --i) {
            Class<?> ownerType = declaringClasses.get(i);
            if (lastOwnerType == null) {
                TypeVariable<?>[] typeParams = ownerType.getTypeParameters();
                if (typeParams.length > 0) {
                    lastOwnerType = new ParameterizedTypeImpl(ownerType, ownerType.getDeclaringClass(), typeParams);
                }
            } else {
                lastOwnerType = new ParameterizedTypeImpl(ownerType, lastOwnerType, ownerType.getTypeParameters());
            }
        }
        return lastOwnerType == null
            ? rawType.getDeclaringClass()
            : lastOwnerType;
    }

    /**
     * Collects all declaring classes iteratively that are relevant for the raw type's owner type hierarchy.
     * All declaring classes are collected up to the first static class that is encountered. This represents
     * the set of classes that might have type parameters which are still accessible in the {@code rawType}.
     * <p>
     * The list is inspected afterwards and the last element in the returned list that has type parameters will be
     * the top-most class to be a ParameterizedType in the owner type hierarchy.
     *
     * @implNote
     *     Specifically defined to return {@link ArrayList} to guarantee that access by index is efficient.
     *
     * @param rawType the raw type whose declaring classes should be gathered
     * @return list of relevant declaring classes that need to be processed
     */
    private static ArrayList<Class<?>> collectRelevantDeclaringClasses(Class<?> rawType) {
        ArrayList<Class<?>> declaringClasses = new ArrayList<>();
        Class<?> currentClass = rawType.getDeclaringClass();
        while (currentClass != null) {
            declaringClasses.add(currentClass);
            // All non-static classes and the first static declaring class are relevant, the rest is not
            if (Modifier.isStatic(currentClass.getModifiers())) {
                break;
            }
            currentClass = currentClass.getDeclaringClass();
        }
        return declaringClasses;
    }

    private void updateNewTypeArgumentEntry(int index, @Nullable Type type) {
        if (type == null) {
            newTypeArguments[index] = typeParameters[index];
        } else {
            newTypeArguments[index] = type;
        }
    }

    private int findIndexOfMatchingTypeParam(Predicate<TypeVariable<?>> filter, Supplier<String> exceptionMessage) {
        int index = 0;
        for (TypeVariable<?> typeParam : typeParameters) {
            if (filter.test(typeParam)) {
                return index;
            }
            ++index;
        }
        throw new IllegalArgumentException(exceptionMessage.get());
    }
}
