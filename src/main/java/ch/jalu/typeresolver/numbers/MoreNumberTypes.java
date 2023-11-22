package ch.jalu.typeresolver.numbers;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Contains {@link NumberType} implementations for additional Java number types that aren't represented in
 * {@link StandardNumberType}.
 * <p>
 * Various methods in {@link NumberTypes} combine {@link StandardNumberType} with the number types of this class.
 */
public final class MoreNumberTypes {

    /**
     * Character: [0, 65535]. Does not implement {@link Number} but acts as a number in various ways; for instance,
     * a {@code char} can be implicitly cast to an {@code int}.
     */
    public static final NumberType<Character> CHARACTER = new CharacterNumberType();

    /**
     * AtomicInteger (same value range as {@link StandardNumberType#INTEGER}).
     */
    public static final NumberType<AtomicInteger> ATOMIC_INTEGER =
        new AtomicNumberType<>(AtomicInteger.class, StandardNumberType.TYPE_INTEGER, AtomicInteger::new);

    /**
     * AtomicLong (same value range as {@link StandardNumberType#LONG}).
     */
    public static final NumberType<AtomicLong> ATOMIC_LONG =
        new AtomicNumberType<>(AtomicLong.class, StandardNumberType.TYPE_LONG, AtomicLong::new);

    private MoreNumberTypes() {
    }

    /**
     * Atomic number type implementation: uses a base number type and wraps it in its atomic equivalent.
     *
     * @param <B> the base number type (e.g. Integer)
     * @param <A> the atomic type (e.g. AtomicInteger)
     */
    private static final class AtomicNumberType<B extends Number, A extends Number> implements NumberType<A> {

        private final Class<A> type;
        private final NumberType<B> baseType;
        private final Function<B, A> toAtomicFn;

        AtomicNumberType(Class<A> type, NumberType<B> baseType, Function<B, A> toAtomicFn) {
            this.type = type;
            this.baseType = baseType;
            this.toAtomicFn = toAtomicFn;
        }

        @Override
        public Class<A> getType() {
            return type;
        }

        @Override
        public A convertUnsafe(Number number) {
            B value = baseType.convertUnsafe(number);
            return toAtomicFn.apply(value);
        }

        @Override
        public A convertToBounds(Number numberToConvert) {
            B value = baseType.convertToBounds(numberToConvert);
            return toAtomicFn.apply(value);
        }

        @Override
        public ValueRangeComparison compareToValueRange(Number number) {
            return baseType.compareToValueRange(number);
        }

        @Override
        public ValueRange<A> getValueRange() {
            ValueRange<B> baseValueRange = baseType.getValueRange();
            A atomicMinValue = toAtomicFn.apply(baseValueRange.getMinInOwnType());
            A atomicMaxValue = toAtomicFn.apply(baseValueRange.getMaxInOwnType());
            return new ValueRangeImpl<>(atomicMinValue, atomicMaxValue,
                baseValueRange.getMinValue(), baseValueRange.getMaxValue(), false, false);
        }
    }

    /**
     * Implementation of the {@link Character} "number" type.
     */
    private static class CharacterNumberType implements NumberType<Character> {

        private final int maxValue = Character.MAX_VALUE;
        private final int minValue = Character.MIN_VALUE;

        CharacterNumberType() {
        }

        @Override
        public Class<Character> getType() {
            return Character.class;
        }

        @Override
        public Character convertUnsafe(Number number) {
            return (char) number.intValue();
        }

        @Override
        public ValueRangeComparison compareToValueRange(Number number) {
            ValueRangeComparison rangeComparison = StandardNumberType.TYPE_INTEGER.compareToValueRange(number);
            if (rangeComparison == ValueRangeComparison.WITHIN_RANGE) {
                int intValue = StandardNumberType.TYPE_INTEGER.convertUnsafe(number);
                if (intValue > maxValue) {
                    return ValueRangeComparison.ABOVE_MAXIMUM;
                } else if (intValue < minValue) {
                    return ValueRangeComparison.BELOW_MINIMUM;
                }
            }
            return rangeComparison;
        }

        @Override
        public Character convertToBounds(Number numberToConvert) {
            int result = StandardNumberType.TYPE_INTEGER.convertToBounds(numberToConvert);
            if (result > maxValue) {
                return maxValue;
            } else if (result < minValue) {
                return minValue;
            }
            return (char) result;
        }

        @Override
        public ValueRange<Character> getValueRange() {
            return new ValueRangeImpl<>(Character.MIN_VALUE, Character.MAX_VALUE,
                BigDecimal.valueOf(minValue), BigDecimal.valueOf(maxValue), false, false);
        }
    }
}
