package ch.jalu.typeresolver.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public enum InfiniteNumberRange implements ConvertingValueRange {

    BIG_DECIMAL,

    BIG_INTEGER;

    @Override
    public BigDecimal getMinValue() {
        return null;
    }

    @Override
    public BigDecimal getMaxValue() {
        return null;
    }

    @Override
    public boolean supportsDecimals() {
        return this == BIG_DECIMAL;
    }

    public static BigDecimal toBigDecimal(Number number) {
        if (number instanceof BigInteger) {
            return new BigDecimal((BigInteger) number);
        }
        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        }
        if (number instanceof Integer || number instanceof Long
            || number instanceof AtomicInteger || number instanceof AtomicLong
            || number instanceof Short || number instanceof Byte) {
            long value = number.longValue();
            return BigDecimal.valueOf(value);
        }
        if (number instanceof Double || number instanceof Float) {
            double value = number.doubleValue();
            return BigDecimal.valueOf(value);
        }
        return null; // todo
    }

    public static BigInteger toBigInteger(Number number) {
        if (number instanceof BigInteger) {
            return (BigInteger) number;
        }
        if (number instanceof BigDecimal) {
            return ((BigDecimal) number).unscaledValue();
        }
        // todo extract type checks?
        if (number instanceof Integer || number instanceof Long
                || number instanceof AtomicInteger || number instanceof AtomicLong
                || number instanceof Short || number instanceof Byte) {
            long value = number.longValue();
            return BigInteger.valueOf(value);
        }
        if (number instanceof Double || number instanceof Float) {
            double value = number.doubleValue();
            return new BigInteger(Double.toString(value));
        }
        return null; // todo
    }

    @Override
    public Optional<Number> convertToTypeIfNoLossOfMagnitude(Number number) {
        switch (this) {
            case BIG_DECIMAL:
                return Optional.of(toBigDecimal(number));
            case BIG_INTEGER:
                return Optional.of(toBigInteger(number));
            default:
                throw new IllegalStateException("Unsupported range type: " + this.name());
        }
    }

    @Override
    public boolean isEqualOrSupersetOf2(ConvertingValueRange other) {
        return true;
    }
}
