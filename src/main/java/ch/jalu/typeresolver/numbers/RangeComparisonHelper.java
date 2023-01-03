package ch.jalu.typeresolver.numbers;

import java.math.BigDecimal;

final class RangeComparisonHelper {

    private static final double LONG_MIN_DOUBLE = (double) Long.MIN_VALUE;
    private static final double LONG_MAX_DOUBLE = (double) Long.MAX_VALUE;
    
    private RangeComparisonHelper() {
    }

    static int returnCompareToCodeForNonFiniteValues(double d) {
        if (Double.isFinite(d)) {
            return 0;
        } else if (d == Double.POSITIVE_INFINITY) {
            return 1;
        } else if (d == Double.NEGATIVE_INFINITY) {
            return -1;
        } else { // Double.isNaN(d)
            return 2;
        }
    }

    static int compareToRange(int value, int min, int max) {
        if (value < min) {
            return -1;
        } else if (value > max) {
            return 1;
        }
        return 0;
    }

    static int compareToRange(long value, long min, long max) {
        if (value < min) {
            return -1;
        } else if (value > max) {
            return 1;
        }
        return 0;
    }

    static int compareToRange(double value, double min, double max) {
        if (value < min) {
            return -1;
        } else if (value > max) {
            return 1;
        }
        return 0;
    }

    static int compareToRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) {
            return -1;
        } else if (value.compareTo(max) > 0) {
            return 1;
        }
        return 0;
    }

    static int compareToLongRange(double value) {
        int compareToResult = returnCompareToCodeForNonFiniteValues(value);
        return compareToResult == 0 ? compareToRange(value, LONG_MIN_DOUBLE, LONG_MAX_DOUBLE) : compareToResult;
    }
}
