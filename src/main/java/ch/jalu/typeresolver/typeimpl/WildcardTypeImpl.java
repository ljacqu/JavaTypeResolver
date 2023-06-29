package ch.jalu.typeresolver.typeimpl;

import ch.jalu.typeresolver.CommonTypeUtils;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;

/**
 * Implementation of {@link WildcardType}.
 */
public class WildcardTypeImpl implements WildcardType {

    private final Type[] upperBounds;
    private final Type[] lowerBounds;

    /**
     * Constructor. Prefer the more specific static creator methods when possible.
     *
     * @param upperBounds the upper bounds
     * @param lowerBounds the lower bounds
     */
    public WildcardTypeImpl(Type[] upperBounds, Type[] lowerBounds) {
        this.upperBounds = upperBounds;
        this.lowerBounds = lowerBounds;
    }

    /**
     * Creates a new wildcard type "? extends T", where T is the given upperBound.
     *
     * @param upperBound the upper bound
     * @return new wildcard type whose upper bound is the given type
     */
    public static WildcardType newWildcardExtends(Type upperBound) {
        return new WildcardTypeImpl(new Type[]{ upperBound }, new Type[0]);
    }

    /**
     * Creates a new wildcard type "? super T", where T is the given lowerBound.
     *
     * @param lowerBound the lower bound
     * @return new wildcard type whose lower bound is the given type
     */
    public static WildcardType newWildcardSuper(Type lowerBound) {
        // Set Object.class as upper bound to be in line with the JRE behavior
        return new WildcardTypeImpl(new Type[]{ Object.class }, new Type[]{ lowerBound });
    }

    /**
     * Creates a new unbounded wildcard "?", as returned by the JRE (upper bound is set to Object.class).
     *
     * @return new wildcard with no bounds
     */
    public static WildcardType newUnboundedWildcard() {
        return new WildcardTypeImpl(new Type[]{ Object.class }, new Type[0]);
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Type[] bounds;
        if (lowerBounds.length > 0) {
            bounds = lowerBounds;
            sb.append("? super ");
        } else if (!CommonTypeUtils.hasExplicitUpperBound(this)) {
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
