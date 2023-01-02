package ch.jalu.typeresolver.numbers;

import java.util.Optional;

public interface ConvertingValueRange extends ValueRange { // should be a sealed type

    Optional<Number> convertToTypeIfNoLossOfMagnitude(Number number);

}
