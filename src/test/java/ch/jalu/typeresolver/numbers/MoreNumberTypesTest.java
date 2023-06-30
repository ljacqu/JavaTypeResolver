package ch.jalu.typeresolver.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test for {@link MoreNumberTypes}.
 */
class MoreNumberTypesTest {

    @Test
    void shouldConvertToCharacter() {
        // given / when / then
        new NumberTypeConversionTester<>(MoreNumberTypes.CHARACTER)
            .verifyConversions((short) 1, (char) 1, (char) 1, (char) 1)
            .verifyConversions((short) -23, '\uFFE9', null, Character.MIN_VALUE)
            .verifyConversions(0, (char) 0, (char) 0, (char) 0)
            .verifyConversions(Character.MAX_VALUE + 1, (char) 0, null, Character.MAX_VALUE)
            .verifyConversions(-1, '\uFFFF', null, Character.MIN_VALUE)
            .verifyConversions(-123456L, '\u1DC0', null, Character.MIN_VALUE)
            .verifyConversions(3453158812312L, '\uEA98', null, Character.MAX_VALUE)
            .verifyConversions(72.1414, 'H', 'H', 'H')
            .verifyConversions(-3.3, '\uFFFD', null, Character.MIN_VALUE)
            .verifyConversions(65536.0, (char) 0, null, Character.MAX_VALUE)
            .verifyConversions(new BigDecimal("65"), 'A', 'A', 'A')
            .verifyConversions(new BigDecimal("127000"), '\uF018', null, Character.MAX_VALUE)
            .verifyConversions(new BigDecimal("-98734"), '\u7E52', null, Character.MIN_VALUE);
    }

    @Test
    void shouldConvertToAtomicInteger() {
        // given / when / then
        int intMin = Integer.MIN_VALUE;
        int intMax = Integer.MAX_VALUE;

        new NumberTypeConversionTester<>(MoreNumberTypes.ATOMIC_INTEGER)
            .verifyConversions((short) 1, atomic(1), atomic(1), atomic(1))
            .verifyConversions(-450, atomic(-450), atomic(-450), atomic(-450))
            .verifyConversions(intMin, atomic(intMin), atomic(intMin), atomic(intMin))
            .verifyConversions((double) intMax, atomic(intMax), atomic(intMax), atomic(intMax))
            .verifyConversions(new BigDecimal("1234567890"), atomic(1234567890), atomic(1234567890), atomic(1234567890))
            .verifyConversions(2234567890L, atomic(-2060399406), null, atomic(intMax))
            .verifyConversions(-3210987654f, atomic(intMin), null, atomic(intMin));
    }

