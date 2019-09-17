package ch.jalu.typeresolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class NestedTypeReference<O> extends TypeInfo {

    @Override
    protected Type inferTypeForNoArgsConstructor() {
        Type genericSuperClass = getClass().getGenericSuperclass();
        if (!(genericSuperClass instanceof ParameterizedType)) {
            throw new IllegalStateException("Parent class is not a parameterized type");
        }
        TypeInfo typeInfo = new TypeInfo(genericSuperClass).getTypeArgumentInfo(0).getTypeArgumentInfo(0);
        if (typeInfo == null) {
            throw new IllegalStateException("Type within type reference not a parameterized type?");
        }

        return typeInfo.getType();
    }

    public <T extends Type> T type() {
        return (T) getType();
    }
}
