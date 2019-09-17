package ch.jalu.typeresolver;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A typed TypeInfo implementation which can be instantiated with a concrete type resolved at runtime:
 * <br> {@code TypeInfo typeInfo = new TypeReference<List<String>>() {};}
 *
 * @param <T> the type that will be wrapped by the TypeInfo
 */
public abstract class TypeReference<T> extends TypeInfo {

    /**
     * Constructor. See javadoc on class for proper usage.
     */
    public TypeReference() {
    }

    @Override
    protected Type inferTypeForNoArgsConstructor() {
        Type genericSuperClass = getClass().getGenericSuperclass();
        if (!(genericSuperClass instanceof ParameterizedType)) {
            throw new IllegalStateException("Parent class is not a parameterized type. "
                + "Are you missing the type argument? See example in class Javadoc");
        }
        return ((ParameterizedType) genericSuperClass).getActualTypeArguments()[0];
    }
}
