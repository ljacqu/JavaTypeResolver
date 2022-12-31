package ch.jalu.typeresolver.typeimpl;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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

        return new ParameterizedTypeImpl(rawType, generateOwnerType(rawType), rawType.getTypeParameters());
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
    private static Type generateOwnerType(Class<?> rawType) {
        Class<?> directEnclosingClass = rawType.getEnclosingClass();
        if (directEnclosingClass == null || Modifier.isStatic(rawType.getModifiers())) {
            return directEnclosingClass;
        }

        List<Class<?>> ownerTypes = collectOwnerTypeHierarchyForEnclosedNonStaticClass(rawType);
        if (ownerTypes == null) {
            return directEnclosingClass;
        }

        Type lastOwner = null;
        for (int i = ownerTypes.size() - 1; i >= 0; --i) {
            Class<?> ownerType = ownerTypes.get(i);
            if (lastOwner == null && firstOwnerTypeShouldBeClass(ownerType, ownerTypes)) {
                lastOwner = ownerType;
            } else {
                lastOwner = new ParameterizedTypeImpl(ownerType, lastOwner, ownerType.getTypeParameters());
            }

        }
        return lastOwner;
    }

    /**
     * Returns whether the top-most owner type in the hierarchy should be represented as {@link Class}. Semantically,
     * the top-most owner is only represented as a ParameterizedType if it has type parameters which are accessible
     * in the scope of the parameterized type's raw type. This is only the case if it is not static and if the class
     * one level lower in the hierarchy is also not static.
     * The provided list of {@code allOwnerTypes} is sorted from lowest to highest in the type hierarchy and already
     * includes the enclosing class of the last class with relevant type arguments (if available).
     *
     * @param firstOwnerType the top-most owner type in the hierarchy to inspect
     * @param allOwnerTypes all owner types in the hierarchy, from lowest to highest
     * @return true if the first owner type should be represented as Class, false for ParameterizedType
     */
    private static boolean firstOwnerTypeShouldBeClass(Class<?> firstOwnerType, List<Class<?>> allOwnerTypes) {
        if (Modifier.isStatic(firstOwnerType.getModifiers()) || firstOwnerType.getTypeParameters().length == 0) {
            return true;
        }
        if (allOwnerTypes.size() > 1) {
            Class<?> nextClass = allOwnerTypes.get(allOwnerTypes.size() - 2);
            return Modifier.isStatic(nextClass.getModifiers());
        }
        return false;
    }

    /**
     * Returns the classes which will make up the hierarchy of owner types for the given raw type, which may not be
     * static or and which must have an enclosing class.
     * The list goes from lowest enclosing class to highest, i.e. the first entry is the rawType's direct enclosing
     * class and the last entry is the class with the least nesting that is relevant. In some circumstances, the last
     * entry in the returned list should be a Class and not a ParameterizedType (handled by {@link #generateOwnerType}).
     * <p>
     * This method returns null if the owner type is just the rawType's enclosing class as Class object (allows the
     * caller of this method to fast-track this case, avoiding the need to perform the same checks again).
     *
     * @param rawType the non-static enclosed class to inspect
     * @return list of classes that will make up the owner type hierarchy; null if it should just be the enclosing class
     *         as Class object
     */
    @Nullable
    private static List<Class<?>> collectOwnerTypeHierarchyForEnclosedNonStaticClass(Class<?> rawType) {
        List<Class<?>> declaringClasses = collectRelevantEnclosingClasses(rawType);

        // Iterate backwards and find the highest class in the hierarchy that has type parameters
        Class<?> lastClassWithTypeParams = null;
        int lastClassWithTypeParamsIndex = -1;
        for (int i = declaringClasses.size() - 1; i >= 0; --i) {
            Class<?> declaringClass = declaringClasses.get(i);
            if (declaringClass.getTypeParameters().length > 0) {
                lastClassWithTypeParams = declaringClass;
                lastClassWithTypeParamsIndex = i;
                break;
            }
        }

        if (lastClassWithTypeParams == null) {
            // No type parameters anywhere -> owner type will be a simple Class. Represented by returning null.
            return null;
        }
        if (lastClassWithTypeParams.getDeclaringClass() == null) {
            return declaringClasses.subList(0, lastClassWithTypeParamsIndex + 1);
        }

        if (declaringClasses.size() >= lastClassWithTypeParamsIndex + 2) {
            return declaringClasses.subList(0, lastClassWithTypeParamsIndex + 2);
        }
        declaringClasses.add(lastClassWithTypeParams.getDeclaringClass());
        return declaringClasses;
    }

    /**
     * Collects all enclosing classes iteratively that are relevant for the raw type's owner type hierarchy.
     * All enclosing classes are collected up to the first static class that is encountered. This represents
     * the set of classes that might have type parameters which are still accessible in the {@code rawType}.
     * <p>
     * The list is inspected afterwards and the last element in the returned list that has type parameters will be
     * the last class to be a ParameterizedType in the owner type hierarchy.
     *
     * @implNote
     *     Specifically defined to return {@link ArrayList} to guarantee that access by index is efficient
     *     and that the list can be modified further.
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
            currentClass = currentClass.getDeclaringClass();
        }
        return declaringClasses;
    }
}