    @Test
    void shouldConvertToAtomicLong() {
        // given / when / then
        long longMin = Long.MIN_VALUE;
        long longMax = Long.MAX_VALUE;

        new NumberTypeConversionTester<>(MoreNumberTypes.ATOMIC_LONG)
            .verifyConversions((byte) 8, atomic(8L), atomic(8L), atomic(8L))
            .verifyConversions(-450, atomic(-450L), atomic(-450L), atomic(-450L))
            .verifyConversions(longMin, atomic(longMin), atomic(longMin), atomic(longMin))
            .verifyConversions((double) longMax, atomic(longMax), atomic(longMax), atomic(longMax))
            .verifyConversions(new BigDecimal("123456789011"), atomic(123456789011L), atomic(123456789011L), atomic(123456789011L))
            .verifyConversions(new BigDecimal("4442093872983572342311"), atomic(-3571448780429597145L), null, atomic(longMax))
            .verifyConversions(-32109876566643045983f, atomic(-9223372036854775808L), null, atomic(longMin));
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

    @Test
    void shouldReturnCharacterRange() {
        // given / when
        ValueRange<Character> charRange = MoreNumberTypes.CHARACTER.getValueRange();

        // then
        assertThat(charRange.getMinValue(), equalTo(BigDecimal.ZERO));
        assertThat(charRange.getMinInOwnType(), equalTo(Character.MIN_VALUE));
        assertThat(charRange.getMaxValue(), equalTo(new BigDecimal("65535")));
        assertThat(charRange.getMaxInOwnType(), equalTo(Character.MAX_VALUE));

        assertThat(charRange.supportsDecimals(), equalTo(false));
        assertThat(charRange.hasInfinityAndNaN(), equalTo(false));
    }

    @Test
    void shouldReturnRangesForAtomicIntegerAndAtomicLong() {
        // given / when
        ValueRange<AtomicInteger> atomicIntRange = MoreNumberTypes.ATOMIC_INTEGER.getValueRange();
        ValueRange<AtomicLong> atomicLongRange = MoreNumberTypes.ATOMIC_LONG.getValueRange();

        // then
        assertThat(atomicIntRange.getMinValue(), equalTo(BigDecimal.valueOf(Integer.MIN_VALUE)));
        AtomicInteger minAtomicInt = atomicIntRange.getMinInOwnType();
        assertThat(minAtomicInt.get(), equalTo(Integer.MIN_VALUE));

        assertThat(atomicIntRange.getMaxValue(), equalTo(BigDecimal.valueOf(Integer.MAX_VALUE)));
        AtomicInteger maxAtomicInt = atomicIntRange.getMaxInOwnType();
        assertThat(maxAtomicInt.get(), equalTo(Integer.MAX_VALUE));
        assertThat(atomicIntRange.supportsDecimals(), equalTo(false));

        assertThat(atomicLongRange.getMinValue(), equalTo(BigDecimal.valueOf(Long.MIN_VALUE)));
        AtomicLong minAtomicLong = atomicLongRange.getMinInOwnType();
        assertThat(minAtomicLong.get(), equalTo(Long.MIN_VALUE));

        assertThat(atomicLongRange.getMaxValue(), equalTo(BigDecimal.valueOf(Long.MAX_VALUE)));
        AtomicLong maxAtomicLong = atomicLongRange.getMaxInOwnType();
        assertThat(maxAtomicLong.get(), equalTo(Long.MAX_VALUE));
        assertThat(atomicLongRange.supportsDecimals(), equalTo(false));
    }

    @Test
    void shouldReturnWhetherSupportsAllTypes() {
        // given / when
        Set<NumberType<?>> fullySupportedByCharacter = NumberTypes.streamThroughAll()
            .filter(MoreNumberTypes.CHARACTER::supportsAllValuesOf)
            .collect(Collectors.toSet());
        Set<NumberType<?>> fullySupportedByAtomicInteger = NumberTypes.streamThroughAll()
            .filter(MoreNumberTypes.ATOMIC_INTEGER::supportsAllValuesOf)
            .collect(Collectors.toSet());
        Set<NumberType<?>> fullySupportedByAtomicLong = NumberTypes.streamThroughAll()
            .filter(MoreNumberTypes.ATOMIC_LONG::supportsAllValuesOf)
            .collect(Collectors.toSet());

        // then
        assertThat(fullySupportedByCharacter, contains(MoreNumberTypes.CHARACTER));
        assertThat(fullySupportedByAtomicInteger, equalTo(getExpectedFullySupportedNumberTypesByAtomicType(false)));
        assertThat(fullySupportedByAtomicLong, equalTo(getExpectedFullySupportedNumberTypesByAtomicType(true)));
    }

    private static Set<NumberType<?>> getExpectedFullySupportedNumberTypesByAtomicType(boolean isLong) {
        Set<NumberType<?>> expectedSupportedTypes = new HashSet<>();
        expectedSupportedTypes.add(StandardNumberType.BYTE);
        expectedSupportedTypes.add(StandardNumberType.SHORT);
        expectedSupportedTypes.add(MoreNumberTypes.CHARACTER);
        expectedSupportedTypes.add(StandardNumberType.INTEGER);
        expectedSupportedTypes.add(MoreNumberTypes.ATOMIC_INTEGER);
        if (isLong) {
            expectedSupportedTypes.add(StandardNumberType.LONG);
            expectedSupportedTypes.add(MoreNumberTypes.ATOMIC_LONG);
        }
        return expectedSupportedTypes;
    }

    private static AtomicInteger atomic(int i) {
        return new AtomicInteger(i);
    }

    private static AtomicLong atomic(long l) {
        return new AtomicLong(l);
    }

    private static boolean isEqual(NumberType<?> numberType, Object value, Object expected) {
        if (numberType == MoreNumberTypes.CHARACTER) {
            return Objects.equals(value, expected);
        } else if (numberType == MoreNumberTypes.ATOMIC_INTEGER) {
            if (expected != null) {
                return value instanceof AtomicInteger && ((AtomicInteger) value).intValue() == ((AtomicInteger) expected).intValue();
            }
            return value == null;
        } else if (numberType == MoreNumberTypes.ATOMIC_LONG) {
            if (expected != null) {
                return value instanceof AtomicLong && ((AtomicLong) value).longValue() == ((AtomicLong) expected).longValue();
            }
            return value == null;
        }
        throw new IllegalStateException("Unexpected number type '" + numberType + "'");
    }

    private static final class NumberTypeConversionTester<N> {

        private final NumberType<N> numberType;

        private NumberTypeConversionTester(NumberType<N> numberType) {
            this.numberType = numberType;
        }

        NumberTypeConversionTester<N> verifyConversions(Number input,
                                                        N expectedUnsafe, N expectedSafe, N expectedWithinBounds) {
            assertIsEqualOrThrow(input, numberType.convertUnsafe(input), expectedUnsafe);
            assertIsEqualOrThrow(input, numberType.convertIfNoLossOfMagnitude(input).orElse(null), expectedSafe);
            assertIsEqualOrThrow(input, numberType.convertToBounds(input), expectedWithinBounds);
            return this;
        }

        private void assertIsEqualOrThrow(Number input, Object actual, Object expected) {
            if (!isEqual(numberType, actual, expected)) {
                fail("For input " + input + " (" + input.getClass().getSimpleName()
                    + "), expected '" + expected + "' but got: " + actual);
            }
        }
    }
}