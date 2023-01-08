package ch.jalu.typeresolver.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.DoubleAccumulator;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * Test for {@link MoreNumberTypes}.
 */
class MoreNumberTypesTest {

    @Test
    void shouldUnwrapNumberType() {
        // given
        DoubleAccumulator dblAcc = new DoubleAccumulator((a, b) -> a * b, 1.0);
        dblAcc.accumulate(6);
        dblAcc.accumulate(0.5); // 3

        DoubleAdder dblAdd = new DoubleAdder();
        dblAdd.add(3.14);
        dblAdd.add(10); // 13.14

        LongAccumulator longAcc = new LongAccumulator((a, b) -> a / b, 60L);
        longAcc.accumulate(4L); // 15
        longAcc.accumulate(3L); // 5

        LongAdder longAdd = new LongAdder();
        longAdd.add(20_000L);
        longAdd.add(-7L); // 19_993

        // when / then
        assertThat(MoreNumberTypes.unwrapToStandardNumberType('A'), equalTo(65));
        assertThat(MoreNumberTypes.unwrapToStandardNumberType(new AtomicInteger(33)), equalTo(33));
        assertThat(MoreNumberTypes.unwrapToStandardNumberType(new AtomicLong(-999L)), equalTo(-999L));

        assertThat(MoreNumberTypes.unwrapToStandardNumberType(dblAcc), equalTo(3.0));
        assertThat(MoreNumberTypes.unwrapToStandardNumberType(dblAdd), equalTo(13.14));
        assertThat(MoreNumberTypes.unwrapToStandardNumberType(longAcc), equalTo(5L));
        assertThat(MoreNumberTypes.unwrapToStandardNumberType(longAdd), equalTo(19_993L));

        assertThat(MoreNumberTypes.unwrapToStandardNumberType(19L), equalTo(19L));
        assertThat(MoreNumberTypes.unwrapToStandardNumberType((byte) 5), equalTo((byte) 5));
        assertThat(MoreNumberTypes.unwrapToStandardNumberType(BigDecimal.TEN), equalTo(BigDecimal.TEN));
        assertThat(MoreNumberTypes.unwrapToStandardNumberType(3.1415f), equalTo(3.1415f));

        assertThat(MoreNumberTypes.unwrapToStandardNumberType(null), nullValue());
        assertThat(MoreNumberTypes.unwrapToStandardNumberType("14"), nullValue());
        assertThat(MoreNumberTypes.unwrapToStandardNumberType(false), nullValue());
        assertThat(MoreNumberTypes.unwrapToStandardNumberType(Collections.emptyList()), nullValue());
    }

    @Test
    void shouldConvertToCharacter() {
        // given / when / then
        new NumberTypeConversionTester<>(MoreNumberTypes.CHARACTER)
            .verifyConversions((short) 1, (char) 1, (char) 1, (char) 1)
            .verifyConversions((short) -23, '\uFFE9', null, Character.MIN_VALUE)
            .verifyConversions(0, (char) 0, (char) 0, (char) 0)
            .verifyConversions(Character.MAX_VALUE + 1, (char) 0, null, Character.MAX_VALUE)
            .verifyConversions(-1, '\uFFFF', null, Character.MIN_VALUE)
            .verifyConversions(72.1414, 'H', 'H', 'H')
            .verifyConversions(-3.3, '\uFFFD', null, Character.MIN_VALUE)
            .verifyConversions(65536.0, (char) 0, null, Character.MAX_VALUE)
            .verifyConversions(new BigDecimal("65"), 'A', 'A', 'A')
            .verifyConversions(new BigDecimal("127000"), '\uF018', null, Character.MAX_VALUE)
            .verifyConversions(new BigDecimal("-98734"), '\u7E52', null, Character.MIN_VALUE);
    }

    @Test
    void shouldHandleNaN() {
        // given
        float fNaN = Float.NaN;
        double dNaN = Double.NaN;

        // when / then
        assertThat(MoreNumberTypes.CHARACTER.convertUnsafe(fNaN), equalTo((char) 0));
        assertThat(MoreNumberTypes.CHARACTER.convertIfNoLossOfMagnitude(fNaN), equalTo(Optional.empty()));
        assertThat(MoreNumberTypes.CHARACTER.convertUnsafe(dNaN), equalTo((char) 0));
        assertThat(MoreNumberTypes.CHARACTER.convertIfNoLossOfMagnitude(dNaN), equalTo(Optional.empty()));

        assertThat(MoreNumberTypes.ATOMIC_INTEGER.convertUnsafe(fNaN).get(), equalTo(0));
        assertThat(MoreNumberTypes.ATOMIC_INTEGER.convertIfNoLossOfMagnitude(fNaN), equalTo(Optional.empty()));
        assertThat(MoreNumberTypes.ATOMIC_INTEGER.convertUnsafe(dNaN).get(), equalTo(0));
        assertThat(MoreNumberTypes.ATOMIC_INTEGER.convertIfNoLossOfMagnitude(dNaN), equalTo(Optional.empty()));

        assertThat(MoreNumberTypes.ATOMIC_LONG.convertUnsafe(fNaN).get(), equalTo(0L));
        assertThat(MoreNumberTypes.ATOMIC_LONG.convertIfNoLossOfMagnitude(fNaN), equalTo(Optional.empty()));
        assertThat(MoreNumberTypes.ATOMIC_LONG.convertUnsafe(dNaN).get(), equalTo(0L));
        assertThat(MoreNumberTypes.ATOMIC_LONG.convertIfNoLossOfMagnitude(dNaN), equalTo(Optional.empty()));
    }

