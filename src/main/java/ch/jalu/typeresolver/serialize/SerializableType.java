package ch.jalu.typeresolver.serialize;

import ch.jalu.typeresolver.CommonTypeUtils;
import ch.jalu.typeresolver.typeimpl.GenericArrayTypeImpl;
import ch.jalu.typeresolver.typeimpl.ParameterizedTypeImpl;
import ch.jalu.typeresolver.typeimpl.WildcardTypeImpl;

import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

/**
 * Represents a {@link Type} in a serializable manner.
 * Create a serializable type with {@link SerializableType#from}, convert back with {@link SerializableType#getType}.
 * <p>
 * Limitations:<ul>
 *  <li>An exception is thrown for type variables that aren't declared by a class (see Javadoc on
 *     {@link SerializableTypeVariable} for more information).</li>
 *  <li>An exception is thrown if the Type is not of any standard subtype: {@link Class}, {@link GenericArrayType},
 *      {@link ParameterizedType}, {@link TypeVariable} or {@link WildcardType}.</li>
 * </ul>
 */
public abstract class SerializableType implements Serializable {

    private transient Type type;

    /**
     * @return the type wrapped by this instance
     */
    public Type getType() {
        if (type == null) {
            type = toType();
        }
        return type;
    }

    /**
     * Returns a {@link SerializableType} representing the given type.
     *
     * @param type the type to map
     * @return serializable type representing the given type
     */
    public static SerializableType from(Type type) {
        if (type == null) {
            return null;
        } else if (type instanceof Class<?>) {
             return new ClassWrapper((Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            return SerializableParameterizedType.from((ParameterizedType) type);
        } else if (type instanceof TypeVariable<?>) {
            return SerializableTypeVariable.from((TypeVariable<?>) type);
        } else if (type instanceof GenericArrayType) {
            return SerializableGenericArrayType.from((GenericArrayType) type);
        } else if (type instanceof WildcardType) {
            return SerializableWildcardType.from((WildcardType) type);
        }

        throw new IllegalArgumentException("Unknown type implementation: " + type.getClass());
    }

    /**
     * @return creates the Type this object represents
     */
    protected abstract Type toType();

    private static SerializableType[] convertTypes(Type[] types) {
        return Arrays.stream(types)
            .map(SerializableType::from)
            .toArray(SerializableType[]::new);
    }

    private static Type[] convertSerializableTypes(SerializableType[] serTypes) {
        return Arrays.stream(serTypes)
            .map(SerializableType::toType)
            .toArray(Type[]::new);
    }

    /**
     * SerializableType impl. that wraps a Class (Class is already serializable).
     */
    public static class ClassWrapper extends SerializableType {
        private static final long serialVersionUID = 1;

        private final Class<?> clazz;

        public ClassWrapper(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public Class<?> toType() {
            return clazz;
        }
    }

    /**
     * SerializableType impl. for parameterized types.
     */
    public static class SerializableParameterizedType extends SerializableType {
        private static final long serialVersionUID = 1;

        private final Class<?> rawType;
        private final SerializableType[] actualTypeArguments;
        private final SerializableType ownerType;

        public SerializableParameterizedType(Class<?> rawType,
                                             SerializableType[] actualTypeArguments,
                                             SerializableType ownerType) {
            this.rawType = rawType;
            this.actualTypeArguments = actualTypeArguments;
            this.ownerType = ownerType;
        }

        public static SerializableParameterizedType from(ParameterizedType pt) {
            SerializableType ownerType = SerializableType.from(pt.getOwnerType());
            SerializableType[] typeArgs = convertTypes(pt.getActualTypeArguments());
            return new SerializableParameterizedType(CommonTypeUtils.getRawType(pt), typeArgs, ownerType);
        }

        @Override
        public ParameterizedType toType() {
            Type[] actualTypeArgs = convertSerializableTypes(actualTypeArguments);
            Type owner = ownerType == null ? null : ownerType.toType();
            return new ParameterizedTypeImpl(rawType, owner, actualTypeArgs);
        }
    }

    /**
     * SerializableType impl. for generic array types.
     */
    public static class SerializableGenericArrayType extends SerializableType {
        private static final long serialVersionUID = 1;

        private final SerializableType genericComponentType;

        public SerializableGenericArrayType(SerializableType componentType) {
            this.genericComponentType = componentType;
        }

        public static SerializableGenericArrayType from(GenericArrayType gat) {
            return new SerializableGenericArrayType(from(gat.getGenericComponentType()));
        }

        @Override
        public GenericArrayType toType() {
            return new GenericArrayTypeImpl(genericComponentType.toType());
        }
    }

    /**
     * SerializableType impl. for type variables. Only type variables from classes are supported; an exception is
     * thrown for type variables of methods or constructors.
     * <p>
     * Background: The internal implementation of {@link TypeVariable} in the JDK only considers objects equal to each
     * other that implement that same class. As such, we cannot create instances of another class and rely on resolving
     * the type variable back based on the class declaring it and the type variable's index in the declaring class's
     * type variables.
     */
    public static class SerializableTypeVariable extends SerializableType {
        private static final long serialVersionUID = 1;

        private final Class<?> origin;
        private final int index;

        public SerializableTypeVariable(Class<?> origin, int index) {
            this.origin = origin;
            this.index = index;
        }

        /**
         * Creates a SerializableType for the given type variable. An exception is thrown for type variables that
         * aren't declared by a class (see Javadoc on {@link SerializableTypeVariable} for more information).
         *
         * @param tv the type variable to map
         * @return serializable representation of the type variable
         */
        public static SerializableTypeVariable from(TypeVariable<?> tv) {
            GenericDeclaration declarer = tv.getGenericDeclaration();
            if (declarer instanceof Class<?>) {
                int index = findIndex(declarer, tv);
                return new SerializableTypeVariable((Class<?>) declarer, index);
            } else {
                throw new IllegalArgumentException(
                    "Type variable must be declared by a class, but found declarer: " + declarer);
            }
        }

        @Override
        public TypeVariable<?> toType() {
            TypeVariable<? extends Class<?>>[] typeParams = origin.getTypeParameters();
            if (index >= typeParams.length) {
                throw new IllegalStateException("No type variable at index " + index + " for " + origin);
            }
            return typeParams[index];
        }

        private static int findIndex(GenericDeclaration declarer, TypeVariable<?> type) {
            int index = 0;
            for (TypeVariable<?> typeParameter : declarer.getTypeParameters()) {
                if (typeParameter.equals(type)) {
                    return index;
                }
                ++index;
            }

            throw new IllegalArgumentException("Type variable '" + type + "' does not belong to " + declarer);
        }
    }

    /**
     * SerializableType impl. for wildcard types.
     */
    public static class SerializableWildcardType extends SerializableType {
        private static final long serialVersionUID = 1;

        private final SerializableType[] upperBounds;
        private final SerializableType[] lowerBounds;

        public SerializableWildcardType(SerializableType[] upperBounds, SerializableType[] lowerBounds) {
            this.upperBounds = upperBounds;
            this.lowerBounds = lowerBounds;
        }

        public static SerializableWildcardType from(WildcardType wt) {
            SerializableType[] upperBounds = convertTypes(wt.getUpperBounds());
            SerializableType[] lowerBounds = convertTypes(wt.getLowerBounds());
            return new SerializableWildcardType(upperBounds, lowerBounds);
        }

        @Override
        public WildcardType toType() {
            Type[] upperBounds = convertSerializableTypes(this.upperBounds);
            Type[] lowerBounds = convertSerializableTypes(this.lowerBounds);
            return new WildcardTypeImpl(upperBounds, lowerBounds);
        }
    }
}
