package ch.jalu.typeresolver.numbers;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public final class OtherNumberTypes {

    public static final NumberType<Character> CHARACTER = new CharacterNumberType();

    public static final NumberType<AtomicInteger> ATOMIC_INTEGER = new AtomicNumberType<>(AtomicInteger.class,
        StandardNumberType.INTEGER, AtomicInteger::new);

    public static final NumberType<AtomicLong> ATOMIC_LONG = new AtomicNumberType<>(AtomicLong.class,
        StandardNumberType.LONG, AtomicLong::new);

    private OtherNumberTypes() {
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
            return baseType.getValueRange();
        }
    }

    private static final class CharacterNumberType implements NumberType<Character> {

        private final int maxValue = Character.MAX_VALUE;
        private final int minValue = Character.MIN_VALUE;

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
        public ValueRange getValueRange() {
            return new ValueRange() {

                @Override
                public BigDecimal getMinValue() {
                    return BigDecimal.valueOf(Character.MIN_VALUE);
                }

                @Override
                public BigDecimal getMaxValue() {
                    return BigDecimal.valueOf(Character.MAX_VALUE);
                }

                @Override
                public boolean supportsDecimals() {
                    return false;
                }
            };
        }
    }
}
