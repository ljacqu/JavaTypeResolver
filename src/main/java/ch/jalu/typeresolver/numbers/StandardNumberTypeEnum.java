package ch.jalu.typeresolver.numbers;

import ch.jalu.typeresolver.primitives.Primitives;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntFunction;

public enum StandardNumberTypeEnum implements ValueRange {

    BYTE(Byte.class) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.byteValue();
        }
    },

    SHORT(Short.class) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.shortValue();
        }
    },

    INTEGER(Integer.class) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.intValue();
        }
    },

    LONG(Long.class) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.longValue();
        }
    },

    FLOAT(Float.class) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.floatValue();
        }
    },

    DOUBLE(Double.class) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.doubleValue();
        }
    },

    BIG_INTEGER(BigInteger.class) {
        @Override
        public Number convertUnsafe(Number number) {
            return convertToBigInteger(number, getEnumForObjectTypeOrThrow(number), this::getNumberForCompareToResult);
        }
    },

    BIG_DECIMAL(BigDecimal.class) {
        @Override
        public Number convertUnsafe(Number number) {
            return convertToBigDecimal(number, getEnumForObjectTypeOrThrow(number), this::getNumberForCompareToResult);
        }
    };

    private static final Map<Class<?>, StandardNumberTypeEnum> typeToEnumEntry =
        initReferenceTypeToStandardNumberTypeMap();

    private final Class<? extends Number> type;

    StandardNumberTypeEnum(Class<? extends Number> type) {
        this.type = type;
    }

    public StandardNumberType<?> asNumberType() {
        return StandardNumberType.fromClass(type);
    }

    public Class<? extends Number> getType() {
        return type;
    }

    @Override
    public boolean supportsDecimals() {
        switch (this) {
            case FLOAT:
            case DOUBLE:
            case BIG_DECIMAL:
                return true;
            default:
                return false;
        }
    }

    public abstract Number convertUnsafe(Number number);

    public Optional<Number> convertIfNoLossOfMagnitude(Number number) {
        return Optional.ofNullable(convertInternal(number, cr -> null));
    }

    public Number convertToBounds(Number number) {
        return convertInternal(number, this::getNumberForCompareToResult);
    }

    private Number convertInternal(Number number, IntFunction<Number> fnIfOutsideRange) {
        StandardNumberTypeEnum type = getEnumForObjectTypeOrThrow(number);
        if (this.supportsAllValuesOf(type)) {
            return convertUnsafe(number);
        }

        switch (this) {
            case BYTE:
            case SHORT:
            case INTEGER:
                return convertToIntOrSmaller(number, type, fnIfOutsideRange);
            case LONG:
                return convertToLong(number, type, fnIfOutsideRange);
            case FLOAT:
            case DOUBLE:
                return convertToFloatOrDouble(number, type, fnIfOutsideRange);
            case BIG_DECIMAL:
                return convertToBigDecimal(number, type, fnIfOutsideRange);
            case BIG_INTEGER:
                return convertToBigInteger(number, type, fnIfOutsideRange);
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    Number getNumberForCompareToResult(int compareToResult) {
        switch (compareToResult) {
            case 1: // Too large
                return isBigNumberType() ? convertUnsafe(0) : getMaxInOwnType();
            case 2: // NaN
                return convertUnsafe(0);
            case -1: // Too small
                return isBigNumberType() ? convertUnsafe(0) : getMinInOwnType();
            default:
                throw new IllegalStateException("Unexpected value: " + compareToResult);
        }
    }

    private static Number convertToBigDecimal(Number number, StandardNumberTypeEnum numberType,
                                              IntFunction<Number> fnIfOutsideRange) {
        switch (numberType) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
                long lngValue = number.longValue();
                return BigDecimal.valueOf(lngValue);
            case FLOAT:
            case DOUBLE:
                double dblValue = number.doubleValue();
                int compareToForFiniteness = returnCompareToCodeForNonFiniteValues(dblValue);
                return compareToForFiniteness == 0
                    ? BigDecimal.valueOf(dblValue)
                    : fnIfOutsideRange.apply(compareToForFiniteness);
            case BIG_INTEGER:
                return new BigDecimal((BigInteger) number);
            case BIG_DECIMAL:
                return number;
            default:
                throw new IllegalStateException("Unexpected value: " + numberType);
        }
    }

    private static Number convertToBigInteger(Number number, StandardNumberTypeEnum numberType,
                                              IntFunction<Number> fnIfOutsideRange) {
        switch (numberType) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
                long lngValue = number.longValue();
                return BigInteger.valueOf(lngValue);
            case FLOAT:
            case DOUBLE:
                double dblValue = number.doubleValue();
                int compareToForFiniteness = returnCompareToCodeForNonFiniteValues(dblValue);
                return compareToForFiniteness == 0
                    ? BigDecimal.valueOf(dblValue).toBigInteger()
                    : fnIfOutsideRange.apply(compareToForFiniteness);
            case BIG_INTEGER:
                return number;
            case BIG_DECIMAL:
                return ((BigDecimal) number).toBigInteger();
            default:
                throw new IllegalStateException("Unexpected value: " + numberType);
        }
    }

    private Number convertToIntOrSmaller(Number number, StandardNumberTypeEnum numberType,
                                         IntFunction<Number> fnIfOutsideRange) {
        StandardNumberTypeEnum knownRange = numberType;
        int rangeComparison = 0;
        if (!LONG.supportsAllValuesOf(numberType)) {
            rangeComparison = compareToLongRange(number, numberType);
            knownRange = LONG;
        }

        if (rangeComparison == 0) {
            switch (knownRange) {
                case BYTE:
                case SHORT:
                case INTEGER:
                    rangeComparison = compareToRange(number.intValue(), getMinAsIntOrThrow(), getMaxAsIntOrThrow());
                    break;
                case LONG:
                    rangeComparison = compareToRange(number.longValue(), getMinAsIntOrThrow(), getMaxAsIntOrThrow());
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + numberType);
            }
        }
        return rangeComparison == 0 ? convertUnsafe(number) : fnIfOutsideRange.apply(rangeComparison);
    }

    private Number convertToLong(Number number, StandardNumberTypeEnum numberType,
                                 IntFunction<Number> fnIfOutsideRange) {
        int comparisonToLongRange = compareToLongRange(number, numberType);
        return comparisonToLongRange == 0 ? convertUnsafe(number) : fnIfOutsideRange.apply(comparisonToLongRange);
    }

    private static int compareToLongRange(Number number, StandardNumberTypeEnum numberType) {
        switch (numberType) {
            case FLOAT:
            case DOUBLE:
                return LongValueRange.compareToRange(number.doubleValue());
            case BIG_INTEGER:
            case BIG_DECIMAL:
                BigDecimal bigDecimal = convertBigNumberToBigDecimal(number);
                return LongValueRange.compareToRange(bigDecimal);
            default:
                throw new IllegalStateException("Unexpected value: " + numberType);
        }
    }

    private Number convertToFloatOrDouble(Number number, StandardNumberTypeEnum numberType,
                                          IntFunction<Number> fnIfOutsideRange) {
        int rangeComparison;
        if (this == FLOAT && numberType == DOUBLE) {
            double doubleValue = number.doubleValue();
            rangeComparison = Double.isInfinite(doubleValue)
                ? 0
                : compareToRange(number.doubleValue(), -Float.MAX_VALUE, Float.MAX_VALUE);
        } else {
            switch (numberType) {
                case BIG_INTEGER:
                case BIG_DECIMAL:
                    BigDecimal bigDecimal = convertBigNumberToBigDecimal(number);
                    rangeComparison = compareToRange(bigDecimal, getMinValue(), getMaxValue());
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + numberType);
            }
        }

        return rangeComparison == 0
            ? convertUnsafe(number)
            : fnIfOutsideRange.apply(rangeComparison);
    }

    private static BigDecimal convertBigNumberToBigDecimal(Number number) {
        if (number instanceof BigDecimal) {
            return (BigDecimal) number;
        } else if (number instanceof BigInteger) {
            return new BigDecimal((BigInteger) number);
        } else {
            throw new IllegalArgumentException("Unexpected argument of type " + number.getClass());
        }
    }

    private static int returnCompareToCodeForNonFiniteValues(double d) {
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

    private static Optional<Integer> compareToCodeIfDoubleNotFinite(double d) {
        if (Double.isFinite(d)) {
            return Optional.empty();
        } else if (d == Double.POSITIVE_INFINITY) {
            return Optional.of(1);
        } else if (d == Double.NEGATIVE_INFINITY) {
            return Optional.of(-1);
        } else { // Double.isNaN(d)
            return Optional.of(2);
        }
    }

    private static int compareToRange(int value, int min, int max) {
        if (value < min) {
            return -1;
        } else if (value > max) {
            return 1;
        }
        return 0;
    }

    private static int compareToRange(long value, int min, int max) {
        if (value < min) {
            return -1;
        } else if (value > max) {
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

    private static int compareToRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) {
            return -1;
        } else if (value.compareTo(max) > 0) {
            return 1;
        }
        return 0;
    }

    private boolean isBigNumberType() {
        return this == BIG_DECIMAL || this == BIG_INTEGER;
    }

    private boolean hasNanAndInfinity() {
        return this == FLOAT || this == DOUBLE;
    }

    public BigDecimal getMinValue() {
        switch (this) {
            case BYTE:
                return BigDecimal.valueOf(Byte.MIN_VALUE);
            case SHORT:
                return BigDecimal.valueOf(Short.MIN_VALUE);
            case INTEGER:
                return BigDecimal.valueOf(Integer.MIN_VALUE);
            case LONG:
                return LongValueRange.LONG_MIN_BD;
            case FLOAT:
                return BigDecimal.valueOf(-Float.MAX_VALUE);
            case DOUBLE:
                return BigDecimal.valueOf(-Double.MAX_VALUE);
            case BIG_INTEGER:
            case BIG_DECIMAL:
                return null;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    private int getMinAsIntOrThrow() {
        switch (this) {
            case BYTE:
                return Byte.MIN_VALUE;
            case SHORT:
                return Short.MIN_VALUE;
            case INTEGER:
                return Integer.MIN_VALUE;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public Number getMinInOwnType() {
        switch (this) {
            case BYTE:
                return Byte.MIN_VALUE;
            case SHORT:
                return Short.MIN_VALUE;
            case INTEGER:
                return Integer.MIN_VALUE;
            case LONG:
                return Long.MIN_VALUE;
            case FLOAT:
                return BigDecimal.valueOf(-Float.MAX_VALUE);
            case DOUBLE:
                return BigDecimal.valueOf(-Double.MAX_VALUE);
            case BIG_INTEGER:
            case BIG_DECIMAL:
                return null;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public BigDecimal getMaxValue() {
        switch (this) {
            case BYTE:
                return BigDecimal.valueOf(Byte.MAX_VALUE);
            case SHORT:
                return BigDecimal.valueOf(Short.MAX_VALUE);
            case INTEGER:
                return BigDecimal.valueOf(Integer.MAX_VALUE);
            case LONG:
                return LongValueRange.LONG_MAX_BD;
            case FLOAT:
                return BigDecimal.valueOf(Float.MAX_VALUE);
            case DOUBLE:
                return BigDecimal.valueOf(Double.MAX_VALUE);
            case BIG_INTEGER:
            case BIG_DECIMAL:
                return null;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    private int getMaxAsIntOrThrow() {
        switch (this) {
            case BYTE:
                return Byte.MAX_VALUE;
            case SHORT:
                return Short.MAX_VALUE;
            case INTEGER:
                return Integer.MAX_VALUE;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public Number getMaxInOwnType() {
        switch (this) {
            case BYTE:
                return Byte.MAX_VALUE;
            case SHORT:
                return Short.MAX_VALUE;
            case INTEGER:
                return Integer.MAX_VALUE;
            case LONG:
                return Long.MAX_VALUE;
            case FLOAT:
                return BigDecimal.valueOf(Float.MAX_VALUE);
            case DOUBLE:
                return BigDecimal.valueOf(Double.MAX_VALUE);
            case BIG_INTEGER:
            case BIG_DECIMAL:
                return null;
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    public boolean isSupersetOrEqualTo(StandardNumberTypeEnum other) {
        switch (this) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
            case FLOAT:
            case DOUBLE:
                return this.ordinal() >= other.ordinal();
            case BIG_INTEGER:
            case BIG_DECIMAL:
                return true;
            default:
                throw new IllegalStateException("Unsupported enum type: " + this);
        }
    }

    public boolean supportsAllValuesOf(StandardNumberTypeEnum other) {
        if (this == other) {
            return true;
        }
        return this.isSupersetOrEqualTo(other) && (this.hasNanAndInfinity() || !other.hasNanAndInfinity());
    }

    public static StandardNumberTypeEnum fromClass(Class<?> clazz) {
        return typeToEnumEntry.get(Primitives.toReferenceType(clazz));
    }

    @Nullable
    public static StandardNumberTypeEnum getEnumForObjectType(Number number) {
        StandardNumberTypeEnum enumFromClass = fromClass(number.getClass());
        if (enumFromClass != null) {
            return enumFromClass;
        } else if (number instanceof BigInteger) {
            return BIG_INTEGER;
        } else if (number instanceof BigDecimal) {
            return BIG_DECIMAL;
        }
        return null;
    }

    public static StandardNumberTypeEnum getEnumForObjectTypeOrThrow(Number number) {
        StandardNumberTypeEnum enumFromClass = fromClass(number.getClass());
        if (enumFromClass != null) {
            return enumFromClass;
        } else if (number instanceof BigInteger) {
            return BIG_INTEGER;
        } else if (number instanceof BigDecimal) {
            return BIG_DECIMAL;
        }
        throw new IllegalArgumentException("Unsupported number argument: " + number.getClass());
    }

    private static Map<Class<?>, StandardNumberTypeEnum> initReferenceTypeToStandardNumberTypeMap() {
        Map<Class<?>, StandardNumberTypeEnum> referenceTypeToNumberType = new HashMap<>();
        referenceTypeToNumberType.put(Byte.class, BYTE);
        referenceTypeToNumberType.put(Short.class, SHORT);
        referenceTypeToNumberType.put(Integer.class, INTEGER);
        referenceTypeToNumberType.put(Long.class, LONG);
        referenceTypeToNumberType.put(Float.class, FLOAT);
        referenceTypeToNumberType.put(Double.class, DOUBLE);
        referenceTypeToNumberType.put(BigInteger.class, BIG_INTEGER);
        referenceTypeToNumberType.put(BigDecimal.class, BIG_DECIMAL);
        return Collections.unmodifiableMap(referenceTypeToNumberType);
    }

    private static final class LongValueRange {

        private static final double LONG_MIN_DOUBLE = (double) Long.MIN_VALUE;
        private static final double LONG_MAX_DOUBLE = (double) Long.MAX_VALUE;
        private static final BigDecimal LONG_MIN_BD = BigDecimal.valueOf(Long.MIN_VALUE);
        private static final BigDecimal LONG_MAX_BD = BigDecimal.valueOf(Long.MAX_VALUE);

        private LongValueRange() {
        }

        static int compareToRange(double value) {
            return compareToCodeIfDoubleNotFinite(value)
                .orElseGet(() -> StandardNumberTypeEnum.compareToRange(value, LONG_MIN_DOUBLE, LONG_MAX_DOUBLE));
        }

        static int compareToRange(BigDecimal value) {
            return StandardNumberTypeEnum.compareToRange(value, LONG_MIN_BD, LONG_MAX_BD);
        }
    }
}
