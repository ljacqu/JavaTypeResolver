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
     * Exception: a value of 0 is returned for BigDecimal and BigInteger types if {@code NaN} or infinity is encountered
     * because no minimum or maximum value exists for these types.
     *
     * @param number the number to convert
     * @return value of this type that is closest to the given number, never null
     */
    N convertToBounds(Number number);

    Optional<N> convertIfNoLossOfMagnitude(Number number);

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

    ValueRange<N> getValueRange();

    default boolean supportsAllValuesOf(NumberType<?> other) {
        if (other == this) {
            return true;
        }
        return this.getValueRange().isEqualOrSupersetOf(other.getValueRange());
    }

    // todo method that tells whether the number fits into this type?
}
