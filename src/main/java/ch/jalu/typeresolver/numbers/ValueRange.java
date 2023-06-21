package ch.jalu.typeresolver.numbers;

import javax.annotation.Nullable;
import java.math.BigDecimal;

/**
 * Value range of a number type.
 *
 * @param <T> number type this range describes
 */
public interface ValueRange<T> {

    /**
     * @return the minimum value this type can represent; null if there is no explicit bound
     */
    @Nullable
    BigDecimal getMinValue();

    /**
     * @return the maximum value this type can represent; null if there is no explicit bound
     */
    @Nullable
    BigDecimal getMaxValue();

    /**
     * @return the smallest finite value the type can represent, in the type itself (null if the type has no limit)
     */
    @Nullable
    T getMinInOwnType();

    /**
     * @return the largest finite value the type can represent, in the type itself (null if the type has no limit)
     */
    @Nullable
    T getMaxInOwnType();

    /**
     * @return true if this value can represent decimals, false otherwise
     */
    boolean supportsDecimals();

    /**
     * Returns whether the type has values for {@code NaN} and infinity, such as Double
     * with {@link Double#POSITIVE_INFINITY}.
     *
     * @return true if the type can represent infinity and NaN
     */
    boolean hasInfinityAndNaN();

    /**
     * Specifies whether {@code this} range can represent <b>all</b> values of the given value range without loss of
     * magnitude. {@link ValueRange#supportsDecimals() Support for decimals} is ignored by this method as this does not
     * constitute a loss of magnitude; as such, {@code BIG_INTEGER.supportsAllValuesOf(BIG_DECIMAL)} returns true.
     * <p>
     * In contrast to {@link #isEqualOrSupersetOf}, this method aims to specify whether <b>every</b> possible value of
     * the {@code other} range can be represented by {@code this} type, including infinity and NaN. Because of this,
     * {@code BIG_INTEGER.getValueRange().supportsAllValuesOf(DOUBLE.getValueRange()} evaluates to false, while
     * comparing the same ranges with {@link #isEqualOrSupersetOf} evaluates to true.
     * <p>
     * Developers comparing two number types can use {@link NumberType#supportsAllValuesOf(NumberType)} as a shortcut
     * to this method.
     *
     * @param other the number type to check
     * @return true if all values of the given number type can be represented by this type, false otherwise
     */
    default boolean supportsAllValuesOf(ValueRange<?> other) {
        return this.isEqualOrSupersetOf(other) && (this.hasInfinityAndNaN() || !other.hasInfinityAndNaN());
    }

    /**
     * Returns whether this value range is equal or larger to the given value range. In other words, it returns
     * true only if all values in the {@code other} range fit into {@code this} range.
     * <p>
     * The comparison is based on whether converting a value from the {@code other} range potentially leads to a loss of
     * magnitude; loss of precision is not considered. {@link #supportsDecimals() Support for decimals} is irrelevant to
     * this method. Similarly, the value ranges are not aware about special values like {@link Double#NaN} and whether
     * they can be represented by the type {@code this} value range is associated with.
     * As such, the value range of BigInteger is considered to be a superset of {@link Double} despite not having
     * decimals and not being able to represent infinity or NaN.
     * <p>
     * Use {@link #supportsAllValuesOf} if you want to specifically check that <b>every</b> value (such as NaN) of a
     * type can be represented.
     *
     * @param other the other range to compare with
     * @return true if this.minValue &lt;= other.minValue and this.maxValue &gt;= other.maxValue
     */
    default boolean isEqualOrSupersetOf(ValueRange<?> other) {
        if (this.getMinValue() != null) {
            if (other.getMinValue() == null || this.getMinValue().compareTo(other.getMinValue()) > 0) {
                return false;
            }
        }
        if (this.getMaxValue() != null) {
            if (other.getMaxValue() == null || this.getMaxValue().compareTo(other.getMaxValue()) < 0) {
                return false;
            }
        }
        return true;
    }
}