    @Test
    void shouldHandleInfinities() {
        // given
        float fPosInf = Float.POSITIVE_INFINITY;
        float fNegInf = Float.NEGATIVE_INFINITY;
        double dPosInf = Double.POSITIVE_INFINITY;
        double dNegInf = Double.NEGATIVE_INFINITY;

        // when / then
        assertThat(MoreNumberTypes.CHARACTER.convertUnsafe(fPosInf), equalTo(Character.MAX_VALUE));
        assertThat(MoreNumberTypes.CHARACTER.convertIfNoLossOfMagnitude(fPosInf), equalTo(Optional.empty()));
        assertThat(MoreNumberTypes.CHARACTER.convertUnsafe(fNegInf), equalTo(Character.MIN_VALUE));
        assertThat(MoreNumberTypes.CHARACTER.convertIfNoLossOfMagnitude(fNegInf), equalTo(Optional.empty()));
        assertThat(MoreNumberTypes.CHARACTER.convertUnsafe(dPosInf), equalTo(Character.MAX_VALUE));
        assertThat(MoreNumberTypes.CHARACTER.convertIfNoLossOfMagnitude(dPosInf), equalTo(Optional.empty()));
        assertThat(MoreNumberTypes.CHARACTER.convertUnsafe(dNegInf), equalTo(Character.MIN_VALUE));
        assertThat(MoreNumberTypes.CHARACTER.convertIfNoLossOfMagnitude(dNegInf), equalTo(Optional.empty()));

        assertThat(MoreNumberTypes.ATOMIC_INTEGER.convertUnsafe(fPosInf).get(), equalTo(Integer.MAX_VALUE));
        assertThat(MoreNumberTypes.ATOMIC_INTEGER.convertIfNoLossOfMagnitude(fPosInf), equalTo(Optional.empty()));
        assertThat(MoreNumberTypes.ATOMIC_INTEGER.convertUnsafe(fNegInf).get(), equalTo(Integer.MIN_VALUE));
        assertThat(MoreNumberTypes.ATOMIC_INTEGER.convertIfNoLossOfMagnitude(fNegInf), equalTo(Optional.empty()));
        assertThat(MoreNumberTypes.ATOMIC_INTEGER.convertUnsafe(dPosInf).get(), equalTo(Integer.MAX_VALUE));
        assertThat(MoreNumberTypes.ATOMIC_INTEGER.convertIfNoLossOfMagnitude(dPosInf), equalTo(Optional.empty()));
        assertThat(MoreNumberTypes.ATOMIC_INTEGER.convertUnsafe(dNegInf).get(), equalTo(Integer.MIN_VALUE));
        assertThat(MoreNumberTypes.ATOMIC_INTEGER.convertIfNoLossOfMagnitude(dNegInf), equalTo(Optional.empty()));

        assertThat(MoreNumberTypes.ATOMIC_LONG.convertUnsafe(fPosInf).get(), equalTo(Long.MAX_VALUE));
        assertThat(MoreNumberTypes.ATOMIC_LONG.convertIfNoLossOfMagnitude(fPosInf), equalTo(Optional.empty()));
        assertThat(MoreNumberTypes.ATOMIC_LONG.convertUnsafe(fNegInf).get(), equalTo(Long.MIN_VALUE));
        assertThat(MoreNumberTypes.ATOMIC_LONG.convertIfNoLossOfMagnitude(fNegInf), equalTo(Optional.empty()));
        assertThat(MoreNumberTypes.ATOMIC_LONG.convertUnsafe(dPosInf).get(), equalTo(Long.MAX_VALUE));
        assertThat(MoreNumberTypes.ATOMIC_LONG.convertIfNoLossOfMagnitude(dPosInf), equalTo(Optional.empty()));
        assertThat(MoreNumberTypes.ATOMIC_LONG.convertUnsafe(dNegInf).get(), equalTo(Long.MIN_VALUE));
        assertThat(MoreNumberTypes.ATOMIC_LONG.convertIfNoLossOfMagnitude(dNegInf), equalTo(Optional.empty()));
    }

    // todo: Test supportsValuesOf method also with the standard number type impls

    private static final class NumberTypeConversionTester<N> {

        private final NumberType<N> numberType;

        private NumberTypeConversionTester(NumberType<N> numberType) {
            this.numberType = numberType;
        }

        NumberTypeConversionTester<N> verifyConversions(Number input,
                                                        N expectedUnsafe, N expectedSafe, N expectedWithinBounds) {
            assertThat(numberType.convertUnsafe(input), equalTo(expectedUnsafe));
            assertThat(numberType.convertIfNoLossOfMagnitude(input), equalTo(Optional.ofNullable(expectedSafe)));
            assertThat(numberType.convertToBounds(input), equalTo(expectedWithinBounds));
            return this;
        }
    }
}