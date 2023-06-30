package ch.jalu.typeresolver.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link ValueRangeImpl}.
 */
class ValueRangeImplTest {

    @Test
    void shouldThrowForMinArgMismatches() {
        // given / when
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> new ValueRangeImpl<>(0, 10, null, BigDecimal.TEN, false, false));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> new ValueRangeImpl<>(null, 10, BigDecimal.ZERO, BigDecimal.TEN, false, false));

        // then
        assertThat(ex1.getMessage(), equalTo("min and minAsBigDecimal must both be null or not null, but found: min=0, minAsBigDecimal=null"));
        assertThat(ex2.getMessage(), equalTo("min and minAsBigDecimal must both be null or not null, but found: min=null, minAsBigDecimal=0"));
    }

    @Test
    void shouldThrowForMaxArgMismatches() {
        // given / when
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
            () -> new ValueRangeImpl<>(0, 10, BigDecimal.ZERO, null, false, false));
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class,
            () -> new ValueRangeImpl<>(0, null, BigDecimal.ZERO, BigDecimal.TEN, false, false));

        // then
        assertThat(ex1.getMessage(), equalTo("max and maxAsBigDecimal must both be null or not null, but found: max=10, maxAsBigDecimal=null"));
        assertThat(ex2.getMessage(), equalTo("max and maxAsBigDecimal must both be null or not null, but found: max=null, maxAsBigDecimal=10"));
    }
}