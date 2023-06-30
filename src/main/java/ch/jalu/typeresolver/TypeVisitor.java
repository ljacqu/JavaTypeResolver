package ch.jalu.typeresolver;

import ch.jalu.typeresolver.array.ArrayTypeProperties;
import ch.jalu.typeresolver.array.ArrayTypeUtils;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility for gathering all parent types of a given type.
 */
final class TypeVisitor {

    private TypeVisitor() {
    }

    static Set<Type> gatherAllTypes(Type type) {
        return gatherAllTypes(type, new TypeVariableResolver(type));
    }

    static Set<Type> gatherAllTypes(Type type, TypeVariableResolver resolver) {
        return gatherAllTypes(type, resolver, new HashSet<>(), Function.identity());
    }

    /**
     * Gathers all super classes and interfaces, recursively, of the given type. Adds all found types into the
     * provided collection.
     *
     * @param type the type of which all supertypes should be gathered
     * @param resolver resolver used to resolve parent information
     * @param collection the collection to add the results to
     * @param typeToElementFn function to convert the found types before adding to the collection
     * @param <E> the type of the entries in the collection
     * @param <C> the concrete collection type
     * @return the provided collection argument (for convenience)
     */
    static <E, C extends Collection<? super E>> C gatherAllTypes(Type type, TypeVariableResolver resolver,
                                                                 C collection, Function<Type, E> typeToElementFn) {
        if (CommonTypeUtils.getDefinitiveClass(type) != null) {
            Consumer<Type> typeConsumer = aType -> {
                E typeAsElement = typeToElementFn.apply(resolver.resolve(aType));
                collection.add(typeAsElement);
            };

            visitClassesRecursively(type, typeConsumer);
        }
        return collection;
    }

    static void visitAllTypes(Type type, TypeVariableResolver resolver, Consumer<Type> typeConsumer) {
        if (CommonTypeUtils.getDefinitiveClass(type) != null) {
            visitClassesRecursively(type, aType -> typeConsumer.accept(resolver.resolve(aType)));
        }
    }

    private static void visitClassesRecursively(@Nullable Type type, Consumer<Type> typeConsumer) {
        if (type == null) {
            return;
        }

        Class<?> typeAsClass = CommonTypeUtils.getDefinitiveClass(type);
        if (!typeAsClass.isArray()) {
            typeConsumer.accept(type);
            visitClassesRecursively(typeAsClass.getGenericSuperclass(), typeConsumer);
            for (Type genericInterface : typeAsClass.getGenericInterfaces()) {
                visitClassesRecursively(genericInterface, typeConsumer);
            }
        } else {
            ArrayTypeProperties arrayProperties = ArrayTypeUtils.getArrayProperty(type);
            List<Type> componentTypeList = gatherAllTypesOfComponent(arrayProperties);

            // An array like Double[][] is also a Number[][] or an Object[][], but only for the same dimension
            for (Type component : componentTypeList) {
                Type arrayType = ArrayTypeUtils.createArrayType(component, arrayProperties.getDimension());
                typeConsumer.accept(arrayType);
            }

            // Arrays implement Serializable & Cloneable, so a Double[][] is also a Serializable[] and a Serializable...
            List<Type> arrayClassParents = Arrays.asList(Serializable.class, Cloneable.class, Object.class);
            for (int dimension = arrayProperties.getDimension() - 1; dimension >= 0; --dimension) {
                for (Type arrayClassParent : arrayClassParents) {
                    Type arrayType = ArrayTypeUtils.createArrayType(arrayClassParent, dimension);
                    typeConsumer.accept(arrayType);
                }
            }
        }
    }

    private static List<Type> gatherAllTypesOfComponent(ArrayTypeProperties arrayProperties) {
        TypeVariableResolver componentResolver = new TypeVariableResolver(arrayProperties.getComponentType());
        List<Type> typesOfComponent = new ArrayList<>();
        visitAllTypes(arrayProperties.getComponentType(), componentResolver, typesOfComponent::add);

        // If we were based on something like List[][] we won't get Object in the components since List is an interface,
        // but List[][] is also an Object[][] so we add it here, making sure NOT to do so for primitive component types
        // (e.g. float[][] is not an Object[][])
        if (!CommonTypeUtils.getDefinitiveClass(arrayProperties.getComponentType()).isPrimitive()
                && !typesOfComponent.contains(Object.class)) {
            typesOfComponent.add(Object.class);
        }
        return typesOfComponent;
    }
}
