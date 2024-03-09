package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.CommonTypeUtils;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

/**
 * Implementation of {@link ParameterizedType}, with same {@link Object#equals} and {@link Object#hashCode()}
 * as the JDK implementation uses internally.
 */
public class ParameterizedTypeImpl implements ParameterizedType {

    private final Class<?> rawType;
    private final Type ownerType;
    private final Type[] actualTypeArguments;

    /**
     * Constructor. Performs no validation on the arguments whatsoever.
     * <p>
     * You can build parameterized types dynamically with {@link ParameterizedTypeBuilder}.
     *
     * @param rawType the raw type
     * @param ownerType the owner type
     * @param actualTypeArguments the type arguments
     */
    public ParameterizedTypeImpl(Class<?> rawType, @Nullable Type ownerType, Type... actualTypeArguments) {
        this.rawType = rawType;
        this.ownerType = ownerType;
        this.actualTypeArguments = actualTypeArguments;
    }

    /**
     * Copy constructor.
     *
     * @param parameterizedType the parameterized type to copy from
     */
    public ParameterizedTypeImpl(ParameterizedType parameterizedType) {
        this.rawType = CommonTypeUtils.getRawType(parameterizedType);
        this.ownerType = parameterizedType.getOwnerType();
        this.actualTypeArguments = parameterizedType.getActualTypeArguments();
    }

    @Override
    public Type[] getActualTypeArguments() {
        return actualTypeArguments.clone();
    }

    @Override
    public Class<?> getRawType() {
        return rawType;
    }

    @Nullable
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
}
