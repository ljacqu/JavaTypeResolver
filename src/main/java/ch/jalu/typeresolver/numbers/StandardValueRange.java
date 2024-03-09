package ch.jalu.typeresolver.numbers;

import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.function.Function;

/**
 * Provides value ranges of the standard Java number types: byte, short, integer, long, float, double, BigInteger
 * and BigDecimal. Used by {@link StandardNumberType}.
 */
public enum StandardValueRange implements ValueRange {
    // To get the value range with the right type parameter, use NumberType#getValueRange.
    // For example: StandardNumberType.TYPE_INTEGER.getValueRange() returns ValueRange<Integer>

    /** Byte: [-128, 127]. */
    BYTE(Byte.MIN_VALUE, Byte.MAX_VALUE, v -> BigDecimal.valueOf((long) v)),

    /** Short: [-32768, 32767]. */
    SHORT(Short.MIN_VALUE, Short.MAX_VALUE, v -> BigDecimal.valueOf((long) v)),

    /** Integer: [-2147483648, 2147483647]. */
    INTEGER(Integer.MIN_VALUE, Integer.MAX_VALUE, v -> BigDecimal.valueOf((long) v)),

    /** Long: [-9223372036854775808, 9223372036854775807]. */
    LONG(Long.MIN_VALUE, Long.MAX_VALUE, v -> BigDecimal.valueOf((long) v)),

    /** Float: [-3.4028235E38, 3.4028235E38].  */
    FLOAT(-Float.MAX_VALUE, Float.MAX_VALUE, v -> BigDecimal.valueOf((double) v)),

    /** Double: [-1.7976931348623157E308, 1.7976931348623157E308]. */
    DOUBLE(-Double.MAX_VALUE, Double.MAX_VALUE, v -> BigDecimal.valueOf((double) v)),

    /** Big integer: integer with a theoretically infinite range of supported values. */
    BIG_INTEGER,

    /** Big decimal: supports decimals and has a theoretically infinite range of supported values. */
    BIG_DECIMAL;

    private final Number minInOwnType;
    private final BigDecimal min;
    private final Number maxInOwnType;
    private final BigDecimal max;

    /**
     * Constructor for ranges with min and max values.
     *
     * @param min the smallest value of the range
     * @param max the largest value of the range
     * @param toBigDecimal function to convert the min and max values to BigDecimal
     * @param <T> the number type of range
     */
    <T extends Number> StandardValueRange(T min, T max, Function<T, BigDecimal> toBigDecimal) {
        this.minInOwnType = min;
        this.min = toBigDecimal.apply(min);
        this.maxInOwnType = max;
        this.max = toBigDecimal.apply(max);
    }

    /**
     * Constructor for ranges without min and max bounds.
     */
    StandardValueRange() {
        this.minInOwnType = null;
        this.min = null;
        this.maxInOwnType = null;
        this.max = null;
    }

    /**
     * Returns the value range for the type of the given number. This method supports all numbers that can be assigned
     * to an instantiable Number class in the JDK, such as Integer, Byte and BigDecimal. If the number type is unknown,
     * an {@link IllegalArgumentException} is thrown.
     * <p>
     * The returned range should <b>not</b> be used to derive the given number's class, as multiple types can map to
     * the same value range. For example:<pre>{@code
     *   StandardValueRange range1 = detectRangeOrThrow(1L);                  // Result: LONG
     *   StandardValueRange range2 = detectRangeOrThrow(new AtomicLong(2));   // Result: LONG
     *   StandardValueRange range3 = detectRangeOrThrow(new LongAdder());     // Result: LONG
     *
     *   BigDecimal bigDecimalExtension = new BigDecimal("4") { };            // anonymous extension
     *   StandardValueRange range4 = detectRangeOrThrow(bigDecimalExtension); // Result: BIG_DECIMAL
     * }</pre>
     * <p>
     * Use {@link StandardNumberType#fromNumberClass} if you want to match the number strictly by its type.
     *
     * @param number the number to determine the value range for
     * @return the enum entry corresponding to the number's value range
     */
    public static StandardValueRange findRangeOrThrow(Number number) {
        Class<? extends Number> nrClass = NumberTypes.unwrapToStandardNumberType(number).getClass();
        StandardNumberType enumFromClass = StandardNumberType.fromClass(nrClass);

        if (enumFromClass != null) {
            return (StandardValueRange) enumFromClass.getValueRange();
        } else if (number instanceof BigInteger) {
            return BIG_INTEGER;
        } else if (number instanceof BigDecimal) {
            return BIG_DECIMAL;
        }
        throw new IllegalArgumentException("Unsupported number type: " + number.getClass());
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

    @Override
    public boolean hasInfinityAndNaN() {
        switch (this) {
            case FLOAT:
            case DOUBLE:
                return true;
            default:
                return false;
        }
    }

    /**
     * Returns whether this range supports all values of the given range. See {@link ValueRange#supportsAllValuesOf} for
     * details. This method exists as a convenience to avoid unchecked warnings when comparing two
     * {@link StandardValueRange} objects.
     *
     * @param other the range to check with
     * @return true if all values of the given number type can be represented by this type, false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean supportsAllValuesOf(StandardValueRange other) {
        return ValueRange.super.supportsAllValuesOf(other);
    }

    @Override
    public String toString() {
        return "StandardValueRange[" + name() + "]";
    }

    @Override
    public @Nullable BigDecimal getMinValue() {
        return min;
    }

    @Override
    public @Nullable BigDecimal getMaxValue() {
        return max;
    }

    @Override
    public @Nullable Number getMinInOwnType() {
        return minInOwnType;
    }

    @Override
    public @Nullable Number getMaxInOwnType() {
        return maxInOwnType;
    }
}
