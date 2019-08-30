package ch.jalu.typeresolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public class TypeInfo {

    private final Type type;

    public TypeInfo(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public Class<?> toClass() {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = ((ParameterizedType) type);
            // Current implementations can only have Class<?> as raw type, so cast without checking
            return (Class<?>) pt.getRawType();
        }
        return null;
    }

    public TypeInfo getGenericTypeInfo(int index) {
        if (type instanceof ParameterizedType) {
            Type[] genericTypes = ((ParameterizedType) type).getActualTypeArguments();
            if (index < genericTypes.length) {
                return new TypeInfo(genericTypes[index]);
            }
        }
        return null;
    }

    public Class<?> getGenericTypeAsClass(int index) {
        TypeInfo genericTypeInfo = getGenericTypeInfo(index);
        return genericTypeInfo == null ? null : genericTypeInfo.toClass();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof TypeInfo) {
            return Objects.equals(this.type, ((TypeInfo) obj).type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    @Override
    public String toString() {
        return "TypeInfo[type=" + type + "]";
    }
}
