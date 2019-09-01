package ch.jalu.typeresolver.typeimpl;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

/**
 * Implementation of {@link WildcardType}.
 */
public class WildcardTypeImpl implements WildcardType {

    private final Type[] upperBounds;
    private final Type[] lowerBounds;

    public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
        this.upperBounds = upperBounds;
        this.lowerBounds = lowerBounds;
    }

    @Override
    public Type[] getUpperBounds() {
        return upperBounds;
    }

    @Override
    public Type[] getLowerBounds() {
        return lowerBounds;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof WildcardType) {
            WildcardType that = (WildcardType) obj;
            return Arrays.equals(that.getLowerBounds(), this.lowerBounds)
                && Arrays.equals(that.getUpperBounds(), this.upperBounds);
        }
        return false;
    }

    @Override // Same behavior as implementation in sun.reflect
    public int hashCode() {
        return Arrays.hashCode(lowerBounds) ^ Arrays.hashCode(upperBounds);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        Type[] bounds;
        if (lowerBounds.length > 0) {
            bounds = lowerBounds;
            sb.append("? super ");
        } else if (upperBounds.length == 0 || upperBounds[0].equals(Object.class)) {
            return "?";
        } else { // upperBounds.length > 0
            bounds = upperBounds;
            sb.append("? extends ");
        }

        for (int i = 0; i < bounds.length; ++i) {
            if (i > 0) {
                sb.append(" & ");
            }
            sb.append(bounds[i].getTypeName());
        }
        return sb.toString();
    }
}
