package ch.jalu.typeresolver;

import ch.jalu.typeresolver.numbers.ValueRange;

import java.util.Optional;

public interface NumberType<N> {

    Class<N> getType();

    N convertUnsafe(Number number);

    Optional<N> convertIfNoLossOfMagnitude(Number number);

    ValueRange getValueRange();

    default boolean valueRangeIsEqualOrSupersetOf(NumberType<?> otherType) {
        return this.getValueRange().isEqualOrSupersetOf(otherType.getValueRange());
    }
}
