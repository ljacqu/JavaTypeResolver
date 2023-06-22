package ch.jalu.typeresolver.reference;

import ch.jalu.typeresolver.TypeInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A typed {@link TypeInfo} implementation which can be instantiated with a concrete type resolved at runtime for
 * easy creation of more complex {@link Type} implementations. For example, a parameterized type can be encapsulated in
 * a type reference the following way:<pre>{@code
 *  TypeInfo typeInfo = new TypeReference<List<String>>() {};
 *  typeInfo.getType(); // List<String>
 * }</pre>
 *
 * @param <T> the type that will be held by the TypeInfo
 */
public abstract class TypeReference<T> extends TypeInfo {

    /**
     * Constructor. See Javadoc on class for proper usage.
     */
    public TypeReference() {
    }

    @Override
    protected Type inferTypeForNoArgsConstructor() {
        Type genericSuperClass = getClass().getGenericSuperclass();
        if (!(genericSuperClass instanceof ParameterizedType)) {
            throw new IllegalStateException("The parent class is not a parameterized type. "
                + "Are you missing the type argument in your declaration? Please refer to the Javadoc on the class.");
        }
        return ((ParameterizedType) genericSuperClass).getActualTypeArguments()[0];
    }
}
