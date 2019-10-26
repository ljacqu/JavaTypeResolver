package ch.jalu.typeresolver;

import ch.jalu.typeresolver.typeimpl.ParameterizedTypeImpl;
import ch.jalu.typeresolver.typeimpl.WildcardTypeImpl;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static ch.jalu.typeresolver.CommonTypeUtil.getRawType;

/**
 * Allows to resolve type variables to actual classes from previous context (extension of a class with a type parameter,
 * class containing a field with a type argument).
 * <p>
 * For instance, if we want to visit all fields recursively and have a class such as:<pre>
 * class Foo {
 *    private Optional&lt;String&gt; name;
 * }
 * </pre>
 * When we visit the {@code Optional} class we may stumble upon a field defined as {@code T value}; from the previous
 * context we know that the {@code T} type variable can be translated to {@code String}.
 * <p>
 * Or, for example, we are processing a class which extends another class that has a type parameter:<pre>
 * class Bar extends ArrayList&lt;Long&gt; {
 * }
 * </pre>
 * With this context, if we process the {@link java.util.ArrayList#get(int)} method, we know that it will return a
 * Long as the {@code E} type variable can be translated to a {@code Long}.
 */
class TypeVariableResolver {

    private final Map<TypeVariableData, Type> typeRules = new HashMap<>();

    TypeVariableResolver(Type type) {
        registerTypes(type, new HashSet<>());
    }

    Type resolve(Type type) {
        if (type instanceof TypeVariable<?>) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            Type lookedUpType = typeRules.get(new TypeVariableData(tv));
            if (lookedUpType != null) {
                return resolve(lookedUpType);
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] resolvedTypes = resolveTypes(pt.getActualTypeArguments());
            return new ParameterizedTypeImpl(getRawType(pt), pt.getOwnerType(), resolvedTypes);
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            Type[] upperBounds = resolveTypes(wt.getUpperBounds());
            Type[] lowerBounds = resolveTypes(wt.getLowerBounds());
            return new WildcardTypeImpl(upperBounds, lowerBounds);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            Type resolvedComponentType = resolve(gat.getGenericComponentType());
            return CommonTypeUtil.createArrayType(resolvedComponentType);
        }
        return type;
    }

    Type[] resolveTypes(Type[] types) {
        Type[] resolvedTypes = new Type[types.length];
        for (int i = 0; i < types.length; ++i) {
            resolvedTypes[i] = resolve(types[i]);
        }
        return resolvedTypes;
    }

    private void registerTypes(Type type, Set<Class<?>> processedClasses) {
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            registerTypesFromParentAndInterfaces(clazz, processedClasses);
            registerTypes(clazz.getEnclosingClass(), processedClasses);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            registerParameterizedTypes(pt, processedClasses);
            registerTypesFromParentAndInterfaces(getRawType(pt), processedClasses);
            registerTypes(pt.getOwnerType(), processedClasses);
        }
    }

    private void registerTypesFromParentAndInterfaces(Class<?> clazz, Set<Class<?>> processedClasses) {
        Class<?> currentClass = clazz;
        while (!Object.class.equals(currentClass) && currentClass != null) {
            registerTypes(currentClass.getGenericSuperclass(), processedClasses);
            for (Type genericInterfaceType : currentClass.getGenericInterfaces()) {
                registerTypes(genericInterfaceType, processedClasses);
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    private void registerParameterizedTypes(ParameterizedType parameterizedType, Set<Class<?>> processedClasses) {
        Class<?> rawType = getRawType(parameterizedType);
        if (processedClasses.contains(rawType)) {
            return;
        }

        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        for (int i = 0; i < typeArguments.length; ++i) {
            typeRules.put(new TypeVariableData(rawType.getTypeParameters()[i]), typeArguments[i]);
        }
        processedClasses.add(rawType);
    }

    /**
     * Contains the identifying properties of a {@link TypeVariable}. This class wraps them, as the implementation class
     * of {@link TypeVariable} has a strict equals method that only matches objects of its own class.
     */
    private static final class TypeVariableData {
        private final GenericDeclaration declarer;
        private final String name;

        TypeVariableData(TypeVariable<?> tv) {
            this.declarer = tv.getGenericDeclaration();
            this.name = tv.getName();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof TypeVariableData) {
                TypeVariableData that = (TypeVariableData) obj;
                return Objects.equals(this.declarer, that.declarer) && Objects.equals(this.name, that.name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            // same behavior as TypeVariableImpl sun.reflect
            return declarer.hashCode() ^ name.hashCode();
        }
    }
}
