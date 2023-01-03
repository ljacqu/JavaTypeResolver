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

import static ch.jalu.typeresolver.numbers.RangeComparisonHelper.compareToRange;
import static ch.jalu.typeresolver.numbers.RangeComparisonHelper.returnCompareToCodeForNonFiniteValues;

/**
 * Represents the standard implementations of {@link Number} and allows to convert from one type to another,
 * among other utilities. See also {@link StandardNumberType}, which is more appropriate if you want to work
 * with a specific type and need type safety.
 *
 * @see StandardNumberTypeEnum#asNumberType()
 * @see StandardNumberTypeEnum#fromClass(Class)
 */
public enum StandardNumberTypeEnum {

    /**
     * Byte type.
     */
    BYTE(Byte.class, ExtendedValueRange.forLongOrSubset(Byte.MIN_VALUE, Byte.MAX_VALUE)) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.byteValue();
        }
    },

    /**
     * Short type.
     */
    SHORT(Short.class, ExtendedValueRange.forLongOrSubset(Short.MIN_VALUE, Short.MAX_VALUE)) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.shortValue();
        }
    },

    /**
     * Integer type.
     */
    INTEGER(Integer.class, ExtendedValueRange.forLongOrSubset(Integer.MIN_VALUE, Integer.MAX_VALUE)) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.intValue();
        }
    },

    /**
     * Long type.
     */
    LONG(Long.class, ExtendedValueRange.forLongOrSubset(Long.MIN_VALUE, Long.MAX_VALUE)) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.longValue();
        }
    },

    /**
     * Float type.
     */
    FLOAT(Float.class, ExtendedValueRange.forDoubleOrFloat(-Float.MAX_VALUE, Float.MAX_VALUE)) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.floatValue();
        }
    },

    /**
     * Double type.
     */
    DOUBLE(Double.class, ExtendedValueRange.forDoubleOrFloat(-Double.MAX_VALUE, Double.MAX_VALUE)) {
        @Override
        public Number convertUnsafe(Number number) {
            return number.doubleValue();
        }
    },

    /**
     * Big integer type.
     */
    BIG_INTEGER(BigInteger.class, ExtendedValueRange.infinite(false)) {
        @Override
        public Number convertUnsafe(Number number) {
            return convertToBigInteger(number, getEnumForObjectTypeOrThrow(number), i -> BigInteger.ZERO);
        }

        @Override
        protected Number convertBoundAware(Number number, IntFunction<Number> numberOutOfBoundsFunction) {
            return convertToBigInteger(number, getEnumForObjectTypeOrThrow(number), numberOutOfBoundsFunction);
        }
    },

    /**
     * Big decimal type.
     */
    BIG_DECIMAL(BigDecimal.class, ExtendedValueRange.infinite(true)) {
        @Override
        public Number convertUnsafe(Number number) {
            return convertToBigDecimal(number, getEnumForObjectTypeOrThrow(number), i -> BigDecimal.ZERO);
        }

        @Override
        protected Number convertBoundAware(Number number, IntFunction<Number> numberOutOfBoundsFunction) {
            return convertToBigDecimal(number, getEnumForObjectTypeOrThrow(number), numberOutOfBoundsFunction);
        }
    };

    private static final Map<Class<?>, StandardNumberTypeEnum> typeToEnumEntry =
        initReferenceTypeToStandardNumberTypeMap();

    private final Class<? extends Number> type;
    private final ExtendedValueRange<? extends Number> range;

    /**
     * Constructor.
     *
     * @param type the number type this entry is for
     * @param range range describing the universe of values supported by this type
     * @param <T> the number type
     */
    <T extends Number> StandardNumberTypeEnum(Class<T> type, ExtendedValueRange<T> range) {
        this.type = type;
        this.range = range;
    }

    public StandardNumberType<?> asNumberType() {
        return StandardNumberType.fromClass(type);
    }

    public ExtendedValueRange<?> getValueRange() {
        return range;
    }

    public Class<? extends Number> getType() {
        return type;
    }

    public abstract Number convertUnsafe(Number number);

    public Optional<Number> convertIfNoLossOfMagnitude(Number number) {
        return Optional.ofNullable(convertBoundAware(number, cr -> null));
    }

    public Number convertToBounds(Number number) {
        return convertBoundAware(number, this::getResultForNonZeroCompareToResult);
    }

    /**
     * Converts a number to {@code this} type safely, invoking the Function argument with the type of overflow
     * if the number's value cannot be represented by {@code this} type.
     * This method returns null only if the {@link IntFunction} returns null, and only returns numbers that are
     * of {@code this} type.
     *
     * @param number the number to convert
     * @param numberOutOfBoundsFunction function that takes an int (1, -1 or 2) and returns a Number of {@code this}
     *                                  type that should be used as result. The argument is explained in
     *                                  {@link #getResultForNonZeroCompareToResult(int)}.
     * @return the converted numbers, some default or null
     */
    protected Number convertBoundAware(Number number, IntFunction<Number> numberOutOfBoundsFunction) {
        StandardNumberTypeEnum type = getEnumForObjectTypeOrThrow(number);
        if (this.supportsAllValuesOf(type)) {
            return convertUnsafe(number);
        }

        switch (this) {
            case BYTE:
            case SHORT:
            case INTEGER:
                return convertToIntOrSmaller(number, type, numberOutOfBoundsFunction);
            case LONG:
                return convertToLong(number, type, numberOutOfBoundsFunction);
            case FLOAT:
            case DOUBLE:
                return convertToFloatOrDouble(number, type, numberOutOfBoundsFunction);
            default:
                // BIG_INTEGER and BIG_DECIMAL override this method, so 'this' cannot be either type here.
                throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    /**
     * Returns a number of {@code this} type that is at the appropriate end of this type's value range
     * as determined by the argument: <ul>
     *   <li>1 to signify that the value was too large for this type</li>
     *   <li>-1 to signify that the value was too small for this type</li>
     *   <li>2 when NaN was encountered but this type does not support it</li>
     * </ul>
     *
     * @param compareToResult the result from the compareTo-like check against this range, based on which an
     *                        appropriate return value should be returned
     * @return number appropriate for the comparison result
     */
    private Number getResultForNonZeroCompareToResult(int compareToResult) {
        if (this == BIG_DECIMAL) {
            return BigDecimal.ZERO;
        } else if (this == BIG_INTEGER) {
            return BigInteger.ZERO;
        }

        switch (compareToResult) {
            case 1: // Too large
                return range.getMaxInOwnType();
            case 2: // NaN
                return convertUnsafe(0);
            case -1: // Too small
                return range.getMinInOwnType();
            default:
                throw new IllegalStateException("Unexpected value: " + compareToResult);
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
     * @param fnIfOutsideRange function providing the value to return if the number to convert was out of range
     * @return the converted number (potentially null in some contexts)
     */
    private static BigDecimal convertToBigDecimal(Number number, StandardNumberTypeEnum numberType,
                                                  IntFunction<Number> fnIfOutsideRange) {
        switch (numberType) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG:
                long longValue = number.longValue();
                return BigDecimal.valueOf(longValue);
            case FLOAT:
            case DOUBLE:
                double doubleValue = number.doubleValue();
                int compareToForFiniteness = returnCompareToCodeForNonFiniteValues(doubleValue);
                return compareToForFiniteness == 0
                    ? BigDecimal.valueOf(doubleValue)
                    : (BigDecimal) fnIfOutsideRange.apply(compareToForFiniteness);
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
     * @param fnIfOutsideRange function providing the value to return if the number to convert was out of range
     * @return the converted number (potentially null in some contexts)
     */
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

    /*
     * Conversion method called when the type to convert *to* is BYTE, SHORT, or INTEGER.
     */
    private Number convertToIntOrSmaller(Number number, StandardNumberTypeEnum numberType,
                                         IntFunction<Number> fnIfOutsideRange) {
        StandardNumberTypeEnum trustedRange = numberType;
        int rangeComparison = 0;
        // Step 1: If number belongs to something with greater range than LONG, check first that it can be represented
        // as a Long so that we can then check if the long value is within bounds.
        if (!LONG.supportsAllValuesOf(numberType)) {
            rangeComparison = compareToLongRange(number, numberType);
            trustedRange = LONG;
        }

        // Step 2: If within LONG bounds, compare the long or int value with this entry's range.
        if (rangeComparison == 0) {
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
        return rangeComparison == 0 ? convertUnsafe(number) : fnIfOutsideRange.apply(rangeComparison);
    }

    private Number convertToLong(Number number, StandardNumberTypeEnum numberType,
                                 IntFunction<Number> fnIfOutsideRange) {
        int comparisonToLongRange = compareToLongRange(number, numberType);
        return comparisonToLongRange == 0 ? convertUnsafe(number) : fnIfOutsideRange.apply(comparisonToLongRange);
    }

    /**
     * Returns the {@code compareTo}-like result of this number's value with the value range of the Long type.
     * Only supports number whose value range is larger than Long.
     *
     * @param number the number to process
     * @param numberType the number's type
     * @return compareTo int result indicating the relation of the number and the Long value range
     *         (cf. {@link #getResultForNonZeroCompareToResult})
     */
    private static int compareToLongRange(Number number, StandardNumberTypeEnum numberType) {
        switch (numberType) {
            case FLOAT:
            case DOUBLE:
                return RangeComparisonHelper.compareToLongRange(number.doubleValue());
            case BIG_INTEGER:
            case BIG_DECIMAL:
                BigDecimal value = convertToBigDecimal(number, numberType, null);
                return RangeComparisonHelper.compareToRange(value, LONG.range.getMinValue(), LONG.range.getMaxValue());
            default:
                throw new IllegalStateException("Unexpected value: " + numberType);
        }
    }

    /*
     * Conversion method called when the type to convert *to* is FLOAT or DOUBLE.
     */
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
                    BigDecimal bigDecimal = convertToBigDecimal(number, numberType, null);
                    rangeComparison = compareToRange(bigDecimal, range.getMinValue(), range.getMaxValue());
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + numberType);
            }
        }

        return rangeComparison == 0
            ? convertUnsafe(number)
            : fnIfOutsideRange.apply(rangeComparison);
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
        return this.isSupersetOrEqualTo(other) && (this.range.hasInfinityAndNaN() || !other.range.hasInfinityAndNaN());
    }

    @Nullable
    public static StandardNumberTypeEnum fromClass(Class<?> clazz) {
        return typeToEnumEntry.get(Primitives.toReferenceType(clazz));
    }

    public static StandardNumberTypeEnum getEnumForObjectTypeOrThrow(Number number) {
        StandardNumberTypeEnum enumFromClass = fromClass(number.getClass());
        if (enumFromClass == null) {
            throw new IllegalArgumentException("Unsupported number argument: " + number.getClass());
        }
        return enumFromClass;
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
