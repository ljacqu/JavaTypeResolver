package ch.jalu.typeresolver.numbers;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntFunction;

enum NonDecimalNumberRange implements ConvertingValueRange {

    /** [-128, 127]. */
    BYTE(Byte.MIN_VALUE, Byte.MAX_VALUE),

    /** [0, 65535]. */
    CHARACTER_AS_INT(Character.MIN_VALUE, Character.MAX_VALUE),

    /** [-32767, 32768]. */
    SHORT(Short.MIN_VALUE, Short.MAX_VALUE),

    /** [-2147483648, 2147483647]. */
    INTEGER(Integer.MIN_VALUE, Integer.MAX_VALUE),

    /** [-9223372036854775808, 9223372036854775807]. */
    LONG(-1, -1) {
        @Override
        public BigDecimal getMinValue() {
            return LongValueRange.LONG_MIN_BD;
        }

        @Override
        public BigDecimal getMaxValue() {
            return LongValueRange.LONG_MAX_BD;
        }
    };

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

    @Override
    public Number convertUnsafe(Number number) {
        switch (this) {
            case LONG:
                return number.longValue();
            case INTEGER:
            case CHARACTER_AS_INT:
                return number.intValue();
            case SHORT:
                return number.shortValue();
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
            case CHARACTER_AS_INT:
                return otherRange == CHARACTER_AS_INT;
            case BYTE:
                return otherRange == BYTE;
            default:
                throw new IllegalStateException("Unsupported range: " + this);
        }
    }

    @Override
    public Optional<Number> convertIfNoLossOfMagnitude(Number number) {
        Number result = convertInternal(number, i -> null);
        return Optional.ofNullable(result);
    }

    @Override
    public Number convertToBounds(Number number) {
        IntFunction<Number> clampToRange;
        if (this == LONG) {
            clampToRange = i -> {
                switch (i) {
                    case  1: return Long.MAX_VALUE;
                    case  0: return 0L; // happens on NaN from float/double
                    case -1: return Long.MIN_VALUE;
                    default:
                        throw new IllegalStateException("Unexpected range comparison: " + i);
                }
            };
        } else {
            clampToRange = i -> {
                switch (i) {
                    case  1: return convertUnsafe(maxVal);
                    case  0: return convertUnsafe(0); // happens on NaN from float/double
                    case -1: return convertUnsafe(minVal);
                    default:
                        throw new IllegalStateException("Unexpected range comparison: " + i);
                }
            };
        }
        return convertInternal(number, clampToRange);
    }

    private Number convertInternal(Number number, IntFunction<Number> fnIfOutsideRange) {
        NonDecimalNumberRange otherRange = toNonDecimalNumberRange(number);
        if (otherRange != null) {
            return convertFromNumberWithKnownRange(number, otherRange, fnIfOutsideRange);
        }

        int comparisonToLongRange;
        if (number instanceof Double || number instanceof Float) {
            double dbl = number.doubleValue();
            if (Double.isNaN(dbl)) {
                return fnIfOutsideRange.apply(0);
            }
            comparisonToLongRange = LongValueRange.compareToRange(dbl);
        } else if (number instanceof BigInteger) {
            comparisonToLongRange = LongValueRange.compareToRange((BigInteger) number);
        } else if (number instanceof BigDecimal) {
            comparisonToLongRange = LongValueRange.compareToRange((BigDecimal) number);
        } else {
            throw new IllegalStateException("Unsupported number type: " + number.getClass());
        }

        return comparisonToLongRange == 0
            ? convertFromNumberWithKnownRange(number, NonDecimalNumberRange.LONG, fnIfOutsideRange)
            : fnIfOutsideRange.apply(comparisonToLongRange);
    }

    private Number convertFromNumberWithKnownRange(Number number, NonDecimalNumberRange numberRange,
                                                   IntFunction<Number> fnIfOutsideRange) {
        if (this.isSupersetOrEqualTo(numberRange)) {
            return convertUnsafe(number);
        }
        if (numberRange == LONG) {
            long value = number.longValue();
            int rangeComparison = compareToRange(value);
            return rangeComparison == 0 ? convertUnsafe(value) : fnIfOutsideRange.apply(rangeComparison);
        } else {
            int value = number.intValue();
            int rangeComparison = compareToRange(value);
            return rangeComparison == 0 ? convertUnsafe(value) : fnIfOutsideRange.apply(rangeComparison);
        }
    }

    private int compareToRange(int value) {
        if (value < minVal) {
            return -1;
        } else if (value > maxVal) {
            return 1;
        }
        return 0;
    }

    private int compareToRange(long value) {
        if (value < minVal) {
            return -1;
        } else if (value > maxVal) {
            return 1;
        }
        return 0;
    }

    static NonDecimalNumberRange toNonDecimalNumberRange(Number number) {
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

    private static final class LongValueRange {

        private static final double LONG_MIN_DOUBLE = (double) Long.MIN_VALUE;
        private static final double LONG_MAX_DOUBLE = (double) Long.MAX_VALUE;
        private static final BigInteger LONG_MIN_BI = BigInteger.valueOf(Long.MIN_VALUE);
        private static final BigInteger LONG_MAX_BI = BigInteger.valueOf(Long.MAX_VALUE);
        private static final BigDecimal LONG_MIN_BD = new BigDecimal(LONG_MIN_BI);
        private static final BigDecimal LONG_MAX_BD = new BigDecimal(LONG_MAX_BI);

        private LongValueRange() {
        }

        static int compareToRange(double value) {
            if (value == Double.POSITIVE_INFINITY) {
                return 1;
            } else if (value == Double.NEGATIVE_INFINITY) {
                return -1;
            }

            if (value < LONG_MIN_DOUBLE) {
                return -1;
            }
            return (value > LONG_MAX_DOUBLE) ? 1 : 0;
        }

        static int compareToRange(BigInteger bigInteger) {
            if (bigInteger.compareTo(LONG_MIN_BI) < 0) {
                return -1;
            } else if (bigInteger.compareTo(LONG_MAX_BI) > 0) {
                return 1;
            }
            return 0;
        }

        static int compareToRange(BigDecimal value) {
            if (value.compareTo(LONG_MIN_BD) < 0) {
                return -1;
            } else if (value.compareTo(LONG_MAX_BD) > 0) {
                return 1;
            }
            return 0;
        }
    }
}
