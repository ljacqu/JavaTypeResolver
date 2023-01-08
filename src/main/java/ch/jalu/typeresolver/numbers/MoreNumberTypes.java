package ch.jalu.typeresolver.numbers;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

public final class MoreNumberTypes {

    /** Character: [0, 65535]. */
    public static final CharacterNumberType CHARACTER = new CharacterNumberType();

    public static final NumberType<AtomicInteger> ATOMIC_INTEGER = new AtomicNumberType<>(AtomicInteger.class,
        StandardNumberType.INTEGER, AtomicInteger::new);

    public static final NumberType<AtomicLong> ATOMIC_LONG = new AtomicNumberType<>(AtomicLong.class,
        StandardNumberType.LONG, AtomicLong::new);

    private MoreNumberTypes() {
    }

    /**
     * Unwraps the object to a basic number type and returns the value if the given object is a {@code Number} or
     * {@code Character}.
     * <p>
     * Specifically, this method converts {@link Character} to an int, and it unwraps the number types
     * {@link AtomicInteger}, {@link AtomicLong}, {@link LongAccumulator}, {@link LongAdder}, {@link DoubleAccumulator}
     * and {@link DoubleAdder} to their respective underlying type.
     *
     * @param object the object to unwrap
     * @return the number value the object could be unwrapped to
     */
    @Nullable
    public static Number unwrapToStandardNumberType(@Nullable Object object) {
        if (object instanceof Character) {
            return (int) (Character) object;
        } else if (object instanceof Number) {
            Number number = (Number) object;
            if (object instanceof AtomicInteger) {
                return number.intValue();
            } else if (object instanceof AtomicLong || object instanceof LongAccumulator
                       || object instanceof LongAdder) {
                return number.longValue();
            } else if (object instanceof DoubleAccumulator || object instanceof DoubleAdder) {
                return number.doubleValue();
            }
            return (Number) object;
        }
        return null;
    }

    private static final class AtomicNumberType<B extends Number, A> implements NumberType<A> {

        private final Class<A> type;
        private final StandardNumberType<B> baseType;
        private final Function<B, A> toAtomicFn;

        private AtomicNumberType(Class<A> type, StandardNumberType<B> baseType, Function<B, A> toAtomicFn) {
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
        public A convertToBounds(Number number) {
            B value = baseType.convertToBounds(number);
            return toAtomicFn.apply(value);
        }

        @Override
        public Optional<A> convertIfNoLossOfMagnitude(Number number) {
            return baseType.convertIfNoLossOfMagnitude(number)
                .map(toAtomicFn);
        }

        @Override
        public ValueRange getValueRange() {
            return new ValueRange() {
                @Override
                public BigDecimal getMinValue() {
                    return baseType.getValueRange().getMinValue();
                }

                @Override
                public BigDecimal getMaxValue() {
                    return baseType.getValueRange().getMaxValue();
                }

                @Override
                public boolean supportsDecimals() {
                    return false;
                }
            };
        }
    }

    public static class CharacterNumberType implements NumberType<Character> {

        private final int maxValue = Character.MAX_VALUE;
        private final int minValue = Character.MIN_VALUE;

        protected CharacterNumberType() {
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
        public Character convertToBounds(Number number) {
            int result = StandardNumberType.INTEGER.convertToBounds(number);
            if (result > maxValue) {
                return maxValue;
            } else if (result < minValue) {
                return minValue;
            }
            return (char) result;
        }

        @Override
        public Optional<Character> convertIfNoLossOfMagnitude(Number number) {
            Optional<Integer> intValue = StandardNumberType.INTEGER.convertIfNoLossOfMagnitude(number);
            if (intValue.isPresent() && minValue <= intValue.get() && intValue.get() <= maxValue) {
                return intValue.map(value -> (char) (int) value);
            }
            return Optional.empty();
        }

        @Override
        public ExtendedValueRange<Character> getValueRange() {
            return new ExtendedValueRange<>(Character.MIN_VALUE, Character.MAX_VALUE,
                BigDecimal.valueOf(minValue), BigDecimal.valueOf(maxValue), false, false);
        }
    }
}
