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
     * Returns a value of {@code this} type that is the closest to the given number. In other words, if the given number
     * exceeds the value range of {@code this} type, the minimum or maximum value is returned, for example:
     * {@code BYTE.convertToBounds(200)} returns 127.
     * <p>
     * Exception: a value of 0 is returned for BigDecimal and BigInteger types if infinity is encountered,
     * because no minimum or maximum value exists for these types. {@code NaN} is converted as 0.
     *
     * @param number the number to convert
     * @return value of this type that is closest to the given number, never null
     */
    N convertToBounds(Number number);

    /**
     * Converts the number to {@code this} type only if no loss of magnitude and meaning occurs, otherwise returns
     * an empty optional. For example, {@code BYTE.convertIfNoLossOfMagnitude(200)} returns an empty Optional (as 200
     * exceeds the max value of the Byte type, 127), while {@code BYTE.convertIfNoLossOfMagnitude(12)} would return
     * an Optional with {@code (byte) 12} as value.
     * 
     * @param number the number to potentially convert
     * @return optional with the value converted to this type, or empty if it cannot be represented in this type
     */
    default Optional<N> convertIfNoLossOfMagnitude(Number number) {
        if (compareToValueRange(number) == ValueRangeComparison.WITHIN_RANGE) {
            return Optional.of(convertUnsafe(number));
        }
        return Optional.empty();
    }

    /**
     * Converts the given number to this type without considering underflow or overflow. Among primitive types,
     * the behavior of this method is exactly like casting a type to another, for example {@code byte b = (byte) 200}.
     * In this example, a value of -56 is assigned to {@code b} due to overflow. {@code BYTE.convertUnsafe(200)} also
     * yields a value of -56.
     * <p>
     * This method never returns null. When {@code this} type is a BigDecimal or BigInteger, non-finite values from
     * Float and Double are converted to 0, e.g. {@code BIG_DECIMAL.convert(Double.POSITIVE_INFINITY)} returns
     * {@code BigDecimal.ZERO}.
     * <p>
     * Use {@link #convertToBounds} to convert values that are beyond {@code this} type's range to the nearest possible
     * value.
     *
     * @param number the number to cast
     * @return the number cast to this type, never null
     */
    N convertUnsafe(Number number);

    /**
     * Returns information about the available set of values this type has.
     * 
     * @return this type's range of possible values
     */
    ValueRange<N> getValueRange();

    /**
     * Compares the given value to this type's value range, indicating whether it can be represented by this type
     * without loss of magnitude, or why it cannot. Use {@link #convertToBounds} if you want to convert the number
     * to this type and use the closest possible value.
     *
     * @param number the number to process
     * @return comparison result of the number and this type's set of possible values
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
