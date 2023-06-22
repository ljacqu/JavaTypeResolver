package ch.jalu.typeresolver.numbers;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import static ch.jalu.typeresolver.numbers.StandardValueRange.findRangeOrThrow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link StandardValueRange}.
 */
class StandardValueRangeTest {

    @Test
    void shouldHaveCorrectExampleInTopComment() {
        // given / when
        ValueRange<Integer> range = StandardNumberType.TYPE_INTEGER.getValueRange();

        // then
        // The real assertion is being able to assign the call to ValueRange<Integer> :)
        assertThat(range, notNullValue());
    }

    @Test
    void shouldHaveMinAndMaxValuesAsCorrectType() {
        for (StandardValueRange range : StandardValueRange.values()) {
            if (range == StandardValueRange.BIG_INTEGER || range == StandardValueRange.BIG_DECIMAL) {
                assertThat(range.getMinInOwnType(), nullValue());
                assertThat(range.getMaxInOwnType(), nullValue());
                assertThat(range.getMinValue(), nullValue());
                assertThat(range.getMaxValue(), nullValue());
            } else {
                StandardNumberType numberType = StandardNumberType.valueOf(range.name());
                assertThat(range.getMinInOwnType(), instanceOf(numberType.getType()));
                assertThat(range.getMaxInOwnType(), instanceOf(numberType.getType()));
                assertThat(range.getMinValue(), notNullValue());
                assertThat(range.getMaxValue(), notNullValue());
            }
        }
    }

    @Test
    void shouldReturnAppropriateRange() {
        // given / when / then
        assertThat(StandardValueRange.findRangeOrThrow((short) 2), equalTo(StandardValueRange.SHORT));
        assertThat(StandardValueRange.findRangeOrThrow(30), equalTo(StandardValueRange.INTEGER));
        assertThat(StandardValueRange.findRangeOrThrow(45f), equalTo(StandardValueRange.FLOAT));
        assertThat(StandardValueRange.findRangeOrThrow(BigDecimal.TEN), equalTo(StandardValueRange.BIG_DECIMAL));

        BigInteger bigInteger = new BigInteger(new byte[]{ 20 }) { };
        assertThat(StandardValueRange.findRangeOrThrow(bigInteger), equalTo(StandardValueRange.BIG_INTEGER));

        BigDecimal bigDecimalExt = new BigDecimal("20") { };
        assertThat(StandardValueRange.findRangeOrThrow(bigDecimalExt), equalTo(StandardValueRange.BIG_DECIMAL));
    }

    @Test
    void shouldThrowForUnknownType() {
        // given / when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> findRangeOrThrow(new NumberTestImpl()));

        // then
        assertThat(ex.getMessage(), equalTo("Unsupported number type: class ch.jalu.typeresolver.numbers.NumberTestImpl"));
    }

    /**
     * Tests the code sample in the Javadoc of {@link StandardValueRange#findRangeOrThrow}. This test
     * attempts to ensure that the code in the Javadoc is correct and runnable.
     */
    @Test
    void shouldHaveRunnableExampleCodeInJavadoc() {
        // given / when
        StandardValueRange range1 = findRangeOrThrow(1L);                  // Result: LONG
        StandardValueRange range2 = findRangeOrThrow(new AtomicLong(2));   // Result: LONG
        StandardValueRange range3 = findRangeOrThrow(new LongAdder());     // Result: LONG

        BigDecimal bigDecimalExtension = new BigDecimal("4") { };            // anonymous extension
        StandardValueRange range4 = findRangeOrThrow(bigDecimalExtension); // Result: BIG_DECIMAL

        // then
        assertThat(range1, equalTo(StandardValueRange.LONG));
        assertThat(range2, equalTo(StandardValueRange.LONG));
        assertThat(range3, equalTo(StandardValueRange.LONG));
        assertThat(range4, equalTo(StandardValueRange.BIG_DECIMAL));
    }
}
