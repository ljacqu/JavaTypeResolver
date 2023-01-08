package ch.jalu.typeresolver.numbers;

import javax.annotation.Nullable;
import java.math.BigDecimal;

/**
 * Simple implementation of {@link ValueRange}.
 *
 * @param <T> the number type this range describes
 */
public class ValueRangeImpl<T> implements ValueRange<T> {

    private final T minOwnType;
    private final T maxOwnType;
    private final BigDecimal min;
    private final BigDecimal max;
    private final boolean supportsDecimals;
    private final boolean hasInfinityAndNaN;

    /**
     * Constructor.
     *
     * @param min the minimum value the type can represent, in its own type
     * @param max the maximum value the type can represent, in its own type
     * @param minAsBigDecimal the minimum value the type can represent, as BigDecimal
     * @param maxAsBigDecimal the maximum value the type can represent, as BigDecimal
     * @param supportsDecimals whether this type can have decimals
     * @param hasInfinityAndNaN whether this type has values for NaN and infinity
     */
    public ValueRangeImpl(T min, T max, BigDecimal minAsBigDecimal, BigDecimal maxAsBigDecimal,
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

    @Override
    @Nullable
    public BigDecimal getMaxValue() {
        return max;
    }

    @Override
    @Nullable
    public T getMinInOwnType() {
        return minOwnType;
    }

    @Override
    @Nullable
    public T getMaxInOwnType() {
        return maxOwnType;
    }

    @Override
    public boolean supportsDecimals() {
        return supportsDecimals;
    }

    @Override
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
    static <T extends Number> ValueRangeImpl<T> forLongOrSubset(T min, T max) {
        BigDecimal minBd = BigDecimal.valueOf(min.longValue());
        BigDecimal maxBd = BigDecimal.valueOf(max.longValue());
        return new ValueRangeImpl<>(min, max, minBd, maxBd, false, false);
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
    static <T extends Number> ValueRangeImpl<T> forDoubleOrFloat(T min, T max) {
        BigDecimal minBd = BigDecimal.valueOf(min.doubleValue());
        BigDecimal maxBd = BigDecimal.valueOf(max.doubleValue());
        return new ValueRangeImpl<>(min, max, minBd, maxBd, true, true);
    }

    /**
     * Creates a range with no min or max bounds. Defines that infinity and NaN are not supported.
     * Used to describe BigInteger and BigDecimal.
     *
     * @param supportsDecimals whether this range supports decimals
     * @param <T> the number type
     * @return the range without any min/max bounds
     */
    static <T> ValueRangeImpl<T> infinite(boolean supportsDecimals) {
        return new ValueRangeImpl<>(null, null, null, null, supportsDecimals, false);
    }
}
