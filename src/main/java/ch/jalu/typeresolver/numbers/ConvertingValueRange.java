package ch.jalu.typeresolver.numbers;

import java.util.Optional;

interface ConvertingValueRange extends ValueRange { // should be a sealed type

    Number convertUnsafe(Number number);

    Number convertToBounds(Number number);

    Optional<Number> convertIfNoLossOfMagnitude(Number number);

}
