package ch.jalu.typeresolver.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static ch.jalu.typeresolver.numbers.StandardNumberType.T_BIG_DECIMAL;
import static ch.jalu.typeresolver.numbers.StandardNumberType.T_BIG_INTEGER;
import static ch.jalu.typeresolver.numbers.StandardNumberType.T_BYTE;
import static ch.jalu.typeresolver.numbers.StandardNumberType.T_DOUBLE;
import static ch.jalu.typeresolver.numbers.StandardNumberType.T_FLOAT;
import static ch.jalu.typeresolver.numbers.StandardNumberType.T_INTEGER;
import static ch.jalu.typeresolver.numbers.StandardNumberType.T_LONG;
import static ch.jalu.typeresolver.numbers.StandardNumberType.T_SHORT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test for {@link NumberType}.
 */
class NumberTypeTest {

    @Test
    void shouldHaveCorrectJavaDoc_convertToBounds() {
        assertThat(T_INTEGER.convertToBounds(new BigDecimal("1234.6")), equalTo(1234)); // returns 1234
        assertThat(T_DOUBLE.convertToBounds(new AtomicLong(-800)), equalTo(-800.0)); // returns -800.0
        assertThat(T_BYTE.convertToBounds(200), equalTo((byte) 127)); // returns (byte) 127, the max value of Byte
        assertThat(T_SHORT.convertToBounds(Double.NEGATIVE_INFINITY), equalTo((short) -32768));// returns (short) -32768, the min value of Short
        assertThat(T_BIG_DECIMAL.convertToBounds(Double.NEGATIVE_INFINITY), equalTo(BigDecimal.ZERO)); // returns 0
        assertThat(T_LONG.convertToBounds(Double.NEGATIVE_INFINITY), equalTo(Long.MIN_VALUE)); // returns Long.MIN_VALUE
    }

    @Test
    void shouldHaveCorrectJavaDoc_convertIfNoLossOfMagnitude() {
        assertThat(T_SHORT.convertIfNoLossOfMagnitude(40), equalTo(Optional.of((short) 40))); // Optional.of((short) 40)
        assertThat(T_SHORT.convertIfNoLossOfMagnitude(100_000L), equalTo(Optional.empty())); // Optional.empty()
        assertThat(T_FLOAT.convertIfNoLossOfMagnitude(Double.NaN), equalTo(Optional.of(Float.NaN))); // Optional.of(Float.NaN)
        assertThat(T_LONG.convertIfNoLossOfMagnitude(Double.NaN), equalTo(Optional.empty())); // Optional.empty()
    }

    @Test
    void shouldHaveCorrectJavaDoc_convertUnsafe() {
        assertThat(T_BYTE.convertUnsafe(-255), equalTo((byte) 1)); // returns (byte) 1, due to underflow
        assertThat(T_LONG.convertUnsafe(200), equalTo(200L)); // returns 200L
        assertThat(T_FLOAT.convertUnsafe(Double.POSITIVE_INFINITY), equalTo(Float.POSITIVE_INFINITY)); // returns Float.POSITIVE_INFINITY
        assertThat(T_INTEGER.convertUnsafe(Double.POSITIVE_INFINITY), equalTo(Integer.MAX_VALUE)); // returns Integer.MAX_VALUE
        assertThat(T_BIG_INTEGER.convertUnsafe(Double.POSITIVE_INFINITY), equalTo(BigInteger.ZERO)); // returns BigInteger.ZERO
    }
}