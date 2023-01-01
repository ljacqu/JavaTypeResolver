package ch.jalu.typeresolver.typeimpl;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of {@link ParameterizedType}, with same {@link Object#equals} and {@link Object#hashCode()}
 * as the JRE implementation.
 */
public class ParameterizedTypeImpl implements ParameterizedType {

    private final Class<?> rawType;
    private final Type ownerType;
    private final Type[] actualTypeArguments;

    public ParameterizedTypeImpl(Class<?> rawType, Type ownerType, Type... actualTypeArguments) {
        this.rawType = rawType;
        this.ownerType = ownerType;
        this.actualTypeArguments = actualTypeArguments;
    }

    /**
     * Creates a new {@link ParameterizedTypeImpl} instance which represents the given class as a
     * parameterized type with the original type arguments. Throws an exception if the given raw type
     * has no type parameters.
     *
     * @param rawType the type to create a parameterized type object for
     * @return parameterized type of the class and its type arguments
     */
    public static ParameterizedTypeImpl newTypeWithTypeParameters(Class<?> rawType) {
        if (rawType.getTypeParameters().length == 0) {
            throw new IllegalArgumentException("Class '" + rawType + "' has no type arguments");
        }

        return new ParameterizedTypeImpl(rawType, createOwnerType(rawType), rawType.getTypeParameters());
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments;
    }

    @Override
    public Class<?> getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return ownerType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ParameterizedType) {
            ParameterizedType that = (ParameterizedType) obj;
            return Objects.equals(this.ownerType, that.getOwnerType())
                && Objects.equals(this.rawType, that.getRawType())
                && Arrays.equals(this.actualTypeArguments, that.getActualTypeArguments());
        }
        return false;
    }

    @Override // Same behavior as implementation in sun.reflect
    public int hashCode() {
        return Arrays.hashCode(this.actualTypeArguments)
            ^ Objects.hashCode(this.ownerType)
            ^ Objects.hashCode(this.rawType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (ownerType != null) {
            sb.append(ownerType.getTypeName())
                .append("$")
                .append(rawType.getSimpleName());
        } else {
            sb.append(rawType.getName());
        }

        if (actualTypeArguments.length > 0) {
            sb.append("<");
            for (int i = 0; i < actualTypeArguments.length; ++i) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(actualTypeArguments[i].getTypeName());
            }
            sb.append(">");
        }
        return sb.toString();
    }

    /**
     * Creates the appropriate Type with generic information (if needed) to be used as
     * {@link ParameterizedType#getOwnerType() owner type} of a parameterized type impl with the given raw type.
     *
     * @param rawType the raw type whose owner type should be created with generic information
     * @return owner type to use for the raw type
     */
    @Nullable
    private static Type createOwnerType(Class<?> rawType) {
        Class<?> directEnclosingClass = rawType.getEnclosingClass();
        if (directEnclosingClass == null || Modifier.isStatic(rawType.getModifiers())) {
            return directEnclosingClass;
        }
        return createOwnerTypeHierarchyForEnclosedNonStaticClass(rawType);
    }

    /**
     * Creates the appropriate owner type with relevant generic info for use in a ParameterizedType
     * with the given raw type, which may not be static and which must have an enclosing class.
     * <p>
     * The returned type is either the class's enclosing class as Class object if there are no type parameters in
     * the enclosing classes that are available in the class's scope. Otherwise, a hierarchy of ParameterizedType
     * is returned up to the last class with type parameters available in the class's scope.
     *
     * @param rawType the non-static enclosed class to inspect
     * @return the appropriate owner type for a parameterized type with the given raw type
     */
    private static Type createOwnerTypeHierarchyForEnclosedNonStaticClass(Class<?> rawType) {
        List<Class<?>> enclosingClasses = collectRelevantEnclosingClasses(rawType);

        Type lastOwnerType = null;
        for (int i = enclosingClasses.size() - 1; i >= 0; --i) {
            Class<?> ownerType = enclosingClasses.get(i);
            if (lastOwnerType == null) {
                TypeVariable<?>[] typeParams = ownerType.getTypeParameters();
                if (typeParams.length > 0) {
                    lastOwnerType = new ParameterizedTypeImpl(ownerType, ownerType.getEnclosingClass(), typeParams);
                }
            } else {
                lastOwnerType = new ParameterizedTypeImpl(ownerType, lastOwnerType, ownerType.getTypeParameters());
            }
        }
        return lastOwnerType == null
            ? rawType.getEnclosingClass()
            : lastOwnerType;
    }

    /**
     * Collects all enclosing classes iteratively that are relevant for the raw type's owner type hierarchy.
     * All enclosing classes are collected up to the first static class that is encountered. This represents
     * the set of classes that might have type parameters which are still accessible in the {@code rawType}.
     * <p>
     * The list is inspected afterwards and the last element in the returned list that has type parameters will be
     * the top-most class to be a ParameterizedType in the owner type hierarchy.
     *
     * @implNote
     *     Specifically defined to return {@link ArrayList} to guarantee that access by index is efficient.
     *
     * @param rawType the raw type whose enclosing classes should be gathered
     * @return list of relevant enclosing classes that need to be processed
     */
    private static ArrayList<Class<?>> collectRelevantEnclosingClasses(Class<?> rawType) {
        ArrayList<Class<?>> declaringClasses = new ArrayList<>();
        Class<?> currentClass = rawType.getEnclosingClass();
        while (currentClass != null) {
            declaringClasses.add(currentClass);
            // All non-static classes and the first static enclosing class is relevant, the rest is not
            if (Modifier.isStatic(currentClass.getModifiers())) {
                break;
            }
            currentClass = currentClass.getEnclosingClass();
        }
        return declaringClasses;
    }
}
