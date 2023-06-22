package ch.jalu.typeresolver.numbers;

import ch.jalu.typeresolver.primitives.Primitives;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static ch.jalu.typeresolver.numbers.RangeComparisonHelper.compareToRange;



/**
 * {@link NumberType} implementations for standard Java number types: byte, short, integer, long, float, double,
 * BigInteger and BigDecimal. Allows to convert from one type to another and to compare the types' value ranges
 * among each other.
 * <p>
 * To use a number type with type safety, please use the constants {@link #TYPE_BYTE}, {@link #TYPE_SHORT} etc.
 *
 * @see StandardNumberType#fromClass(Class)
 * @see NumberType
 */
public enum StandardNumberType implements NumberType {
    // Note: NumberType is implemented without type argument by this enum because of Java type limitations;
    // see the constants below the enum entries (e.g. T_BYTE) to refer to the entries in a type-safe manner.

    /** Byte: [-128, 127]. */
    BYTE(Byte.class, StandardValueRange.BYTE) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.byteValue();
        }
    },

    /** Short: [-32768, 32767]. */
    SHORT(Short.class, StandardValueRange.SHORT) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.shortValue();
        }
    },

    /** Integer: [-2147483648, 2147483647]. */
    INTEGER(Integer.class, StandardValueRange.INTEGER) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.intValue();
        }
    },

    /** Long: [-9223372036854775808, 9223372036854775807]. */
    LONG(Long.class, StandardValueRange.LONG) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.longValue();
        }
    },

    /** Float: [-3.4028235E38, 3.4028235E38].  */
    FLOAT(Float.class, StandardValueRange.FLOAT) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.floatValue();
        }
    },

    /** Double: [-1.7976931348623157E308, 1.7976931348623157E308]. */
    DOUBLE(Double.class, StandardValueRange.DOUBLE) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.doubleValue();
        }
    },

    /** Big integer: integer with a theoretically infinite range of supported values. */
    BIG_INTEGER(BigInteger.class, StandardValueRange.BIG_INTEGER) {
        @Override
        public Number convertUnsafe(Number number) {
            return convertToBigInteger(number, StandardValueRange.findRangeOrThrow(number));
        }
    },

    /** Big decimal: supports decimals and has a theoretically infinite range of supported values. */
    BIG_DECIMAL(BigDecimal.class, StandardValueRange.BIG_DECIMAL) {
        @Override
        public Number convertUnsafe(Number number) {
            return convertToBigDecimal(number, StandardValueRange.findRangeOrThrow(number));
        }
    };

    public static final NumberType<Byte> TYPE_BYTE = (NumberType<Byte>) BYTE;
    public static final NumberType<Short> TYPE_SHORT = (NumberType<Short>) SHORT;
    public static final NumberType<Integer> TYPE_INTEGER = (NumberType<Integer>) INTEGER;
    public static final NumberType<Long> TYPE_LONG = (NumberType<Long>) LONG;
    public static final NumberType<Float> TYPE_FLOAT = (NumberType<Float>) FLOAT;
    public static final NumberType<Double> TYPE_DOUBLE = (NumberType<Double>) DOUBLE;
    public static final NumberType<BigInteger> TYPE_BIG_INTEGER = (NumberType<BigInteger>) BIG_INTEGER;
    public static final NumberType<BigDecimal> TYPE_BIG_DECIMAL = (NumberType<BigDecimal>) BIG_DECIMAL;

    private static final Map<Class<?>, StandardNumberType> typeToEnumEntry =
        initReferenceTypeToStandardNumberTypeMap();

    private final Class<? extends Number> type;
    private final StandardValueRange range;

    /**
     * Constructor.
     *
     * @param type the number type this entry is for
     * @param range range describing the universe of values supported by this type
     * @param <T> the number type
     */
    <T extends Number> StandardNumberType(Class<T> type, StandardValueRange range) {
        this.type = type;
        this.range = range;
    }

    @Override
    public Class<? extends Number> getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The range is always typed the same as this entry's {@link #getType() type}.
     */
    @Override
    public ValueRange<?> getValueRange() {
        return range;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always returns a number of this entry's {@link #getType() type}.
     */
    @Override
    public Optional<Number> convertIfNoLossOfMagnitude(Number number) {
        if (compareToValueRange(number) == ValueRangeComparison.WITHIN_RANGE) {
            return Optional.of(convertUnsafe(number));
        }
        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always returns a number of this entry's {@link #getType() type}.
     */
    @Override
    public Number convertToBounds(Number numberToConvert) {
        ValueRangeComparison rangeComparison = compareToValueRange(numberToConvert);
        if (rangeComparison == ValueRangeComparison.WITHIN_RANGE) {
            return convertUnsafe(numberToConvert);
        }
        return getFallbackForValueOutOfRange(rangeComparison);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Always returns a number of this entry's {@link #getType() type}.
     */
    @Override
    public abstract Number convertUnsafe(Number number);

    @Override
    public ValueRangeComparison compareToValueRange(Number number) {
        StandardValueRange range = StandardValueRange.findRangeOrThrow(number);
        if (this.range.supportsAllValuesOf(range)) {
            return ValueRangeComparison.WITHIN_RANGE;
        }

        switch (this) {
            case BYTE:
            case SHORT:
            case INTEGER:
                return compareToRangeOfIntOrSmaller(number, range);
            case LONG:
                return compareToLongRange(number, range);
            case FLOAT:
            case DOUBLE:
                return compareToFloatOrDoubleRange(number, range);
            case BIG_INTEGER:
            case BIG_DECIMAL:
                return ValueRangeComparison.getErrorForNonFiniteValue(number).orElse(ValueRangeComparison.WITHIN_RANGE);
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    /**
     * Returns the {@link NumberType} instance of this enum that corresponds to the given class, null otherwise.
     * Primitive and reference types are supported (e.g., {@code short.class} and {@code Short.class} both map to
     * {@link #SHORT}).
     * <br>Use {@link #fromNumberClass} to get the same entry as a typed {@link NumberType}.
     *
     * @param clazz the class to find the number type for
     * @return the instance matching the given type, or null if not applicable
     */
    @Nullable
    public static StandardNumberType fromClass(Class<?> clazz) {
        return typeToEnumEntry.get(Primitives.toReferenceType(clazz));
    }

    /**
     * Returns the instance that corresponds to this class, or null if not applicable.
     * Primitive and reference types are supported (e.g., {@code short.class} and {@code Short.class} both map to
     * {@link #TYPE_SHORT}).
     *
     * @param clazz the class to find the number type for
     * @param <T> the number type
     * @return the instance matching the given type, or null if not applicable
     * @see NumberTypes#from
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T extends Number> NumberType<T> fromNumberClass(@Nullable Class<T> clazz) {
        return fromClass(clazz);
    }

    /**
     * Creates a stream of all number types in this enum, typed as {@link NumberType}.
     *
     * @return stream of all number types in this enum
     */
    @SuppressWarnings("unchecked")
    public static Stream<NumberType<? extends Number>> streamThroughAll() {
        return Arrays.stream(values());
    }

    /**
     * Creates a stream of the number type instances that represent one of the six Java number types that
     * are associated with a primitive type: byte, short, int, long, float, and double.
     *
     * @return stream of the number types for the six primitive number types
     */
    public static Stream<NumberType<? extends Number>> streamThroughPrimitiveTypes() {
        return Stream.of(TYPE_BYTE, TYPE_SHORT, TYPE_INTEGER, TYPE_LONG, TYPE_FLOAT, TYPE_DOUBLE);
    }

    @Override
    public String toString() {
        return "StandardNumberType[" + name() + "]";
    }

    /**
     * Returns a fallback number based on the failed range comparison result.
     *
     * @param comparison the failed comparison result for which an appropriate fallback should be returned
     * @return number appropriate for the comparison result (always of the type this entry represents, never null)
     */
    private Number getFallbackForValueOutOfRange(ValueRangeComparison comparison) {
        if (this == BIG_DECIMAL) {
            return BigDecimal.ZERO;
        } else if (this == BIG_INTEGER) {
            return BigInteger.ZERO;
        }

        switch (comparison) {
            case BELOW_MINIMUM:
            case UNSUPPORTED_NEGATIVE_INFINITY:
                return range.getMinInOwnType();
            case ABOVE_MAXIMUM:
            case UNSUPPORTED_POSITIVE_INFINITY:
                return range.getMaxInOwnType();
            case UNSUPPORTED_NAN:
                return convertUnsafe(0);
            default:
                throw new IllegalStateException("Unexpected value: " + comparison);
        }
    }

    /**
     * Conversion <b>to</b> BigDecimal for any of the conversion flavors: {@link #convertUnsafe},
     * {@link #convertToBounds} or {@link #convertIfNoLossOfMagnitude}.
     * <p>
     * All values of the supported Number implementations can be converted to BigDecimal, except for
     * infinity and NaN (float or double).
     *
     * @param number the number to convert
     * @param valueRange the type the number to convert has
     * @return the converted number
     */
    private static BigDecimal convertToBigDecimal(Number number, StandardValueRange valueRange) {
        switch (valueRange) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
                long longValue = number.longValue();
                return BigDecimal.valueOf(longValue);
            case FLOAT:
            case DOUBLE:
                double dblValue = number.doubleValue();
                return ValueRangeComparison.getErrorForNonFiniteValue(dblValue)
                    .map(error -> BigDecimal.ZERO)
                    .orElseGet(() -> BigDecimal.valueOf(dblValue));
            case BIG_INTEGER:
                return new BigDecimal((BigInteger) number);
            case BIG_DECIMAL:
                return (BigDecimal) number;
            default:
                throw new IllegalStateException("Unexpected value: " + valueRange);
        }
    }

    /**
     * Conversion <b>to</b> BigInteger for any of the conversion flavors: {@link #convertUnsafe},
     * {@link #convertToBounds} or {@link #convertIfNoLossOfMagnitude}. Behaves similar to the
     * conversion method that converts to BigDecimal.
     *
     * @param number the number to convert
     * @param range the value range of the number's type to convert
     * @return the converted number
     */
    private static BigInteger convertToBigInteger(Number number, StandardValueRange range) {
        switch (range) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
                long longValue = number.longValue();
                return BigInteger.valueOf(longValue);
            case FLOAT:
            case DOUBLE:
                double dblValue = number.doubleValue();
                return ValueRangeComparison.getErrorForNonFiniteValue(dblValue)
                    .map(error -> BigInteger.ZERO)
                    .orElseGet(() -> BigDecimal.valueOf(dblValue).toBigInteger());
            case BIG_INTEGER:
                return (BigInteger) number;
            case BIG_DECIMAL:
                return ((BigDecimal) number).toBigInteger();
            default:
                throw new IllegalStateException("Unexpected value: " + range);
        }
    }

    private ValueRangeComparison compareToRangeOfIntOrSmaller(Number number, StandardValueRange range) {
        StandardValueRange trustedRange = range;
        ValueRangeComparison rangeComparison = ValueRangeComparison.WITHIN_RANGE;
        // Step 1: If number belongs to something with greater range than LONG, check first that it can be represented
        // as a Long so that we can then check if the long value is within bounds.
        if (!StandardValueRange.LONG.supportsAllValuesOf(range)) {
            rangeComparison = compareToLongRange(number, range);
            trustedRange = StandardValueRange.LONG;
        }

        // Step 2: If within LONG bounds, compare the long or int value with this entry's range.
        if (rangeComparison == ValueRangeComparison.WITHIN_RANGE) {
            switch (trustedRange) {
                // case BYTE: not needed, since all other types' range is a superset of Byte's, meaning that bytes are
                // always converted without any range checks
                case SHORT:
                case INTEGER:
                    rangeComparison = compareToRange(number.intValue(), getMinAsIntOrThrow(), getMaxAsIntOrThrow());
                    break;
                case LONG:
                    rangeComparison = compareToRange(number.longValue(), getMinAsIntOrThrow(), getMaxAsIntOrThrow());
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + range);
            }
        }
        return rangeComparison;
    }

    /**
     * Compares this number's value with the value range of the Long type. Only supports number whose value range
     * is larger than Long.
     *
     * @param number the number to process
     * @param range the number's type
     * @return compareTo int result indicating the relation of the number and the Long value range
     */
    private static ValueRangeComparison compareToLongRange(Number number, StandardValueRange range) {
        switch (range) {
            case FLOAT:
            case DOUBLE:
                return RangeComparisonHelper.compareToLongRange(number.doubleValue());
            case BIG_INTEGER:
            case BIG_DECIMAL:
                BigDecimal value = convertToBigDecimal(number, range);
                return compareToRange(value, LONG.range.getMinValue(), LONG.range.getMaxValue());
            default:
                throw new IllegalStateException("Unexpected value: " + range);
        }
    }

    /*
     * Conversion method called when the type to convert *to* is FLOAT or DOUBLE.
     */
    private ValueRangeComparison compareToFloatOrDoubleRange(Number number, StandardValueRange range) {
        if (this == FLOAT && range == StandardValueRange.DOUBLE) {
            double doubleValue = number.doubleValue();
            return Double.isInfinite(doubleValue)
                ? ValueRangeComparison.WITHIN_RANGE
                : compareToRange(doubleValue, -Float.MAX_VALUE, Float.MAX_VALUE);
        }

        switch (range) {
            case BIG_INTEGER:
            case BIG_DECIMAL:
                BigDecimal bigDecimal = convertToBigDecimal(number, range);
                return compareToRange(bigDecimal, this.range.getMinValue(), this.range.getMaxValue());
            default:
                throw new IllegalStateException("Unexpected value: " + range);
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

    private static Map<Class<?>, StandardNumberType> initReferenceTypeToStandardNumberTypeMap() {
        Map<Class<?>, StandardNumberType> referenceTypeToNumberType = new HashMap<>();
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
}
