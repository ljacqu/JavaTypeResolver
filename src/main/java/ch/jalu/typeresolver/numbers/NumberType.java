package ch.jalu.typeresolver.numbers;

import java.util.Optional;

/**
 * Utility for a type that represents numerical values, e.g. to convert a number to another type or to query some
 * data about the values the type can represent.
 * <p>
 * Standard implementations for basic Java number types are available at
 * {@link StandardNumberType} and {@link MoreNumberTypes}.
 *
 * @param <N>
 * @see StandardNumberType
 * @see NumberTypes#from(Class)
 */
public interface NumberType<N> {

    /**
     * @return the type this instance describes and can convert to
     */
    Class<N> getType();

    /**
     * Returns a value of {@code this} type that is the closest to the given number. The implementations of this library
     * can convert every instantiable {@link Number} class of the JDK (e.g. Integer, AtomicLong, BigDecimal, LongAdder).
     * An IllegalArgumentException is thrown if the number type is unknown.
     * <br>If the given number exceeds the value range of {@code this} type, the minimum or maximum value is returned.
     * Other special cases:<ul>
     *  <li>Float and Double {@code NaN} is converted to 0 if the type does not support the NaN value.</li>
     *  <li>Negative infinity is converted to the type's minimum value if it does not support infinity;
     *      positive infinity converts to the maximum value in such cases.</li>
     *  <li>When converting to BigDecimal or BigInteger, 0 is used for infinity because no minimum or maximum value
     *      exists for these types.</li>
     * </ul>
     * <p>
     * Examples from {@link StandardNumberType}:<pre>{@code
     *   T_INTEGER.convertToBounds(new BigDecimal("1234.6")); // returns `1234`
     *   T_DOUBLE.convertToBounds(new AtomicLong(-800)); // returns `-800.0`
     *   T_BYTE.convertToBounds(200); // returns `(byte) 127`, the max value of Byte
     *   T_SHORT.convertToBounds(Double.NEGATIVE_INFINITY); // returns `(short) -32768`, the min value of Short
     *   T_BIG_DECIMAL.convertToBounds(Double.NEGATIVE_INFINITY); // returns `0`
     *   T_LONG.convertToBounds(Double.NEGATIVE_INFINITY); // returns `Long.MIN_VALUE`
     * }</pre>
     * <p>
     * To catch NaN and infinity before this method, it is possible to use
     * {@link ValueRangeComparison#getErrorForNonFiniteValue(Number)}.
     * <p>
     * Other conversion methods on this interface are {@link #convertIfNoLossOfMagnitude} and {@link #convertUnsafe}.
     *
     * @param numberToConvert the number to convert (not null)
     * @return value of this type that is closest to the given number, never null
     */
    N convertToBounds(Number numberToConvert);

    /**
     * Converts the number to {@code this} type only if no loss of magnitude and meaning occurs, otherwise returns
     * an empty optional. For example, {@code BYTE.convertIfNoLossOfMagnitude(200)} returns an empty Optional (as 200
     * exceeds the max value of the Byte type, 127), while {@code BYTE.convertIfNoLossOfMagnitude(12)} would return
     * an Optional with {@code (byte) 12} as value.
     * <p>
     * Examples:<pre>{@code
     *   T_SHORT.convertIfNoLossOfMagnitude(40); // Optional.of((short) 40)
     *   T_SHORT.convertIfNoLossOfMagnitude(100_000L); // Optional.empty()
     *   T_FLOAT.convertIfNoLossOfMagnitude(Double.NaN); // Optional.of(Float.NaN)
     *   T_LONG.convertIfNoLossOfMagnitude(Double.NaN); // Optional.empty()
     * }</pre>
     * <p>
     * Implementations by this library support all instantiable {@link Number} classes of the JDK as input. An
     * {@link IllegalArgumentException} is thrown for other types.
     * 
     * @param number the number to potentially convert (not null)
     * @return optional with the value converted to this type, or empty if it cannot be represented in this type
     */
    default Optional<N> convertIfNoLossOfMagnitude(Number number) {
        if (compareToValueRange(number) == ValueRangeComparison.WITHIN_RANGE) {
            return Optional.of(convertUnsafe(number));
        }
        return Optional.empty();
    }

