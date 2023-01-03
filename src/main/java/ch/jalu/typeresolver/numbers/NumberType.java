package ch.jalu.typeresolver.numbers;

import java.util.Optional;

public interface NumberType<N> {

    Class<N> getType();

    N convertUnsafe(Number number);

    N convertToBounds(Number number);

    Optional<N> convertIfNoLossOfMagnitude(Number number);

    ValueRange getValueRange();

    default boolean valueRangeIsEqualOrSupersetOf(NumberType<?> otherType) {
        return this.getValueRange().isEqualOrSupersetOf(otherType.getValueRange());
    }
}
