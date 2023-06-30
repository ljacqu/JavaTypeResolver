package ch.jalu.typeresolver.numbers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test for {@link ValueRangeComparison}.
 */
class ValueRangeComparisonTest {

    @Test
    void shouldReturnErrorForInfinity() {
        // given
        Number dPosInf = Double.POSITIVE_INFINITY;
        Number dNegInf = Double.NEGATIVE_INFINITY;
        Number fPosInf = Float.POSITIVE_INFINITY;
        Number fNegInf = Float.NEGATIVE_INFINITY;

        // when / then
        assertThat(ValueRangeComparison.getErrorForNonFiniteValue(dPosInf).get(), equalTo(ValueRangeComparison.UNSUPPORTED_POSITIVE_INFINITY));
        assertThat(ValueRangeComparison.getErrorForNonFiniteValue(dNegInf).get(), equalTo(ValueRangeComparison.UNSUPPORTED_NEGATIVE_INFINITY));
        assertThat(ValueRangeComparison.getErrorForNonFiniteValue(fPosInf).get(), equalTo(ValueRangeComparison.UNSUPPORTED_POSITIVE_INFINITY));
        assertThat(ValueRangeComparison.getErrorForNonFiniteValue(fNegInf).get(), equalTo(ValueRangeComparison.UNSUPPORTED_NEGATIVE_INFINITY));

        assertThat(ValueRangeComparison.getErrorForNonFiniteValue(new BigDecimal("23.34")).isPresent(), equalTo(false));
        assertThat(ValueRangeComparison.getErrorForNonFiniteValue((Number) 3).isPresent(), equalTo(false));
        assertThat(ValueRangeComparison.getErrorForNonFiniteValue(999999999999999999999999999999.9).isPresent(), equalTo(false));
        assertThat(ValueRangeComparison.getErrorForNonFiniteValue(-243985734539f).isPresent(), equalTo(false));

        // Note: method is not annotated to explicitly support null...
        assertThat(ValueRangeComparison.getErrorForNonFiniteValue(null).isPresent(), equalTo(false));
    }

    @ParameterizedTest
    @EnumSource(ValueRangeComparison.class)
    void shouldReturnWhetherIsUnsupportedInfinityEntry(ValueRangeComparison comparison) {
        // given
        Set<ValueRangeComparison> infinityErrors = EnumSet.of(ValueRangeComparison.UNSUPPORTED_POSITIVE_INFINITY, ValueRangeComparison.UNSUPPORTED_NEGATIVE_INFINITY);

        // when
        boolean isUnsupportedInfinity = comparison.isUnsupportedInfinity();

        // then
        assertThat(isUnsupportedInfinity, equalTo(infinityErrors.contains(comparison)));
    }

    @ParameterizedTest
    @EnumSource(ValueRangeComparison.class)
    void shouldReturnWhetherIsUnsupportedInfinityOrNaNEntry(ValueRangeComparison comparison) {
        // given
        Set<ValueRangeComparison> nonFiniteValueEntries = EnumSet.of(
            ValueRangeComparison.UNSUPPORTED_POSITIVE_INFINITY, ValueRangeComparison.UNSUPPORTED_NEGATIVE_INFINITY,
            ValueRangeComparison.UNSUPPORTED_NAN);

        // when
        boolean isUnsupportedNonFiniteValue = comparison.isUnsupportedNonFiniteValue();

        // then
        assertThat(isUnsupportedNonFiniteValue, equalTo(nonFiniteValueEntries.contains(comparison)));
    }

    @ParameterizedTest
    @EnumSource(ValueRangeComparison.class)
    void shouldReturnWhetherIsTooLarge(ValueRangeComparison comparison) {
        // given
        Set<ValueRangeComparison> valueTooLargeEntries = EnumSet.of(ValueRangeComparison.ABOVE_MAXIMUM, ValueRangeComparison.UNSUPPORTED_POSITIVE_INFINITY);

        // when
        boolean isTooLarge = comparison.isTooLarge();

        // then
        assertThat(isTooLarge, equalTo(valueTooLargeEntries.contains(comparison)));
    }

    @ParameterizedTest
    @EnumSource(ValueRangeComparison.class)
    void shouldReturnWhetherIsTooSmall(ValueRangeComparison comparison) {
        // given
        Set<ValueRangeComparison> valueTooSmallEntries = EnumSet.of(ValueRangeComparison.BELOW_MINIMUM, ValueRangeComparison.UNSUPPORTED_NEGATIVE_INFINITY);

        // when
        boolean isTooSmall = comparison.isTooSmall();

        // then
        assertThat(isTooSmall, equalTo(valueTooSmallEntries.contains(comparison)));
    }
}