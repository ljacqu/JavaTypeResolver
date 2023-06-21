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
 * To use a number type with type safety, please use the constants {@link #T_BYTE}, {@link #T_SHORT} etc.
 *
 * @see StandardNumberTypeEnum#fromClass(Class)
 * @see NumberType
 */
public enum StandardNumberTypeEnum implements NumberType {

    /** Byte: [-128, 127]. */
    BYTE(Byte.class, ValueRangeImpl.forLongOrSubset(Byte.MIN_VALUE, Byte.MAX_VALUE)) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.byteValue();
        }
    },

    /** Short: [-32768, 32767]. */
    SHORT(Short.class, ValueRangeImpl.forLongOrSubset(Short.MIN_VALUE, Short.MAX_VALUE)) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.shortValue();
        }
    },

    /** Integer: [-2147483648, 2147483647]. */
    INTEGER(Integer.class, ValueRangeImpl.forLongOrSubset(Integer.MIN_VALUE, Integer.MAX_VALUE)) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.intValue();
        }
    },

    /** Long: [-9223372036854775808, 9223372036854775807]. */
    LONG(Long.class, ValueRangeImpl.forLongOrSubset(Long.MIN_VALUE, Long.MAX_VALUE)) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.longValue();
        }
    },

    /** Float: [-3.4028235E38, 3.4028235E38].  */
    FLOAT(Float.class, ValueRangeImpl.forDoubleOrFloat(-Float.MAX_VALUE, Float.MAX_VALUE)) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.floatValue();
        }
    },

    /** Double: [-1.7976931348623157E308, 1.7976931348623157E308]. */
    DOUBLE(Double.class, ValueRangeImpl.forDoubleOrFloat(-Double.MAX_VALUE, Double.MAX_VALUE)) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.doubleValue();
        }
    },

    /** Big integer: integer with a theoretically infinite range of supported values. */
    BIG_INTEGER(BigInteger.class, ValueRangeImpl.infinite(false)) {
        @Override
        public Number convertUnsafe(Number number) {
            return convertToBigInteger(number, getRangeOfValueOrThrow(number));
        }
    },

    /** Big decimal: supports decimals and has a theoretically infinite range of supported values. */
    BIG_DECIMAL(BigDecimal.class, ValueRangeImpl.infinite(true)) {
        @Override
        public Number convertUnsafe(Number number) {
            return convertToBigDecimal(number, getRangeOfValueOrThrow(number));
        }
    };

    public static final NumberType<Byte> T_BYTE = (NumberType<Byte>) BYTE;
    public static final NumberType<Short> T_SHORT = (NumberType<Short>) SHORT;
    public static final NumberType<Integer> T_INTEGER = (NumberType<Integer>) INTEGER;
    public static final NumberType<Long> T_LONG = (NumberType<Long>) LONG;
    public static final NumberType<Float> T_FLOAT = (NumberType<Float>) FLOAT;
    public static final NumberType<Double> T_DOUBLE = (NumberType<Double>) DOUBLE;
    public static final NumberType<BigInteger> T_BIG_INTEGER = (NumberType<BigInteger>) BIG_INTEGER;
    public static final NumberType<BigDecimal> T_BIG_DECIMAL = (NumberType<BigDecimal>) BIG_DECIMAL;

    private static final Map<Class<?>, StandardNumberTypeEnum> typeToEnumEntry =
        initReferenceTypeToStandardNumberTypeMap();

    private final Class<? extends Number> type;
    private final ValueRange<? extends Number> range;

    /**
     * Constructor.
     *
     * @param type the number type this entry is for
     * @param range range describing the universe of values supported by this type
     * @param <T> the number type
     */
    <T extends Number> StandardNumberTypeEnum(Class<T> type, ValueRange<T> range) {
        this.type = type;
        this.range = range;
    }

    /**
     * @return the type this instance describes and can convert to (corresponds to {@link NumberType#getType()}
     */
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
    public Number convertToBounds(Number number) {
        ValueRangeComparison rangeComparison = compareToValueRange(number);
        if (rangeComparison == ValueRangeComparison.WITHIN_RANGE) {
            return convertUnsafe(number);
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

    /**
     * Same as {@link NumberType#supportsAllValuesOf}.
     *
     * @param other the type to check whether this instance can represent all its values
     * @return true if this type can represent all values of the given type without loss of magnitude; false otherwise
     */
    public boolean supportsAllValuesOf(StandardNumberTypeEnum other) {
        if (this == other) {
            return true;
        }
        return this.range.supportsAllValuesOf(other.range);
    }

    @Override
    public ValueRangeComparison compareToValueRange(Number number) {
        StandardNumberTypeEnum type = getRangeOfValueOrThrow(number);
        if (this.supportsAllValuesOf(type)) {
            return ValueRangeComparison.WITHIN_RANGE;
        }

        switch (this) {
            case BYTE:
            case SHORT:
            case INTEGER:
                return compareToRangeOfIntOrSmaller(number, type);
            case LONG:
                return compareToLongRange(number, type);
            case FLOAT:
            case DOUBLE:
                return compareToFloatOrDoubleRange(number, type);
            case BIG_INTEGER:
            case BIG_DECIMAL:
                return ValueRangeComparison.getErrorForNonFiniteValue(number).orElse(ValueRangeComparison.WITHIN_RANGE);
            default:
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    @Nullable
    public static StandardNumberTypeEnum fromClass(Class<?> clazz) {
        return typeToEnumEntry.get(Primitives.toReferenceType(clazz));
    }

    /**
     * Returns the instance that corresponds to this class, or null if not applicable.
     *
     * @param clazz the class to find the number type for
     * @param <T> the number type
     * @return the instance matching the desired type, or null if not applicable
     * @see NumberTypes#from
     */
    @Nullable
    public static <T extends Number> NumberType<T> fromNumberClass(@Nullable Class<T> clazz) {
        return fromClass(clazz);
    }

    public static Stream<NumberType<? extends Number>> streamThroughAll() {
        return Arrays.stream(values());
    }

    /**
     * Creates a stream of the number type instances that represent one of the six Java number types that
     * are associated with a primitive type: byte, short, int, long, float, and double.
     *
     * @return stream of the number types for the six primitive number types
     */
    public static Stream<NumberType<?>> streamThroughPrimitiveTypes() {
        return Stream.of(T_BYTE, T_SHORT, T_INTEGER, T_LONG, T_FLOAT, T_DOUBLE);
    }

    /**
     * Returns the enum entry that corresponds to this number's type for <b>reading</b> the value. If no match could be
     * made, an exception is thrown.
     * <p>
     * Unlike {@link #fromClass}, this method also matches extensions of BigInteger and BigDecimal to their respective
     * enum entry, which means that converting numbers with the enum entry returned by this method does not necessarily
     * create numbers of the same class as {@code number}:<code><pre>
     *   BigDecimal bigDecimalExtension = new BigDecimal("20") { }; // anonymous extension
     *   StandardNumberTypeEnum entry = StandardNumberTypeEnum.findEntryForReadingValueOrThrow(bigDecimalExtension);
     *   System.out.println(entry.convertUnsafe(0).getClass().equals(bigDecimalExtension.getClass())); // false
     * </pre></code>
     * <p>
     * This is a somewhat moot point as there is little reason to extend BigInteger or BigDecimal. Do not use this
     * method if you need to produce values that are of the exact same class as the number was given (e.g. when passing
     * a value into a method or setting a value to a field).
     *
     * @implNote For the conversions, we map the number to an entry of this enum only to have a sense of what value
     *           range the number is in. Refer to {@link #getRangeOfValueOrThrow} to see how it's done; if you do this,
     *           it's important to only use the enum entry for its value range—its type and the number argument's class
     *           might be quite different!
     *
     * @param number the number to get the entry for
     * @return enum entry corresponding to the number
     */
    public static StandardNumberTypeEnum findEntryForReadingValueOrThrow(Number number) {
        StandardNumberTypeEnum enumFromClass = fromClass(number.getClass());
        if (enumFromClass != null) {
            return enumFromClass;
        } else if (number instanceof BigInteger) {
            return BIG_INTEGER;
        } else if (number instanceof BigDecimal) {
            return BIG_DECIMAL;
        }
        throw new IllegalArgumentException("Unsupported number type: " + number.getClass());
    }

    @Override
    public String toString() {
        return "StandardNumberTypeEnum[" + type.getSimpleName() + "]";
    }

    private static StandardNumberTypeEnum getRangeOfValueOrThrow(Number number) {
        return findEntryForReadingValueOrThrow(NumberTypes.unwrapToStandardNumberType(number));
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
     * infinity and NaN (float or double). If such a value is encountered, the "outside range" function
     * is called (returns null or BigDecimal zero, depending on conversion flavor).
     *
     * @param number the number to convert
     * @param numberType the type the number to convert has
     * @return the converted number
     */
    private static BigDecimal convertToBigDecimal(Number number, StandardNumberTypeEnum numberType) {
        switch (numberType) {
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
                throw new IllegalStateException("Unexpected value: " + numberType);
        }
    }

    /**
     * Conversion <b>to</b> BigDecimal for any of the conversion flavors: {@link #convertUnsafe},
     * {@link #convertToBounds} or {@link #convertIfNoLossOfMagnitude}. Behaves similar to the
     * conversion method that converts to BigDecimal.
     *
     * @param number the number to convert
     * @param numberType the type the number to convert has
     * @return the converted number
     */
    private static BigInteger convertToBigInteger(Number number, StandardNumberTypeEnum numberType) {
        switch (numberType) {
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
                throw new IllegalStateException("Unexpected value: " + numberType);
        }
    }

    private ValueRangeComparison compareToRangeOfIntOrSmaller(Number number, StandardNumberTypeEnum numberType) {
        StandardNumberTypeEnum trustedRange = numberType;
        ValueRangeComparison rangeComparison = ValueRangeComparison.WITHIN_RANGE;
        // Step 1: If number belongs to something with greater range than LONG, check first that it can be represented
        // as a Long so that we can then check if the long value is within bounds.
        if (!LONG.supportsAllValuesOf(numberType)) {
            rangeComparison = compareToLongRange(number, numberType);
            trustedRange = LONG;
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
                    throw new IllegalStateException("Unexpected value: " + numberType);
            }
        }
        return rangeComparison;
    }

    /**
     * Returns the {@code compareTo}-like result of this number's value with the value range of the Long type.
     * Only supports number whose value range is larger than Long.
     *
     * @param number the number to process
     * @param numberType the number's type
     * @return compareTo int result indicating the relation of the number and the Long value range
     */
    private static ValueRangeComparison compareToLongRange(Number number, StandardNumberTypeEnum numberType) {
        switch (numberType) {
            case FLOAT:
            case DOUBLE:
                return RangeComparisonHelper.compareToLongRange(number.doubleValue());
            case BIG_INTEGER:
            case BIG_DECIMAL:
                BigDecimal value = convertToBigDecimal(number, numberType);
                return compareToRange(value, LONG.range.getMinValue(), LONG.range.getMaxValue());
            default:
                throw new IllegalStateException("Unexpected value: " + numberType);
        }
    }

    /*
     * Conversion method called when the type to convert *to* is FLOAT or DOUBLE.
     */
    private ValueRangeComparison compareToFloatOrDoubleRange(Number number, StandardNumberTypeEnum numberType) {
        ValueRangeComparison rangeComparison;
        if (this == FLOAT && numberType == DOUBLE) {
            double doubleValue = number.doubleValue();
            rangeComparison = Double.isInfinite(doubleValue)
                ? ValueRangeComparison.WITHIN_RANGE
                : compareToRange(doubleValue, -Float.MAX_VALUE, Float.MAX_VALUE);
        } else {
            switch (numberType) {
                case BIG_INTEGER:
                case BIG_DECIMAL:
                    BigDecimal bigDecimal = convertToBigDecimal(number, numberType);
                    rangeComparison = compareToRange(bigDecimal, range.getMinValue(), range.getMaxValue());
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + numberType);
            }
        }

        return rangeComparison;
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
}
