package ch.jalu.typeresolver.numbers;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test for {@link OtherNumberTypes}.
 */
class OtherNumberTypesTest {

    @Test
    void shouldHandleNaN() {
        // given
        float fNaN = Float.NaN;
        double dNaN = Double.NaN;

        // when / then
        assertThat(OtherNumberTypes.CHARACTER.convertUnsafe(fNaN), equalTo((char) 0));
        assertThat(OtherNumberTypes.CHARACTER.convertIfNoLossOfMagnitude(fNaN), equalTo(Optional.empty()));
        assertThat(OtherNumberTypes.CHARACTER.convertUnsafe(dNaN), equalTo((char) 0));
        assertThat(OtherNumberTypes.CHARACTER.convertIfNoLossOfMagnitude(dNaN), equalTo(Optional.empty()));

        assertThat(OtherNumberTypes.ATOMIC_INTEGER.convertUnsafe(fNaN).get(), equalTo(0));
        assertThat(OtherNumberTypes.ATOMIC_INTEGER.convertIfNoLossOfMagnitude(fNaN), equalTo(Optional.empty()));
        assertThat(OtherNumberTypes.ATOMIC_INTEGER.convertUnsafe(dNaN).get(), equalTo(0));
        assertThat(OtherNumberTypes.ATOMIC_INTEGER.convertIfNoLossOfMagnitude(dNaN), equalTo(Optional.empty()));

        assertThat(OtherNumberTypes.ATOMIC_LONG.convertUnsafe(fNaN).get(), equalTo(0L));
        assertThat(OtherNumberTypes.ATOMIC_LONG.convertIfNoLossOfMagnitude(fNaN), equalTo(Optional.empty()));
        assertThat(OtherNumberTypes.ATOMIC_LONG.convertUnsafe(dNaN).get(), equalTo(0L));
        assertThat(OtherNumberTypes.ATOMIC_LONG.convertIfNoLossOfMagnitude(dNaN), equalTo(Optional.empty()));
    }

    @Test
    void shouldHandleInfinities() {
        // given
        float fPosInf = Float.POSITIVE_INFINITY;
        float fNegInf = Float.NEGATIVE_INFINITY;
        double dPosInf = Double.POSITIVE_INFINITY;
        double dNegInf = Double.NEGATIVE_INFINITY;

        // when / then
        assertThat(OtherNumberTypes.CHARACTER.convertUnsafe(fPosInf), equalTo(Character.MAX_VALUE));
        assertThat(OtherNumberTypes.CHARACTER.convertIfNoLossOfMagnitude(fPosInf), equalTo(Optional.empty()));
        assertThat(OtherNumberTypes.CHARACTER.convertUnsafe(fNegInf), equalTo(Character.MIN_VALUE));
        assertThat(OtherNumberTypes.CHARACTER.convertIfNoLossOfMagnitude(fNegInf), equalTo(Optional.empty()));
        assertThat(OtherNumberTypes.CHARACTER.convertUnsafe(dPosInf), equalTo(Character.MAX_VALUE));
        assertThat(OtherNumberTypes.CHARACTER.convertIfNoLossOfMagnitude(dPosInf), equalTo(Optional.empty()));
        assertThat(OtherNumberTypes.CHARACTER.convertUnsafe(dNegInf), equalTo(Character.MIN_VALUE));
        assertThat(OtherNumberTypes.CHARACTER.convertIfNoLossOfMagnitude(dNegInf), equalTo(Optional.empty()));

        assertThat(OtherNumberTypes.ATOMIC_INTEGER.convertUnsafe(fPosInf).get(), equalTo(Integer.MAX_VALUE));
        assertThat(OtherNumberTypes.ATOMIC_INTEGER.convertIfNoLossOfMagnitude(fPosInf), equalTo(Optional.empty()));
        assertThat(OtherNumberTypes.ATOMIC_INTEGER.convertUnsafe(fNegInf).get(), equalTo(Integer.MIN_VALUE));
        assertThat(OtherNumberTypes.ATOMIC_INTEGER.convertIfNoLossOfMagnitude(fNegInf), equalTo(Optional.empty()));
        assertThat(OtherNumberTypes.ATOMIC_INTEGER.convertUnsafe(dPosInf).get(), equalTo(Integer.MAX_VALUE));
        assertThat(OtherNumberTypes.ATOMIC_INTEGER.convertIfNoLossOfMagnitude(dPosInf), equalTo(Optional.empty()));
        assertThat(OtherNumberTypes.ATOMIC_INTEGER.convertUnsafe(dNegInf).get(), equalTo(Integer.MIN_VALUE));
        assertThat(OtherNumberTypes.ATOMIC_INTEGER.convertIfNoLossOfMagnitude(dNegInf), equalTo(Optional.empty()));

        assertThat(OtherNumberTypes.ATOMIC_LONG.convertUnsafe(fPosInf).get(), equalTo(Long.MAX_VALUE));
        assertThat(OtherNumberTypes.ATOMIC_LONG.convertIfNoLossOfMagnitude(fPosInf), equalTo(Optional.empty()));
        assertThat(OtherNumberTypes.ATOMIC_LONG.convertUnsafe(fNegInf).get(), equalTo(Long.MIN_VALUE));
        assertThat(OtherNumberTypes.ATOMIC_LONG.convertIfNoLossOfMagnitude(fNegInf), equalTo(Optional.empty()));
        assertThat(OtherNumberTypes.ATOMIC_LONG.convertUnsafe(dPosInf).get(), equalTo(Long.MAX_VALUE));
        assertThat(OtherNumberTypes.ATOMIC_LONG.convertIfNoLossOfMagnitude(dPosInf), equalTo(Optional.empty()));
        assertThat(OtherNumberTypes.ATOMIC_LONG.convertUnsafe(dNegInf).get(), equalTo(Long.MIN_VALUE));
        assertThat(OtherNumberTypes.ATOMIC_LONG.convertIfNoLossOfMagnitude(dNegInf), equalTo(Optional.empty()));
    }
}