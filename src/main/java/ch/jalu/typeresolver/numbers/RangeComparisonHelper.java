package ch.jalu.typeresolver.numbers;

import java.math.BigDecimal;

/**
 * Internal helper to inspect value ranges.
 */
final class RangeComparisonHelper {

    private static final double LONG_MIN_AS_DOUBLE = (double) Long.MIN_VALUE;
    private static final double LONG_MAX_AS_DOUBLE = (double) Long.MAX_VALUE;
    
    private RangeComparisonHelper() {
    }

    /**
     * Returns the appropriate comparison result for the value with respect to the min and max.
     *
     * @param value the value to check
     * @param min the minimum value (inclusive) of the range
     * @param max the maximum value (inclusive) of the range
     * @return range comparison result
     */
    static ValueRangeComparison compareToRange(int value, int min, int max) {
        if (value < min) {
            return ValueRangeComparison.BELOW_MINIMUM;
        } else if (value > max) {
            return ValueRangeComparison.ABOVE_MAXIMUM;
        }
        return ValueRangeComparison.WITHIN_RANGE;
    }

    /**
     * Returns the appropriate comparison result for the value with respect to the min and max.
     *
     * @param value the value to check
     * @param min the minimum value (inclusive) of the range
     * @param max the maximum value (inclusive) of the range
     * @return range comparison result
     */
    static ValueRangeComparison compareToRange(long value, long min, long max) {
        if (value < min) {
            return ValueRangeComparison.BELOW_MINIMUM;
        } else if (value > max) {
            return ValueRangeComparison.ABOVE_MAXIMUM;
        }
        return ValueRangeComparison.WITHIN_RANGE;
    }

    /**
     * Returns the appropriate comparison result for the value with respect to the min and max.
     *
     * @param value the value to check
     * @param min the minimum value (inclusive) of the range
     * @param max the maximum value (inclusive) of the range
     * @return range comparison result
     */
    static ValueRangeComparison compareToRange(double value, double min, double max) {
        if (value < min) {
            return ValueRangeComparison.BELOW_MINIMUM;
        } else if (value > max) {
            return ValueRangeComparison.ABOVE_MAXIMUM;
        }
        return ValueRangeComparison.WITHIN_RANGE;
    }

    /**
     * Returns the appropriate comparison result for the value with respect to the min and max.
     *
     * @param value the value to check
     * @param min the minimum value (inclusive) of the range
     * @param max the maximum value (inclusive) of the range
     * @return range comparison result
     */
    static ValueRangeComparison compareToRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) {
            return ValueRangeComparison.BELOW_MINIMUM;
        } else if (value.compareTo(max) > 0) {
            return ValueRangeComparison.ABOVE_MAXIMUM;
        }
        return ValueRangeComparison.WITHIN_RANGE;
    }

    static ValueRangeComparison compareToLongRange(double value) {
        return ValueRangeComparison.getErrorForNonFiniteValue(value)
            .orElseGet(() -> compareToRange(value, LONG_MIN_AS_DOUBLE, LONG_MAX_AS_DOUBLE));
    }
}
