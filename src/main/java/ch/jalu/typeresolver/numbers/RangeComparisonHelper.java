package ch.jalu.typeresolver.numbers;

import java.math.BigDecimal;

final class RangeComparisonHelper {

    private static final double LONG_MIN_DOUBLE = (double) Long.MIN_VALUE;
    private static final double LONG_MAX_DOUBLE = (double) Long.MAX_VALUE;
    
    private RangeComparisonHelper() {
    }

    static ValueRangeComparison compareToRange(int value, int min, int max) {
        if (value < min) {
            return ValueRangeComparison.BELOW_MINIMUM;
        } else if (value > max) {
            return ValueRangeComparison.ABOVE_MAXIMUM;
        }
        return ValueRangeComparison.WITHIN_RANGE;
    }

    static ValueRangeComparison compareToRange(long value, long min, long max) {
        if (value < min) {
            return ValueRangeComparison.BELOW_MINIMUM;
        } else if (value > max) {
            return ValueRangeComparison.ABOVE_MAXIMUM;
        }
        return ValueRangeComparison.WITHIN_RANGE;
    }

    static ValueRangeComparison compareToRange(double value, double min, double max) {
        if (value < min) {
            return ValueRangeComparison.BELOW_MINIMUM;
        } else if (value > max) {
            return ValueRangeComparison.ABOVE_MAXIMUM;
        }
        return ValueRangeComparison.WITHIN_RANGE;
    }

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
            .orElseGet(() -> compareToRange(value, LONG_MIN_DOUBLE, LONG_MAX_DOUBLE));
    }
}
