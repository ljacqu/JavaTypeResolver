package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.CommonTypeUtil;
import ch.jalu.typeresolver.TypeInfo;

import javax.annotation.Nullable;
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
 * Builder of parameterized types.
 */
public class ParameterizedTypeBuilder {

    private final Class<?> rawType;
    @Nullable
    private final Type ownerType;
    private final TypeVariable<?>[] typeParameters;
    private final Type[] oldTypeArguments;
    private final Type[] newTypeArguments;

    /**
     * Creates a builder with the given parameterized type as initial values.
     *
     * @param parameterizedType the parameterized type to start the builder off with
     */
    public ParameterizedTypeBuilder(ParameterizedType parameterizedType) {
        this.rawType = CommonTypeUtil.getRawType(parameterizedType);
        this.ownerType = parameterizedType.getOwnerType();
        this.typeParameters = rawType.getTypeParameters();
        this.oldTypeArguments = parameterizedType.getActualTypeArguments();
        this.newTypeArguments = new Type[typeParameters.length];
    }

    private ParameterizedTypeBuilder(Class<?> rawType) {
        TypeVariable<?>[] typeParams = rawType.getTypeParameters();
        if (typeParams.length == 0) {
            throw new IllegalArgumentException("Class '" + rawType + "' has no type arguments");
        }

        this.rawType = rawType;
        this.ownerType = createOwnerType(rawType);
        this.typeParameters = typeParams;
        this.oldTypeArguments = new Type[typeParameters.length];
        this.newTypeArguments = new Type[typeParameters.length];
    }

    /**
     * Creates a builder with the given Class as its base type.
     *
     * @param clazz the raw type of the parameterized type that will be created
     * @return new builder with the given class as raw type
     */
    public static ParameterizedTypeBuilder newTypeFromClass(Class<?> clazz) {
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
     * @param baseType the collection type
     * @param typeArgument the type argument of the collection
     * @return parameterized type
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
     * @param mapImplType the map implementation type to use
     * @param keyType the key type argument
     * @param valueType the value type argument
     * @return parameterized type
     */
    public static ParameterizedTypeImpl newMapType(Class<? extends Map> mapImplType,
                                                   @Nullable Type keyType, @Nullable Type valueType) {
        return new ParameterizedTypeBuilder(mapImplType)
            .withTypeArg(0, keyType)
            .withTypeArg(1, valueType)
            .build();
    }

    /**
     * @see #withTypeArg(int, Type)
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
     * @see #withTypeArg(String, Type)
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
     * @param typeParameterName the name of the type parameter to change
     * @param type the type to set (or null to reset to the type variable)
     * @return this builder
     */
    public ParameterizedTypeBuilder withTypeArg(String typeParameterName, @Nullable Type type) {
        int index = findIndexOfMatchingTypeParam(p -> p.getName().equals(typeParameterName),
            () -> "No type parameter '" + typeParameterName + "' on " + rawType);
        updateNewTypeArgumentEntry(index, type);
        return this;
    }

    /**
     * @see #withTypeArg(TypeVariable, Type)
     */
    public ParameterizedTypeBuilder withTypeArg(TypeVariable<?> typeVariable, TypeInfo typeInfo) {
        return withTypeArg(typeVariable, typeInfo.getType());
    }

    /**
     * Sets the given type variable to the provided type. Null can be provided to reset the type parameter back to
     * the class's type variable. Throws an exception if the type variable could not be matched.
     * <p>
     *  Example:<pre>{@code
     *    TypeVariable<?> tv = Optional.class.getTypeParameters()[0];
     *    newTypeFromClass(Optional.class).withTypeArg(tv, Integer.class);
     * }</pre>
     *
     * @param typeVariable the type variable to match
     * @param type the type to set (or null to reset to the type variable)
     * @return this builder
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
     * Creates a parameterized type with the configured raw type and type arguments.
     *
     * @return new parameterized type
     */
    public ParameterizedTypeImpl build() {
        for (int i = 0; i < typeParameters.length; ++i) {
            if (newTypeArguments[i] == null) {
                if ((newTypeArguments[i] = oldTypeArguments[i]) == null) {
                    newTypeArguments[i] = typeParameters[i];
                }
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
    @Nullable
    public static Type createOwnerType(Class<?> rawType) {
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
