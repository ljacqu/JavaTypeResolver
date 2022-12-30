package ch.jalu.typeresolver.typeimpl;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    private static Type generateOwnerType(Class<?> rawType) {
        List<Class<?>> ownerTypes = collectOwnerTypeHierarchy(rawType);
        int size = ownerTypes.size();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            Class<?> owner = ownerTypes.get(0);
            if (owner.getTypeParameters().length > 0 && !Modifier.isStatic(owner.getModifiers())) {
                return new ParameterizedTypeImpl(owner, null, owner.getTypeParameters());
            }
            return owner;
        }

        Collections.reverse(ownerTypes);
        Type lastOwner = null;
        int index = 0;
        if (Modifier.isStatic(ownerTypes.get(0).getModifiers()) || ownerTypes.get(0).getTypeParameters().length == 0) {
            lastOwner = ownerTypes.get(0);
            index = 1;
        }

        while (index < ownerTypes.size()) {
            Class<?> ownerType = ownerTypes.get(index);
            lastOwner = new ParameterizedTypeImpl(ownerType, lastOwner, ownerType.getTypeParameters());
            ++index;
        }
        return lastOwner;
    }

    private static List<Class<?>> collectOwnerTypeHierarchy(Class<?> rawType) {
        final Class<?> directClass = rawType.getDeclaringClass();
        if (directClass == null) {
            return Collections.emptyList();
        }

        // Collect all declaring classes
        List<Class<?>> declaringClasses = new ArrayList<>();
        Class<?> currentClass = directClass;
        while (currentClass != null) {
            declaringClasses.add(currentClass);
            currentClass = currentClass.getDeclaringClass();
        }

        // Iterate from the back and find first non-static class with params
        int lastNonStaticClassWithParams = -1;
        for (int i = declaringClasses.size() - 1; i >= 0; --i) {
            Class<?> owner = declaringClasses.get(i);
            if (!Modifier.isStatic(owner.getModifiers()) && owner.getTypeParameters().length > 0) {
                lastNonStaticClassWithParams = i;
                break;
            }
        }

        // No non-static class with params means we only need the direct declaring class
        if (lastNonStaticClassWithParams < 0) {
            return Collections.singletonList(directClass);
        }
        // Last non-static class with params is at the top of the hierarchy -> return full list
        if (lastNonStaticClassWithParams + 1 >= declaringClasses.size()) {
            return declaringClasses;
        }

        // The class before the last non-static class with params is included; if it's static and has type parameters,
        // the class before that one (if exists) is also included.
        // (I don't understand the semantics of this, but this is what the JDK does.)
        Class<?> previousClass = declaringClasses.get(lastNonStaticClassWithParams + 1);
        int endIndexInclusive = lastNonStaticClassWithParams + 1;
        if (Modifier.isStatic(previousClass.getModifiers()) && previousClass.getTypeParameters().length > 0) {
            ++endIndexInclusive; // Potentially out of bounds -- handled below
        }

        int safeEndIndexExclusive = Math.min(endIndexInclusive + 1, declaringClasses.size());
        return declaringClasses.subList(0, safeEndIndexExclusive);
    }
}
