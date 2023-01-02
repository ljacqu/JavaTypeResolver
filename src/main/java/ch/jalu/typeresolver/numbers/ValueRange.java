package ch.jalu.typeresolver.numbers;

import javax.annotation.Nullable;
import java.math.BigDecimal;

public interface ValueRange {

    @Nullable
    BigDecimal getMinValue();

    @Nullable
    BigDecimal getMaxValue();

    boolean supportsDecimals();

    default boolean isEqualOrSupersetOf(ValueRange other) { // todo remove?
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
