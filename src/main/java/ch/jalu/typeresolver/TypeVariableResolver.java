package ch.jalu.typeresolver;

import ch.jalu.typeresolver.typeimpl.GenericArrayTypeImpl;
import ch.jalu.typeresolver.typeimpl.ParameterizedTypeImpl;
import ch.jalu.typeresolver.typeimpl.WildcardTypeImpl;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * Allows to resolve type variables to actual classes from previous context (extension of a class with a type variable,
 * class containing a field with a type variable).
 * <p>
 * For instance, if we want to visit all fields recursively and have a class such as:<pre>
 * class Foo {
 *    private Optional&lt;String&gt; name;
 * }
 * </pre>
 * When we visit the {@code Optional} class we may stumble upon a field defined as {@code T value}; from the previous
 * context we know that the {@code T} type variable can be translated to {@code String}.
 */
public class TypeVariableResolver {

    private final Table<Class, String, Type> typeRules = HashBasedTable.create();

    public TypeVariableResolver(Type type) {
        registerTypes(type);
    }

    private TypeVariableResolver(TypeVariableResolver parentResolver) {
        typeRules.putAll(parentResolver.typeRules);
    }

    public TypeVariableResolver createChildResolver(Type type) {
        TypeVariableResolver childResolver = new TypeVariableResolver(this);
        childResolver.registerTypes(type);
        return childResolver;
    }

    public Type resolve(Type type) {
        if (type instanceof TypeVariable<?>) {
            TypeVariable<?> tv = (TypeVariable<?>) type;
            Object declaringMember = tv.getGenericDeclaration();
            Type lookedUpType = typeRules.get(declaringMember, tv.getName());
            if (lookedUpType != null) {
                return resolve(lookedUpType);
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] resolvedTypes = resolve(pt.getActualTypeArguments());
            return new ParameterizedTypeImpl((Class<?>) pt.getRawType(), pt.getOwnerType(), resolvedTypes);
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType) type;
            Type[] upperBounds = resolve(wt.getUpperBounds());
            Type[] lowerBounds = resolve(wt.getLowerBounds());
            return createWildcardType(upperBounds, lowerBounds);
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            Type resolvedComponentType = resolve(gat.getGenericComponentType());
            if (resolvedComponentType instanceof Class<?> || resolvedComponentType instanceof ParameterizedType) {
                Class<?> componentClass = new TypeInfo(resolvedComponentType).toClass();
                return Array.newInstance(componentClass, 0).getClass();
            }
            return new GenericArrayTypeImpl(resolvedComponentType);
        }
        return type;
    }

    private Type[] resolve(Type[] types) {
        Type[] resolvedTypes = new Type[types.length];
        for (int i = 0; i < types.length; ++i) {
            resolvedTypes[i] = resolve(types[i]);
        }
        return resolvedTypes;
    }

    private WildcardType createWildcardType(Type[] upperBounds, Type[] lowerBounds) {
        if (lowerBounds.length == 1) {
            Type lowerBound = lowerBounds[0];
            if (lowerBound instanceof WildcardType && ((WildcardType) lowerBound).getLowerBounds().length > 0) {
                return (WildcardType) lowerBound;
            }
        } else if (upperBounds.length == 1) {
            Type upperBound = upperBounds[0];
            if (upperBound instanceof WildcardType && ((WildcardType) upperBound).getLowerBounds().length == 0) {
                return (WildcardType) upperBound;
            }
        }
        return new WildcardTypeImpl(upperBounds, lowerBounds);
    }

    private void registerTypes(Type type) {
        if (type instanceof Class<?>) {
            registerTypesFromParentAndInterfaces((Class) type);
        } else if (type instanceof ParameterizedType) {
            Class<?> rawType = (Class<?>) ((ParameterizedType) type).getRawType();
            registerTypesFromParentAndInterfaces(rawType);
            registerParameterizedTypes((ParameterizedType) type);
        }
    }

    private void registerTypesFromParentAndInterfaces(Class<?> clazz) {
        Class<?> currentClass = clazz;
        while (!Object.class.equals(currentClass) && currentClass != null) {
            registerTypes(currentClass.getGenericSuperclass());
            for (Type genericInterfaceType : currentClass.getGenericInterfaces()) {
                registerTypes(genericInterfaceType);
            }
            currentClass = currentClass.getSuperclass();
        }
    }

    private void registerParameterizedTypes(ParameterizedType parameterizedType) {
        Class<?> rawType = (Class<?>) parameterizedType.getRawType();
        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        for (int i = 0; i < typeArguments.length; ++i) {
            typeRules.put(rawType, rawType.getTypeParameters()[i].getName(), typeArguments[i]);
        }
    }
}
