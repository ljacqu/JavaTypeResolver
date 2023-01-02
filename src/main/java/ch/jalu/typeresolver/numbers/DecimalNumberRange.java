package ch.jalu.typeresolver.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

public enum DecimalNumberRange implements ConvertingValueRange {

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
    public Optional<Number> convertToTypeIfNoLossOfMagnitude(Number number) {
        if (NonDecimalNumberRange.toNonDecimalNumberRange(number) != null || number instanceof Float) {
            return Optional.of(toNumberTypeUnsafe(number));
        }
        if (number instanceof Double) {
            double value = (double) number;
            if (this == DOUBLE || Float.MIN_VALUE <= value && value <= Float.MAX_VALUE || !Double.isFinite(value)) {
                return Optional.of(toNumberTypeUnsafe(value));
            }
            return Optional.empty();
        }
        if (number instanceof BigInteger) {
            BigDecimal bd = new BigDecimal((BigInteger) number);
            return minValue.compareTo(bd) <= 0 && maxValue.compareTo(bd) >= 0
                ? Optional.of(toNumberTypeUnsafe(bd))
                : Optional.empty();
        }
        if (number instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) number;
            return minValue.compareTo(bd) <= 0 && maxValue.compareTo(bd) >= 0
                ? Optional.of(toNumberTypeUnsafe(bd))
                : Optional.empty();
        }
        throw new IllegalStateException("Unsupported number type: " + number.getClass());
    }

    private Number toNumberTypeUnsafe(Number number) {
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
