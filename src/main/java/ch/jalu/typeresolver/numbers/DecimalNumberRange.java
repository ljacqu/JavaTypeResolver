package ch.jalu.typeresolver.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.function.IntFunction;

enum DecimalNumberRange implements ConvertingValueRange {

    FLOAT(-Float.MAX_VALUE, Float.MAX_VALUE),

    DOUBLE(-Double.MAX_VALUE, Double.MAX_VALUE);

    private final BigDecimal minValue;
    private final BigDecimal maxValue;

    DecimalNumberRange(double minValue, double maxValue) {
        this.minValue = BigDecimal.valueOf(minValue);
        this.maxValue = BigDecimal.valueOf(maxValue);
    }

    @Override
    public BigDecimal getMinValue() {
        return minValue;
    }

    @Override
    public BigDecimal getMaxValue() {
        return maxValue;
    }

    @Override
    public boolean supportsDecimals() {
        return true;
    }

    @Override
    public Optional<Number> convertIfNoLossOfMagnitude(Number number) {
         return Optional.ofNullable(convertRangeAware(number, i -> null));
    }

    @Override
    public Number convertToBounds(Number number) {
        IntFunction<Number> clampToRangeFn;
        if (this == DOUBLE) {
            clampToRangeFn = i -> i == 1 ? Double.MAX_VALUE : -Double.MAX_VALUE;
        } else { // this == FLOAT
            clampToRangeFn = i -> i == 1 ? Float.MAX_VALUE : -Float.MAX_VALUE;
        }
        return convertRangeAware(number, clampToRangeFn);
    }

    private Number convertRangeAware(Number number, IntFunction<Number> fnIfOutsideRange) {
        if (NonDecimalNumberRange.toNonDecimalNumberRange(number) != null || number instanceof Float) {
            return convertUnsafe(number);
        }
        if (number instanceof Double) {
            double value = (double) number;
            if (this == DOUBLE || !Double.isFinite(value)) {
                return convertUnsafe(value);
            }
            int rangeComparison = compareToRange(value, -Float.MAX_VALUE, Float.MAX_VALUE);
            return rangeComparison == 0 ? convertUnsafe(value) : fnIfOutsideRange.apply(rangeComparison);
        }

        BigDecimal value;
        if (number instanceof BigInteger) {
            value = new BigDecimal((BigInteger) number);
        } else if (number instanceof BigDecimal) {
            value = (BigDecimal) number;
        } else {
            throw new IllegalStateException("Unsupported number type: " + number.getClass());
        }

        int compareToRange = compareToRange(value);
        return compareToRange == 0 ? convertUnsafe(number) : fnIfOutsideRange.apply(compareToRange);
    }

    private int compareToRange(BigDecimal value) {
        if (value.compareTo(minValue) < 0) {
            return -1;
        } else if (value.compareTo(maxValue) > 0) {
            return 1;
        }
        return 0;
    }

    private static int compareToRange(double value, double min, double max) {
        if (value < min) {
            return -1;
        } else if (value > max) {
            return 1;
        }
        return 0;
    }

    @Override
    public Number convertUnsafe(Number number) {
        switch (this) {
            case FLOAT:
                return number.floatValue();
            case DOUBLE:
                return number.doubleValue();
            default:
                throw new IllegalStateException("Unsupported range type: " + this);
        }
    }
}
