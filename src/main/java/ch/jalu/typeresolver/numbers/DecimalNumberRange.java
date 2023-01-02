package ch.jalu.typeresolver.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public enum DecimalNumberRange implements ConvertingValueRange {

    FLOAT(Float.MIN_VALUE, Float.MAX_VALUE),

    DOUBLE(Double.MIN_VALUE, Double.MAX_VALUE);

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
        if (number instanceof Integer || number instanceof Long || number instanceof Float
            || number instanceof AtomicInteger || number instanceof AtomicLong || number instanceof Short
            || number instanceof Byte) {
            return Optional.of(toNumberTypeUnsafe(number));
        }
        if (number instanceof Double) {
            double value = (double) number;
            if (this == DOUBLE || Float.MIN_VALUE <= value && value <= Float.MAX_VALUE) {
                return Optional.of(value);
            }
            return Optional.empty();
        }
        if (number instanceof BigInteger) {
            BigDecimal bd = new BigDecimal((BigInteger) number);
            return minValue.compareTo(bd) <= 0 && maxValue.compareTo(bd) >= 0
                ? Optional.of(number.doubleValue())
                : Optional.empty();
        }
        if (number instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) number;
            return minValue.compareTo(bd) <= 0 && maxValue.compareTo(bd) >= 0
                ? Optional.of(number.doubleValue())
                : Optional.empty();
        }
        return Optional.empty(); // todo throw on unknown?
    }

    @Override
    public boolean isEqualOrSupersetOf2(ConvertingValueRange other) {
        if (other instanceof NonDecimalNumberRange) {
            return true;
        } else if (other instanceof InfiniteNumberRange) {
            return false;
        } else if (other instanceof DecimalNumberRange) {
            return this != FLOAT || other != DOUBLE;
        }
        throw new IllegalStateException("No other range types expected");
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
