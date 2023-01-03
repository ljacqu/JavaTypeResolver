package ch.jalu.typeresolver.numbers;

import java.util.Optional;

public interface NumberType<N> {

    Class<N> getType();

    N convertUnsafe(Number number);

    N convertToBounds(Number number);

    Optional<N> convertIfNoLossOfMagnitude(Number number);

    ValueRange getValueRange();

    default boolean supportsAllValuesOf(NumberType<?> other) {
        if (other == this) {
            return true;
        }
        return this.getValueRange().isEqualOrSupersetOf(other.getValueRange());
    }
}