    /**
     * Converts the given number to {@code this} type without considering underflow or overflow. Among primitive types,
     * this method behaves like casting one type to another. For example, {@code byte b = (byte) 200}
     * assigns a value of -56 to {@code b} due to overflow. Similarly, {@code BYTE.convertUnsafe(200)} returns -56.
     * <p>
     * This method never returns null. When {@code this} type is a BigDecimal or BigInteger, NaN and infinity
     * values from Float and Double are converted to 0. For instance,
     * {@code BIG_DECIMAL.convertUnsafe(Double.POSITIVE_INFINITY)} returns {@code BigDecimal.ZERO}.
     * For other types, the conversion follows the default behavior of Java: e.g.,
     * {@code BYTE.convertUnsafe(Double.NaN)} = {@code (byte) Double.NaN} = {@code (byte) 0}.
     * <p>
     * Use {@link #convertToBounds} to convert values that are beyond {@code this} type's range to the nearest possible
     * value, or {@link #convertIfNoLossOfMagnitude} to only convert the number if it can be represented by the type.
     * <p>
     * Some implementations by this library throw an {@link IllegalArgumentException} if a number is provided that is
     * not of an instantiable {@link Number} class of the JDK.
     *
     * <p>Examples:
     * <pre>{@code
     *   T_BYTE.convertUnsafe(-255); // returns (byte) 1, due to underflow
     *   T_LONG.convertUnsafe(200); // returns 200L
     *   T_FLOAT.convertUnsafe(Double.POSITIVE_INFINITY); // returns Float.POSITIVE_INFINITY
     *   T_INTEGER.convertUnsafe(Double.POSITIVE_INFINITY); // returns Integer.MAX_VALUE
     *   T_BIG_INTEGER.convertUnsafe(Double.POSITIVE_INFINITY); // returns BigInteger.ZERO
     * }</pre>
     *
     * @param number the number to cast (not null)
     * @return the number cast to this type, never null
     */
    N convertUnsafe(Number number);

    /**
     * Returns information about the value range this type can represent.
     * 
     * @return this type's range of possible values
     */
    ValueRange<N> getValueRange();

    /**
     * Compares the given value to this type's value range, indicating whether it can be represented by this type
     * without loss of magnitude, or why it cannot. Use {@link #convertToBounds} if you want to convert the number
     * to this type and use the closest possible value.
     *
     * <p>The comparison result represents the relationship between the given number and the set of possible values
     * for this type, expressed through the {@link ValueRangeComparison} enum. Examples:
     * <pre>{@code
     *   ValueRangeComparison result1 = T_INTEGER.compareToValueRange(42L);
     *   // result1 = ValueRangeComparison.WITHIN_RANGE
     *
     *   ValueRangeComparison result2 = T_INTEGER.compareToValueRange(Double.POSITIVE_INFINITY);
     *   // result2 = ValueRangeComparison.UNSUPPORTED_POSITIVE_INFINITY
     * }</pre>
     *
     * @param number the number to process
     * @return the comparison result indicating the relationship between the number and this type's value range
     */
    ValueRangeComparison compareToValueRange(Number number);

    /**
     * Convenience method for {@link ValueRange#supportsAllValuesOf}. This method returns true if {@code this} type
     * can represent <b>all</b> values of the {@code other} type without loss of magnitude or meaning. Support for
     * decimals are not considered. See {@link ValueRange#supportsAllValuesOf} for a more detailed description.
     * <p>
     * If this method returns {@code true}, then calling {@link NumberType#compareToValueRange} on {@code this} type
     * with any number of the given {@code other} type will always return {@link ValueRangeComparison#WITHIN_RANGE}.
     * This also means that {@link #convertIfNoLossOfMagnitude} will always return a non-empty Optional.
     * 
     * @param other the number type to check
     * @return true if all values of the given number type can be represented by this type, false otherwise
     * @see ValueRange#supportsAllValuesOf 
     */
    default boolean supportsAllValuesOf(NumberType<?> other) {
        if (other == this) {
            return true;
        }
        return this.getValueRange().supportsAllValuesOf(other.getValueRange());
    }
}
