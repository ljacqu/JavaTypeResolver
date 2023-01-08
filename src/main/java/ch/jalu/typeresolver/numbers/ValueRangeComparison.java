package ch.jalu.typeresolver.numbers;

import java.util.Optional;

/**
 * Result of the comparison of a number with a type's value range.
 */
public enum ValueRangeComparison {

    /**
     * The number is within the value range, i.e. can be represented without loss of magnitude.
     */
    WITHIN_RANGE,

    /**
     * The number is smaller than the minimum value of the range.
     */
    BELOW_MINIMUM,

    /**
     * The number is larger than the maximum value of the range.
     */
    ABOVE_MAXIMUM,

    /**
     * The number is positive infinity, which is not supported by the target type.
     */
    UNSUPPORTED_POSITIVE_INFINITY,

    /**
     * The number is negative infinity, which is not supported by the target type.
     */
    UNSUPPORTED_NEGATIVE_INFINITY,

    /**
     * The number is {@code NaN} (Not a Number), which is not supported by the target type.
     */
    UNSUPPORTED_NAN;

    /**
     * Returns the appropriate "Unsupported" comparison entry if the given number represents a non-finite value
     * from Float or Double. Useful when attempting to convert to a type that has no support for non-finite values.
     *
     * @param number the number to process
     * @return optional with unsupported comparison value if the number is infinity/NaN, when applicable
     */
    public static Optional<ValueRangeComparison> getErrorForNonFiniteValue(Number number) {
        if (number instanceof Double || number instanceof Float) {
            return getErrorForNonFiniteValue(number.doubleValue());
        }
        return Optional.empty();
    }

    /**
     * Returns the appropriate "Unsupported" comparison entry if the given double represents a non-finite value
     * from Float or Double. Useful when attempting to convert to a type that has no support for non-finite values.
     *
     * @param value the number to process
     * @return optional with unsupported comparison value if the number is infinity/NaN, when applicable
     */
    public static Optional<ValueRangeComparison> getErrorForNonFiniteValue(double value) {
        if (Double.isFinite(value)) {
            return Optional.empty();
        } else if (value == Double.POSITIVE_INFINITY) {
            return Optional.of(UNSUPPORTED_POSITIVE_INFINITY);
        } else if (value == Double.NEGATIVE_INFINITY) {
            return Optional.of(UNSUPPORTED_NEGATIVE_INFINITY);
        } else { // Double.isNaN(value)
            return Optional.of(UNSUPPORTED_NAN);
        }
    }

    /**
     * @return true if this entry is due to a too small finite value, or due to unsupported negative infinity;
     *         false otherwise
     */
    public boolean isTooSmall() {
        return this == BELOW_MINIMUM || this == UNSUPPORTED_NEGATIVE_INFINITY;
    }

    /**
     * @return true if this entry is due to a too large finite value, or due to unsupported positive infinity;
     *         false otherwise
     */
    public boolean isTooLarge() {
        return this == ABOVE_MAXIMUM || this == UNSUPPORTED_POSITIVE_INFINITY;
    }

    /**
     * @return true if this entry is an unsupported infinity error, false otherwise
     */
    public boolean isUnsupportedInfinity() {
        return this == UNSUPPORTED_POSITIVE_INFINITY || this == UNSUPPORTED_NEGATIVE_INFINITY;
    }

    /**
     * @return true if this entry is an error due to an unsupported infinity or NaN value, false otherwise
     */
    public boolean isUnsupportedNonFiniteValue() {
        return isUnsupportedInfinity() || this == UNSUPPORTED_NAN;
    }
}
