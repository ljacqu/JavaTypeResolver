package ch.jalu.typeresolver.numbers;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Objects;

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
    public ValueRangeImpl(@Nullable T min, @Nullable T max,
                          @Nullable BigDecimal minAsBigDecimal, @Nullable BigDecimal maxAsBigDecimal,
                          boolean supportsDecimals, boolean hasInfinityAndNaN) {
        if (Objects.isNull(min) != Objects.isNull(minAsBigDecimal)) {
            throw new IllegalArgumentException("min and minAsBigDecimal must both be null or not null, but found: min="
                + min + ", minAsBigDecimal=" + minAsBigDecimal);
        }
        if (Objects.isNull(max) != Objects.isNull(maxAsBigDecimal)) {
            throw new IllegalArgumentException("max and maxAsBigDecimal must both be null or not null, but found: max="
                + max + ", maxAsBigDecimal=" + maxAsBigDecimal);
        }

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
}
