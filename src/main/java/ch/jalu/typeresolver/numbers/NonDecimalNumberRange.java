package ch.jalu.typeresolver.numbers;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public enum NonDecimalNumberRange implements ConvertingValueRange {

    /** [-128, 127]. */
    BYTE(Byte.MIN_VALUE, Byte.MAX_VALUE),

    /** [0, 65535]. */
    CHARACTER(Character.MIN_VALUE, Character.MAX_VALUE), // todo character unused

    /** [-32767, 32768]. */
    SHORT(Short.MIN_VALUE, Short.MAX_VALUE),

    /** [-2147483648, 2147483647]. */
    INTEGER(Integer.MIN_VALUE, Integer.MAX_VALUE),

    /** [-9223372036854775808, 9223372036854775807]. */
    LONG(-1, -1);

    private final int minVal;
    private final int maxVal;

    NonDecimalNumberRange(int minVal, int maxVal) {
        this.minVal = minVal;
        this.maxVal = maxVal;
    }

    @Override
    public BigDecimal getMinValue() {
        return BigDecimal.valueOf(minVal);
    }

    @Override
    public BigDecimal getMaxValue() {
        return BigDecimal.valueOf(maxVal);
    }

    @Override
    public boolean supportsDecimals() {
        return false;
    }

    public Number convertToBasicTypeUnsafe(Number number) {
        switch (this) {
            case LONG:
                return number.longValue();
            case INTEGER:
                return number.intValue();
            case SHORT:
                return number.shortValue();
            case CHARACTER:
                throw new UnsupportedOperationException("Should not be called with " + this.name());
            case BYTE:
                return number.byteValue();
            default:
                throw new IllegalStateException("Unsupported range type: " + this);
        }
    }

    boolean isSupersetOrEqualTo(NonDecimalNumberRange otherRange) {
        switch (this) {
            case LONG:
                return true;
            case INTEGER:
                return otherRange != LONG;
            case SHORT:
                return otherRange == SHORT || otherRange == BYTE;
            case CHARACTER:
                return otherRange == CHARACTER;
            case BYTE:
                return otherRange == BYTE;
            default:
                throw new IllegalStateException("Unsupported range: " + this);
        }
    }

    @Override
    public Optional<Number> convertToTypeIfNoLossOfMagnitude(Number number) {
        NonDecimalNumberRange otherRange = toNonDecimalNumberRange(number);
        if (otherRange != null) {
            Number simpleConvertedNumber = convertFromNumberInNonDecimalNumberRange(number, otherRange);
            return Optional.ofNullable(simpleConvertedNumber);
        }

        if (number instanceof BigInteger) {
            BigInteger bi = (BigInteger) number;
            if (LongValueRange.isInRange(bi)) {
                return Optional.ofNullable(convertFromNumberInNonDecimalNumberRange(bi, NonDecimalNumberRange.LONG));
            }
        } else if (number instanceof BigDecimal) {
            BigDecimal bd = (BigDecimal) number;
            if (LongValueRange.isInRange(bd)) {
                return Optional.ofNullable(convertFromNumberInNonDecimalNumberRange(bd, NonDecimalNumberRange.LONG));
            }
        }
        return Optional.empty();
    }

    @Nullable
    private Number convertFromNumberInNonDecimalNumberRange(Number number, NonDecimalNumberRange otherRange) {
        if (this.isSupersetOrEqualTo(otherRange)) {
            return convertToBasicTypeUnsafe(number);
        }
        if (otherRange == LONG) {
            long value = number.longValue();
            return minVal <= value && value <= maxVal ? value : null;
        } else {
            int value = number.intValue();
            return minVal <= value && value <= maxVal ? convertToBasicTypeUnsafe(value) : null;
        }
    }

    private NonDecimalNumberRange toNonDecimalNumberRange(Number number) {
        if (number instanceof Integer || number instanceof AtomicInteger) {
            return INTEGER;
        } else if (number instanceof Long || number instanceof AtomicLong) {
            return LONG;
        } else if (number instanceof Byte) {
            return BYTE;
        } else if (number instanceof Short) {
            return SHORT;
        }
        return null;
    }

    @Override
    public boolean isEqualOrSupersetOf2(ConvertingValueRange other) {
        if (other instanceof InfiniteNumberRange || other instanceof DecimalNumberRange) {
            return false;
        }
        if (other instanceof NonDecimalNumberRange) {
            return isSupersetOrEqualTo((NonDecimalNumberRange) other);
        }
        throw new IllegalStateException("No other range type supported");
    }

    private static final class LongValueRange {

        private static final BigInteger LONG_MIN_BI = BigInteger.valueOf(Long.MIN_VALUE);
        private static final BigInteger LONG_MAX_BI = BigInteger.valueOf(Long.MAX_VALUE);
        private static final BigDecimal LONG_MIN_BD = new BigDecimal(LONG_MIN_BI);
        private static final BigDecimal LONG_MAX_BD = new BigDecimal(LONG_MAX_BI);

        private LongValueRange() {
        }

        static boolean isInRange(BigInteger bigInteger) {
            return LONG_MIN_BI.compareTo(bigInteger) <= 0 && LONG_MAX_BI.compareTo(bigInteger) >= 0;
        }

        static boolean isInRange(BigDecimal bigDecimal) {
            return LONG_MIN_BD.compareTo(bigDecimal) <= 0 && LONG_MAX_BD.compareTo(bigDecimal) >= 0;
        }
    }
}
