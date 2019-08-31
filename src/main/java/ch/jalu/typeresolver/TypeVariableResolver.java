package ch.jalu.typeresolver;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

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

    public TypeVariableResolver(Class<?> clazz) {
        registerTypesFromParentAndInterfaces(clazz);
    }

    private TypeVariableResolver(TypeVariableResolver parentResolver) {
        typeRules.putAll(parentResolver.typeRules);
    }

    public TypeVariableResolver createChildResolver(Type type) {
        TypeVariableResolver childResolver = new TypeVariableResolver(this);
        if (type instanceof Class<?>) {
            childResolver.registerTypesFromParentAndInterfaces((Class) type);
        } else {
            childResolver.registerTypes(type);
        }
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
            Type[] resolvedTypes = new Type[pt.getActualTypeArguments().length];
            for (int i = 0; i < pt.getActualTypeArguments().length; ++i) {
                resolvedTypes[i] = resolve(pt.getActualTypeArguments()[i]);
            }
            return new ParameterizedTypeImpl(resolvedTypes, pt);
        }
        return type;
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

    private void registerTypes(@Nullable Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) pt.getRawType();
            Type[] typeArguments = pt.getActualTypeArguments();
            for (int i = 0; i < typeArguments.length; ++i) {
                typeRules.put(rawType, rawType.getTypeParameters()[i].getName(), typeArguments[i]);
            }
        }
    }

    /**
     * Parameterized type implementation which wraps another parameterized type but allows to
     * set custom generic types. Used to resolve the parameterized type's type arguments, which
     * may be type variables which could be resolved to concrete types.
     */
    private static class ParameterizedTypeImpl implements ParameterizedType {

        private final Type[] actualTypeArguments;
        private final Type rawType;
        private final Type ownerType;

        private ParameterizedTypeImpl(Type[] actualTypeArguments, ParameterizedType originalParameterizedType) {
            this.actualTypeArguments = actualTypeArguments;
            this.rawType = originalParameterizedType.getRawType();
            this.ownerType = originalParameterizedType.getOwnerType();
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments;
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return ownerType;
        }
    }
}
