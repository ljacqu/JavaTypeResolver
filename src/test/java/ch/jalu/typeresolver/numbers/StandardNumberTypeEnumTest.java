package ch.jalu.typeresolver.numbers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.BIG_DECIMAL;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.BIG_INTEGER;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.BYTE;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.DOUBLE;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.FLOAT;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.INTEGER;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.LONG;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.SHORT;
import static ch.jalu.typeresolver.numbers.StandardNumberTypeEnum.findEntryForReadingValueOrThrow;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link StandardNumberTypeEnum}.
 */
class StandardNumberTypeEnumTest {

    // Note: StandardNumberType tests conversions among number types, which uses the enum implementation in the
    // background, so StandardNumberTypeTest covers all conversions of the enum class.

    @Test
    void shouldReturnAppropriateEnumEntry() {
        // given / when / then
        assertThat(StandardNumberTypeEnum.findEntryForReadingValueOrThrow((short) 2), equalTo(StandardNumberTypeEnum.SHORT));
        assertThat(StandardNumberTypeEnum.findEntryForReadingValueOrThrow(30), equalTo(StandardNumberTypeEnum.INTEGER));
        assertThat(StandardNumberTypeEnum.findEntryForReadingValueOrThrow(45f), equalTo(StandardNumberTypeEnum.FLOAT));
        assertThat(StandardNumberTypeEnum.findEntryForReadingValueOrThrow(BigDecimal.TEN), equalTo(StandardNumberTypeEnum.BIG_DECIMAL));

        BigInteger bigInteger = new BigInteger(new byte[]{ 20 }) { };
        assertThat(StandardNumberTypeEnum.findEntryForReadingValueOrThrow(bigInteger), equalTo(StandardNumberTypeEnum.BIG_INTEGER));

        BigDecimal bigDecimalExt = new BigDecimal("20") { };
        assertThat(StandardNumberTypeEnum.findEntryForReadingValueOrThrow(bigDecimalExt), equalTo(StandardNumberTypeEnum.BIG_DECIMAL));
    }

    @Test
    void shouldThrowForUnknownType() {
        // given / when
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> findEntryForReadingValueOrThrow(new AtomicLong(32)));

        // then
        assertThat(ex.getMessage(), equalTo("Unsupported number type: class java.util.concurrent.atomic.AtomicLong"));
    }

    @Test
    void shouldConvertToStandardNumberType() {
        // given
        List<StandardNumberType<?>> allNumberTypes = StandardNumberType.streamThroughAll().collect(Collectors.toList());
        List<StandardNumberType<?>> seenNumberTypes = new ArrayList<>();

        for (StandardNumberTypeEnum enumEntry : StandardNumberTypeEnum.values()) {

            // when
            StandardNumberType<?> numberType = enumEntry.asNumberType();

            // then
            assertThat(enumEntry.getType(), equalTo(numberType.getType()));
            assertThat(numberType.asEnum(), equalTo(enumEntry));
            seenNumberTypes.add(numberType);
        }

        assertThat(seenNumberTypes, equalTo(allNumberTypes));
    }

    @ParameterizedTest
    @EnumSource(StandardNumberTypeEnum.class)
    void shouldReturnWhetherItSupportsAllValuesOfOtherType(StandardNumberTypeEnum type) {
        // given / when
        Set<StandardNumberTypeEnum> fullySupportedTypes = Arrays.stream(StandardNumberTypeEnum.values())
            .filter(type::supportsAllValuesOf)
            .collect(Collectors.toSet());

        // then
        assertThat(fullySupportedTypes, equalTo(getExpectedFullySupportedTypes(type)));
    }

    /**
     * Tests the code sample in the JavaDoc of {@link StandardNumberTypeEnum#findEntryForReadingValueOrThrow}. This test
     * attempts to ensure that the code in the Javadoc is correct and runnable.
     */
    @Test
    void shouldHaveRunnableExampleCodeInJavadoc() {
        BigDecimal bigDecimalExtension = new BigDecimal("20") { }; // anonymous extension
        StandardNumberTypeEnum entry = StandardNumberTypeEnum.findEntryForReadingValueOrThrow(bigDecimalExtension);
        // System.out.println(entry.convertUnsafe(0).getClass().equals(bigDecimalExtension.getClass())); // false

        boolean output = entry.convertUnsafe(0).getClass().equals(bigDecimalExtension.getClass());
        assertThat(output, equalTo(false));
    }

    // More tests are done in depth for StandardNumberType; this is just to cover the duplicated method on the enum.
    @Test
    void shouldConvertIfNoLossOfMagnitude() {
        // given
        byte b = (byte) 38;
        long l = 1_234_567_890;
        double d = 9777777777777778000000000000000000000000d;
        BigDecimal bd = new BigDecimal("-32768.5");

        // when / then
        assertThat(SHORT.convertIfNoLossOfMagnitude(b).get(), equalTo((short) 38));
        assertThat(SHORT.convertIfNoLossOfMagnitude(l).isPresent(), equalTo(false));
        assertThat(SHORT.convertIfNoLossOfMagnitude(d).isPresent(), equalTo(false));
        assertThat(SHORT.convertIfNoLossOfMagnitude(bd).get(), equalTo((short) -32768));

        assertThat(INTEGER.convertIfNoLossOfMagnitude(b).get(), equalTo(38));
        assertThat(INTEGER.convertIfNoLossOfMagnitude(l).get(), equalTo(1_234_567_890));
        assertThat(INTEGER.convertIfNoLossOfMagnitude(d).isPresent(), equalTo(false));
        assertThat(INTEGER.convertIfNoLossOfMagnitude(bd).get(), equalTo(-32768));

        assertThat(FLOAT.convertIfNoLossOfMagnitude(b).get(), equalTo(38f));
        assertThat(FLOAT.convertIfNoLossOfMagnitude(l).get(), equalTo(1_234_567_890f));
        assertThat(FLOAT.convertIfNoLossOfMagnitude(d).isPresent(), equalTo(false));
        assertThat(FLOAT.convertIfNoLossOfMagnitude(bd).get(), equalTo(-32768.5f));

        assertThat(BIG_INTEGER.convertIfNoLossOfMagnitude(b).get(), equalTo(BigInteger.valueOf(38)));
        assertThat(BIG_INTEGER.convertIfNoLossOfMagnitude(l).get(), equalTo(BigInteger.valueOf(1_234_567_890)));
        assertThat(BIG_INTEGER.convertIfNoLossOfMagnitude(d).get(), equalTo(new BigInteger("9777777777777778000000000000000000000000")));
        assertThat(BIG_INTEGER.convertIfNoLossOfMagnitude(bd).get(), equalTo(BigInteger.valueOf(-32768)));
    }

    private static EnumSet<StandardNumberTypeEnum> getExpectedFullySupportedTypes(StandardNumberTypeEnum type) {
        switch (type) {
            case BYTE:
                return EnumSet.of(BYTE);
            case SHORT:
                return EnumSet.of(BYTE, SHORT);
            case INTEGER:
                return EnumSet.of(BYTE, SHORT, INTEGER);
            case LONG:
                return EnumSet.of(BYTE, SHORT, INTEGER, LONG);
            case FLOAT:
                return EnumSet.of(BYTE, SHORT, INTEGER, LONG, FLOAT);
            case DOUBLE:
                return EnumSet.of(BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE);
            case BIG_INTEGER:
            case BIG_DECIMAL:
                return EnumSet.of(BYTE, SHORT, INTEGER, LONG, BIG_INTEGER, BIG_DECIMAL);
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
    }
}