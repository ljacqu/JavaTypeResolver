package ch.jalu.typeresolver.numbers;

import org.jetbrains.annotations.Nullable;

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
     * magnitude. Loss of precision is not considered. {@link #supportsDecimals() Support for decimals} is ignored
     * by this method as it does not constitute a loss of magnitude. For example,
     * {@code BIG_INTEGER.supportsAllValuesOf(BIG_DECIMAL)} returns true.
     * <p>
     * In contrast to {@link #isEqualOrSupersetOf}, this method specifies whether <b>every</b> possible value of
     * the {@code other} range can be represented by {@code this} range, including infinity and NaN. Therefore,
     * {@code BIG_INTEGER.getValueRange().supportsAllValuesOf(DOUBLE.getValueRange()} evaluates to false
     * (because BigInteger cannot represent infinity or NaN), while comparing the same ranges with
     * {@link #isEqualOrSupersetOf} evaluates to true.
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
     * Returns whether this value range is equal or larger than the given value range. In other words, it returns
     * true only if all values in the {@code other} range fit into {@code this} range. The comparison is based on the
     * minimum and maximum values of the ranges. The presence or absence of decimals or the ability to represent special
     * values like {@link Double#NaN} is not taken into account.
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
