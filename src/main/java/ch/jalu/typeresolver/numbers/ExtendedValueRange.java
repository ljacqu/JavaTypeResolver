package ch.jalu.typeresolver.numbers;

import javax.annotation.Nullable;
import java.math.BigDecimal;

/**
 * Extension of {@link ValueRange} with more data.
 *
 * @param <T> the number type this range describes
 */
public class ExtendedValueRange<T> implements ValueRange {

    private final T minOwnType;
    private final T maxOwnType;
    private final BigDecimal min;
    private final BigDecimal max;
    private final boolean supportsDecimals;
    private final boolean hasInfinityAndNaN;

    public ExtendedValueRange(T min, T max, BigDecimal minAsBigDecimal, BigDecimal maxAsBigDecimal,
                              boolean supportsDecimals, boolean hasInfinityAndNaN) {
        this.minOwnType = min;
        this.maxOwnType = max;
        this.min = minAsBigDecimal;
        this.max = maxAsBigDecimal;
        this.supportsDecimals = supportsDecimals;
        this.hasInfinityAndNaN = hasInfinityAndNaN;
    }

    @Override
    @Nullable
    public BigDecimal getMinValue() {
        return min;
    }

    /**
     * @return the smallest finite value the type can represent, in the type itself (null if the type has no limit)
     */
    @Nullable
    public T getMinInOwnType() {
        return minOwnType;
    }

    @Override
    @Nullable
    public BigDecimal getMaxValue() {
        return max;
    }

    /**
     * @return the largest finite value the type can represent, in the type itself (null if the type has no limit)
     */
    @Nullable
    public T getMaxInOwnType() {
        return maxOwnType;
    }

    @Override
    public boolean supportsDecimals() {
        return supportsDecimals;
    }

    /**
     * Returns whether the type has values for {@code NaN} and infinity, such as Double
     * with {@link Double#POSITIVE_INFINITY}.
     *
     * @return true if the type can represent infinity and NaN
     */
    public boolean hasInfinityAndNaN() {
        return hasInfinityAndNaN;
    }


    // The static creator methods are intentionally package-private as they do not perform enough validation on the
    // method arguments to be made public.

    /**
     * Constructor from (min, max) pairs that are of a "subtype" of Long (Long or another number type with a smaller
     * range than Long). Set to not have support for decimals and no values for infinity or NaN.
     *
     * @param min min value
     * @param max max value
     * @param <T> the number type (Long or subset)
     * @return range with the given min, max
     */
    static <T extends Number> ExtendedValueRange<T> forLongOrSubset(T min, T max) {
        BigDecimal minBd = BigDecimal.valueOf(min.longValue());
        BigDecimal maxBd = BigDecimal.valueOf(max.longValue());
        return new ExtendedValueRange<>(min, max, minBd, maxBd, false, false);
    }

    /**
     * Used for double and float: creates a range from the given values and defines that it supports decimals
     * and has support for infinity and NaN.
     *
     * @param min min value of the range
     * @param max max value of the range
     * @param <T> the number type (Float or Double)
     * @return range for float/double
     */
    static <T extends Number> ExtendedValueRange<T> forDoubleOrFloat(T min, T max) {
        BigDecimal minBd = BigDecimal.valueOf(min.doubleValue());
        BigDecimal maxBd = BigDecimal.valueOf(max.doubleValue());
        return new ExtendedValueRange<>(min, max, minBd, maxBd, true, true);
    }

    /**
     * Creates a range with no min or max bounds. Defines that infinity and NaN are not supported.
     * Used to describe BigInteger and BigDecimal.
     *
     * @param supportsDecimals whether this range supports decimals
     * @param <T> the number type
     * @return the range without any min/max bounds
     */
    static <T> ExtendedValueRange<T> infinite(boolean supportsDecimals) {
        return new ExtendedValueRange<>(null, null, null, null, supportsDecimals, false);
    }
}
